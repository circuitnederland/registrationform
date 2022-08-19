import org.cyclos.entities.system.CustomFieldPossibleValue
import org.cyclos.entities.system.ExternalRedirectExecution
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

try{
    def fields = eMandates.callback(storage, transactionId)
    String status = (fields.status as CustomFieldPossibleValue).internalName
    return scriptParameters["result.${status}"]
}catch (ValidationException vE) {
    return vE.getMessage()
}
