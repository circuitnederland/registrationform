import org.cyclos.model.ValidationException

def usrDTO = userService.load(user.id)
def usr = scriptHelper.wrap(usrDTO)

// Check if the given iban and e-mail match those in the cyclos user profile.
if (formParameters.email != user.email) {
    throw new ValidationException("Het ingevoerde e-mailadres (${formParameters.email}) komt niet overeen met het e-mailadres van deze gebruiker in Cyclos (${user.email}).")
}
if (!utils.isIbansEqual(usr.iban, formParameters.iban)){
    throw new ValidationException("Het ingevoerde IBAN (${formParameters.iban}) komt niet overeen met het IBAN van deze gebruiker in Cyclos (${usr.iban}). Controleer of je het IBAN correct hebt ingevuld of, als het IBAN in Cyclos niet juist is, pas dan eerst het IBAN van deze gebruiker in Cyclos aan in het geverifieerde IBAN voordat je deze operatie laat uitvoeren.")
}

// Check if the amount paid equals what Cyclos expects (contribution + optional aankoop_saldo).
BigDecimal contribution = utils.getLidmaatschapsbijdrage(usr)
BigDecimal aankoop_saldo = (usr.aankoop_saldo?:0)
BigDecimal totalAmount = contribution + aankoop_saldo

if (formParameters.amount != totalAmount) {
    def amountEntered = formatter.format(formParameters.amount)
    def amountCyclos = formatter.format(totalAmount)
    throw new ValidationException("Het ingevoerde bedrag (${amountEntered}) komt niet overeen met wat Cyclos voor deze gebruiker verwacht (${amountCyclos}).")
}

// All checks are done, so we can now validate the user.

// First mark the user to be a 'not_ideal' case, so our activateUser extension can skip checking for an iDEAL payment in Mollie.
// Use the userService for changing the user profile fields, so it becomes visible in the user profile history.
usr.payment_id = "not_ideal"
usr.payment_url = ""
usr.betaald = "betaald"
userService.save(usrDTO)

// Call userService to validate the user.
userValidationService.manuallyValidateRegistration(new UserLocatorVO(user.id))

// Accept all personal agreements if the admin confirmed the user has done this.
if (formParameters.agreements_accepted) {
    utils.acceptAgreements(user)
}

// Create the transactions.
String method = formParameters.method.internalName
def paymentInfo = [
	'consName': formParameters.accountName,
	'iban': formParameters.iban,
	'bic': ''
]
utils.processRegistrationPayments(user, totalAmount, contribution, method, paymentInfo)

// Return to the user profile.
String fullUrl = sessionData.configuration.fullUrl
String userId = scriptHelper.maskId(user.id)
return "${fullUrl}#users.users.search%7Cusers.users.profile!id=${userId}"
