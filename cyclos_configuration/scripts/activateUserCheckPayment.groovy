import org.cyclos.entities.banking.PaymentTransferType
import org.cyclos.entities.banking.SystemAccountType
import org.cyclos.impl.access.DirectUserSessionData
import org.cyclos.model.ValidationException
import org.cyclos.model.banking.accounts.SystemAccountOwner
import org.cyclos.model.banking.transactions.PerformPaymentDTO
import org.cyclos.model.banking.transfertypes.TransferTypeVO
import org.cyclos.model.users.users.UserLocatorVO

/*
 * This script expects the following parameters: 
 * 
 * systemDebit = debiet
 * fromDebitPaymentType = Aankoop_Units
 * userAccountType = handelsrekening
 * firstMembershipPaymentType = eerste_lidmaatschapsbijdrage
 * 
 */

def usr = scriptHelper.wrap(user)
if (usr.brokers) {
	// stop the script
	return
}

def Boolean activate = false;
def paymentResponse

// 3: check betaald field
if (!usr.betaald?.value?.equalsIgnoreCase("heeft betaald")) {
	// 4 TESTED: get payment_id field value
	def paymentId = usr.payment_id
	// 4: TESTED: get payment status @ molly
	paymentResponse = mollie.getPayment(paymentId)

	//5: TESTED: check payment status
	switch(paymentResponse.status) {
		case "paid":
		// 6: verify the amount paid
			if (actions.verifyAmount(paymentResponse.amount, usr)) {
				// 8: set betaald field to the internal value for 'Heeft betaald'
				usr.betaald = 'betaald'
				activate = true;
			} else {
				// 6b not correct amount:
				actions.mailAlert(usr, paymentResponse.amount)
				// and fail
				throw new ValidationException("Benodigd lidmaatschap is niet volledig betaald. Er is GEEN mail gestuurd, want dit is verdacht...")
			}
			break;
		case "pending":
		// payment is pending,
			actions.mailPending(usr)
			throw new ValidationException("Betaling is in 'pending' state. Er is een mail gestuurd met het advies het later nog eens te proberen.")
			break;
		default:
		// 7: TESTED other status, need new payment object from mollie
			actions.mailNotPaid(usr, scriptHelper.maskId(user.id))
		// and fail
		//TODO 7c: display a message if done by admin
			throw new ValidationException("Benodigd lidmaatschap is niet betaald. Er is een mail gestuurd.")
	}
} else {
	// 9: heeft betaald volgens cyclos
	activate = true;
}


//activate actions
if (activate) {
	// 9 accept all personal agreements //TESTED: works.
	invokerHandler.runAs(new DirectUserSessionData(user, sessionData)) {
		def pendingAgreements = agreementLogService.getPendingAgreements()
		if (pendingAgreements) {
			agreementLogService.accept(pendingAgreements.toSet())
		}
	}
	def lidmaatschap = actions.getLidmaatschapsbijdrage(usr)

	// 9: aankoopsaldo van debit naar user
	PerformPaymentDTO credit = new PerformPaymentDTO()
	credit.from = SystemAccountOwner.instance()
	credit.to = new UserLocatorVO(id: user.id)
	SystemAccountType debiet = entityManagerHandler.find(
			SystemAccountType, scriptParameters.systemDebit)
	PaymentTransferType fromDebietPaymentType =  entityManagerHandler.find(
			PaymentTransferType, scriptParameters.fromDebitPaymentType, debiet)
	credit.type = new TransferTypeVO(fromDebietPaymentType.id)
	credit.amount = lidmaatschap + (usr.aankoop_saldo?:0)
    credit.description = scriptParameters.fromDebitPaymentDescription
	paymentService.perform(credit)

	// 10: fee van user naar sys organization
	PerformPaymentDTO membership = new PerformPaymentDTO()
	membership.from = new UserLocatorVO(id: user.id)
	membership.to = SystemAccountOwner.instance()
	PaymentTransferType toBeheerPaymentType =  entityManagerHandler.find(
			PaymentTransferType,        "${scriptParameters.userAccountType}.${scriptParameters.firstMembershipPaymentType}")
	membership.type = new TransferTypeVO(toBeheerPaymentType.id)
	membership.amount = lidmaatschap
    membership.description = scriptParameters.firstMembertshipPaymentDescription
	paymentService.perform(membership)

	//11: store bank account info on member record.
	if (paymentResponse?.details) {
		def consName = paymentResponse.details.consumerName
		def iban = paymentResponse.details.consumerAccount
		def bic = paymentResponse.details.consumerBic
		idealRecord.create(user, consName, iban, bic)
		if (!usr.iban.equalsIgnoreCase(iban)) {
			actions.mailDifferentBankAccount(usr)
		}
	}

}
