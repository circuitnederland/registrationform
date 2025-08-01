// This Web service script starts a new registration wizard execution.
// The Url mappings of this Web service contain (optional) community and type request params:
// {CYCLOS_URL}/run/inschrijven/{community}/{type}
// The script retrieves the community and type from the request URL if present.
// Steps in the registration wizard are setup as: intro/community/type/email/...etc
// If community is known, the wizard is set to go from step intro to step type, skipping the community step.
// If both community and type are known, the wizard is set to go from step intro to step email, skipping both community and type steps.
import org.apache.http.HttpStatus
import org.cyclos.entities.system.CustomWizard
import org.cyclos.entities.system.CustomWizardExecution
import org.cyclos.impl.access.SessionData
import org.cyclos.impl.system.CustomWizardExecutionStorage
import org.cyclos.impl.system.CustomWizardServiceLocal
import org.cyclos.impl.system.ScriptHelper
import org.cyclos.model.system.wizards.CustomWizardExecutionData
import org.cyclos.model.system.wizards.CustomWizardVO
import org.cyclos.model.system.wizards.StartCustomWizardParams
import org.cyclos.utils.MapParameterStorage
import org.cyclos.model.utils.ResponseInfo

CustomWizardServiceLocal customWizardService = binding.customWizardService
MapParameterStorage pathVariables = binding.pathVariables
ScriptHelper scriptHelper = binding.scriptHelper
SessionData sessionData = binding.sessionData

// Start a wizard execution
CustomWizardExecutionData run = customWizardService.start(new StartCustomWizardParams(
        wizard: new CustomWizardVO(internalName: 'registration')))
def key = run.key
CustomWizardExecution execution = customWizardService.findExecution(key) as CustomWizardExecution
CustomWizardExecutionStorage storage = execution.storage as CustomWizardExecutionStorage
CustomWizard wizard = execution.wizard as CustomWizard

// Find the chosen values of the custom wizard fields based on the community and type in the request URL (they may or may not exist).
def communityField = wizard.customFields.find { it.internalName == 'community'}
def communityValue = communityField.possibleValues.find { it.internalName == pathVariables.community }
def typeField = wizard.customFields.find { it.internalName == 'type'}
def typeValue = typeField.possibleValues.find { it.internalName == pathVariables.type }

// Prefill the wizard custom fields with the chosen values from the URL. They may be null, which just leads to no prefilled value.
def customValues = storage.customValues ?: [:]
customValues.community = communityValue
customValues.type = typeValue
storage.customValues = customValues

// If the community or type is known, skip these steps. The first step will still be the intro step.
if (communityValue) {
    def steps = storage.steps
    steps[0].transitions[0].id = typeValue ? 'email' : 'type'
    storage.steps = steps
}

// Send a redirect to the wizard execution
def response = new ResponseInfo(HttpStatus.SC_MOVED_TEMPORARILY)
response.setHeader('Location', "${sessionData.requestData.baseUrl}#system.wizards.run!key=${key}")
return response
