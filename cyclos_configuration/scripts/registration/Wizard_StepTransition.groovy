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

// Before we go to the emandate step, check if the user choose manual payment instead of emandate. If so, skip to the manual payment step.
// The internal names of the possible values of the payment method custom wizard field are the same as those of the corresponding steps,
// so we can just return the chosen payment method field internal name to return the correct step.
if ('emandate' == step.internalName) {
    return customValues.payment_method.internalName
}

// If the user choose emandate as their payment method, skip the manual payment step after showing the emandate feedback step.
// Note: consumers will automatically see the first step for consumers after the profilefields_companies step we return here instead.
if (previousStep.internalName == 'emandate_feedback') {
    return 'profilefields_companies'
}
