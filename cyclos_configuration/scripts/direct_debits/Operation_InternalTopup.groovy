/**
 * Custom operation script to let users topup their balance leading to a direct debit user record (incasso).
 */

Map<String, String> scriptParameters = binding.scriptParameters
BigDecimal amount = new BigDecimal(formParameters.amount)
User user = formParameters.user

new DirectDebits(binding).transferTopupUnits(user, amount)

return [
    notification: scriptParameters["topup.success"],
    backTo: "buyCredits",
    reRun: true
]
