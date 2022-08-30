import org.cyclos.entities.system.ExternalRedirectExecution
import org.cyclos.entities.users.User
import org.cyclos.impl.system.EntityBackedParameterStorage

ExternalRedirectExecution execution = binding.execution
Map<String, Object> formParameters = binding.formParameters
def objectMapper = binding.objectMapper
User user = formParameters.user
String bankId = formParameters.debtorBank.internalName

def eMandates = new EMandates(binding)
def storage = new EntityBackedParameterStorage(objectMapper, execution)
return eMandates.newMandateRequest(user, "operation${execution.id}", 
	user.username, storage, bankId)
