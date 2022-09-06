import org.cyclos.entities.system.CustomOperation
import org.cyclos.entities.users.QRecordCustomFieldPossibleValue
import org.cyclos.entities.users.RecordCustomFieldPossibleValue
import org.cyclos.entities.users.User
import org.cyclos.impl.access.ConfigurationHandler
import org.cyclos.impl.contentmanagement.DataTranslationHandler
import org.cyclos.impl.system.ScriptHelper
import org.cyclos.impl.utils.formatting.FormatterImpl

// Read the binding variables
ScriptHelper scriptHelper = binding.scriptHelper
ConfigurationHandler configurationHandler = binding.configurationHandler
DataTranslationHandler dataTranslationHandler = binding.dataTranslationHandler
Map<String, String> scriptParameters = binding.scriptParameters
User user = binding.user
FormatterImpl formatter = binding.formatter

// Get the current user and eMandates status
def config = configurationHandler.getAccessor(user)
def emandates = new EMandates(binding)
def record = emandates.current(user)

def fields = record ? scriptHelper.wrap(record) : null
RecordCustomFieldPossibleValue statusValue = fields?.status
def status = statusValue?.internalName ?: 'none'
def statusMessage = scriptParameters["status.${status}"]
def usr = scriptHelper.wrap(user)
def locked = usr.emandates_lock?.internalName
def cssClass = locked ? ' class="disabled"' : ''
def lockedMessage = locked ? scriptParameters["locked.${locked}"] : ''
CustomOperation lockingOperation = entityManagerHandler.find(CustomOperation, 'eMandateLockingByUser')
lockingOperation?.label = (locked == 'withdrawn') ? scriptParameters["lockingbutton.reset"] : scriptParameters["lockingbutton.withdraw"]
// Build an HTML content accordingly
String html = locked ? "<div>${lockedMessage}</div><br>" : ''
html += "<div${cssClass}><div>${statusMessage}</div>"
if (fields) {
	def details = scriptParameters["details"]
	html += "<div>${details}</div>"

	def bankNameField = record.type.fields.find { it.internalName == 'bankName' }
	def bankNameLabel = dataTranslationHandler.getName(config.language, bankNameField)
	def bankName = fields.bankName
	html += "<div><strong>${bankNameLabel}:</strong> ${bankName}</div>"

	def iban = fields.iban
	if (iban) {		
		def ibanField = record.type.fields.find { it.internalName == 'iban' }
		def ibanLabel = dataTranslationHandler.getName(config.language, ibanField)
		html += "<div><strong>${ibanLabel}:</strong> ${iban}</div>"
	}

	def accountName = fields.accountName
	if (accountName) {
		def accountNameField = record.type.fields.find { it.internalName == 'accountName' }
		def accountNameLabel = dataTranslationHandler.getName(config.language, accountNameField)
		html += "<div><strong>${accountNameLabel}:</strong> ${accountName}</div>"
	}

	def signerName = fields.signerName
	if (signerName) {
		def signerNameField = record.type.fields.find { it.internalName == 'signerName' }
		def signerNameLabel = dataTranslationHandler.getName(config.language, signerNameField)
		html += "<div><strong>${signerNameLabel}:</strong> ${signerName}</div>"
	}

	def statusField = record.type.fields.find { it.internalName == 'status' }
	def statusLabel = dataTranslationHandler.getName(config.language, statusField)
	def pv = QRecordCustomFieldPossibleValue.recordCustomFieldPossibleValue
	def statusValueLabel = dataTranslationHandler.getValue(config.language, statusValue, pv.value)
	html += "<div><strong>${statusLabel}:</strong> ${statusValueLabel}</div>"
	
	def statusDate = fields.statusDate
	if (statusDate) {
		def statusDateField = record.type.fields.find { it.internalName == 'statusDate' }
		def statusDateLabel = dataTranslationHandler.getName(config.language, statusDateField)
		html += "<div><strong>${statusDateLabel}:</strong> ${formatter.format(statusDate)}</div>"
	}
}
html += "</div>"

return [
    content: html,
    actions: [
        createEMandate: [
            parameters: [
                user: user.id
            ],
            enabled: status != 'success'
        ],
        amendEMandate: [
            parameters: [
                user: user.id
            ],
            enabled: status == 'success'
        ],
        eMandateLockingByUser: [
            parameters: [
                user: user.id
            ],
            enabled: status == 'success' && locked != 'blocked'
        ]
    ]
]
