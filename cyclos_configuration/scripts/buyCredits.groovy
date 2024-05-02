import org.cyclos.entities.users.User
import org.cyclos.entities.system.CustomOperation
import org.cyclos.model.utils.TimeField
import org.cyclos.server.utils.DateHelper

import java.text.SimpleDateFormat

/**
 * Custom operation script giving users access to functionality to add money to their balance.
 * They can always buy units by simply transferring money to our bank, which is explained in the 'buyViaBank' Custom operation.
 * When they have a valid emandate, they can also choose to topup their balance, after which we do a direct debit from their bank account.
 * This is handled by the 'topupViaDirectDebit' Custom operation.
 * They can also access the emandate functionality from here to create/update/cancel their emandate.
 * The explanation text will be different for each situation, i.e. no emandate yet, a valid or invalid emandate, etc.
 */

/**
 * Helper class to determine the dynamic parts (texts/labels/buttons) used in the buy credits operation screen.
 * Kept the class inside this operation script instead of a library, because it needs dependencies to both the EMandates and DirectDebit library.
 */
class BuyCredits {
    Binding binding
    User user
    ScriptHelper scriptHelper
    Utils utils
    Map<String,Object> vars
    String topupCode
    String emandateBtnLabel
    String topupMsg
    String bankMsg
    
    BuyCredits(Binding binding, User user, Utils utils) {
        this.binding = binding
        this.user = user
        this.utils = utils
        init()
    }

    void init() {
        scriptHelper = binding.variables.scriptHelper as ScriptHelper
        vars = [:]
        topupCode = determineTopupSituation()
        String eMandateSituation = ('none' == topupCode) ? 'issueEMandate' : 'manageEMandate'
        emandateBtnLabel = utils.dynamicMessage("bcButton${eMandateSituation.capitalize()}")
        topupMsg = utils.dynamicMessage("topupStatus${topupCode.capitalize()}", vars)
        bankMsg = utils.dynamicMessage('bcBuyViaBank')
    }
    
    /**
     * Returns a string indicating whether the given user is allowed to do a new topup.
     * It checks whether the user has a valid eMandate and is not blocked by the financial admin.
     * And it checks the restrictions regarding previous topups the user may have done.
     */
    String determineTopupSituation() {
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

        // Check if the iban of the user is the same as the iban of the emandate.
        if (!utils.isIbansEqual(fields.iban as String, usr.iban as String)) {
            return 'wrongIBAN'
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
        // Note: creationDate may be null if the direct debit was filtered out above with a settled_cancelled status.
        if (creationDate) {
            Date oneWeekAfter = DateHelper.shiftToBegin(DateHelper.add(creationDate, TimeField.DAYS, 7), null)
            if (new Date().before(oneWeekAfter)) {
                vars = ['weekafter': new SimpleDateFormat("d MMMM yyyy", new Locale('nl')).format(oneWeekAfter)]
                return 'weeklimit'
            }
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
}

String html = ''
Long userId = null
String topupCode = ''
Utils utils = new Utils(binding)
Boolean isEMTurnedOff = utils.techDetailBoolean('ddTurnedOff')
if ( isEMTurnedOff ) {
    // If emandate functionality is turned off temporarily, show the message explaining this is turned off.
    html = "<div>${utils.dynamicMessage('bcEMandatesTurnedOff')}</div><br>"
} else {
    User user = binding.user
    BuyCredits buyCredits = new BuyCredits(binding, user, utils)
    CustomOperation eMandateOperation = entityManagerHandler.find(CustomOperation, 'eMandate')
    eMandateOperation?.label = buyCredits.emandateBtnLabel
    userId = user.id
    topupCode = buyCredits.topupCode

    html = "<div>${buyCredits.topupMsg}</div><br>"
    html += "<div>${buyCredits.bankMsg}</div><br>"
}

return [
    content: html,
    actions: [
        topupViaDirectDebit: [
            parameters: [
                user: userId
            ],
            enabled: ! isEMTurnedOff && topupCode == 'success'
        ],
        buyViaBank: [
            enabled: true
        ],
        eMandate: [
            parameters: [
                user: userId
            ],
            enabled: ! isEMTurnedOff
        ]
    ]
]
