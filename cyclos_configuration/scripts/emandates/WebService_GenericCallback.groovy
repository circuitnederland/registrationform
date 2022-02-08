import org.cyclos.model.utils.RequestInfo
import org.cyclos.model.utils.ResponseInfo
import org.springframework.http.HttpStatus

RequestInfo request = binding.request

def transactionId = request.getParameter('trxid')
def entranceCode = request.getParameter('ec')

def eMandates = new EMandates(binding)
return eMandates.genericCallback(transactionId, entranceCode)
