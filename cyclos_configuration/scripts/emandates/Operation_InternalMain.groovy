import org.cyclos.entities.system.CustomOperation
import org.cyclos.entities.users.RecordCustomFieldPossibleValue
import org.cyclos.entities.users.User
import org.cyclos.impl.system.ScriptHelper

// Read the binding variables
ScriptHelper scriptHelper = binding.scriptHelper
Map<String, String> scriptParameters = binding.scriptParameters

// Get the current user and eMandates status
User user = formParameters.user
def emandates = new EMandates(binding)
def record = emandates.current(user)

String html = ''
def status = ''
def locked = null
if (record) {
	def fields = record ? scriptHelper.wrap(record) : null
	RecordCustomFieldPossibleValue statusValue = fields?.status
	status = statusValue?.internalName ?: 'none'
    // If the redirect during the emandate creation failed (indicated by an empty statusDate field), try to get the status now.
    if(status == 'open' && fields?.statusDate == null) {
        fields = emandates.updateStatus(record)
        statusValue = fields?.status
	    status = statusValue?.internalName ?: 'none'
        // If the status request lead to a new success status, let the user know.
        if (status == 'success') {
            html += "<div>${scriptParameters['result.success']}</div><br>"
            html += "<div><strong>${scriptParameters['result.success.retry']}</strong></div><br>"
        }
    }
	def statusMessage = scriptParameters["status.${status}"]
	def usr = scriptHelper.wrap(user)
	locked = usr.emandates_lock?.internalName
	def withdrawn = fields.isWithdrawn
	def cssClass = ( locked || withdrawn ) ? ' class="disabled"' : ''
	def lockedMessage = locked ? scriptParameters["locked.${locked}"] : ''
	def withdrawnMessage = withdrawn ? scriptParameters["locked.withdrawn"] : ''
	CustomOperation lockingOperation = entityManagerHandler.find(CustomOperation, 'eMandateWithdrawingByUser')
	lockingOperation?.label = withdrawn ? scriptParameters["lockingbutton.reset"] : scriptParameters["lockingbutton.withdraw"]

	html += locked ? "<div>${lockedMessage}</div><br>" : ''
	html += withdrawn ? "<div>${withdrawnMessage}</div><br>" : ''
	html += "<div${cssClass}><div>${statusMessage}</div>"
    html += "<div>${scriptParameters['details']}</div>"
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
            enabled: ['none', 'failure', 'expired', 'cancelled'].contains(status) && ! locked
        ],
        amendEMandate: [
            parameters: [
                user: user.id
            ],
            enabled: ['open', 'pending', 'success'].contains(status) && ! locked
        ],
        eMandateWithdrawingByUser: [
            parameters: [
                user: user.id
            ],
            enabled: status == 'success' && ! locked
        ]
    ]
]
