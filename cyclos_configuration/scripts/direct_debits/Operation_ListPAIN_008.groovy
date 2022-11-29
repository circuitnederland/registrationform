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
        recordId: it.id
    ]
}
return [
    columns: [
        [header: "Datum", property: "batchDate"],
        [header: "Batch ID", property: "batchId"],
    ],
    rows: rows,
    totalCount: page.totalCount,
    hasNextPage: page.hasNextPage
]
