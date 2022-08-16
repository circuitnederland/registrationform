import org.cyclos.entities.system.ExternalRedirectExecution
import org.cyclos.impl.system.EntityBackedParameterStorage
import org.cyclos.model.utils.RequestInfo

import com.fasterxml.jackson.databind.ObjectMapper

ExternalRedirectExecution execution = binding.execution
ObjectMapper objectMapper = binding.objectMapper
RequestInfo request = binding.request
def transactionId = request.getParameter('transactionId')

def eMandates = new EMandates(binding)
def storage = new EntityBackedParameterStorage(objectMapper, execution)
return eMandates.callback(storage, transactionId)
