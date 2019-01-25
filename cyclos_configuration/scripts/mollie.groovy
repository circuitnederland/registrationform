import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import java.util.Date
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.mail.internet.InternetAddress
import org.cyclos.entities.users.RecordCustomField
import org.cyclos.entities.users.SystemRecord
import org.cyclos.entities.users.SystemRecordType
import org.cyclos.entities.users.User
import org.cyclos.entities.users.QRecordCustomFieldValue
import org.cyclos.entities.users.UserRecord
import org.cyclos.entities.users.UserRecordType
import org.cyclos.impl.access.DirectUserSessionData
import org.cyclos.impl.system.ScriptHelper
import org.cyclos.impl.users.RecordServiceLocal
import org.cyclos.impl.utils.persistence.EntityManagerHandler
import org.cyclos.model.EntityNotFoundException
import org.cyclos.model.users.records.RecordDataParams
import org.cyclos.model.users.records.UserRecordDTO
import org.cyclos.model.users.recordtypes.RecordTypeVO
import org.cyclos.model.users.users.UserLocatorVO
import org.cyclos.server.utils.MessageProcessingHelper
import org.cyclos.utils.BigDecimalHelper
import org.cyclos.utils.DateTime
import org.cyclos.utils.StringHelper
import org.springframework.mail.javamail.MimeMessageHelper
import org.cyclos.entities.banking.PaymentTransferType
import org.cyclos.entities.banking.SystemAccountType
import org.cyclos.model.banking.accounts.SystemAccountOwner
import org.cyclos.model.banking.transactions.PaymentVO
import org.cyclos.model.banking.transactions.PerformPaymentDTO
import org.cyclos.model.banking.transfertypes.TransferTypeVO
import org.cyclos.model.system.entitylogs.EntityPropertyLogQuery
import org.cyclos.model.system.entitylogs.EntityLogType
import org.cyclos.model.system.entitylogs.EntityPropertyLogVO

/**
 * This script expects the parameters as mentioned in mollie.properties.
 */

/**
 * Class containing several static constants used throughout the other scripts.
 */
