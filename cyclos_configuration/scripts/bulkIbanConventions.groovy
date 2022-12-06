import org.cyclos.impl.users.BulkActionUserResult
import org.cyclos.model.users.bulkactions.BulkActionUserStatus
import org.apache.commons.validator.routines.checkdigit.IBANCheckDigit

/**
 * Bulk action script to ensure all existing users have an IBAN that complies to our conventions (spaces and uppercase letters).
 */

Utils utils = new Utils(binding)
def usr = scriptHelper.wrap(user)
def iban = usr.iban

// If the IBAN is not a valid IBAN, this must be fixed manually.
if ( ! IBANCheckDigit.IBAN_CHECK_DIGIT.isValid(iban.replaceAll("\\s", "")) ) {
    return new BulkActionUserResult( BulkActionUserStatus.ERROR, "Invalid IBAN: ${iban}" )
}

if( utils.isIbanConventionCompliant(iban) ) {
    // The IBAN pattern is fine, skip the user.
    return BulkActionUserStatus.SKIPPED
}

// Correct the pattern of the IBAN and save it in the user profile.
usr.iban = utils.correctIbanPattern(iban)
return BulkActionUserStatus.SUCCESS
