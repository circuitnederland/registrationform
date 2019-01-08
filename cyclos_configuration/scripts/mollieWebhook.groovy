/*
 * This script checks the payment status in Mollie for the posted payment id.
 * 
 * If the payment status in Mollie is 'paid', the 'Betaald' profile field of the
 * Cyclos user that belongs to this payment is updated to 'betaald'. Also the 
 * payment_url profile field of the Cyclos user is emptied, because it is no longer needed.
 * If the posted paymentId from Mollie is not the same as the payment_id of the Cyclos user,
 * but does appear in the profile history of the user, we also change the payment_id of the Cyclos user.
 * This happens when a user first tries to activate before having paid (so Cyclos generates a new payment_id)
 * and than pays in an old Mollie screen he has still open (with the previous payment_id).
 *
 * In all other cases, nothing happens. This could be if:
 * - No payment id is posted at all.
 * - Mollie does not return a payment for the posted payment id.
 * - Mollie indicates the payment has a status other than 'paid'.
 * - The payment in Mollie does not contain a 'user' metadata field.
 * - Cyclos can not find a user with the given username.
 *
 */

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
		def usrDTO = userServiceSecurity.load(user.id)
		def usr = scriptHelper.wrap(usrDTO)
        paymentIdInCyclos = usr.payment_id
		
		// Check whether the paymentId field of this user in Cyclos is the same as the paymentId Mollie posted.
		if (paymentIdInCyclos != paymentIdFromMollie) {
			// The user probably paid a previous payment with an old Mollie screen still open.
			// Search for it in the profilehistory.
			List<String> paymentIds = utils.getPaymentIdsFromProfileHistory(usr)
			if (paymentIds.contains(paymentIdFromMollie)) {
				// We found an old payment in the user history the user paid now. So set this as the current payment_id.
				usr.payment_id = paymentIdFromMollie
			} else {
				// The user paid a payment we can not find in Cyclos. That should not happen, so inform the tech team and stop.
				throw new Exception(utils.prepareMessage("wrongPaymentIsPaid"))
			}
		}

        // Check if the Cyclos Betaald field is already 'betaald'.
		if (usr.betaald) {
            if ('betaald' == usr.betaald.getInternalName()) {
				// Maybe an admin did this, or maybe the user did two payments. Either way, inform the tech team and stop.
				throw new Exception(utils.prepareMessage("userWasAlreadySetOnPaid"))
            }
        }
        
        // Set the betaald field of the user to 'betaald'.
		usr.betaald = "betaald"

		// Empty the payment_url field of the user, because it is not needed anymore.
		usr.payment_url = ""

		// Save the userprofile field changes.
		userServiceSecurity.save(usrDTO)
	} 
} catch (Exception e) {
    // Exceptions can happen in several cases as described at the top. We only use them to send an email to the tech team. No output is needed to the caller of the webhook (Mollie).
	def vars = ["error": e.getMessage(), 'user': userName, 'paymentIdFromMollie': paymentIdFromMollie, 'paymentIdInCyclos': paymentIdInCyclos, 'ipAddress': ipAddress]
	utils.sendMailToTechTeam("Foutmelding bij verwerken Mollie betaling nieuwe gebruiker", utils.prepareMessage("webhookError", vars), true)
} finally {
    // Always return a 200 response code to Mollie otherwise it will try again.
	return ""
}
