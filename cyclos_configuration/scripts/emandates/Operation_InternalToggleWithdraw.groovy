import org.cyclos.entities.users.User

Map<String, String> scriptParameters = binding.scriptParameters
Map<String, Object> formParameters = binding.formParameters
User user = formParameters.user

// Toggle the value of the emandates_lock profile field between 'withdrawn' and empty.
def usrDTO = userService.load(user.id)
def usr = scriptHelper.wrap(usrDTO)
def lock = usr.emandates_lock?.internalName
def result = ''
switch(lock) {
    case 'withdrawn':
        usr.emandates_lock = ''
        result = scriptParameters["locking.reset"]
        break
    case null:
        usr.emandates_lock = 'withdrawn'
        result = scriptParameters["locking.withdrawn"]
        break
    default:
        // Do nothing and bail out. We should never get here, the user may only toggle between 'withdrawn' and empty, not change any other lock possible value ('blocked').
        return scriptParameters["locking.failed"]
}
userService.save(usrDTO)

return [
    notification: result,
    reRun: true
]
