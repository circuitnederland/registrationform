/**
 * Toggle the isWithdrawn field in the current emandate record of the user.
 *
 */

import org.cyclos.entities.users.SystemRecord

Utils utils = new Utils(binding)
Map<String, Object> formParameters = binding.formParameters
User user = formParameters.user

def emandates = new EMandates(binding)
SystemRecord record = emandates.current(user)
def fields = scriptHelper.wrap(record)

def result = ''
if (fields.isWithdrawn) {
    // The emandate is withdrawn. Let the user re-activate it.
    fields.isWithdrawn = false
    result = utils.dynamicMessage("emResultReset")
} else {
    // The emandate is active. Let the user withdraw it.
    fields.isWithdrawn = true
    result = utils.dynamicMessage("emResultWithdrawn")
}

return [
    notification: result,
    backTo: "buyCredits",
    reRun: true
]
