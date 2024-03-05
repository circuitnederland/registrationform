import org.cyclos.entities.system.CustomFieldPossibleValue
import org.cyclos.entities.system.ExternalRedirectExecution
import org.cyclos.entities.utils.EntityBackedParameterStorage
import org.cyclos.model.utils.RequestInfo

import com.fasterxml.jackson.databind.ObjectMapper

ExternalRedirectExecution execution = binding.execution
ObjectMapper objectMapper = binding.objectMapper
RequestInfo request = binding.request
def transactionId = request.getParameter('transactionId')
def eMandates = new EMandates(binding)
def storage = new EntityBackedParameterStorage(objectMapper, execution)

def fields = eMandates.callback(storage, transactionId)

// Inform the user about the result and return to the buy credits main operation screen (which only works in the app, on main the user returns to home).
String status = (fields.status as CustomFieldPossibleValue).internalName
return [
    notification: new Utils(binding).dynamicMessage("emResult${status.capitalize()}"),
    backTo: "buyCredits",
    reRun: true
]
