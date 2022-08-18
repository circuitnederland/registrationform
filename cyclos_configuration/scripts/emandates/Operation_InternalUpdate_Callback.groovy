import org.cyclos.entities.system.CustomFieldPossibleValue
import org.cyclos.entities.system.ExternalRedirectExecution
import org.cyclos.entities.users.User
import org.cyclos.impl.system.EntityBackedParameterStorage
import org.cyclos.model.ValidationException
import org.cyclos.model.utils.RequestInfo

import com.fasterxml.jackson.databind.ObjectMapper

ExternalRedirectExecution execution = binding.execution
ObjectMapper objectMapper = binding.objectMapper
RequestInfo request = binding.request
def transactionId = request.getParameter('transactionId')

def eMandates = new EMandates(binding)
def storage = new EntityBackedParameterStorage(objectMapper, execution)
def fields = eMandates.callback(storage, transactionId)

String status = (fields.status as CustomFieldPossibleValue).internalName
if ('success' == status && fields.owner instanceof User) {
    // Store the iban from the eMandate in the user profile.
    try{
	    def usrDTO = userService.load((fields.owner as User).id)
        def usr = scriptHelper.wrap(usrDTO)
        usr.iban = fields.iban
        userService.save(usrDTO)
    } catch (ValidationException vE) {
        return scriptParameters["errorSaveIBAN"] + " '${vE.validation?.firstError}'."
    }
}

return scriptParameters["result.${status}"]
