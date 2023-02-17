import org.cyclos.entities.users.User
import org.cyclos.entities.system.CustomOperation
import org.cyclos.model.utils.TimeField
import org.cyclos.server.utils.DateHelper

/**
 * Custom operation script giving users access to functionality to add money to their balance.
 * They can always buy units by simply transferring money to our bank, which is explained in the 'buyViaBank' Custom operation.
 * When they have a valid emandate, they can also choose to topup their balance, after which we do a direct debit from their bank account.
 * This is handled by the 'topupViaDirectDebit' Custom operation.
 * They can also access the emandate functionality from here to create/update/cancel their emandate.
 * The explanation text will be different for each situation, i.e. no emandate yet, a valid or invalid emandate, etc.
 */

Map<String, String> scriptParameters = binding.scriptParameters
User user = binding.user

/**
 * Returns a string indicating whether the given user is allowed to do a new topup.
 * It checks whether the user has a valid eMandate and is not blocked by the financial admin.
 * And it checks the restrictions regarding previous topups the user may have done.
 */
String topupSituation(User user) {
    // Check if a financial admin has blocked this user for using emandates.
    def usr = scriptHelper.wrap(user)
    def isBlocked = ('blocked' == usr.emandates_lock?.internalName)
    if (isBlocked) {
        return 'blocked'
    }
    
    // Check if the user has issued an emandate.
    def record = new EMandates(binding).current(user)
    if (! record) {
        return 'none'
    }

    // Check if the current emandate has status succes.
    def fields = scriptHelper.wrap(record)
    if ('success' != fields?.status?.internalName) {
        return 'inactive'
    }

    // Check if the user withdrew their emandate.
    def isWithdrawn = fields.isWithdrawn
    if (isWithdrawn) {
        return 'withdrawn'
    }

    // Get all direct debits the user has, to check some additional restrictions.
    def directDebits = new DirectDebits(binding).getDirectDebits(user)
    if (directDebits.isEmpty()) {
        // If the user has no direct debits at all, there is nothing more to check.
        return 'success'
    }

    // Check if the user already did a topup this week (currently, users are only allowed one topup per week).
    // Ignore records with status settled_cancelled.
    directDebits = directDebits.findAll{
        'settled_cancelled' != scriptHelper.wrap(it).status?.internalName
    }
    // Use the creation date of the first record, which is the most recent, since the query result is sorted by creation date by default.
    Date creationDate = directDebits[0]?.creationDate
    Date oneWeekAgo = DateHelper.subtract(new Date(), TimeField.DAYS, 7)
    if (creationDate?.after(oneWeekAgo) ) {
        return 'weeklimit'
    }

    // Check if the user has a direct debit that failed and is not settled yet.
    def unsettledDebits = directDebits.findAll{
        ['failed', 'failed_permanently', 'cancelled'].contains(scriptHelper.wrap(it).status?.internalName)
    }
    if (! unsettledDebits.isEmpty()) {
        return 'unsettled'
    }

    // Anything else is a success situation.
    return 'success'
}

String topupSituation = topupSituation(user)
String eMandateSituation = ('none' == topupSituation) ? 'issueEMandate' : 'manageEMandate'
CustomOperation eMandateOperation = entityManagerHandler.find(CustomOperation, 'eMandate')
eMandateOperation?.label = scriptParameters["buyCredits.${eMandateSituation}"]
String topupExplanation = scriptParameters["buyCredits.topup.${topupSituation}"]

String html = "<div>${scriptParameters["buyCredits.general"]}</div><br>"
html += "<div>${topupExplanation}</div><br>"
html += "<div>${scriptParameters["buyCredits.buyViabank"]}</div><br>"

return [
    content: html,
    actions: [
        topupViaDirectDebit: [
            parameters: [
                user: user.id
            ],
            enabled: topupSituation == 'success'
        ],
        buyViaBank: [
            enabled: true
        ],
        eMandate: [
            parameters: [
                user: user.id
            ],
            enabled: true
        ]
    ]
]
