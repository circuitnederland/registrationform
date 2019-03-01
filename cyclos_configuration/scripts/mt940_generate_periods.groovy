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

List<DynamicFieldValueVO> options = new ArrayList<>()
for (PredefinedPeriodData rawOption : rawOptions) {
    Pair<Date, Date> pair = DateHelper.createPeriod(rawOption, timeZone);
    def value = "${conversionHandler.toDateTime(pair.first)},${conversionHandler.toDateTime(pair.second)}"
    def label = PredefinedPeriodDataValueFormatter.instance().format(null, rawOption, formatter)
    options.add(new DynamicFieldValueVO(value, label))
}
options.add(new DynamicFieldValueVO("custom", "Aangepast"))
options.get(0).defaultValue = true
return options