class Constants {
    static final String REGISTRATION = 'registration'
    static final String TOPUP = 'topup'
    static final String IDEAL = 'ideal'
    static final String PAID = 'paid'
    static final String PENDING = 'pending'
    static final String OPEN = 'open'
    static final String NOT_IDEAL = 'not_ideal'
    static final String BETAALD = 'betaald'
    static final Map<String, String> techMessages = [
        topupSuccess: "Opwaarderen is gelukt. Je Circulaire Euro's zijn bijgeschreven.",
        topupPending: "Opwaarderen is in behandeling. Zodra je betaling is verwerkt, worden je Circulaire Euro's bijgeschreven.",
        topupFailed: "Opwaarderen is niet gelukt. Mogelijke oorzaken: je hebt de betaling via iDEAL geannuleerd of de sessie is verlopen. Als dit volgens jou een andere oorzaak betreft, neem dan alsjeblieft contact op met de administratie via info@circuitnederland.nl of 030-2314314.",
		paymentAlreadyUsed: "Bij activeren van gebruiker #user# is gebleken dat de paymentID (#payment_id#) van deze gebruiker al eerder gebruikt is. De activatie van de gebruiker is hierdoor mislukt.",
		paymentTooOld: "De payment met paymentID #payment_id# van gebruiker #user# is langer geleden aangemaakt dan gebruikelijk. De activatie of opwaardeer-actie van de gebruiker is hierdoor mislukt.",
		incorrectAmount: "Het door gebruiker #user# betaalde bedrag (#paidAmount#) in de payment (paymentID: #payment_id#) is anders dan het bedrag dat hij/zij zou moeten betalen (#expectedAmount#). De activatie of opwaardeer-actie van de gebruiker is hierdoor mislukt.",
		wrongSource: "De payment met paymentId #payment_id# van gebruiker #user# heeft een verkeerde source in de metadata: '#source#' in plaats van 'registration' of 'topup'. De activatie of opwaardeer-actie van de gebruiker is hierdoor mislukt.",
		userWasAlreadySetOnPaid: "Het Betaald veld stond al op 'betaald'. Dit kan duiden op een dubbele betaling door de gebruiker of een handmatige wijziging door een admin.",
		wrongPaymentIsPaid: "De id van de payment die de gebruiker zojuist in Mollie heeft betaald is anders dan de payment id die voor deze gebruiker in Cyclos staat (#payment_id#).",
        wrongOwnerIdealDetailRecord: "Het idealDetail userrecord met paymentId #payment_id# is van een andere user (#owner#) dan de user in de Mollie payment (#user#).",
        wrongContentsIdealDetailRecord: "Het idealDetail userrecord voor user #user# met paymentId #payment_id# heeft een verkeerde inhoud voor method, source en/of amount. Dit duidt op een poging om het opwaarderen te misbruiken.\r\n\r\nRecord inhoud: #idealDetailInfo#.",
        reusedIdealDetailRecord: "Het idealDetail userrecord voor user #user# met paymentId #payment_id# was al gevuld. Dit duidt op een poging om het opwaarderen te misbruiken.\r\n\r\nRecord inhoud: #idealDetailInfo#.",
        idealDetailRecordModified: "Het idealDetail userrecord voor user #user# met paymentId #payment_id# was ofwel gewijzigd ofwel niet aangemaakt door de user. Dit duidt op een poging om het opwaarderen te misbruiken.\r\n\r\nRecord inhoud: #idealDetailInfo#.",
		webhookError: "Er is een exception opgetreden in het mollieWebhook script:\r\n\r\n#error#\r\n\r\nGegevens op dit moment:\r\nIP-adres: #ipAddress#\r\npayment id van Mollie: #paymentIdFromMollie#\r\nuserId: #user#",
		generalError: "Er is iets misgegaan#moment#. Wil je alsjeblieft contact opnemen met de administratie via info@circuitnederland.nl of 030-2314314?",
		generalErrorWithRetry: "Er is iets misgegaan#moment#. Wil je het alsjeblieft nog een keer proberen en als het probleem blijft dan contact opnemen met de administratie via info@circuitnederland.nl of 030-2314314?",
		techError: "Er is een exception opgetreden in een Cyclos script:\r\n\r\n#error#"
	]
}

/**
 * Class used to store / retrieve the authentication information for Mollie.
 * A system record type is used with the access key (string).
 */
class MollieAuth {
    String recordTypeName = 'mollyConnect'
    String accessKeyFieldName = 'accessKey'
    String adminMailAddressFieldName = 'adminMailAddress'
    String notPaidFieldName = 'notPaid'
    String pendingFieldName = 'pending'
    String differentBankAccountFieldName = 'differentBankAccount'
    String fatalErrorFieldName = 'fatalError'
    String mollieUnreachableFieldName = 'mollieUnreachable'
    String registrationRootUrlFieldName = 'registrationRootUrl'

    SystemRecordType recordType
    SystemRecord record
    Map<String, Object> wrapped

    public MollieAuth(Object binding) {
        // Read the record type and the parameters for field internal names.
        recordType = binding.entityManagerHandler
                .find(SystemRecordType, recordTypeName)

        // Should return the existing instance, of a single form type.
        // Otherwise it would be an error
        record = binding.recordService.newEntity(
                new RecordDataParams(recordType: new RecordTypeVO(id: recordType.id)))
        if (!record.persistent) throw new IllegalStateException(
            "No instance of system record ${recordType.name} was found")
        wrapped = binding.scriptHelper.wrap(record, recordType.fields)
    }

	public String getAccessKey() {
		wrapped[accessKeyFieldName]
	}

    public String getAdminMailAddress() {
        wrapped[adminMailAddressFieldName]
    }

    public String getNotPaid() {
        wrapped[notPaidFieldName]
    }

