/**
 * Generates a custom validation URL. Used in the e-mail Cyclos sends new users to ask them to validate their account.
 */
import org.cyclos.entities.users.SystemRecord
import org.cyclos.entities.users.SystemRecordType
import org.cyclos.impl.utils.LinkType
import org.cyclos.model.users.records.RecordDataParams
import org.cyclos.model.users.recordtypes.RecordTypeVO

// Only generate a custom link if this is the validation link.
if(type != LinkType.REGISTRATION_VALIDATION) {
	return null
}
// We are supposed to have a validationKey in this case.
if(validationKey?.empty) {
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
