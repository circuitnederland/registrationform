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
- Show in results: Yes

XML
- Name: XML
- Internal name: xml
- Data type: Multiple line text
- Ignore value sanitization: Yes

# Scripts

## Library script

- Name: directDebit Library
- Script code: `direct_debits\Library.groovy`
- Parameters: `Library.properties` (adjust the values to the real values)

## Scheduled task script

- Name: directDebit Generate PAIN.008
- Included libraries: directDebit Library
- Script code: `direct_debits\ScheduledTask_GeneratePain008.groovy`

## Extension point script

- Name: directDebit Create record at topup
- Script code executed when the data is saved: `direct_debits\ExtensionPoint_TopupCreateDirectDebit.groovy`

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
- [User data] User records: Set the new Incasso record to 'View' and only the fields 'Status' and 'Comment' to 'Edit'.
