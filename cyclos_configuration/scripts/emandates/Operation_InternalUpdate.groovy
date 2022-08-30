import org.cyclos.entities.system.ExternalRedirectExecution
import org.cyclos.entities.users.User

ExternalRedirectExecution execution = binding.execution
Map<String, Object> formParameters = binding.formParameters
User user = formParameters.user
String bankId = formParameters.debtorBank.internalName

def eMandates = new EMandates(binding)
return eMandates.amendMandateRequest(user, execution, bankId)
