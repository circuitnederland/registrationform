/**
 * Generates a custom validation URL. Used in the e-mail Cyclos sends new users to ask them to validate their account.
 */
import org.cyclos.entities.users.SystemRecord
import org.cyclos.entities.users.SystemRecordType
import org.cyclos.impl.utils.LinkType
import org.cyclos.model.users.records.RecordDataParams
import org.cyclos.model.users.recordtypes.RecordTypeVO

// Temporary fix for operations of resulttype external_redirect in the mobile app.
// Code can be removed after migrating to Cyclos 4.12. Credits to Rodrigo Leon.
if(type == LinkType.EXTERNAL_REDIRECT && sessionData.channelName == "mobile") {
	def operationId = scriptHelper.maskId(externalRedirectExecution.id)
	def token = externalRedirectExecution.verificationToken
    return linkGeneratorHandler.mobileRedirect("/externalRedirect?id=${operationId}&token=${token}", sessionData.loggedUser)
}

// Only generate a custom link if this is the validation link.
if (type != LinkType.REGISTRATION_VALIDATION) {
	return null
}
// Don't generate a custom validation URL for operators.
if (user.operator) {
    return null
}
// We are supposed to have a validationKey in this case.
if (validationKey?.empty) {
    return null
}

// Build up the custom URL and return this.
String recordTypeName = 'mollyConnect'
String fieldName = 'registrationRootUrl'
SystemRecordType recordType = binding.entityManagerHandler.find(SystemRecordType, recordTypeName)
SystemRecord record = binding.recordService.newEntity(new RecordDataParams(recordType: new RecordTypeVO(id: recordType.id)))
if (!record.persistent) throw new IllegalStateException("No instance of system record ${recordType.name} was found")
wrapped = binding.scriptHelper.wrap(record, recordType.fields)
if (!wrapped.containsKey(fieldName)) throw new Exception("FieldName ${fieldName} is missing in ${recordTypeName} system record.")

return wrapped[fieldName] + scriptParameters.validationURL + "?validationKey=${validationKey}"
