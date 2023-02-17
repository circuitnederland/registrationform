import static groovy.transform.TypeCheckingMode.SKIP

import org.cyclos.entities.banking.Account
import org.cyclos.entities.banking.PaymentTransferType
import org.cyclos.entities.banking.Transaction
import org.cyclos.entities.system.CustomFieldPossibleValue
import org.cyclos.entities.users.SystemRecord
import org.cyclos.entities.users.User
import org.cyclos.entities.users.UserRecord
import org.cyclos.impl.system.ScriptHelper
import org.cyclos.impl.banking.AccountServiceLocal
import org.cyclos.impl.banking.PaymentServiceLocal
import org.cyclos.impl.users.RecordServiceLocal
import org.cyclos.impl.utils.persistence.EntityManagerHandler
import org.cyclos.model.banking.accounts.SystemAccountOwner
import org.cyclos.model.banking.transactions.PaymentVO
import org.cyclos.model.banking.transactions.PerformPaymentDTO
import org.cyclos.model.banking.transfertypes.TransferTypeVO
import org.cyclos.model.system.fields.CustomFieldPossibleValueVO
import org.cyclos.model.system.fields.CustomFieldValueForSearchDTO
import org.cyclos.model.system.fields.CustomFieldVO
import org.cyclos.model.system.fields.LinkedEntityVO
import org.cyclos.model.users.records.RecordDataParams
import org.cyclos.model.users.records.RecordVO
import org.cyclos.model.users.records.SystemRecordQuery
import org.cyclos.model.users.records.UserRecordQuery
import org.cyclos.model.users.recordtypes.RecordTypeVO
import org.cyclos.model.users.users.UserLocatorVO
import org.cyclos.model.users.users.UserVO
import org.cyclos.model.utils.TimeField
import org.cyclos.server.utils.DateHelper
import org.cyclos.server.utils.MessageProcessingHelper
import org.cyclos.utils.Page

import groovy.transform.TypeChecked
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil

import java.text.SimpleDateFormat

@TypeChecked
class DirectDebits {

    Binding binding
	ScriptHelper scriptHelper
    Map<String, String> scriptParameters
    EntityManagerHandler entityManagerHandler
	AccountServiceLocal accountService
	PaymentServiceLocal paymentService
	RecordServiceLocal recordService
    PAIN_008 pain_008

    DirectDebits(Binding binding) {
        binding = binding
        def vars = binding.variables
        scriptHelper = vars.scriptHelper as ScriptHelper
        scriptParameters = vars.scriptParameters as Map<String, String>
        entityManagerHandler = vars.entityManagerHandler as EntityManagerHandler
		accountService = vars.accountService as AccountServiceLocal
		paymentService = vars.paymentService as PaymentServiceLocal
		recordService = vars.recordService as RecordServiceLocal
        pain_008 = new PAIN_008(binding)
    }

    /**
     * Updates the given directDebit record according to the given action.
     */
    void updateDirectDebit(UserRecord record, String action, Map<String, Object> settlement) {
        def recordDTO = recordService.load(record.id)
        def fields = scriptHelper.wrap(recordDTO)
        def curStatus = (fields.status as CustomFieldPossibleValue).internalName
        def newStatus = curStatus

        switch(action) {
            case 'cancel':
                newStatus = 'cancelled'
                break
            case 'fail':
                newStatus = (curStatus == 'submitted') ? 'failed' : 'permanently_failed'
                break
            case 'retry':
                newStatus = 'retry'
                break
            case 'settle_paid':
                newStatus = (curStatus == 'cancelled') ? 'settled_cancelled' : 'settled_failed'
                fields.settlement = 'paid'
                fields.settlement_iban = new Utils(binding).ibanByConvention(settlement.iban as String)
                break
            case 'settle_revoked':
                newStatus = (curStatus == 'cancelled') ? 'settled_cancelled' : 'settled_failed'
                fields.settlement = 'revoked'
                fields.settlement_transaction = this._revokeTopup(record.user.id, fields.transaction as Transaction)
                break
        }
        fields.status = newStatus
        fields.comments = settlement?.comments
        recordService.save(recordDTO)
    }

