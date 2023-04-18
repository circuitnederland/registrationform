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

def fields = eMandates.callback(storage, transactionId)

// Update the IBAN in the user profile.
eMandates.updateUserIBAN(fields)

// Inform the user about the result.
String status = (fields.status as CustomFieldPossibleValue).internalName
return new Utils(binding).dynamicMessage("emResult${status.capitalize()}")
