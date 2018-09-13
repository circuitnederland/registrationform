/**
 * The createUser script creates a payment in Mollie and saves the payment id together with the payment url in the user profile in Cyclos.
 * It also fills the Betaald field to the initial value 'Niet betaald'.
 * It passes metadata to the payment with 'user': the username and 'source': 'registration' (to differentiate from payments made via the 'topup' function).
 */

import org.cyclos.model.ValidationException

try {
	// Use the userService to load the user DTO.
	// Reason: this way, the changes we make in the user profile fields will be visible in the profile history.
	def usrDTO = userService.load(user.id)
	def usr = scriptHelper.wrap(usrDTO)

	// Create a new payment in Mollie.
	BigDecimal contribution = utils.getLidmaatschapsbijdrage(usr)
	BigDecimal aankoop_saldo = (usr.aankoop_saldo?:0)
	def json = utils.setupMollieRegistrationPayment(contribution, aankoop_saldo, usr.username)

    // Store the payment id and the payment URL from Mollie in the Cyclos user profile.
    usr.payment_id = json.id
    usr.payment_url = json._links.checkout.href

	// Fill the Betaald field of the Cyclos user with the initial value of 'Niet betaald'.
	usr.betaald = 'niet_betaald'

	// Save the user DTO.
	userService.save(usrDTO)
} catch (Exception e) {
	utils.sendMailToTechTeam("Foutmelding bij registratie nieuwe gebruiker", utils.prepareMessage("techError", ["error": e.getMessage()]))
	throw new ValidationException(utils.prepareMessage("fatalError"))
}
