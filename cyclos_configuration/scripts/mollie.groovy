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
import org.cyclos.utils.DateTime
import org.springframework.mail.javamail.MimeMessageHelper
import org.cyclos.entities.banking.PaymentTransferType
import org.cyclos.entities.banking.SystemAccountType
import org.cyclos.model.banking.accounts.SystemAccountOwner
import org.cyclos.model.banking.transactions.PaymentVO
import org.cyclos.model.banking.transactions.PerformPaymentDTO
import org.cyclos.model.banking.transfertypes.TransferTypeVO

/**
 * This script expects the following parameters
 * 

# Transaction settings
systemDebit = debietrekening
fromDebitPaymentType = Aankoop_units
userAccountType = handelsrekening
firstMembershipPaymentType = lidmaatschapsbijdrage
fromDebitPaymentDescription = Aankoop eenheden circuit Nederland. Dit is het bedrag dat u betaald heeft via Ideal, hier betaalt u automatisch uw lidmaatschapsbijdrage mee in circulair geld. 
firstMembertshipPaymentDescription = Betaling eerste lidmaatschapsbijdrage

 # Mollie payment settings
 # Nr of days a payment may be in the past before considered too old when validating user.
 mollie_payment.max_age = 61
 mollie_payment.description = Jaarlijkse contributie (€#lidmaatschapsbijdrage#) + startsaldo (€#aankoop_saldo#) voor gebruiker #username#.
 confirmationUrlPart = /registration_finished.php
 validationUrlPart = /confirm_email.php
 mollieWebhookUrlPart = /run/paid

 # Mailaddress of the circuitnederland tech team - for severe technical problems.
 techTeamMail = tech@circuitnederland.nl

 # Messages to TechTeam:
 paymentAlreadyUsed = Bij activeren van gebruiker #user# is gebleken dat de paymentID (#payment_id#) van deze gebruiker al eerder gebruikt is. De activatie van de gebruiker is hierdoor mislukt.
 paymentTooOld = Bij activeren van gebruiker #user# is gebleken dat de betaling van payment met paymentID: #payment_id# langer geleden is dan gebruikelijk. De activatie van de gebruiker is hierdoor mislukt.
 incorrectAmount = Bij activeren van gebruiker #user# is gebleken dat het betaalde bedrag (#paidAmount#) in de payment (paymentID: #payment_id#) lager is dan het bedrag dat hij/zij zou moeten betalen (#expectedAmount#). De activatie van de gebruiker is hierdoor mislukt.
 wrongSource = Bij activeren van gebruiker #user# heeft de payment een verkeerde source in de metadata: '#source#' in plaats van 'registration' (paymentID: #payment_id#). De activatie van de gebruiker is hierdoor mislukt.
 userWasAlreadySetOnPaid = Het Betaald veld stond al op 'betaald'. Dit kan duiden op een handmatige wijziging door een admin.
 wrongPaymentIsPaid = De id van de payment die de gebruiker zojuist in Mollie heeft betaald is anders dan de payment id die voor deze gebruiker in Cyclos staat.
 webhookError = Er is een exception opgetreden in het mollieWebhook script:\r\n\r\n#error#\r\n\r\nGegevens op dit moment:\r\nIP-adres: #ipAddress#\r\npayment id van Mollie: #paymentIdFromMollie#\r\nGebruikersnaam: #user#\r\npayment id in Cyclos: #paymentIdInCyclos#
 techError = Er is een exception opgetreden in een Cyclos script:\r\n\r\n#error#
 */

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
    String transactionnumberFieldName = 'transactionnumber'
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
	public UserRecord create(User user, String consumerName, String iban, String bic, String paymentId, String method, String transactionnumber, BigDecimal amount, Boolean paid, String source) {
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
        wrapped[transactionnumberFieldName] = transactionnumber
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
    public Object createPayment(String amount, String description, String redirectUrl, String webhookUrl, String userName, String source) {
        def jsonBody = [
            amount: [
                currency: "EUR",
                value: amount
            ],
            description: description, 
            redirectUrl: redirectUrl,
            webhookUrl: webhookUrl,
            metadata: [
                user: userName,
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
    public Object setupMollieRegistrationPayment(BigDecimal contribution, BigDecimal aankoop_saldo, String userName, String validationKey = null) {
        def params = binding.scriptParameters
        def formatter = binding.formatter
        def vars = [
         lidmaatschapsbijdrage: formatter.format(contribution),
         aankoop_saldo: formatter.format(aankoop_saldo),
         username: userName
        ]
        // Convert the total amount to a string with two decimals and a dot as separator - Mollie needs the amount like this.
        String amount = (contribution + aankoop_saldo).setScale(2)
        String description = MessageProcessingHelper.processVariables(params.'mollie_payment.description', vars)
        String redirectUrl = auth.registrationRootUrl
        redirectUrl += validationKey? params.validationUrlPart + "?validationKey=${validationKey}" : params.confirmationUrlPart
        String webhookUrl = binding.sessionData.configuration.rootUrl + params.mollieWebhookUrlPart
        return mollie.createPayment(amount, description, redirectUrl, webhookUrl, userName, "registration")
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
     * Creates the two transactions needed when a new user is being validated (aankoop saldo from debiet to user and contribution from user to sys).
     * Also creates an idealDetail userrecord storing the relevant info of the aankoop transaction.
     */
    public void processRegistrationPayments(User user, BigDecimal totalAmount, BigDecimal contribution, String method, Map<String, String> paymentInfo, String paymentId = ''){
        // Make a Cyclos payment from debit to user with the total amount the user paid.
        EntityManagerHandler entityManagerHandler = binding.entityManagerHandler
        def scriptParameters = binding.scriptParameters
        def paymentService = binding.paymentService
        PerformPaymentDTO credit = new PerformPaymentDTO()
        credit.from = SystemAccountOwner.instance()
        credit.to = new UserLocatorVO(id: user.id)
        SystemAccountType debiet = entityManagerHandler.find(
                SystemAccountType, scriptParameters.systemDebit)
        PaymentTransferType fromDebietPaymentType =  entityManagerHandler.find(
                PaymentTransferType, scriptParameters.fromDebitPaymentType, debiet)
        credit.type = new TransferTypeVO(fromDebietPaymentType.id)
        credit.amount = totalAmount
        credit.description = scriptParameters.fromDebitPaymentDescription
        PaymentVO paymentVO = paymentService.perform(credit)

        // Make a Cyclos payemnt from user to sys organization with the contribution fee.
        PerformPaymentDTO membership = new PerformPaymentDTO()
        membership.from = new UserLocatorVO(id: user.id)
        membership.to = SystemAccountOwner.instance()
        PaymentTransferType toBeheerPaymentType =  entityManagerHandler.find(
                PaymentTransferType,        "${scriptParameters.userAccountType}.${scriptParameters.firstMembershipPaymentType}")
        membership.type = new TransferTypeVO(toBeheerPaymentType.id)
        membership.amount = contribution
        membership.description = scriptParameters.firstMembershipPaymentDescription
        paymentService.perform(membership)

        // Store bank account info on member record.
        def consName = paymentInfo['consumerName']?: ''
        def iban = paymentInfo['iban']?: ''
        def bic = paymentInfo['bic']?: ''
        String transactionnumber = paymentVO.transactionNumber
        Boolean paid = true
        String source = 'registration'
        idealRecord.create(user, consName, iban, bic, paymentId, method, transactionnumber, totalAmount, paid, source)
        def usr = binding.scriptHelper.wrap(user)
        if (!usr.iban.equalsIgnoreCase(iban)) {
            sendMailToAdmin("Circuit Nederland: different bank account", prepareMessage("differentBankAccount", ["user": usr.name]), true)
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
     * Finds the text with the given errorCode, either from a script parameter or from a field in the auth systemrecord.
     * If no text is found, the errorCode itself is returned as a fallback.
     * If the vars parameter is passed, the variables are replaced in the text found.
     */
    public String prepareMessage(String errorCode, Map<String, ?> vars = null, Boolean isHtml = false){
        // Note: we must check if the auth object has the property before trying to access it, to prevent a MissingPropertyException.
        String authField = auth.hasProperty(errorCode) ? auth."${errorCode}" : null

        // Get the message either from a script parameter or from the auth systemrecord. Falling back to the errorCode itself if neither exists.
        String message = binding.scriptParameters."${errorCode}" ?: authField ?: errorCode

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
