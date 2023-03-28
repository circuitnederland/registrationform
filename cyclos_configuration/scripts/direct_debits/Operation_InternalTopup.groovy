/**
 * Custom operation script to let users topup their balance leading to a direct debit user record (incasso).
 */

Utils utils = new Utils(binding)
BigDecimal amount = new BigDecimal(formParameters.amount)
User user = formParameters.user

new DirectDebits(binding).transferTopupUnits(user, amount)

return [
    notification: utils.dynamicMessage("topupResultSuccess"),
    backTo: "buyCredits",
    reRun: true
]
