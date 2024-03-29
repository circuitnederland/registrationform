import org.cyclos.entities.users.User

Utils utils = new Utils(binding)
Map<String, Object> formParameters = binding.formParameters
User user = formParameters.user

// Toggle the value of the emandates_lock profile field between 'blocked' and empty.
def usrDTO = userService.load(user.id)
def usr = scriptHelper.wrap(usrDTO)
def lock = usr.emandates_lock?.internalName
def result = ''
switch(lock) {
    case 'blocked':
        usr.emandates_lock = ''
        result = utils.dynamicMessage("emResultUnblocked")
        break
    case null:
        usr.emandates_lock = 'blocked'
        result = utils.dynamicMessage("emResultBlocked")
        break
    default:
        // Do nothing and bail out. We should never get here, the user may only toggle between 'blocked' and empty.
        return "${utils.dynamicMessage("emResultError")} ${usr.emandates_lock}"
}
userService.save(usrDTO)

return [
    notification: result,
    reRun: true
]
