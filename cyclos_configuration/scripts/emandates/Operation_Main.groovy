import org.cyclos.entities.system.CustomOperation
import org.cyclos.entities.users.RecordCustomFieldPossibleValue
import org.cyclos.entities.users.User
import org.cyclos.impl.system.ScriptHelper

// Read the binding variables
ScriptHelper scriptHelper = binding.scriptHelper
Map<String, String> scriptParameters = binding.scriptParameters
User user = binding.user

// Get the current user and eMandates status
def emandates = new EMandates(binding)
def record = emandates.current(user)

String html = ''
def status = ''
if (record) {
	def fields = record ? scriptHelper.wrap(record) : null
	RecordCustomFieldPossibleValue statusValue = fields?.status
	status = statusValue?.internalName ?: 'none'
	def statusMessage = scriptParameters["status.${status}"]
	def usr = scriptHelper.wrap(user)
	def locked = usr.emandates_lock?.internalName
	def withdrawn = fields.isWithdrawn
	def cssClass = ( locked || withdrawn ) ? ' class="disabled"' : ''
	def lockedMessage = locked ? scriptParameters["locked.${locked}"] : ''
	def withdrawnMessage = withdrawn ? scriptParameters["locked.withdrawn"] : ''
	CustomOperation lockingOperation = entityManagerHandler.find(CustomOperation, 'eMandateWithdrawingByUser')
	lockingOperation?.label = withdrawn ? scriptParameters["lockingbutton.reset"] : scriptParameters["lockingbutton.withdraw"]

	html += locked ? "<div>${lockedMessage}</div><br>" : ''
	html += withdrawn ? "<div>${withdrawnMessage}</div><br>" : ''
	html += "<div${cssClass}><div>${statusMessage}</div>"
	html += emandates.emandateHtml(record, user)
	html += "</div>"
} else {
	html += "<div>${scriptParameters['status.none']}</div>"
}

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
        eMandateWithdrawingByUser: [
            parameters: [
                user: user.id
            ],
            enabled: status == 'success'
        ]
    ]
]