    public String getPending() {
        wrapped[pendingFieldName]
    }

    public String getDifferentBankAccount() {
        wrapped[differentBankAccountFieldName]
    }

    public String getFatalError() {
        wrapped[fatalErrorFieldName]
    }

    public String getMollieUnreachable() {
        wrapped[mollieUnreachableFieldName]
    }

    public String getRegistrationRootUrl() {
        wrapped[registrationRootUrlFieldName]
    }
}

/**
 * Class used to store / retrieve iDEAL payments as user records in Cyclos.
 */
class IdealDetailRecord {
	String recordTypeName = 'idealDetail'
	String consumerNameFieldName = 'consumerName'
	String ibanFieldName = 'iban'
    String bicFieldName = 'bic'
    String paymentIdFieldName = 'paymentId'
    String methodFieldName = 'method'
    String transactionFieldName = 'transaction'
    String amountFieldName = 'amount'
    String paidFieldName = 'paid'
    String sourceFieldName = 'source'

	UserRecordType recordType
	Map<String, RecordCustomField> fields

	private EntityManagerHandler entityManagerHandler
	private RecordServiceLocal recordService
	private ScriptHelper scriptHelper

	public IdealDetailRecord(Object binding) {
		entityManagerHandler = binding.entityManagerHandler
		recordService = binding.recordService
		scriptHelper = binding.scriptHelper
		recordType = binding.entityManagerHandler.find(UserRecordType, recordTypeName)
		fields = [:]
		recordType.fields.each {f -> fields[f.internalName] = f}
	}

    /**
     * Creates an iDEAL record for the given user.
     */
	public UserRecord create(User user, String consumerName, String iban, String bic, String paymentId, String method, PaymentVO transaction, BigDecimal amount, Boolean paid, String source) {
		RecordDataParams newParams = new RecordDataParams(
				[user: new UserLocatorVO(id: user.id),
					recordType: new RecordTypeVO(id: recordType.id)])
		UserRecordDTO dto = recordService.getDataForNew(newParams).getDto()
		Map<String, Object> wrapped = scriptHelper.wrap(dto, recordType.fields)
		wrapped[consumerNameFieldName] = consumerName
		wrapped[ibanFieldName] = iban
        wrapped[bicFieldName] = bic
        wrapped[paymentIdFieldName] = paymentId
        wrapped[methodFieldName] = method
        wrapped[transactionFieldName] = transaction
        wrapped[amountFieldName] = amount
        wrapped[paidFieldName] = paid
		wrapped[sourceFieldName] = source

		// Save the record DTO and return the entity.
		Long id = recordService.save(dto)
		return entityManagerHandler.find(UserRecord, id)
	}

    /**
     * Finds the record by id.
     */
    public UserRecord find(Long id) {
        try {
            UserRecord userRecord = entityManagerHandler.find(UserRecord, id)
            if (userRecord.type != recordType) {
                return null
            }
            return userRecord
        } catch (EntityNotFoundException e) {
            return null
        }
    }

    /**
     * Finds the idealDetail userrecord by paymentId.
     * Since the paymentId should be unique, this method throws an Exception if more than one record is found with the same paymentId.
     */
    public UserRecord findByPaymentId(String paymentId){
        // The paymentId is a custom field on the record, so look for the value in the recordCustomFieldValues.
        // If we find something, return the owner of the recordCustomFieldValue, i.e. the record.
        QRecordCustomFieldValue fv = QRecordCustomFieldValue.recordCustomFieldValue
        Vector results = entityManagerHandler.from(fv)
            .select(fv.owner())
            .where(
                fv.owner().type().internalName.eq(recordTypeName),
                fv.field().internalName.eq(paymentIdFieldName),
                fv.stringValue.eq(paymentId))
            .fetch()
        if (results.size() != 1){
            throw new Exception("Search for ${recordTypeName} userrecord with ${paymentIdFieldName} ${paymentId} resulted in ${results.size()} records instead of exactly one record.")
        }
        return results.firstElement()
    }
}

