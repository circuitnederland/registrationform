// Code block 'Script code executed on transitions between steps' of the Registration Wizard script
import org.cyclos.model.users.groups.InitialGroupVO

// When we go to the email step, the community and type should be known.
// Either selected by the visitor in previous steps, or prefilled by the Web service with information from the request URL.
// Glue those two pieces together to form the internal name of the Group and set this as the Group we register in.

if ('email' == step.internalName) {
    def dto = userService.getPublicRegistrationData(
        new InitialGroupVO(internalName: "${customValues.community.internalName}_${customValues.type.internalName}"), null, null).dto;
    storage.registration = dto
    
    return null
}

// We ask companies for their company name and consumers for their full name.
// Because we want different labels for companies and consumers, we use two wizard custom fields.
// After the accountinfo step, we store this info in the Cyclos Name field of the user that will be created by the registration wizard.
if ('accountinfo_companies' == previousStep.internalName) {
    registration.name = customValues.company_name
    storage.registration = registration
    return null
}
if ('accountinfo_consumers' == previousStep.internalName) {
    registration.name = customValues.consumer_name
    storage.registration = registration
    return null
}

// Fill the authorized_signatory profile field.
// We need to do this via a wizard field, because we want it to be required during registration.
// Making the profile field itself required is problematic for existing users, because this would mean they can not change their profile anymore.
if ('eMandate_companies' == previousStep.internalName) {
    def usr = scriptHelper.wrap(registration)
    usr.authorized_signatory = customValues.authorized_signatory
    storage.registration = registration
    return null
}

// Fill the date of birth profile field.
// We need to do this via a wizard field, because we want it to be required during registration.
// Making the profile field itself required is problematic for existing users, because this would mean they can not change their profile anymore.
if ('profilefields_companies' == previousStep.internalName || 'contactfields_consumers' == previousStep.internalName) {
    def usr = scriptHelper.wrap(registration)
    usr.geboortedatum = customValues.date_of_birth
    storage.registration = registration
    return null
}
