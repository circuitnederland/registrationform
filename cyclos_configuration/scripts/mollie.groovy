import java.util.concurrent.CountDownLatch

import javax.mail.internet.InternetAddress

import org.cyclos.entities.users.RecordCustomField
import org.cyclos.entities.users.SystemRecord
import org.cyclos.entities.users.SystemRecordType
import org.cyclos.entities.users.User
import org.cyclos.entities.users.UserRecord
import org.cyclos.entities.users.UserRecordType
import org.cyclos.impl.system.ScriptHelper
import org.cyclos.impl.users.RecordServiceLocal
import org.cyclos.impl.utils.persistence.EntityManagerHandler
import org.cyclos.model.EntityNotFoundException
import org.cyclos.model.users.records.RecordDataParams
import org.cyclos.model.users.records.UserRecordDTO
import org.cyclos.model.users.recordtypes.RecordTypeVO
import org.cyclos.model.users.users.UserLocatorVO
import org.cyclos.server.utils.MessageProcessingHelper
import org.springframework.mail.javamail.MimeMessageHelper

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * This script expects the following parameters
 * 
 # Settings for the access token record type
 mollie.recordType = mollyConnect
 auth.token = accessKey
 auth.testMode = testMode
 mollie.notPaidYetMail = notPaidYetMail
 mollie.unreachable = mollieUnreachable
 mollie.pendingMail = pendingMail
 admin.mail.address = adminMailAddress
 # Mail not paid settings
 notPaid.subject = Betaal NU je openstaande bedrag voor Cirquit Nederland (en snel een beetje)
 notPaid.link = http://bla.bla.nl
 # Pending mail settings
 pendingMail.subject = "Circuit Nederland activatie kan (nog) niet verwerkt worden"
 # user record type settings
 bank.recordType = idealDetail
 bank.consumerName = consumerName
 bank.iban = iban
 bank.bic = bic
 */


/**
 * Class used to store / retrieve the authentication information for Mollie.
 * A system record type is used, with the following fields: access token 
 * (string) and testMode (Boolean).
 */
class MollieConnect {
	String recordTypeName
	String accessKeyFieldName
	Boolean testModeFieldName
	String payAgainMailFieldName
	String noConnectionMailFieldName
	String pendingMailFieldName
	String adminMailAddressFieldName

	SystemRecordType recordType
	SystemRecord record
	Map<String, Object> wrapped

	public MollieConnect(Object binding) {
		def params = binding.scriptParameters
		recordTypeName = params.'mollie.recordType' ?: 'mollyConnect'
		accessKeyFieldName = params.'auth.token' ?: 'accessKey'
		testModeFieldName = params.'auth.testMode' ?: 'testMode'
		payAgainMailFieldName = params.'mollie.notPaidYetMail' ?: 'notPaidYetMail'
		noConnectionMailFieldName = params.'mollie.unreachable' ?: 'mollieUnreachable'
		pendingMailFieldName = params.'mollie.pendingMail' ?: 'pendingMail'
		adminMailAddressFieldName = params.'admin.mail.address' ?: 'adminMailAddress'

		// Read the record type and the parameters for field internal names
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
	public Boolean getTestMode() {
		wrapped[testModeFieldName]
	}
	public String getPayAgainMail() {
		wrapped[payAgainMailFieldName]
	}
	public String getNoConnectionMail() {
		wrapped[noConnectionMailFieldName]
	}
	public String getPendingMail() {
		wrapped[pendingMailFieldName]
	}
	public String getAdminMailAddress() {
		wrapped[adminMailAddressFieldName]
	}
}

// Instantiate the objects
MollieConnect connect = new MollieConnect(binding)
MollieService mollie = new MollieService(connect)

/**
 * Class used to interact with Mollie services
 */
class MollieService {
	String baseUrl

	private ScriptHelper scriptHelper
	private MollieConnect auth

	public MollieService(MollieConnect auth) {
		this.auth = auth
		baseUrl = 'https://api.mollie.nl'
	}

	/**
	 * gets a payment from Mollie by its id
	 */
	public Object getPayment(String id) {
		Object json = getRequest("${baseUrl}/v1/payments/${id}")
		return json
	}

	private getRequest(url) {
		def http = new HTTPBuilder(url)
		CountDownLatch latch = new CountDownLatch(1)
		def responseJson = null
		def responseError = []

		// Perform the request
		http.request(Method.GET, ContentType.JSON) {
			headers.'Authorization' = "Bearer ${auth.accessKey}"

			response.success = { resp, json ->
				responseJson = json
				latch.countDown()
			}

			response.failure = { resp ->
				responseError << resp.statusLine.statusCode
				responseError << resp.statusLine.reasonPhrase
				latch.countDown()
			}
		}

		latch.await()
		if (!responseError.empty) {
			throw new RuntimeException("Error making Mollie request to ${url}"
			+ ", got error code ${responseError[0]}: ${responseError[1]}")
		}
		return responseJson
	}

}

/**
 * Defines cyclos actions which are to be taken as a response to some mollie status. 
 */
class CyclosActions {

