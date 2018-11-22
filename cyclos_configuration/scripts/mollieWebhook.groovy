/*
 * This script checks the payment status in Mollie for the posted payment id.
 * 
 * If the payment status in Mollie is 'paid', the 'Betaald' profile field of the
 * Cyclos user that belongs to this payment is updated to 'betaald'. Also the 
 * payment_url profile field of the Cyclos user is emptied, because it is no longer needed.
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

        // Check if the Cyclos Betaald field is already 'betaald'. This should normally not be the case, but could have been set by an admin.
		if (usr.betaald) {
            if ('betaald' == usr.betaald.getInternalName()) {
				throw new Exception(utils.prepareMessage("userWasAlreadySetOnPaid"))
            }
        }
        
        // Set the betaald field of the user to 'betaald'.
		usr.betaald = "betaald"

		// Empty the payment_url field of the user, because it is not needed anymore.
		usr.payment_url = ""

		// Save the userprofile field changes.
		userServiceSecurity.save(usrDTO)
		
		// Check whether the payment id field of this user in Cyclos is the same as the payment id Mollie posted.
		if (paymentIdInCyclos != paymentIdFromMollie) {
			// The user probably paid a previous payment with an old Mollie screen still open after validating. An admin should look into this.
			throw new Exception(utils.prepareMessage("wrongPaymentIsPaid"))
		}
	} 
} catch (Exception e) {
    // Exceptions can happen in several cases as described at the top. We only use them to send an email to the tech team. No output is needed to the caller of the webhook (Mollie).
	def vars = ["error": e.getMessage(), 'user': userName, 'paymentIdFromMollie': paymentIdFromMollie, 'paymentIdInCyclos': paymentIdInCyclos, 'ipAddress': ipAddress]
	utils.sendMailToTechTeam("Foutmelding bij verwerken Mollie betaling nieuwe gebruiker", utils.prepareMessage("webhookError", vars), true)
} finally {
    // Always return a 200 response code to Mollie otherwise it will try again.
	return ""
}
