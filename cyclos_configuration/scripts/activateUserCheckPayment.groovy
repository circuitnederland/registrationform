import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.cyclos.impl.access.DirectUserSessionData
import org.cyclos.model.ValidationException
import org.cyclos.model.utils.TransactionLevel

// @todo: change the code below back to use the userService, so changes are shown in the profile history.
// For now, we don't use this, because a side-effect is that Cyclos does not set the validationKey to null.
// See GH issue #51.
// def usrDTO = userService.load(user.id)
// def usr = scriptHelper.wrap(usrDTO)
def usr = scriptHelper.wrap(user)

String paymentId = usr.payment_id

if (paymentId == 'not_ideal') {
	// Stop the script; this user is being activated manually because of an alternative payment method.
	// First, empty the payment_id, so it is less obvious we use this 'not_ideal' string for special cases.
	usr.payment_id = ""
	// @todo: change this back to use the userService - GH issue #51.
	// userService.save(usrDTO)
	return
}

try{
	def paymentResponse = utils.findRelevantPaymentForUser(usr)
	BigDecimal contribution = utils.getLidmaatschapsbijdrage(usr)
	BigDecimal aankoop_saldo = (usr.aankoop_saldo?:0)
	BigDecimal totalAmount = contribution + aankoop_saldo

	switch(paymentResponse.status) {
		case "paid":
			// Check whether the paid payment is the one we have in the user profile.
			if (paymentResponse.id != paymentId) {
				// The active payment is not the paid one. This can happen in theory when the webhook was not fast enough.
				// Change the payment_id in the user profile to the paid payment and continue with this paymentId.
				paymentId = paymentResponse.id
				usr.payment_id = paymentId
			}
			// Verify the payment is not older than the user itself. Take into account the server-times might be some hours apart.
			LocalDateTime paymentTime = LocalDateTime.parse(paymentResponse.createdAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
			LocalDateTime userTime = LocalDateTime.ofInstant(user.creationDate.toInstant(), ZoneId.systemDefault())
			if (paymentTime.plusHours(3).isBefore(userTime)){
				throw new Exception(utils.prepareMessage("paymentTooOld", ['user': user.username, 'payment_id': paymentId]))
			}
			// Verify whether the paid amount is correct.
			BigDecimal paidAmount = new BigDecimal(paymentResponse.amount.value)
			if (paidAmount < totalAmount) {
				def vars = ['user': usr.username, 'payment_id': paymentId, 'paidAmount': formatter.format(paidAmount), 'expectedAmount': formatter.format(totalAmount)]
				throw new Exception(utils.prepareMessage("incorrectAmount", vars))
			}
			// Verify the payment originaly came from a registration. Payments can also have source ‘topup’, but not when activating a user.
			if (paymentResponse.metadata?.source != 'registration'){
				// @todo: if the payment metadata has no source information at all, is this an error? Or is it simply an older payment?
				throw new Exception(utils.prepareMessage("wrongSource", ['user': usr.username, 'payment_id': paymentId, 'source': paymentResponse.metadata?.source]))
			}
			// All checks are oke: set betaald field to the internal value for 'Heeft betaald'.
			// @todo: normally the mollieWebhook should have set the betaald field already to 'Heeft betaald', right after the user paid in mollie.
			// So should we remove this code here? And if so, should we replace it with a check whether the betaald field is indeed 'Heeft betaald'? Or just ignore if it is not?
			// And if we DO want to set the betaald veld here (again), should we use a parallel transaction for this, so it will always work even if something later on fails? I don't think so?
			usr.betaald = 'betaald'
			// @todo: change this back to use the userService - GH issue #51.
			// userService.save(usrDTO)
			break;
		case "pending":
			throw new ValidationException(utils.prepareMessage("pending", null, true))
		default:
			// According to https://docs.mollie.com/payments/status-changes payment status can be one of: open, canceled, pending, expired, failed or paid.
			// So, if the status is not paid or pending, it is one of: open, canceled, expired or failed.
			// In all these cases the user has not paid yet. So we create a new payment in Mollie and store its info in the user profile.

			// Create a new payment in Mollie.
			// Note: the validationKey is in the user object, not in usr.
			def json = utils.setupMollieRegistrationPayment(contribution, aankoop_saldo, user, user.validationKey)
			String payment_id = json.id
			String payment_url = json._links.checkout.href

	        // Store the payment id and the payment URL from Mollie in the Cyclos user profile.
	        // Use a parallel transaction for this, because the main transaction will get a rollback on failing the validation of this user.
			def future = invokerHandler.runAsInParallelTransaction(sessionData, TransactionLevel.READ_WRITE) {
			    // Fetch the user again
				usrDTO = userService.load(user.id)
				usr = scriptHelper.wrap(usrDTO)
		        usr.payment_id = payment_id
		        usr.payment_url = payment_url

				// Save the userprofile field changes.
				userService.save(usrDTO)

				// @todo: in the (new) documentation we wrote 'Also make sure to set the betaald field to not paid.'. But the field can not be anything else yet. Should we check this?
				// // Fill the Betaald field of the Cyclos user with the initial value of 'Niet betaald'.
				// usr.betaald = 'niet_betaald'
			}
			// Wait for the transaction to finish.
			future.get()

			// Only then throw the exception to rollback the main transaction.
			def vars = ['lidmaat': formatter.format(contribution), 'aankoop': formatter.format(aankoop_saldo), 'totaal': formatter.format(totalAmount), 'link': "<a href=\"${payment_url}\">Naar betaalscherm</a>"]
			throw new ValidationException(utils.prepareMessage("notPaid", vars, true))
	}

	// Activate actions:

	// Accept all personal agreements, except when the user was created by a broker.
	// When a broker registers a user we can not be sure the user already accepted the agreements, so we don't accept them here.
	// The user will then be prompted to accept the agreements on first login.
	if (!user.brokers) {
		utils.acceptAgreements(user)
	}

	// Create the transactions.
	String method = 'ideal'
	def paymentInfo = [
		'consName': paymentResponse.details.consumerName,
		'iban': paymentResponse.details.consumerAccount,
		'bic': paymentResponse.details.consumerBic
	]
	utils.processRegistrationPayments(user, totalAmount, contribution, method, paymentInfo, paymentId)
} catch (ValidationException vE) {
	throw vE
} catch (Exception e) {
	utils.sendMailToTechTeam("Foutmelding bij activeren nieuwe gebruiker", utils.prepareMessage("techError", ["error": e.getMessage()]))
	throw new ValidationException(utils.prepareMessage("fatalError"))
}
