import org.cyclos.entities.users.SystemRecord

def variables = binding.variables
def currentPage = variables.currentPage as Integer
def pageSize = variables.pageSize as Integer

def page = new DirectDebits(binding).listPAIN_008(currentPage, pageSize) 
def rows = page.pageItems.stream().collect {
    def record = entityManagerHandler.find(SystemRecord.class, it.id)
    def fields = scriptHelper.wrap(record)
    [
        batchDate: it.creationDate,
        batchId: fields.batchId,
        nrOfTrxs: fields.nrOfTrxs,
        totalAmount: fields.totalAmount,
        recordId: it.id
    ]
}
return [
    columns: [
        [header: "Datum", property: "batchDate"],
        [header: "Batch ID", property: "batchId"],
        [header: "Aantal incasso's", property: "nrOfTrxs"],
        [header: "Totaalbedrag", property: "totalAmount"],
    ],
    rows: rows,
    totalCount: page.totalCount,
    hasNextPage: page.hasNextPage
]
