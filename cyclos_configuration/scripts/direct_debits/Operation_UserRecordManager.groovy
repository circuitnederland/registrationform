import org.cyclos.entities.users.UserRecord

/**
  * Custom operation script with Scope 'Record'. Enables financial admins to manage directDebit user records,
  * for example, to mark a record as failed if the bank informs us that an incasso has failed.
  * Financial admins can only change specific fields in certain situations. Each situation has its own custom operation,
  * but they all share this same script. The 'action' scriptparameter indicates which situation is applicable.
  */

Map<String, Object> formParameters = binding.formParameters
Map<String, String> scriptParameters = binding.scriptParameters
UserRecord record = binding.record
String action = scriptParameters.action

new DirectDebits(binding).updateDirectDebit(record, action, formParameters)

return [
    notification: "De incasso is succesvol gewijzigd.",
    backToRoot: true,
    reRun: true
]
