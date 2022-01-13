import org.apache.commons.lang3.StringEscapeUtils
import org.cyclos.utils.StringHelper

def rootUrl = sessionData.configuration.fullUrl

// Get the amount. Convert it to plain string unless it is empty (the amount is optional).
def amount = formParameters.amount ? formParameters.amount.toPlainString() : ''

// Get the description
def description = formParameters.description

if (StringHelper.isNotBlank(description)) {
	description = StringHelper.encodeURIComponent(formParameters.description)
	description = StringHelper.replace(description, "+", "%20")
}
    
// Get the to user his username
def to = user.username

def parameters = ""

if (StringHelper.isNotBlank(amount)) {
    parameters += "&amount=${amount}"
}

if (StringHelper.isNotBlank(description)) {
    parameters += "&description=${description}"
}

if (StringHelper.isNotBlank(scriptParameters.currency)) {
    parameters += "&currency=${scriptParameters.currency}"
}

if (StringHelper.isNotBlank(scriptParameters.paymentType)) {
    parameters += "&type=${scriptParameters.paymentType}"
}

def url = "${rootUrl}/pay/?to=${to}${parameters}"
// old code 4.12.x: def qrCode = "${rootUrl}/api/tickets/easy-invoice-qr-code/*:${to}?size=medium${parameters}"
def qrCode = "${rootUrl}/api/easy-invoices/qr-code/*:${to}?size=medium${parameters}"

// Return the result
return """
<p>${scriptParameters.message}</p>
<p>
    <br>
    <a href="${url}" target="blank">${StringEscapeUtils.escapeHtml4(url)}</a><br>&nbsp;<br>
</p>
<p style="text-align:center">
    <img src="${qrCode}">
</p>
"""
