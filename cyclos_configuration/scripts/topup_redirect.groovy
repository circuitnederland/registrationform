/**
 * The topup_redirect script is called by Mollie after the user did a topup.
 * The script checks the status of the topup payment and shows a respons message to the user.
 * The actual topping up is done by the mollieWebhook.
 */

import org.cyclos.model.ValidationException

try {
    // Use the information stored in the session.
    User user = parameterStorage.user
    String paymentId = parameterStorage.paymentId
    BigDecimal amount = parameterStorage.amount

    // Get the Mollie payment - this errors out when the payment found turns out to be of another user.
    def paymentInfo = utils.getMolliePaymentForUser(user, paymentId)

    // Check whether the information in the payment is correct.
    BigDecimal paidAmount = new BigDecimal(paymentInfo.amount.value)
    if (paidAmount != amount) {
        def vars = ['user': user.username, 'payment_id': paymentId, 'paidAmount': formatter.format(paidAmount), 'expectedAmount': formatter.format(amount)]
        throw new Exception(utils.prepareMessage("incorrectAmount", vars))
    }
    if (paymentInfo.metadata?.source != Constants.TOPUP) {
        throw new Exception(utils.prepareMessage("wrongSource", ['user': user.username, 'payment_id': paymentId, 'source': paymentInfo.metadata?.source]))
    }

    // All checks are done. Show the correct message to the user, depending on the payment status.
    String msg = ''
    switch (paymentInfo.status) {
        case Constants.PAID:
            msg = utils.prepareMessage("topupSuccess")
            break;
        case Constants.PENDING:
            msg = utils.prepareMessage("topupPending")
            break;
        default:
            msg = utils.prepareMessage("topupFailed")
    }
    return msg
} catch (Exception e) {
	utils.sendMailToTechTeam("Foutmelding bij redirect na saldo opwaarderen", utils.prepareMessage("techError", ["error": e.getMessage()]))
	return utils.prepareMessage("generalError", ["moment": " bij het verwerken van de opwaardering van je saldo"])
}
