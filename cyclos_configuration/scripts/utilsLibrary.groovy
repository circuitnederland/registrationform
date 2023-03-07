import static groovy.transform.TypeCheckingMode.SKIP

import groovy.transform.TypeChecked
import javax.mail.internet.InternetAddress
import org.cyclos.server.utils.MessageProcessingHelper
import org.springframework.mail.javamail.MimeMessageHelper

/**
 * Utils library class containing several helper methods for use in C3NL scripts.
 */

@TypeChecked
class Utils {
    private Binding binding
	Map<String, String> scriptParameters

    public Utils(Binding binding) {
        this.binding = binding
        def vars = binding.variables
        scriptParameters = vars.scriptParameters as Map<String, String>
    }

    /**
     * Ensures the given iban complies with the pattern conventions for ibans we use (spacing and uppercase).
     *
     * If the given iban already complies with these conventions, it is returned unchanged.
     * If not, the corrected iban is returned.
     * 
     * Note: we also allow non-Dutch ibans, so the number of characters may vary. This is why we
     * can not use a Cyclos input mask for this.
     *
     * This method does NOT check if the given iban is a valid iban, this is done by the validation script.
     * This is because we can not correct invalid ibans automatically, so we let the validation script throw a validation exception.
     */
    String ibanByConvention(String iban) {
        if( this.isIbanConventionCompliant(iban) ) {
            // The iban pattern is fine, return it as-is.
            return iban
        }
        return this.correctIbanPattern(iban)
    }

    /**
     * Correct the pattern of a given IBAN by putting a space after each block of four characters and using uppercase letters.
     */
    String correctIbanPattern(String iban) {
        String correctedIBAN = ''
        int pos = 0
        iban.replaceAll("\\s",'').each {
            correctedIBAN += it
            pos ++
            if ( pos % 4 === 0 ) {
                correctedIBAN += ' '
            }
        }
        return correctedIBAN.toUpperCase()
    }
    
    /**
     * Returns whether the given IBAN complies to the conventions we use for ibans:
     * - A space after each block of four characters.
     * - Only uppercase letters.
     *
     * Example: NL02 ABNA 0123 4567 89
     */
    Boolean isIbanConventionCompliant(String iban) {
        return iban ==~ /^([A-Z0-9]{4} )*[A-Z0-9]{1,4}$/
    }

    /**
     * Checks whether two given IBANs are the same, ignoring upper-/lowercase and spaces.
     */
    public Boolean isIbansEqual(String ibanA, String ibanB){
        return ibanA?.replace(" ","").equalsIgnoreCase(ibanB?.replace(" ", ""))
    }

	/**
	 * Sends an e-mail to the admin with the given message and subject.
	 */
    public void sendMailToAdmin(String subject, String msg, Boolean isOnCommit = false, Boolean isHtml = false) {
        msg = "${scriptParameters.salutationAdmin}\n\n${msg}\n\n${scriptParameters.closingAdmin}"
        sendMail("Admin Circuit Nederland", scriptParameters.adminMail, subject, msg, isOnCommit, isHtml)
    }

    /**
     * Sends an e-mail to the tech team with the given message and subject.
     */
    public void sendMailToTechTeam(String subject, String msg, Boolean isOnCommit = false) {
        sendMail("Tech Team Circuit Nederland", scriptParameters.techTeamMail, subject, msg, isOnCommit)
    }

    /**
     * Sends an e-mail to the requested addressee with the given message and subject.
     */
    @TypeChecked(SKIP)
    public void sendMail(String toName, String toMail, String subject, String msg, Boolean isOnCommit = false, Boolean isHtml = false) {
        def fromEmail = binding.sessionData.configuration.smtpConfiguration.fromAddress
        String fromName = binding.sessionData.configuration.emailName
        def sender = binding.mailHandler.mailSender
        def message = sender.createMimeMessage()
        def helper = new MimeMessageHelper(message, true, "UTF-8")
        helper.to = new InternetAddress(toMail, toName)
        helper.from = new InternetAddress(fromEmail, fromName)
        helper.subject = subject
        if (isHtml) {
            msg = msg.replace("\r\n", "<br>").replace("\n", "<br>").replace("\r", "<br>")
            helper.setText(msg, true)
        } else {
            helper.setText msg
        }
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
     * Returns a message with any placeholders replaced by the dynamic texts in the given vars Map.
     * The message is taken from the scriptParameters by the given errorCode, or is the errorCode itself
     * if no scriptparameter with the given code exists.
     */
    String dynamicMessage(String errorCode, Map<String, Object> vars = null) {
        def messageHolder = scriptParameters[errorCode] ?: errorCode
        return MessageProcessingHelper.processVariables(messageHolder, vars)
    }
}
