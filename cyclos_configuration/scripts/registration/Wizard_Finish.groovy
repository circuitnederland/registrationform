// Code block 'Script code executed when the wizard finishes' of the Registration Wizard script
import org.cyclos.entities.users.User
import org.cyclos.model.users.users.UserLocatorVO

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
