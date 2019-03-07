def periodDefined = formParameters.period.value != "custom"
def period = formParameters.period.value.split(",")
String exportOperation = customOperation.actions[0].actionOperation.internalName

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
