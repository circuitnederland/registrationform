import org.cyclos.entities.system.CustomOperation
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
	def usr = scriptHelper.wrap(user)
	def locked = usr.emandates_lock?.internalName ?: ''
	def withdrawn = fields.isWithdrawn
	def cssClass = ( locked || withdrawn ) ? ' class="disabled"' : ''
	def lockedMessage = locked ? scriptParameters["manager.${locked}"] : ''
	def withdrawnMessage = withdrawn ? scriptParameters["manager.withdrawn"] : ''
	CustomOperation lockingOperation = entityManagerHandler.find(CustomOperation, 'eMandateBlockByAdmin')
	lockingOperation?.label = locked ? scriptParameters["lockingbutton.deblock"] : scriptParameters["lockingbutton.block"]

	html += locked ? "<div>${lockedMessage}</div><br>" : ''
	html += withdrawn ? "<div>${withdrawnMessage}</div><br>" : ''
	html += "<div${cssClass}>"
	html += emandates.emandateHtml(record, user)
	html += "</div>"
} else {
	html += "<div>${scriptParameters['manager.none']}</div>"
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
