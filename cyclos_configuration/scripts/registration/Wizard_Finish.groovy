// Code block 'Script code executed when the wizard finishes' of the Registration Wizard script
import org.cyclos.entities.system.CustomWizardExecution
import org.cyclos.entities.users.SystemRecord
import org.cyclos.entities.users.User
import org.cyclos.impl.system.ScriptHelper
import org.cyclos.model.users.users.UserLocatorVO

CustomWizardExecution execution = binding.execution
ScriptHelper scriptHelper = binding.scriptHelper
User user = binding.user
Map<String, Object> customValues = binding.customValues

// Fill the user's image field with the wizard custom field containing the image that is specific for the chosen type (consumer or company).
def tempImageList = ('bedrijven' == customValues.type.internalName) ? customValues.company_image : customValues.consumer_image
if (tempImageList) {
    def tempImage = tempImageList[0]
	def content = storedFileHandler.getContent(tempImage)
	def vo = new UserLocatorVO(id: user.id)
	userImageService.save(vo, tempImage.name, content, tempImage.contentType)
}

// Once the registration is finished we now have a user object. Set this as the record owner in the emandate system record.
def record = execution.storage.getObject('record') as SystemRecord
// Check if there actually is an emandate record, which may not be the case if the user cancelled/skipped this.
if (record) {
	def fields = scriptHelper.wrap(record)
	fields.owner = user
}
