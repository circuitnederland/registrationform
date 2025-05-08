Map<String, String> scriptParameters = binding.scriptParameters
String currency = scriptParameters.currencyCode
String iban = scriptParameters.iban

Mt940 exporter = new Mt940(binding)
exporter.setCurrencyCode(currency)
exporter.setIban(iban)

return exporter.generateContents()
