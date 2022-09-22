/**
 * Bulk script to find users whose profile contains validation errors.
 */

def usrDTO = userService.load(user.id)
userService.validate(usrDTO) // This returns void, but will throw a ValidationException if the profile contains errors.

return true
