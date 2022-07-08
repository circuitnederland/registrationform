/*
 * Load custom field values script for the Contribution (Lidmaatschapsbijdrage) profile field.
 * This script filters the possible values to those belonging to the group of the current user.
 * This way, each community can use the contribution scales (staffels) they prefer.
 * When a community does not want to use custom scales, their users get the default (standaard) scales.
*/

def groupName = user.group.internalName

// Filter the List of possible values.

// First try to filter the values for the group of this user.
def customList = field.possibleValues.stream()
    .filter(x -> x.internalName.startsWith(groupName))
    .findAll()

// If filtering on the group gives a list values, return that.
if (customList) {
    return customList
}

// If filtering on the group gives no values, return the values for the default user type (companies or consumers).
def userType = groupName.substring(groupName.indexOf("_"))
return field.possibleValues.stream()
    .filter(x -> x.internalName.startsWith("standaard${userType}"))
    .findAll()
