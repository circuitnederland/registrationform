/**
 * Extension point script to ensure the IBAN user profile field complies to our conventions (spaces and uppercase letters).
 */

def usr = scriptHelper.wrap(user)
usr.iban = new Utils(binding).ibanByConvention(usr.iban)
