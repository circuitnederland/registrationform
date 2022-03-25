// Code block 'Script code executed on transitions between steps' of the Registration Wizard script
import org.cyclos.model.users.groups.InitialGroupVO

// When we go to the email step, the community and type should be known.
// Either selected by the visitor in previous steps, or prefilled by the Web service with information from the request URL.
// Glue those two pieces together to form the internal name of the Group and set this as the Group we register in.

if ('email' == step.internalName) {
    def dto = userService.getPublicRegistrationData(
        new InitialGroupVO(internalName: "${customValues.community.internalName}_${customValues.type.internalName}"), null).dto;
    storage.registration = dto
    
    return null
}
