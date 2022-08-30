import org.cyclos.model.utils.RequestInfo

RequestInfo request = binding.request

def transactionId = request.getParameter('trxid')
def entranceCode = request.getParameter('ec')

def eMandates = new EMandates(binding)
return eMandates.genericCallback(transactionId, entranceCode)