	String notPaidMailSubject
	String notPaidMailLink
	String pendingMailSubject
	Object binding
	Object connect
	String mailAlertSubject = "Circuit Nederland verdachte betaling"
	String mailAlertBody = """
Beste admin, 

Circuit-NederlandLid #user# heeft zich juist aangemeld, maar bij de controle op activatie is gebleken dat het bedrag dat 
hij/zij zou moeten betalen te weinig is. 
Te betalen bedrag: #paid#
Zou moeten zijn: #should#

Dit zou niet moeten kunnen, mogelijk is hier mee gerommeld??

(Dit is een automatisch door een script gegenereerd bericht)
"""
	String mailBankAccountSubject = "Circuit Nederland: different bank account"
	String mailBankAccountBody = """
Beste admin, 

Circuit-NederlandLid #user# heeft zich juist aangemeld, maar bij de controle op activatie is gebleken dat er een 
andere bankrekening gebruikt is dan opgegeven via het profiel. Er is een user record van de bankgegevens gemaakt. 

(Dit is een automatisch door een script gegenereerd bericht)

"""

	/**
	 * constructor:	
	 */
	public CyclosActions(Object bBinding, Object cConnect) {
		binding = bBinding
		connect = cConnect
		def params = binding.scriptParameters
		notPaidMailSubject = params.'notPaid.subject' ?: 'notPaid.subject'
		notPaidMailLink = params.'notPaid.link' ?: 'notPaid.link'
		pendingMailSubject = params.'pendingMail.subject' ?: 'pendingMail.subject'
	}

	/**
	 * Verifies the amount paid to mollie. It should be equal to 
	 * the yearly pament + the aankoopsaldo, which both are saved
	 * as profile fields. 
	 * 
	 * @param mollieAmount
	 * @return true if sufficient, false if not sufficient.
	 */
	public boolean verifyAmount(mollieAmount, usr) {
		// retrieve the aankoopsaldo and lidmaatschap fields.
		def lidmaat = getLidmaatschapsbijdrage(usr)
		def BigDecimal mollieAmnt = new BigDecimal(mollieAmount)
		return (mollieAmnt >= lidmaat + (usr.aankoop_saldo?:0))
	}

/**
	 * a user group is a bedrijven group if its group name ends with "bedrijven"
	 * (case insensitve). In any other case, it's considered a particulier. 	
	 */
	private Boolean isBedrijf(usr) {
		return usr.group.name.toLowerCase().endsWith("bedrijven")
	}
	
	public BigDecimal getLidmaatschapsbijdrage(usr) {
		if (isBedrijf(usr)) {
			if (usr.lidmaatschapbedrijven) {
                String regex = "(([1-9]\\d{0,2}(\\.\\d{3})*)|(([1-9]\\d*)?\\d))(,\\d\\d)?"
                String rawlidm = usr.lidmaatschapbedrijven.value
				Matcher matcher = Pattern.compile(regex).matcher(rawlidm)
        		if (matcher.find()) {
            		return new BigDecimal(matcher.group())
                } else {
                    throw new NumberFormatException("cannot read lidmaatschapBedrijven")
                }
			}
			return BigDecimal.ZERO;
		}
		if (usr.lidmaatschapparticulieren) {
			return new BigDecimal(usr.lidmaatschapparticulieren.value)
		}
		return BigDecimal.ZERO;
	}

	public void mailNotPaid(usr, id) {
		def lidmaatschap = getLidmaatschapsbijdrage(usr)
		def linkParams = ['user': id, 'payment': usr.payment_id]
        def rawLink = notPaidMailLink + '?' + linkParams.collect { it }.join('&')
        def htmlLink = '<a href="' + rawLink + '">' + rawLink + '</a>'
		def vars = [
			user: usr.name,
			lidmaat: binding.formatter.format(lidmaatschap),
			aankoop: binding.formatter.format(usr.aankoop_saldo?:0),
			totaal: binding.formatter.format(lidmaatschap + (usr.aankoop_saldo?:0)),
			link : htmlLink
		]
		def rawBody = connect.getPayAgainMail()
		def body = MessageProcessingHelper.processVariables(connect.getPayAgainMail(), vars)
        body = body.replace("\n", "<br>").replace("\r", "<br>")
		def toEmail = usr.email
		def fromEmail = binding.sessionData.configuration.smtpConfiguration.fromAddress
		def sender = binding.mailHandler.mailSender

		// Send the message after Rollback, so we guarantee the activation is failed
		// when the e-mail is sent. No need for a transaction, as there is no db change
		binding.scriptHelper.addOnRollback {
			def message = sender.createMimeMessage()
			def helper = new MimeMessageHelper(message)
			helper.to = new InternetAddress(toEmail)
			helper.from = new InternetAddress(fromEmail)
			helper.subject = notPaidMailSubject
			helper.setText body, true
			sender.send message
		}
	}