    /**
     * Returns whether the given action is available for financial admins on the given directDebit user record.
     * Financial admins can only execute certain actions on directDebit records depending on the status of the record.
     */
    boolean isActionAvailable(UserRecord record, String action) {
        def fields = scriptHelper.wrap(record)
        def curStatus = (fields.status as CustomFieldPossibleValue).internalName

        switch(curStatus) {
            case 'open':
                return action == 'cancel'
                break
            case 'submitted':
            case 'resubmitted':
                return action == 'fail'
                break
            case 'failed':
                return action == 'retry' || action == 'settle_paid' || action == 'settle_revoked'
                break
            case 'cancelled':
            case 'permanently_failed':
                return action == 'settle_paid' || action == 'settle_revoked'
                break
        }

        return false
    }

    /**
    * Creates a topup transaction from the debiet system account to the given user.
    */
    PaymentVO transferTopupUnits(User user, BigDecimal amount){
        PaymentTransferType type = entityManagerHandler.find(PaymentTransferType, scriptParameters['topup.transferType'])
        return paymentService.perform(
            new PerformPaymentDTO(
                [
                    from: SystemAccountOwner.instance(),
                    to: new UserLocatorVO(id: user.id),
                    type: new TransferTypeVO(type.id),
                    amount: amount,
                    description: scriptParameters['topup.description']
                ]
            )
        )
    }

    /**
     * Creates a transaction from the given user to the debiet system account, revoking the topup
     * that was done earlier. The topup has to be revoked because the accompanying direct debit (incasso)
     * has failed.
     */
    private PaymentVO _revokeTopup(Long userId, Transaction topupTransaction){
        PaymentTransferType type = entityManagerHandler.find(PaymentTransferType, scriptParameters['revokeTopup.transferType'])
        String description = MessageProcessingHelper.processVariables(
            type.valueForEmptyDescription,
            [
                "transactienummer": topupTransaction.transactionNumber
            ]
        )
        return paymentService.perform(
            new PerformPaymentDTO(
                [
                    from: new UserLocatorVO(id: userId),
                    to: SystemAccountOwner.instance(),
                    type: new TransferTypeVO(type.id),
                    amount: topupTransaction.amount,
                    description: description
                ]
            )
        )
    }

    /**
     * Generates PAIN.008 data from all direct debit user records with status open or retry.
     * The status of the included direct debit user records is then set to submitted or resubmitted.
     * The batchId and the PAIN.008 xml are stored in a new pain_008 system record.
     */
    String generatePAIN_008() {
        def records = this._getDirectDebitRecords()
        if ( records.isEmpty() ) {
            return 'There are no direct debits to process at this moment.'
        }

        // Add data for each record to process to our pain_008 object.
        records.each { RecordVO recordVO ->
            // Turn the recordVO into a UserRecord entity and use scriptHelper.wrap to reach its custom values.
			def record = entityManagerHandler.find(UserRecord.class, recordVO.id)
            def fields = scriptHelper.wrap(record)

            // Get the active eMandate of the user.
            def eMandate = _getEMandate(record.user)

            // Add the direct debit information to our PAIN_008 object.
            pain_008.addTrx(fields.transaction as Transaction, eMandate as SystemRecord)
        }

        // Generate the PAIN.008 xml.
        def xml = pain_008.getXML()

        // Store the batchId and the generated xml in a new pain_008 system record.
		def data = recordService.getDataForNew(new RecordDataParams(
			recordType: new RecordTypeVO(internalName: 'pain_008')))
		def batchFields = scriptHelper.wrap(data.dto)
		batchFields.batchId = pain_008.batchId
		batchFields.xml = xml
        batchFields.totalAmount = pain_008.totalAmount
        batchFields.nrOfTrxs = pain_008.trxs.size()
		recordService.saveEntity(data.dto)

        // Update the status of the records that are succesfully added to the PAIN.008 string: change status open to submitted and retry to resubmitted.
        // Also store the batchId in the changed records.
        records.each { RecordVO recordVO ->
			def recordDTO = recordService.load(recordVO.id)
            def record = scriptHelper.wrap(recordDTO)
            record.batchId = pain_008.batchId
            record.status = (record.status as CustomFieldPossibleValue).internalName == 'open' ? 'submitted' : 'resubmitted'
            recordService.save(recordDTO)
        }

        return "${records.size()} direct debits were processed succesfully in batch ${pain_008.batchId}."
    }

