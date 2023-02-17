import org.cyclos.entities.users.User
import org.cyclos.entities.utils.DecimalRange
import org.cyclos.model.utils.Range

/**
 * Field Validation script to ensure the amount the user enters in the topup operation is in the allowed min/max range.
 * The allowed range depends on the user type (consumer or company) and these are set in the script parameters.
 */

def variables = binding.variables
def scriptParameters = variables.scriptParameters

User user = sessionData.loggedUser
def groupName = user.group.internalName ?: ''
def (community, userType) = groupName.tokenize('_')
def min_amount = scriptParameters["minimum_amount_${userType}"] ?: 10
def max_amount = scriptParameters["maximum_amount_${userType}"] ?: 150
DecimalRange decimalRange = new DecimalRange(new BigDecimal(min_amount), new BigDecimal(max_amount))

if(! Range.includes(decimalRange, new BigDecimal(value)) ) {
    return "Het bedrag dat u kunt opwaarderen moet tussen ${min_amount} euro en ${max_amount} euro liggen."
} else {
    return true
}
