import org.cyclos.entities.system.CustomWizardExecution
import org.cyclos.model.utils.RequestInfo

CustomWizardExecution execution = binding.execution
RequestInfo request = binding.request

def storage = execution.storage
def registration = storage.registration
def transactionId = request.getParameter('transactionId')

def eMandates = new EMandates(binding)
def fields = eMandates.callback(storage, transactionId)

// If the emandate is successfully issued, store the IBAN in the registration object (at wizard finish we store this in the user profile).
String status = (fields.status as CustomFieldPossibleValue).internalName
if ('success' == status) {
    def usr = scriptHelper.wrap(registration)
    usr.iban = fields.iban
    storage.registration = registration
}

return null
