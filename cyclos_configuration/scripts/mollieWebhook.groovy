/*
 * This script checks the payment status in Mollie for the posted payment id.
 * 
 * If the payment status in Mollie is 'paid', we check the 'source' metadata in the payment.
 *
 * If the 'source' is 'registration', the 'Betaald' profile field of the
 * Cyclos user that belongs to this payment is updated to 'betaald'. Also the 
 * payment_url profile field of the Cyclos user is emptied, because it is no longer needed.
 * If the posted paymentId from Mollie is not the same as the payment_id of the Cyclos user,
 * but does appear in the profile history of the user, we also change the payment_id of the Cyclos user.
 * This happens when a user first tries to activate before having paid (so Cyclos generates a new payment_id)
 * and than pays in an old Mollie screen he has still open (with the previous payment_id).
 *
 * If the 'source' is 'topup', we lookup the userrecord that was made when the payment was created.
 * This record should have the same paymentId and belong to the user identified in the metadata of the payment.
 * If the record is found and has the correct contents, we create a transaction to topup the balance of the user
 * and we update the userrecord.
 *
 * In all other cases, nothing happens. This could be if:
 * - No payment id is posted at all.
 * - Mollie does not return a payment for the posted payment id.
 * - Mollie indicates the payment has a status other than 'paid'.
 * - The payment in Mollie does not contain a 'user' metadata field.
 * - Cyclos can not find a user with the given userId.
 *
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

def handleRegistrationPayment(User user, String paymentIdFromMollie, Utils utils) {
	def usrDTO = userServiceSecurity.load(user.id)
	def usr = scriptHelper.wrap(usrDTO)
	String paymentIdInCyclos = usr.payment_id
	
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
			throw new Exception(utils.prepareMessage("wrongPaymentIsPaid", ['payment_id': paymentIdInCyclos]))
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

def handleTopupPayment(User user, String paymentId, def paymentResponse, Utils utils){
	// Check the creation date of the payment. As an extra security measure, we check if the payment is not older than 3 hours.
    LocalDateTime paymentTime = LocalDateTime.parse(paymentResponse.createdAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
	if (paymentTime.plusHours(3).isBefore(LocalDateTime.now())){
		throw new Exception(utils.prepareMessage("paymentTooOld", ['user': user.username, 'payment_id': paymentId]))
	}

	// Find the userrecord with the given paymentId.
	UserRecord userRecord = utils.idealRecord.findByPaymentId(paymentId)

	// Check if the record is indeed from our user.
	if (userRecord.user.username != user.username){
		throw new Exception(utils.prepareMessage("wrongOwnerIdealDetailRecord", ['owner': userRecord.user.username, 'user': user.username, 'payment_id': paymentId]))
	}

	// Check the contents of the userrecord we found. The source, method and amount fields should be correct.
	def idealDetailInfo = scriptHelper.wrap(userRecord)
	BigDecimal paidAmount = new BigDecimal(paymentResponse.amount.value)
	if (idealDetailInfo.source.internalName != Constants.TOPUP || idealDetailInfo.method != Constants.IDEAL || idealDetailInfo.amount != paidAmount){
		throw new Exception(utils.prepareMessage("wrongContentsIdealDetailRecord", ['user': user.username, 'payment_id': paymentId, 'idealDetailInfo': idealDetailInfo]))
	}
	// Some fields should be empty, use Groovy truth to check that.
	if (idealDetailInfo.iban || idealDetailInfo.bic || idealDetailInfo.consumerName 
		|| idealDetailInfo.paid || idealDetailInfo.transaction){
		throw new Exception(utils.prepareMessage("reusedIdealDetailRecord", ['user': user.username, 'payment_id': paymentId, 'idealDetailInfo': idealDetailInfo]))
	}

	// Check the record was created by the user itself (this happend via the topup script) and was never modified.
	if (userRecord.createdBy.id != user.id || userRecord.modifiedBy ){
		throw new Exception(utils.prepareMessage("idealDetailRecordModified", ['user': user.username, 'payment_id': paymentId, 'idealDetailInfo': idealDetailInfo]))
	}

	// All checks passed. Create a transaction from system to the user for the amount the user paid.
	PaymentVO paymentVO = utils.transferPurchasedUnits(user, paidAmount, Constants.TOPUP)

	// Update the user record.
	idealDetailInfo.consumerName = paymentResponse.details.consumerName?: ''
	idealDetailInfo.iban = paymentResponse.details.consumerAccount?: ''
	idealDetailInfo.bic = paymentResponse.details.consumerBic?: ''
	idealDetailInfo.transaction = paymentVO
	idealDetailInfo.paid = true
	userRecord.lastModifiedDate = new java.util.Date()
}

String ipAddress
String userId = "onbekend"
String paymentIdFromMollie = "onbekend"
String source = "onbekend"

try {
	// Get the ip address of the server triggering this webhook. Can be helpful in case of strange calls.
	ipAddress = request.requestData.remoteAddress
	
    // Get the payment from Mollie with the posted payment id.
	paymentIdFromMollie = request.getParameter("id")
	def paymentResponse = mollie.getPayment(paymentIdFromMollie)
    
    // Check if Mollie indicates this payment has been paid. If not, stop.
	if (paymentResponse.status != Constants.PAID) {
		return ''
	}
        
	// Get the user and source from Mollie response metadata.
	userId = paymentResponse.metadata.user
	source = paymentResponse.metadata?.source
	
	// Get the Cyclos user with this userId.
	User user = userLocatorHandler.locate(new UserLocatorVO(id: scriptHelper.unmaskId(userId))).getUser()

	// Check the source, this should indicate whether this is a registration payment or a topup payment.
	switch (source){
		case Constants.REGISTRATION:
			handleRegistrationPayment(user, paymentIdFromMollie, utils)
			break;
		case Constants.TOPUP:
			handleTopupPayment(user, paymentIdFromMollie, paymentResponse, utils)
			break;
		default:
			// This should not happen, so raise an exception.
			throw new Exception(utils.prepareMessage("wrongSource", ['user': userId, 'payment_id': paymentIdFromMollie, 'source': source]))
	}

} catch (Exception e) {
    // Exceptions can happen in several cases as described at the top. We only use them to send an email to the tech team. No output is needed to the caller of the webhook (Mollie).
	def vars = ["error": e.getMessage(), 'user': userId, 'paymentIdFromMollie': paymentIdFromMollie, 'ipAddress': ipAddress]
	utils.sendMailToTechTeam("Foutmelding bij verwerken Mollie betaling", utils.prepareMessage("webhookError", vars), true)
} finally {
    // Always return a 200 response code to Mollie otherwise it will try again.
	return ""
}
