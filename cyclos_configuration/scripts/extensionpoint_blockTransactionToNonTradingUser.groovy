import org.cyclos.entities.users.User
import org.cyclos.model.banking.accounts.AccountOwner
import org.cyclos.model.ValidationException

/**
 * Extension Point script to block transactions to users who don't use their Handelsrekening account (marked in a profile field).
 * This script belongs to an Extension Point of type 'Transaction', event 'Preview' and is only enabled for transfer type 'Handelsrekening - Handelstransactie'.
 */

AccountOwner user = binding.toOwner

if ( ! user instanceof User ) {
    // The toOwner should be a member. If it is not, something is wrong, so stop.
    return
}

// Check the profile field of the user that would receive the payment indicating active usage of their account.
def usr = scriptHelper.wrap(user)
if ( "not_active" == usr.circ_payments?.internalName ) {
    // This user does not want to receive circulair euro's, so stop the transaction and inform the payer.
    throw new ValidationException(new Utils(binding).dynamicMessage("circ_payment_blocked"))
}
