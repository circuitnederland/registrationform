import org.cyclos.entities.system.CustomOperation
import org.cyclos.entities.users.RecordCustomFieldPossibleValue
import org.cyclos.entities.users.User
import org.cyclos.impl.system.ScriptHelper

ScriptHelper scriptHelper = binding.scriptHelper
Utils utils = new Utils(binding)

// Get the current user and eMandates status
User user = formParameters.user
def emandates = new EMandates(binding)
def record = emandates.current(user)

String html = ''
def status = 'none'
def usr = scriptHelper.wrap(user)
def locked = usr.emandates_lock?.internalName ?: ''
def lockedMessage = locked ? utils.dynamicMessage("emStatusBlocked") : ''
html += locked ? "<div>${lockedMessage}</div><br>" : ''
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
            html += "<div>${utils.dynamicMessage('emResultSuccess')}</div><br>"
            html += "<div><strong>${utils.dynamicMessage('emResultSuccessRetry')}</strong></div><br>"
        }
    }
	def statusMessage = utils.dynamicMessage("emStatus${status.capitalize()}")
	def withdrawn = fields.isWithdrawn
	def cssClass = ( locked || withdrawn ) ? ' class="disabled"' : ''
	def withdrawnMessage = withdrawn ? utils.dynamicMessage("emStatusWithdrawn") : ''
	CustomOperation lockingOperation = entityManagerHandler.find(CustomOperation, 'eMandateWithdrawingByUser')
	lockingOperation?.label = withdrawn ? utils.dynamicMessage("emButtonReset") : utils.dynamicMessage("emButtonWithdraw")

	html += withdrawn && !locked ? "<div>${withdrawnMessage}</div><br>" : ''
	html += "<div${cssClass}><div>${statusMessage}</div>"
    html += "<div>${utils.dynamicMessage('emDetails')}</div>"
	html += emandates.emandateHtml(record, user)
	html += "</div>"
} else {
    def msg = utils.dynamicMessage('emStatusNone', ['iban': usr.iban])
	html += !locked ? "<div>${msg}</div>" : ''
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
