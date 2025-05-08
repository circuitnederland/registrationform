Map<String, String> scriptParameters = binding.scriptParameters
String currency = scriptParameters.currencyCode
String iban = scriptParameters.iban

Camt053 exporter = new Camt053(binding)
exporter.setCurrencyCode(currency)
exporter.setIban(iban)

return exporter.generateContents()