/**
 * Class used to interact with Mollie services.
 */
class MollieService {
	final String BASEURL = 'https://api.mollie.nl'
	final String API_VERSION = 'v2'
	private MollieAuth auth

	public MollieService(MollieAuth auth) {
		this.auth = auth
	}

	/**
	 * Gets a payment from Mollie by its id.
	 */
	public Object getPayment(String id) {
		Object json = getRequest("${BASEURL}/${API_VERSION}/payments/${id}")
		return json
	}

    /**
     * Creates a payment in Mollie.
     */
    public Object createPayment(String amount, String description, String redirectUrl, String webhookUrl, String userId, String source) {
        def jsonBody = [
            amount: [
                currency: "EUR",
                value: amount
            ],
            description: description, 
            redirectUrl: redirectUrl,
            webhookUrl: webhookUrl,
            metadata: [
                user: userId,
                source: source
            ]
        ]
        return postRequest("${BASEURL}/${API_VERSION}/payments", jsonBody)
    }

    /**
     * Cancels the payment with the given id in Mollie, making it impossible for the user to still pay this payment.
     */
    public void cancelPayment(String paymentId) {
        doRequest(Method.DELETE, "${BASEURL}/${API_VERSION}/payments/${paymentId}")
    }

    /**
     * Performs a synchronous get request, accepting JSON.
     */
	private Object getRequest(url) {
        return doRequest(Method.GET, url)
	}

    /**
     * Performs a synchronous post request accepting JSON.
     */
    private Object postRequest(url, jsonBody) {
        return doRequest(Method.POST, url, jsonBody)
    }

    /**
     * Performs a synchronous request, either get or post, and accepting JSON.
     */
    private Object doRequest(method, url, jsonBody = null) {
        def http = new HTTPBuilder(url)
        CountDownLatch latch = new CountDownLatch(1)
        def responseJson = null
        def responseError = []

        // Perform the request
        http.request(method, ContentType.JSON) {
            headers.'Authorization' = "Bearer ${auth.accessKey}"
            if (Method.POST == method) {
                body = jsonBody
            }
            response.success = { resp, json ->
                responseJson = json
                latch.countDown()
            }

            response.failure = { resp, json ->
                responseError << resp.statusLine.statusCode
                responseError << resp.statusLine.reasonPhrase
				responseError << json
                latch.countDown()
            }
        }

        // @todo: add a timeout and check the response of the await() call. If false, throw an Exception("mollieUnreachable").
        latch.await()
        if (!responseError.empty) {
            throw new RuntimeException("Error making Mollie request to ${url}"
            + ", got error code ${responseError[0]}: ${responseError[1]}. JSON: ${responseError[2]}")
        }
        return responseJson
    }
}

class Utils{
    private Object binding
    private MollieAuth auth
    private MollieService mollie
    private IdealDetailRecord idealRecord
	
    public Utils(Object binding, MollieAuth auth, MollieService mollie, IdealDetailRecord idealRecord) {
        this.binding = binding
        this.auth = auth
        this.mollie = mollie
        this.idealRecord = idealRecord
    }
	
    /**
     * Checks whether the given paymentId is used in an idealDetail userrecord that does not belong to the given user.
     */
    public Boolean isPaymentIdUsedBefore(String paymentId, User user) {
        // @todo: implement this function. For now, just return false.
        return false
    }
    
    /**
     * Checks whether the given payment date is older than desired.
     */
    public Boolean isPaymentTooOld(String paymentDate) {
        DateTime now = new DateTime(new Date().format("yyyy-MM-dd"))
        // Add the max age (in days) to the paymentDate. If the result is still in the past, it is too old.
        // @todo: instead of using a script parameter for the max age, would it be better to use binding.cyclosProperties.purgeUnconfirmedUsersDays and add 1 day?
        Long max_days = Long.valueOf(binding.scriptParameters.'mollie_payment.max_age' ?: "61")
        DateTime groovyPlus = new DateTime(paymentDate).add(TimeUnit.DAYS.toMillis(max_days))
        return groovyPlus.before(now)
    }
    
