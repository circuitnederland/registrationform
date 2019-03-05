/**
 * The topup script creates a payment in Mollie and saves the payment information in an idealDetail user record.
 * The script returns the Mollie URL Cyclos should send the user to, so the user can pay the amount to topup with iDEAL.
 */

import org.cyclos.model.ValidationException

try {
    String consumerName = ''
    String iban = ''
    String bic = ''
    String method = Constants.IDEAL
    PaymentVO transaction = null
    BigDecimal amount = new BigDecimal(formParameters.amount)
    Boolean paid = false
    String source = Constants.TOPUP

	// Create a new topup payment in Mollie and retrieve the id of the returned json payment.
	def json = utils.setupMollieTopupPayment(amount, user, returnUrl)
    String paymentId = json.id

    // Store the payment information in a new idealDetail user record.
    UserRecord idealDetailRecord = idealRecord.create(user, consumerName, iban, bic, paymentId, method, transaction, amount, paid, source)

    // Store the relevant information in the session so topup_redirect can check for it.
    parameterStorage.paymentId = paymentId
    parameterStorage.amount = amount
    parameterStorage.user = user

    // Send the user to Mollie.
    return json._links.checkout.href
} catch (Exception e) {
	utils.sendMailToTechTeam("Foutmelding bij saldo opwaarderen door gebruiker", utils.prepareMessage("techError", ["error": e.getMessage()]))
	throw new ValidationException(utils.prepareMessage("generalErrorWithRetry", ["moment": " in het proces van het opwaarderen"]))
}
