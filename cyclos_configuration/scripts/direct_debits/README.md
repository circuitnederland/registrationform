Direct debits
==============

# Transfer types

## Topup

Create a new 'Payment transfer type' in the Account type 'Debiet rekening':

- Name: Opwaarderen
- Internal name: topup
- From: Debiet rekening
- To: Handelsrekening
- Channels: Main, Mobile app

# User record types

## Direct debit

- Name: Incasso
- Internal name: directDebit
- Plural name: Incasso's
- General search: Yes (this results in a submenu for admins that have view permission, we may remove this if we use a custom operation for admins)

Fields:

Status
- Name: Status
- Internal name: status
- Data type: Single selection
- Required: Yes
- Show in results: Yes
- Include as search filter: Yes
- Possible values (including the internal name):
    - Open (open) - Set as Default
    - Cancel (cancel)
    - Submitted (submitted)
    - Retry (retry)
    - Resubmitted (resubmitted)

Comment
- Name: Commentaar
- Internal name: comments
- Data type: Multiple line text

Transaction
- Name: Transactie
- Internal name: transaction
- Data type: Linked Entity
- Linked entity type: Transaction
- Required: Yes

Batch ID
- Name: Batch ID
- Internal name: batchId
- Data type: Single line text
- Show in results: Yes

# System record types

## PAIN_008 Direct debit batch

- Name: Incassobestand
- Internal name: pain_008
- Plural name: Incassobestanden

Fields:

Batch ID
- Name: Batch ID
- Internal name: batchId
- Data type: Single line text
- Unique: Yes
- Required: Yes
- Show in results: Yes

XML
- Name: XML
- Internal name: xml
- Data type: Multiple line text
- Required: Yes
- Ignore value sanitization: Yes

# Scripts

## Library script

- Name: directDebit Library
- Script code: `direct_debits\Library.groovy`
- Parameters: `Library.properties` (adjust the values to the real values)

## Custom operation script

- Name: directDebit List PAIN.008
- Included libraries: directDebit Library
- Script code: `Operation_ListPAIN_008.groovy`

## Custom operation script

- Name: directDebit Download PAIN.008
- Included libraries: directDebit Library
- Script code: `Operation_InternalDownloadPAIN_008.groovy`

## Scheduled task script

- Name: directDebit Generate PAIN.008
- Included libraries: directDebit Library
- Script code: `direct_debits\ScheduledTask_GeneratePain008.groovy`

## Extension point script

- Name: directDebit Create record at topup
- Script code executed when the data is saved: `direct_debits\ExtensionPoint_TopupCreateDirectDebit.groovy`

# Custom operations

## Download PAIN.008

- Name: Download PAIN.008 incassobestand (can be changed)
- Internal name: download_pain_008
- Enabled for channels: Main
- Scope: Internal
- Script: directDebit Download PAIN.008
- Result type: File download

After saving, add a Form field:
- Display name: record ID
- Internal name: recordId
- Required: Yes

## List PAIN.008

- Name: PAIN.008 incassobestanden (can be changed)
- Internal name: list_pain_008
- Enabled for channels: Main
- Scope: System
- Script: directDebit List PAIN.008
- Result type: Result page
- Search automatically on page load: Yes
- Action when clicking a row: Run an internal custom operation
- Custom operation: Download PAIN.008 incassobestand
- Parameters to be passed (comma-separated names): recordId

# Scheduled tasks

## Generate PAIN.008

- Name: directDebit Generate PAIN.008
- Script: 'directDebit Generate PAIN.008'
- Run once per: 7 Days

# Extension points

## Topup transaction

Extension point to create a new directDebit user record for each topup transaction.

- Type: Transaction
- Name: Topup transaction
- Transfer types: 'Debiet rekening - Opwaarderen'
- Events: Confirm
- Script: directDebit Create record at topup

# Permissions

## Member Product

- [General] Records: Set the new Incasso record to 'Enable'.

## Administrator

- [System] System records: Set the new Incassobestand record to 'View'.
- [System] Run system custom operations: Enable the 'PAIN.008 incassobestanden' custom operation.
- [User data] User records: Set the new Incasso record to 'View' and only the fields 'Status' and 'Comment' to 'Edit'.
