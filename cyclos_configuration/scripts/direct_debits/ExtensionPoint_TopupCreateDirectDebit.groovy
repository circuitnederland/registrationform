import org.cyclos.entities.banking.Transaction
import org.cyclos.entities.users.User
import org.cyclos.model.banking.accounts.AccountOwner
import org.cyclos.model.users.records.RecordDataParams
import org.cyclos.model.users.records.UserRecordDTO
import org.cyclos.model.users.recordtypes.RecordTypeVO
import org.cyclos.model.users.users.UserLocatorVO

/**
 * Transaction extension point script to create a new directDebit user record whenever a 
 * topup transaction is made from debiet to a user.
 */

AccountOwner user = binding.toOwner
Transaction transaction = binding.transaction

if ( ! user instanceof User ) {
    // The toOwner should be a member. If it is not, something is wrong, so stop.
    return
}

RecordDataParams params = new RecordDataParams(
    user: new UserLocatorVO(id: user.id),
    recordType: new RecordTypeVO(internalName: 'directDebit')
)
UserRecordDTO record = recordService.getDataForNew(params).dto
def fields = scriptHelper.wrap(record)
fields.transaction = transaction
recordService.save(record)
