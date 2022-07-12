/* 
 * Validates the date of birth user profile field, to check users are old enough to join the circuit.
 * The minimum age (18 years for example) and the validation message are configurable via script parameters, for example:
 *    minimumLeeftijd = 18
 *    melding = De minimum leeftijd om te kunnen deelnemen is #minimumLeeftijd# jaar.
 * These script parameters can either be put in the validation script settings or as validation script parameters in the profile field settings.
*/

import org.cyclos.server.utils.MessageProcessingHelper

// Check whether the date of birth is clearly wrong, i.e. later than today or more than 150 years ago.
def earliestPossibleDoB = Calendar.getInstance()
earliestPossibleDoB.add(Calendar.YEAR, -150)
if (value.after(new Date()) || value.before(earliestPossibleDoB.getTime())) {
    return false
}

// Check whether the user is old enough.
def minimumAge = scriptParameters.minimumLeeftijd as int
def latestPossibleDoB = Calendar.getInstance()
latestPossibleDoB.add(Calendar.YEAR, -minimumAge)
if (value.after(latestPossibleDoB.getTime())) {
    return MessageProcessingHelper.processVariables(scriptParameters.teJongMelding, [minimumLeeftijd: minimumAge])
}

return true
