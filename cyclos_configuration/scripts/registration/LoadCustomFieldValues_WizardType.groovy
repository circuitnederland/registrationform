/*
 * Correct the possible values in the 'type' custom wizard field to form a valid combination with the chosen 'community' custom wizard field.
 * By default, each community has a bedrijven (companies) and particulieren (consumers) group.
 * For communities that should not use one of those groups, use a script parameter with the internal name of the community custom wizard field
 * indicating the type that is valid for this community.
 * For example:
 *   allunited = particulieren
 *   unitedeconomy = bedrijven
 *
 * These script parameters can either be in the script settings or in the 'Load values script parameters' setting of the custom wizard field settings.
*/

def community = execution.storage.customValues.community.internalName
if (scriptParameters["${community}"]) {
    return scriptParameters["${community}"].split(/\s*,\s*/)
}

return field.possibleValues
