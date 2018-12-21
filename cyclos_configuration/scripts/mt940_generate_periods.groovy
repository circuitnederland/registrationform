import org.cyclos.entities.utils.DatePeriod
import org.cyclos.impl.utils.formatting.PredefinedPeriodDataValueFormatter
import org.cyclos.model.system.fields.DynamicFieldValueVO
import org.cyclos.model.utils.PeriodType
import org.cyclos.model.utils.PredefinedPeriodData
import org.cyclos.server.utils.DateHelper
import org.cyclos.utils.Pair
 
def timeZone = sessionData.configuration.timeZone
def numOfQuarters = Integer.parseInt(scriptParameters.quarters)
def numOfMonths = Integer.parseInt(scriptParameters.months)

List<PredefinedPeriodData> rawOptions = []
def lastQuarter = DateHelper.getLastCompletedPeriod(PeriodType.QUARTER, timeZone)
rawOptions.addAll(DateHelper.createPeriodRange(lastQuarter, -numOfQuarters))
def lastMonth = DateHelper.getLastCompletedPeriod(PeriodType.MONTH, timeZone)
rawOptions.addAll(DateHelper.createPeriodRange(lastMonth, -numOfMonths))

List<DynamicFieldValueVO> options = new ArrayList<>();
boolean first = true
for (PredefinedPeriodData rawOption : rawOptions) {
    Pair<Date, Date> pair = DateHelper.createPeriod(rawOption, timeZone);
    DatePeriod period = new DatePeriod(pair);
    def value = "${conversionHandler.toDateTime(pair.first)},${conversionHandler.toDateTime(pair.second)}"
    def label = PredefinedPeriodDataValueFormatter.instance().format(null, rawOption, formatter)
    DynamicFieldValueVO option = new DynamicFieldValueVO(value, label)
    option.defaultValue = first
    options.add(option)
    first = false
}
return options