    /**
     * Prepares a registration payment and calls Mollie to create it.
     */
    public Object setupMollieRegistrationPayment(BigDecimal contribution, BigDecimal aankoop_saldo, User user, String validationKey = null) {
        def params = binding.scriptParameters
        def formatter = binding.formatter
        def vars = [
         lidmaatschapsbijdrage: formatter.format(contribution),
         aankoop_saldo: formatter.format(aankoop_saldo),
         username: user.username
        ]
        // Convert the total amount to a string with two decimals and a dot as separator - Mollie needs the amount like this.
        String amount = BigDecimalHelper.round((contribution + aankoop_saldo), 2).toString()
        String description = MessageProcessingHelper.processVariables(params.'mollie_payment.description', vars)
        String redirectUrl = auth.registrationRootUrl
        // Add the correct redirectUrl, depending on whether the user came from the validate page or not (i.e. the registration form).
        if (validationKey) {
            redirectUrl += params.validationUrlPart + "?validationKey=${validationKey}"
        } else {
            redirectUrl += params.confirmationUrlPart + "?mail=" + StringHelper.encodeURIComponent(user.email) 
        }
        String webhookUrl = binding.sessionData.configuration.rootUrl + params.mollieWebhookUrlPart
        String userId = String.valueOf(binding.scriptHelper.maskId(user.id))
        return mollie.createPayment(amount, description, redirectUrl, webhookUrl, userId, "registration")
    }

    /**
     * Prepares a topup payment and calls Mollie to create it.
     */
    public Object setupMollieTopupPayment(BigDecimal amountToTopup, User user, String returnUrl){
        def params = binding.scriptParameters
        def vars = [
         amount: binding.formatter.format(amountToTopup),
         username: user.username
        ]
        // Convert the amount to a string with two decimals and a dot as separator - Mollie needs the amount like this.
        String amount = BigDecimalHelper.round(amountToTopup, 2).toString()
        String description = MessageProcessingHelper.processVariables(params.'mollie_payment.descriptionTopup', vars)
        String webhookUrl = binding.sessionData.configuration.rootUrl + params.mollieWebhookUrlPart
        String userId = String.valueOf(binding.scriptHelper.maskId(user.id))
        return mollie.createPayment(amount, description, returnUrl, webhookUrl, userId, "topup")
    }

	/**
	 * Determines and returns the contribution amount for the given user.
	 */
	public BigDecimal getLidmaatschapsbijdrage(usr) {
 		if (usr.lidmaatschapparticulieren) {
			return new BigDecimal(usr.lidmaatschapparticulieren.value)
		}
		// For bedrijven the lidmaatschap value contains a string like "150 - bedrijven met minder dan 50 werknemers",
		// so we take the first part to determine the contribution amount.
		String contrib = usr.lidmaatschapbedrijven.value
		String amount = contrib.substring(0, contrib.indexOf(" - "))
		return new BigDecimal(amount)
	}

    /**
    * Finds the relevant Mollie payment for the given user, starting with the paymentId in the user profile.
    * If this leads to a non-paid payment, searches for the profilehistory for all paymentId's the user might have used.
    * Returns payment information for either a paid (or pending) payment or a random payment of this user that was not paid.
    */
    public Object findRelevantPaymentForUser(def usr) {
        // First check the paymentId of the user.
        def paymentResponse = getMolliePaymentForUser(usr, usr.payment_id)
        if (paymentResponse?.status == 'paid' || paymentResponse?.status == 'pending') {
            // The user paid this payment, so return the info on this payment.
            return paymentResponse
        }
        // The current paymentId was not paid. Check if there are other payments for this user that are paid (or pending).
        List alternativePayments = []
        List<String> paymentIds = getPaymentIdsFromProfileHistory(usr)
        paymentIds.each {
            paymentResponse = getMolliePaymentForUser(usr, it)
            if (paymentResponse?.status == 'paid' || paymentResponse?.status == 'pending') {
                alternativePayments.add(paymentResponse)
            }
        }
        if (alternativePayments) {
            // There is at least one payment this user paid. Take the first one.
            paymentResponse = alternativePayments[0]
        }
        return paymentResponse
    }

