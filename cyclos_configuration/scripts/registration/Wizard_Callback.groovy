import org.cyclos.entities.system.CustomWizardExecution
import org.cyclos.model.utils.RequestInfo

CustomWizardExecution execution = binding.execution
RequestInfo request = binding.request

def storage = execution.storage
def customValues = storage.customValues ?: [:]
def registration = storage.registration
def transactionId = request.getParameter('transactionId')

def eMandates = new EMandates(binding)
def fields = eMandates.callback(storage, transactionId)

// Add a debug option on test. This field only exists on test so on prd the if-statement below does nothing.
String debug_status = (customValues.emandate_status_debug as CustomFieldPossibleValue)?.internalName ?: ''
if (debug_status) {
    // Overwrite the status of the emandate with our debug value.
    fields.status = debug_status
}

// Store the status of the emandate in our custom wizard field.
String status = (fields.status as CustomFieldPossibleValue).internalName
customValues.emandate_status = status
storage.customValues = customValues

// If the emandate is successfully issued, store the IBAN in the registration object (at wizard finish we store this in the user profile).
if ('success' == status) {
    def usr = scriptHelper.wrap(registration)
    usr.iban = fields.iban
    storage.registration = registration
}

return null
