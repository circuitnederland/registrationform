/*
 * Load custom field values script for the Contribution (Lidmaatschapsbijdrage) profile field.
 * This script filters the possible values to those belonging to the group of the current user.
 * This way, each community can use the contribution scales (staffels) they prefer.
 * When a community does not want to use custom scales, their users get the default (standaard) scales.
*/

// The internal names of member groups should follow the convention {community}_{type}, where type is bedrijven (companies) or particulieren (consumers).
// For example arnhemshert_bedrijven or utrechtseeuro_particulieren.
def groupName = user.group.internalName ?: ''

// Filter the List of possible values.

// First try to filter the values for the group of this user.
def customList = field.possibleValues.stream()
    .filter(x -> x.internalName.startsWith(groupName))
    .findAll()

// If filtering on the group gives a list values, return that.
if (customList) {
    return customList
}

// If filtering on the group gives no values, try filtering the values for the default user type (companies or consumers).
def (community, userType) = groupName.tokenize('_')
def stdList = field.possibleValues.stream()
    .filter(x -> x.internalName.startsWith("standaard_${userType}"))
    .findAll()

// If we found a standard list of values based on the usertype, return that.
if (stdList) {
    return stdList
}

// If the group internal name is not using the convention of {community}_{usertype}, just return the entire list of possible values.
// This can happen for brokers.
return field.possibleValues
