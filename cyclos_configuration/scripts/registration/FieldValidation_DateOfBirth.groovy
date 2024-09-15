/* 
 * Validates the date of birth registrationwizard custom field, to check users are old enough to join the circuit.
 * The minimum age (18 years for example) is configurable via a script parameter, for example:
 *    minimumAge = 18
 * The error message is configurable in the 'Text messages' system record as the regTooYoung field.
 */

// Check whether the date of birth is clearly wrong, i.e. later than today or more than 150 years ago.
def earliestPossibleDoB = Calendar.getInstance()
earliestPossibleDoB.add(Calendar.YEAR, -150)
if (value.after(new Date()) || value.before(earliestPossibleDoB.getTime())) {
    return false
}

// Check whether the user is old enough.
def minimumAge = scriptParameters.minimumAge as int
def latestPossibleDoB = Calendar.getInstance()
latestPossibleDoB.add(Calendar.YEAR, -minimumAge)
if (value.after(latestPossibleDoB.getTime())) {
    return new Utils(binding).dynamicMessage("regTooYoung", [minimumAge: minimumAge])
}

return true
