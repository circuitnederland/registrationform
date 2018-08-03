/*
 * This script checks the payment status in Mollie for the posted payment id.
 * 
 * If the payment status in Mollie is 'paid', the 'Betaald' profile field of the
 * Cyclos user that belongs to this payment is updated to 'betaald'.
 *
 * In all other cases, nothing happens. This could be if:
 * - No payment id is posted at all.
 * - Mollie does not return a payment for the posted payment id.
 * - Mollie indicates the payment has a status other than 'paid'.
 * - The payment in Mollie does not contain a 'user' metadata field.
 * - Cyclos can not find a user with the given username.
 *
 */


// @todo: move the MailHelper class to a library script.
import javax.mail.internet.InternetAddress
import org.cyclos.server.utils.MessageProcessingHelper
import org.springframework.mail.javamail.MimeMessageHelper
/**
 * This script expects the following parameters:
 * 
 * techTeamMail = tech@circuitnederland.nl
 * 
 */
class MailHelper{
    private Object binding
	
    public MailHelper(Object binding) {
        this.binding = binding
    }
	
	/**
	 * Sends an e-mail to the tech team with the given message and subject.
	 * Does not do a rollback of the transaction.
	 */
    public void sendMailToTechTeam(String msg, String subject, Map msgVars = null) {
        if (msgVars != null) {
            msg = MessageProcessingHelper.processVariables(msg, msgVars)
        }
        sendMail(binding.scriptParameters.techTeamMail, msg, subject)
    }
	
	/**
	 * Sends an e-mail.
	 */
    private void sendMail(String toEmail, String body, String subject, boolean doRollback = false) {
		def fromEmail = binding.sessionData.configuration.smtpConfiguration.fromAddress
		def sender = binding.mailHandler.mailSender
        def message = sender.createMimeMessage()
        def helper = new MimeMessageHelper(message)
        helper.to = new InternetAddress(toEmail)
        helper.from = new InternetAddress(fromEmail)
        helper.subject = subject
        helper.text = body
        if (doRollback) {
            // Send the message after rollback.
            binding.scriptHelper.addOnRollback {
                sender.send message
            }
        } else {
            // Send the message after commit.
            binding.scriptHelper.addOnCommit {
                sender.send message
            }
        }
    }
}

String ipAddress
String userName = "onbekend"
String paymentIdFromMollie = "onbekend"
String paymentIdInCyclos = "onbekend"

try {
	// Get the ip address of the server triggering this webhook. Can be helpful in case of strange calls.
	ipAddress = request.requestData.remoteAddress
	
    // Get the payment from Mollie with the posted payment id.
	paymentIdFromMollie = request.getParameter("id")
	def paymentResponse = mollie.getPayment(paymentIdFromMollie)
    
    // Check if Mollie indicates this payment has been paid.
	if (paymentResponse.status == "paid") {
        
		// Get the username from Mollie response metadata.
		userName = paymentResponse.metadata.user
        
        // Get the Cyclos user with this username.
		def user = conversionHandler.convert(User, userName)
        def bean = scriptHelper.wrap(user)
        paymentIdInCyclos = bean.payment_id
        
        // Check if the Cyclos Betaald field is already 'betaald'. This should normally not be the case, but could have been set by an admin.
		if (bean.betaald) {
            if ('betaald' == bean.betaald.getInternalName()) {
				throw new Exception("Het Betaald veld stond al op 'betaald'. Dit kan duiden op een handmatige wijziging door een admin.")
            }
        }
        
        // Set the betaald field of the user to 'betaald'.
		bean.betaald = "betaald"
		
		// Check whether the payment id field of this user in Cyclos is the same as the payment id Mollie posted.
		if (paymentIdInCyclos != paymentIdFromMollie) {
			// The user probably paid a previous payment with an old Mollie screen still open after validating. An admin should look into this.
			throw new Exception("De id van de payment die de gebruiker zojuist in Mollie heeft betaald is anders dan de payment id die voor deze gebruiker in Cyclos staat.")
		}
	} 
} catch (Exception e) {
    // Exceptions can happen in several cases as described at the top. We only use them to send an email to the tech team. No output is needed to the caller of the webhook (Mollie).
    // Also no rollback is needed, because the only db thing we do in this script is update the Betaald field.
	def mailHelper = new MailHelper(binding)
	String mailSubject = "Foutmelding bij verwerken Mollie betaling nieuwe gebruiker"
	String mailMessage = "Er is iets afwijkends aan de hand bij het verwerken van de Mollie betaling van een nieuw geregistreerde gebruiker:\r\n\r\n#exception#\r\n\r\nGegevens op dit moment:\r\nIP-adres: #ipAddress#\r\npayment id van Mollie: #paymentIdFromMollie#\r\nGebruikersnaam: #userName#\r\npayment id in Cyclos: #paymentIdInCyclos#"
	def mailParams = [
		exception: e.getMessage(),
		ipAddress: ipAddress,
		paymentIdFromMollie: paymentIdFromMollie,
		userName: userName,
		paymentIdInCyclos: paymentIdInCyclos
	]
	mailHelper.sendMailToTechTeam(mailMessage, mailSubject, mailParams)
} finally {
    // Always return a 200 response code to Mollie otherwise it will try again.
	return ""
}
