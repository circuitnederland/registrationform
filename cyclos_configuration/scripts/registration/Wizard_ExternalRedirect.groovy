import org.cyclos.entities.system.CustomWizardExecution

CustomWizardExecution execution = binding.execution
Map<String, Object> customValues = binding.customValues
String bankId = customValues.debtorBank.internalName

def storage = execution.storage
def registration = storage.registration
def username = registration.username

def eMandates = new EMandates(binding)
return eMandates.newMandateRequest(null, "wizard${execution.id}", 
	username, storage, bankId)