    /**
     * Returns a list of directDebit records with status open or retry. These are the records that should be 
     * included in the next PAIN.008 batch.
     */
    private List<RecordVO> _getDirectDebitRecords() {
		def query = new UserRecordQuery()
		query.type = new RecordTypeVO(internalName: 'directDebit')
		query.customValues = [
			new CustomFieldValueForSearchDTO(
				field: new CustomFieldVO(internalName: 'directDebit.status'),
				enumeratedValues: [
					new CustomFieldPossibleValueVO(internalName: 'open'),
					new CustomFieldPossibleValueVO(internalName: 'retry')
				] as Set
			)
		] as Set
		query.setSkipTotalCount(true)
		query.setUnlimited()
        return recordService.search(query).pageItems
    }

	/**
	 * Returns the newest valid eMandate record for the given user.
     * Note: we can not use the current() method in the eMandate library, because this returns the newest record, even if this is not active yet.
     * Here we need the newest active eMandate.
	 */
	private SystemRecord _getEMandate(User user) {
		def query = new SystemRecordQuery()
		query.type = new RecordTypeVO(internalName: 'eMandate')
		query.customValues = [
			new CustomFieldValueForSearchDTO(
				field: new CustomFieldVO(internalName: 'eMandate.owner'),
				linkedEntityValues: [
					new LinkedEntityVO(user.id)
				] as Set
			),
			new CustomFieldValueForSearchDTO(
				field: new CustomFieldVO(internalName: 'eMandate.status'),
				enumeratedValues: [
					new CustomFieldPossibleValueVO(internalName: 'success')
				] as Set
			)
		] as Set
		query.setPageSize(1) // Since the result is by default ordered by creation date, this gives us the newest record.
		def results = recordService.search(query).pageItems
		return results.isEmpty() ? null : entityManagerHandler.find(SystemRecord, results[0].id)
	}
	
    /**
     * Returns a list of all direct debit user records of the given user.
     */
    List<UserRecord> getDirectDebits(User user) {
        def query = new UserRecordQuery()
        query.type = new RecordTypeVO(internalName: 'directDebit')
        query.user = new UserVO(user.id)
        query.setSkipTotalCount(true)
        query.setUnlimited()
        def results = recordService.search(query).pageItems
        // The query returns a list of RecordVO objects, we convert each one to a UserRecord object.
        return results.collect{ entityManagerHandler.find(UserRecord.class, it.id) }
    }
}

// Note: don't typeCheck this class because the builder methods are unknown by the compiler.
class PAIN_008 {

	private ScriptHelper scriptHelper
    private Map<String, String> scriptParameters
    private Writable xml
    private List trxs
    private BigDecimal totalAmount
    private batchId
    private batchCreationDateTime

    PAIN_008(Binding binding) {
        def vars = binding.variables
        scriptHelper = vars.scriptHelper as ScriptHelper
        scriptParameters = vars.scriptParameters as Map<String, String>
        this.trxs = []
        this.totalAmount = 0
    }

    public String getXML() {
        // Bail out if there are no transactions.
        if ( this.trxs.size() < 1 ) {
            return ''
        }

        // Generate a unique batchId, based on the current timestamp.
        def today = new Date()
        def date = new SimpleDateFormat("yyyyMMdd").format(today)
        def epoch = today.getTime()
        this.batchId = "${date}_${epoch}"
        this.batchCreationDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(today)

        // Prepare the xml builder and bind the root tag to it. This will build up the xml hierarchy.
        def builder = new StreamingMarkupBuilder(useDoubleQuotes: true)
        builder.encoding = "utf-8"
        this.xml = builder.bind(this.document)

        // Use XmlUtils serialize to turn the XML into a pretty string with newlines and return this.
        return XmlUtil.serialize(this.xml)
    }

