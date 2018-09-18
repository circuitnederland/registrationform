import org.cyclos.entities.banking.PaymentTransferType
import org.cyclos.entities.banking.SystemAccountType
import org.cyclos.impl.access.DirectUserSessionData
import org.cyclos.model.ValidationException
import org.cyclos.model.banking.accounts.SystemAccountOwner
import org.cyclos.model.banking.transactions.PerformPaymentDTO
import org.cyclos.model.banking.transfertypes.TransferTypeVO
import org.cyclos.model.users.users.UserLocatorVO
import org.cyclos.model.utils.TransactionLevel

/*
 * This script expects the following parameters: 
 * 
 * systemDebit = debiet
 * fromDebitPaymentType = Aankoop_Units
 * userAccountType = handelsrekening
 * firstMembershipPaymentType = eerste_lidmaatschapsbijdrage
 * fromDebitPaymentDescription = Aankoop eenheden circuit Nederland. Dit is het bedrag dat u betaald heeft via Ideal, hier betaalt u automatisch uw lidmaatschapsbijdrage mee in circulair geld.
 * firstMembertshipPaymentDescription = Betaling eerste lidmaatschapsbijdrage
 * 
 */

def usrDTO = userService.load(user.id)
def usr = scriptHelper.wrap(usrDTO)
if (usr.brokers) {
	// stop the script
	return
}

String paymentId = usr.payment_id

try{
	def paymentResponse = mollie.getPayment(paymentId)
	BigDecimal contribution = utils.getLidmaatschapsbijdrage(usr)
	BigDecimal aankoop_saldo = (usr.aankoop_saldo?:0)
	BigDecimal totalAmount = contribution + aankoop_saldo

	switch(paymentResponse.status) {
		case "paid":
			// Verify if the payment_id was used before by someone else. We check the idealDetail userrecords for this. This can happen if an admin reuses a payment_id from someone else.
			if (utils.isPaymentIdUsedBefore(paymentId, user)) {
				throw new Exception(utils.prepareMessage('paymentAlreadyUsed', ['user': usr.username, 'payment_id': paymentId]))
			}
			// Verify if the payment is too old. This can only happen if an admin reuses an old payment_id that is not in a userrecord (which we checked with isPaymentIdUsedBefore above).
			if (utils.isPaymentTooOld(paymentResponse.createdAt)) {
				throw new Exception(utils.prepareMessage('paymentTooOld', ['user': usr.username, 'payment_id': paymentId]))
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
			userService.save(usrDTO)
			break;
		case "pending":
			throw new ValidationException(utils.prepareMessage("pending", null, true))
		case "open":
			// @todo: The current code to cancel the payment does not work (Mollie returns a 422 - The payment cannot be cancelled.). Also the payment isCancelable is false.
			// The reason to try to cancel the payment is explained in GH issue #15. So this issue is still open.

			// // Invalidate the old payment in Mollie, so the user can not pay that anymore, even if he has the Mollie screen with that payment still open somewhere.
			// mollie.cancelPayment(usr.payment_id)
			// // @todo: should we check the result? It should return the payment and we could check if its status is indeed canceled.
			// // @todo: should we empty the payment_id and payment_url in the user profile? If all goes well, they will be filled with new data by the next lines of code anyway.
			// // But if something goes wrong with that, perhaps it is useful to be able to see the old payment data in the user profile?

			// Don't break here, but continue with the default case, setting up a new Mollie payment.
		default:
			// According to https://docs.mollie.com/payments/status-changes payment status can be one of: open, canceled, pending, expired, failed or paid.
			// So, if the status is not paid or pending, it is one of open, canceled, expired or failed.
			// In all these cases the user has not paid yet. So we create a new payment in Mollie and store its info in the user profile.

			// Create a new payment in Mollie.
			// Note: the validationKey is in the user object, not in usr.
			def json = utils.setupMollieRegistrationPayment(contribution, aankoop_saldo, usr.username, user.validationKey)
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

	// Activate actions.
	// Accept all personal agreements.
	invokerHandler.runAs(new DirectUserSessionData(user, sessionData)) {
		def pendingAgreements = agreementLogService.getPendingAgreements()
		if (pendingAgreements) {
			agreementLogService.accept(pendingAgreements.toSet())
		}
	}

	// Make a Cyclos payment from debit to user with the total amount the user paid.
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
	paymentService.perform(credit)

	// Make a Cyclos payemnt from user to sys organization with the contribution fee.
	PerformPaymentDTO membership = new PerformPaymentDTO()
	membership.from = new UserLocatorVO(id: user.id)
	membership.to = SystemAccountOwner.instance()
	PaymentTransferType toBeheerPaymentType =  entityManagerHandler.find(
			PaymentTransferType,        "${scriptParameters.userAccountType}.${scriptParameters.firstMembershipPaymentType}")
	membership.type = new TransferTypeVO(toBeheerPaymentType.id)
	membership.amount = contribution
	membership.description = scriptParameters.firstMembertshipPaymentDescription
	paymentService.perform(membership)

	// Store bank account info on member record.
	if (paymentResponse?.details) {
		def consName = paymentResponse.details.consumerName
		def iban = paymentResponse.details.consumerAccount
		def bic = paymentResponse.details.consumerBic
		idealRecord.create(user, consName, iban, bic)
		if (!usr.iban.equalsIgnoreCase(iban)) {
			utils.sendMailToAdmin("Circuit Nederland: different bank account", utils.prepareMessage("differentBankAccount", ["user": usr.name]), true)
		}
	}
} catch (ValidationException vE) {
	throw vE
} catch (Exception e) {
	utils.sendMailToTechTeam("Foutmelding bij activeren nieuwe gebruiker", utils.prepareMessage("techError", ["error": e.getMessage()]))
	throw new ValidationException(utils.prepareMessage("fatalError"))
}
