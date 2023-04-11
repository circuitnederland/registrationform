import org.cyclos.entities.system.CustomOperation
import org.cyclos.entities.users.User
import org.cyclos.impl.system.ScriptHelper

// Read the binding variables
ScriptHelper scriptHelper = binding.scriptHelper
Utils utils = new Utils(binding)
User user = binding.user

// Get the current user and eMandates status
def emandates = new EMandates(binding)
def record = emandates.current(user)

String html = ''
def status = ''
def usr = scriptHelper.wrap(user)
def locked = usr.emandates_lock?.internalName ?: ''
def lockedMessage = locked ? utils.dynamicMessage("emManagerStatusBlocked") : ''
html += locked ? "<div>${lockedMessage}</div><br>" : ''
CustomOperation lockingOperation = entityManagerHandler.find(CustomOperation, 'eMandateBlockByAdmin')
lockingOperation?.label = locked ? utils.dynamicMessage("emButtonDeblock") : utils.dynamicMessage("emButtonBlock")
if (record) {
	def fields = record ? scriptHelper.wrap(record) : null
	def withdrawn = fields.isWithdrawn
	def cssClass = ( locked || withdrawn ) ? ' class="disabled"' : ''
	def withdrawnMessage = withdrawn ? utils.dynamicMessage("emManagerStatusWithdrawn") : ''

	html += withdrawn ? "<div>${withdrawnMessage}</div><br>" : ''
	html += "<div${cssClass}>"
	html += emandates.emandateHtml(record, user)
	html += "</div>"
} else {
	html += "<div>${utils.dynamicMessage('emManagerStatusNone')}</div>"
}

return [
    content: html,
    actions: [
        eMandateBlockByAdmin: [
            parameters: [
                user: user.id
            ]
        ]
    ]
]