    /**
    * Retrieves the payment information for the given paymentId from Mollie.
    * Checks whether the payment retrieved belongs to the given user indeed.
    * If the userId in the payment does not correspond with the given user, an Exception is thrown.
    */
    public Object getMolliePaymentForUser(def usr, String paymentId) {
        def paymentResponse = mollie.getPayment(paymentId)
        if (paymentResponse.metadata?.user != String.valueOf(binding.scriptHelper.maskId(usr.id))) {
            throw new Exception("Wrong user id ${paymentResponse.metadata?.user} in Mollie payment (${paymentId}) for ${usr.username}.")
        }
        return paymentResponse
    }

    /**
    * Returns a list of paymentId's that exist in the profile history of the given user.
    * The current payment_id for the user is removed from the returned list.
    * So the returned list only contains historical paymentId's for the user, not the current one.
    */
    public List<String> getPaymentIdsFromProfileHistory(def usr) {
        String fieldName = "Payment id"
        EntityPropertyLogQuery q = new EntityPropertyLogQuery()
        q.type = EntityLogType.USER
        q.entityId = usr.id
        q.keywords = fieldName
        q.setUnlimited()
        def logEntriesFound = binding.entityLogService.search(q)
        List<String> result = []
        for( EntityPropertyLogVO logEntry : logEntriesFound ){
            if (logEntry?.name == fieldName && logEntry?.newValue != usr.payment_id) {
                result.add(logEntry.newValue)
            }
        }
        return result
    }

    /**
     * Creates a transaction from system to the given user for buying units.
     * The source can be either registration or topup, which results in different descriptions.
     */
    public PaymentVO transferPurchasedUnits(User user, BigDecimal amount, String source){
        def params = binding.scriptParameters
        PaymentTransferType type = binding.entityManagerHandler.find(PaymentTransferType, params.'unitPurchase.paymentType')
        return binding.paymentService.perform(
            new PerformPaymentDTO([
            from: SystemAccountOwner.instance(),
            to: new UserLocatorVO(id: user.id),
            type: new TransferTypeVO(type.id),
            amount: amount,
            description: (source == Constants.TOPUP) ? params.'unitPurchase.topupDescription' : params.'unitPurchase.registrationDescription'])
        )
    }

    /**
     * Creates a transaction from the given user to system for paying the membership fee.
     * For now, it always uses a description indicating this is the first membership fee. In future this might be refactored so we use this
     * method as well for automatic payment of yearly membership fees, not just the first year.
     */
    public PaymentVO transferMembershipPayment(User user, BigDecimal amount){
        def params = binding.scriptParameters
        PaymentTransferType type = binding.entityManagerHandler.find(PaymentTransferType, params.'membershipFee.paymentType')
        return binding.paymentService.perform(
            new PerformPaymentDTO([
            from: new UserLocatorVO(id: user.id),
            to: SystemAccountOwner.instance(),
            type: new TransferTypeVO(type.id),
            amount: amount,
            description: params.'membershipFee.firstPaymentDescription'])
        )
    }