    public void addTrx(Transaction transaction, SystemRecord eMandate) {
        def eMandateFields = scriptHelper.wrap(eMandate)
        def locale = 'nl' // Tried using transaction.toUser.locale, but this is null, so just hardcoded Dutch locale.
        def transactionDate = new SimpleDateFormat("d MMMM yyyy", new Locale(locale)).format(transaction.date)
        // Make a new Map object with only the relevant information and add it to the trxs list.
        def trx = [:]
        trx.id = transaction.transactionNumber
        trx.amount = transaction.amount
        trx.mandateId = eMandate.id
        trx.mandateDate = new SimpleDateFormat("yyyy-MM-dd").format(eMandateFields.statusDate)
        trx.name = eMandateFields.accountName
        trx.iban = eMandateFields.iban
        trx.description = "${transaction.description} d.d. ${transactionDate}"
        trx.bic = eMandateFields.bankId
        trx.signature = eMandateFields.validationReference
        trx.signerName = eMandateFields.signerName
        this.trxs.add(trx)

        // Add the amount of the transaction to the totalAmount.
        this.totalAmount += trx.amount
    }
 
    Closure document = { b ->
        b.mkp.xmlDeclaration()
        b.mkp.declareNamespace(
            "": "urn:iso:std:iso:20022:tech:xsd:pain.008.001.02",
            "xsi": "http://www.w3.org/2001/XMLSchema-instance"
        )
        b.Document() {
            b.CstmrDrctDbtInitn() {
                this.groupHeader(b)
                this.paymentInformation(b)
            }
        }
    }

    Closure groupHeader = { b ->
        b.GrpHdr() {
            MsgId(this.batchId)
            CreDtTm(this.batchCreationDateTime)
            NbOfTxs(this.trxs.size())
            CtrlSum(this.totalAmount)
            InitgPty() {
                Nm("${this.scriptParameters['creditor.name']}")
            }
        }
    }

    Closure paymentInformation = { b ->
        def aWeekFromNow = DateHelper.add(new Date(), TimeField.DAYS, 7)
        def requestDate = new SimpleDateFormat("yyyy-MM-dd").format(aWeekFromNow)
        b.PmtInf() {
            PmtInfId("${this.batchId}_PID-00001")
            PmtMtd("DD") // Fixed value of 'DD' for direct debits.
            NbOfTxs(this.trxs.size())
            CtrlSum(this.totalAmount)
            PmtTpInf() {
                SvcLvl() {
                    Cd('SEPA') // Fixed value of 'SEPA'.
                }
                LclInstrm() {
                    Cd('CORE')
                }
                SeqTp('RCUR')
            }
            ReqdColltnDt(requestDate)
            Cdtr() {
                Nm("${this.scriptParameters['creditor.name']}")
            }
            CdtrAcct() {
                Id() {
                    IBAN("${this.scriptParameters['creditor.iban']}")
                }
            }
            CdtrAgt() {
                FinInstnId() {
                    BIC("${this.scriptParameters['creditor.bic']}")
                }
            }
            ChrgBr('SLEV') // Fixed value of 'SLEV'.
            CdtrSchmeId() {
                Id() {
                    PrvtId() {
                        Othr() {
                            Id("${this.scriptParameters['creditor.id']}")
                            SchmeNm() {
                                Prtry('SEPA') // Fixed value of 'SEPA'.
                            }
                        }
                    }
                }
            }
            this.trxs.each {
                this.transactionInformation(b, it)
            }
        }
    }

    Closure transactionInformation = { b, trx ->
        b.DrctDbtTxInf() {
            PmtId() {
                EndToEndId("${this.batchId}_${trx.id}")
            }
            InstdAmt(Ccy: "EUR", trx.amount)
            DrctDbtTx() {
                MndtRltdInf() {
                    MndtId(trx.mandateId)
                    DtOfSgntr(trx.mandateDate)
                    ElctrncSgntr(trx.signature)
                }
            }
            DbtrAgt() {
                FinInstnId() {
                    BIC(trx.bic)
                }
            }
            Dbtr() {
                Nm(trx.name)
            }
            DbtrAcct() {
                Id() {
                    IBAN(trx.iban)
                }
            }
            UltmtDbtr() {
                Nm(trx.signerName)
            }
            RmtInf() {
                Ustrd(trx.description)
            }
        }
    }
}