	/**
	 * mail send when payment is in pending state. 
	 * It simply advices the user to try again later
	 */
	public void mailPending(usr) {
		def vars = [
			user: usr.name,
		]
		def rawBody = connect.getPendingMail()
		def body = MessageProcessingHelper.processVariables(rawBody, vars)
		def toEmail = usr.email
		def fromEmail = binding.sessionData.configuration.smtpConfiguration.fromAddress
		def sender = binding.mailHandler.mailSender

		// Send the message after Rollback, so we guarantee the activation is failed
		// when the e-mail is sent. No need for a transaction, as there is no db change
		binding.scriptHelper.addOnRollback {
			def message = sender.createMimeMessage()
			def helper = new MimeMessageHelper(message)
			helper.to = new InternetAddress(toEmail)
			helper.from = new InternetAddress(fromEmail)
			helper.subject = pendingMailSubject
			helper.text = body
			sender.send message
		}
	}

	/**
	 * mail send when a user paid not enough. 
	 */
	//TESTED
	public void mailAlert(usr, paidAmount) {
		def lidmaat = getLidmaatschapsbijdrage(usr)
		def BigDecimal mollieAmnt = new BigDecimal(paidAmount)
		def shouldAmount = lidmaat + (usr.aankoop_saldo?:0)

		def vars = [
			user: usr.name,
			paid: mollieAmnt,
			should: shouldAmount,
		]
		def rawBody = mailAlertBody
		def body = MessageProcessingHelper.processVariables(rawBody, vars)
		def toEmail = connect.getAdminMailAddress()
		def fromEmail = binding.sessionData.configuration.smtpConfiguration.fromAddress
		def sender = binding.mailHandler.mailSender

		// Send the message after Rollback, so we guarantee the activation is failed
		// when the e-mail is sent. No need for a transaction, as there is no db change
		binding.scriptHelper.addOnRollback {
			def message = sender.createMimeMessage()
			def helper = new MimeMessageHelper(message)
			helper.to = new InternetAddress(toEmail)
			helper.from = new InternetAddress(fromEmail)
			helper.subject = mailAlertSubject
			helper.text = body
			sender.send message
		}
	}


	/**
	 * mail send when bankaccount of user is different from profile bank account.
	 */
	public void mailDifferentBankAccount(usr) {
		def vars = [
			user: usr.name,
		]
		def rawBody = mailBankAccountBody
		def body = MessageProcessingHelper.processVariables(rawBody, vars)
		def toEmail = connect.getAdminMailAddress()
		def fromEmail = binding.sessionData.configuration.smtpConfiguration.fromAddress
		def sender = binding.mailHandler.mailSender

		// Send the message after commit
		binding.scriptHelper.addOnCommit {
			def message = sender.createMimeMessage()
			def helper = new MimeMessageHelper(message)
			helper.to = new InternetAddress(toEmail)
			helper.from = new InternetAddress(fromEmail)
			helper.subject = mailBankAccountSubject
			helper.text = body
			sender.send message
		}
	}



}

CyclosActions actions = new CyclosActions(binding, connect);

/**
 * Class used to store / retrieve PayPal payments as user records in Cyclos
 */
class IdealDetailRecord {
	String recordTypeName
	String consumerNameName
	String ibanName
	String bicName

	UserRecordType recordType
	Map<String, RecordCustomField> fields

	private EntityManagerHandler entityManagerHandler
	private RecordServiceLocal recordService
	private ScriptHelper scriptHelper

	public IdealDetailRecord(Object binding) {
		def params = binding.scriptParameters
		recordTypeName = params.'bank.recordType' ?: 'idealDetail'
		consumerNameName = params.'bank.consumerName' ?: 'consumerName'
		ibanName = params.'bank.iban' ?: 'iban'
		bicName = params.'bank.bic' ?: 'bic'

		entityManagerHandler = binding.entityManagerHandler
		recordService = binding.recordService
		scriptHelper = binding.scriptHelper
		recordType = binding.entityManagerHandler.find(UserRecordType, recordTypeName)
		fields = [:]
		recordType.fields.each {f -> fields[f.internalName] = f}
	}

	/**
	 * Creates a payment record for the given user
	 */
	public UserRecord create(User user, String consumerName, String iban, String bic) {
		RecordDataParams newParams = new RecordDataParams(
				[user: new UserLocatorVO(id: user.id),
					recordType: new RecordTypeVO(id: recordType.id)])
		UserRecordDTO dto = recordService.getDataForNew(newParams).getDto()
		Map<String, Object> wrapped = scriptHelper.wrap(dto, recordType.fields)
		wrapped[consumerNameName] = consumerName
		wrapped[ibanName] = iban
		wrapped[bicName] = bic

		// Save the record DTO and return the entity
		Long id = recordService.save(dto)
		return entityManagerHandler.find(UserRecord, id)
	}

	/**
	 * Finds the record by id (not used at present, but might come in handy
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

}

IdealDetailRecord idealRecord = new IdealDetailRecord(binding)