    /**
     * Creates the two transactions needed when a new user is being validated (aankoop saldo from debiet to user and contribution from user to sys).
     * Also creates an idealDetail userrecord storing the relevant info of the aankoop transaction.
     */
    public void processRegistrationPayments(User user, BigDecimal totalAmount, BigDecimal contribution, String method, Map<String, String> paymentInfo, String paymentId = ''){
        String source = Constants.REGISTRATION

        // Make a Cyclos payment from debit to user with the total amount the user paid. Keep the resulting payment so we can store it in the user record below.
        PaymentVO paymentVO = transferPurchasedUnits(user, totalAmount, source)

        // Make a Cyclos payment from user to sys organization with the contribution fee.
        transferMembershipPayment(user, contribution)

        // Store bank account info on member record.
        def consName = paymentInfo['consName']?: ''
        def iban = paymentInfo['iban']?: ''
        def bic = paymentInfo['bic']?: ''
        Boolean paid = true
        idealRecord.create(user, consName, iban, bic, paymentId, method, paymentVO, totalAmount, paid, source)
        def usr = binding.scriptHelper.wrap(user)
        if (!usr.iban.equalsIgnoreCase(iban)) {
            sendMailToAdmin("Circuit Nederland: different bank account", prepareMessage("differentBankAccount", ["user": usr.name]), true)
        }
    }

    /**
     * Accept all personal agreements.
     */
    public void acceptAgreements(User user){
        binding.invokerHandler.runAs(new DirectUserSessionData(user, binding.sessionData)) {
            def pendingAgreements = binding.agreementLogService.getPendingAgreements()
            if (pendingAgreements) {
                binding.agreementLogService.accept(pendingAgreements.toSet())
            }
        }
    }

	/**
	 * Sends an e-mail to the admin with the given message and subject.
	 */
    public void sendMailToAdmin(String subject, String msg, Boolean isOnCommit = false) {
        sendMail("Admin Circuit Nederland", auth.adminMailAddress, subject, msg, isOnCommit)
    }

    /**
     * Sends an e-mail to the tech team with the given message and subject.
     */
    public void sendMailToTechTeam(String subject, String msg, Boolean isOnCommit = false) {
        sendMail("Tech Team Circuit Nederland", binding.scriptParameters.techTeamMail, subject, msg, isOnCommit)
    }

    /**
     * Sends an e-mail to the requested addressee with the given message and subject.
     */
    public void sendMail(String toName, String toMail, String subject, String msg, Boolean isOnCommit = false) {
        def fromEmail = binding.sessionData.configuration.smtpConfiguration.fromAddress
        String fromName = binding.sessionData.configuration.emailName
        def sender = binding.mailHandler.mailSender
        def message = sender.createMimeMessage()
        def helper = new MimeMessageHelper(message)
        helper.to = new InternetAddress(toMail, toName)
        helper.from = new InternetAddress(fromEmail, fromName)
        helper.subject = subject
        helper.setText msg
        if (isOnCommit) {
            binding.scriptHelper.addOnCommit {
                sender.send message
            }
        } else {
            binding.scriptHelper.addOnRollback {
                sender.send message
            }
        }
    }

    /**
     * Finds the text with the given errorCode, either from the techMessages Map constant or from a field in the auth systemrecord.
     * If no text is found, the errorCode itself is returned as a fallback.
     * If the vars parameter is passed, the variables are replaced in the text found.
     */
    public String prepareMessage(String errorCode, Map<String, ?> vars = null, Boolean isHtml = false){
        // Note: we must check if the auth object has the property before trying to access it, to prevent a MissingPropertyException.
        String authField = auth.hasProperty(errorCode) ? auth."${errorCode}" : null

        // Get the message either from the techMessages Map constant or from the auth systemrecord. Falling back to the errorCode itself if neither exists.
        String message = Constants.techMessages[errorCode] ?: authField ?: errorCode

        message = MessageProcessingHelper.processVariables(message, vars)
        if (isHtml) {
            message = message.replace("\r\n", "<br>").replace("\n", "<br>").replace("\r", "<br>")
        }
        return message
    }
}

// Instantiate the objects
MollieAuth auth = new MollieAuth(binding)
MollieService mollie = new MollieService(auth)
IdealDetailRecord idealRecord = new IdealDetailRecord(binding)
Utils utils = new Utils(binding, auth, mollie, idealRecord)
