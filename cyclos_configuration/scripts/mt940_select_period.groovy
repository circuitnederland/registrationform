def periodDefined = formParameters.period.value != "custom"
def period = formParameters.period.value.split(",")
String exportOperation = customOperation.actions[0].actionOperation.internalName

// Pass the script parameters to the export operation via the ScriptStorage.
def storage = scriptStorageHandler.get("mt940_${sessionData.loggedUser.id}")
storage.accountType = scriptParameters.accountType
storage.iban = scriptParameters.iban

return  [
    autoRunAction: exportOperation,
    actions:[
        (exportOperation):[
            parameters:[
                begin: periodDefined ? period[0] : null,
                end: periodDefined ? period[1]  : null
            ]
        ]
    ]
]
