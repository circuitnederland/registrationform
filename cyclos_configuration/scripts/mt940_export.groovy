import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.time.temporal.TemporalField

import org.codehaus.groovy.runtime.ConversionHandler
import org.cyclos.entities.banking.Account
import org.cyclos.entities.banking.AccountType
import org.cyclos.entities.utils.DatePeriod
import org.cyclos.model.banking.accounts.AccountHistoryEntryVO
import org.cyclos.model.banking.accounts.AccountHistoryQuery
import org.cyclos.model.banking.accounts.AccountVO
import org.cyclos.model.users.users.UserVO
import org.cyclos.model.utils.DatePeriodDTO
import org.cyclos.model.utils.FileInfo
import org.cyclos.server.utils.DateHelper
import org.cyclos.server.utils.SerializableInputStream
import org.cyclos.utils.DateTime
import org.cyclos.utils.StringHelper
import org.cyclos.model.ValidationException
import org.cyclos.utils.StringHelper

def timeZone = sessionData.configuration.timeZone
def dateFormat = new SimpleDateFormat("yyMMdd")
dateFormat.timeZone = timeZone
def entryDateFormat = new SimpleDateFormat("yyMMdd")
entryDateFormat.timeZone = timeZone

def formatAmount(amount) {
    return amount.abs().toPlainString().replace('.', ',')
}

def formatSignal(amount) {
    return amount.compareTo(BigDecimal.ZERO) > 0 ? 'C' : 'D'
}

def formatOwner(AccountVO account) {
    if (account.owner instanceof UserVO) {
		def user = userService.find(account.owner.id)       
        return user.username
    }
    return account.type.internalName
}

def formatDescription(description) {
	// First make sure that the description isn't longer then 60 characters
 	description = description?.replaceAll("[\n|\r]+", " ")
 	description = description?.replaceAll("\\s+", " ")
 	description = StringHelper.trim(StringHelper.truncate(description, 60))
 	// Second make sure that no special characters are used
 	description = StringHelper.asciiOnly(StringHelper.unaccent(description))
 	// Finally make sure that no colon character is used, this might mess up the mt940 file
 	return description.replaceAll('\\:', ' ')
}

// Get the form parameters
def period = formParameters.period.value.split(",")
def begin = conversionHandler.toDate(new DateTime(period[0]))
def end = conversionHandler.toDate(new DateTime(period[1]))

// Find the user account
AccountType accountType = entityManagerHandler.find(AccountType, scriptParameters.accountType)
Account account = accountService.load(user, accountType)

// Get the balance at begin / end
def balanceBegin = accountService.getBalance(account, begin)
def balanceEnd = accountService.getBalance(account, end)
def currency = scriptParameters.currencyCode
def accountNumber = scriptParameters.iban

StringBuilder out = new StringBuilder(""":20:CN${dateFormat.format(end)}
:25:${accountNumber}
:28:000
:60F:${formatSignal(balanceBegin)}${dateFormat.format(begin)}${currency}${formatAmount(balanceBegin)}
""")

// List the transfers
AccountHistoryQuery params = new AccountHistoryQuery()
params.setUnlimited()
params.account = new AccountVO(account.id)
params.period = conversionHandler.convert(DatePeriodDTO, new DatePeriod(begin, end))
List<AccountHistoryEntryVO> entries = accountService.searchAccountHistory(params).pageItems
if (entries.empty) {
    throw new ValidationException(scriptParameters['error.noTransfers'])
}
entries.forEach { AccountHistoryEntryVO entry ->
    def date = entryDateFormat.format(conversionHandler.toDate(entry.date))
    def amount = formatAmount(entry.amount)
    def signal = formatSignal(entry.amount)
    def fromTo = formatOwner(entry.relatedAccount)
    def description = formatDescription(entry.description)
    out << ":61:${date}${signal}${amount}NOV NONREF\n"
    out << ":86:${fromTo} > ${description}\n"
}

out << ":62F:${formatSignal(balanceEnd)}${dateFormat.format(end)}${currency}${formatAmount(balanceEnd)}"

def bytes = out.toString().getBytes(StandardCharsets.UTF_8)

return new FileInfo(
    name: "transactions_${dateFormat.format(begin)}_${dateFormat.format(end)}.mt940",
    contentType: "application/octet-stream",
    length: bytes.length,
    content: new SerializableInputStream(bytes)
)
