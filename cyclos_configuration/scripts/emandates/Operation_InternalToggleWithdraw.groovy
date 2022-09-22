/**
 * Toggle the isWithdrawn field in the current emandate record of the user.
 *
 */

import org.cyclos.entities.users.SystemRecord

Map<String, String> scriptParameters = binding.scriptParameters
Map<String, Object> formParameters = binding.formParameters
User user = formParameters.user

def emandates = new EMandates(binding)
SystemRecord record = emandates.current(user)
def fields = scriptHelper.wrap(record)

def result = ''
if (fields.isWithdrawn) {
    // The emandate is withdrawn. Let the user re-activate it.
    fields.isWithdrawn = false
    result = scriptParameters["locking.reset"]
} else {
    // The emandate is active. Let the user withdraw it.
    fields.isWithdrawn = true
    result = scriptParameters["locking.withdrawn"]
}

return [
    notification: result,
    reRun: true
]
