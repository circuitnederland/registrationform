Direct debits
==============

# Transfer types

## Topup

Create a new 'Payment transfer type' in the Account type 'Debiet rekening':

- Name: Opwaarderen met incasso
- Internal name: topup
- From: Debiet rekening
- To: Handelsrekening
- Channels: Main, Mobile app

## Revoke topup

Create a new 'Payment transfer type' in the Account type 'Handelsrekening':

- Name: Terugboeken opwaardering
- Internal name: revoke_topup
- From: Handelsrekening
- To: Debiet rekening
- Value for empty description: Teruggeboekte opwaardering (#transactienummer#) ivm mislukte incasso
- Channels: Main, Mobile app

# User record types

## Direct debit

- Name: Incasso
- Internal name: directDebit
- Plural name: Incasso's
- General search: Yes

Fields:

Batch ID
- Name: Batch ID
- Internal name: batchId
- Data type: Single line text
- Show in results: Yes

Status
- Name: Status incasso
- Internal name: status
- Data type: Single selection
- Required: Yes
- Show in results: Yes
- Include as search filter: Yes
- Possible values (including the internal name):
    - Open (open) - Set as Default
    - Geannuleerd (cancelled)
    - Ingediend (submitted)
    - Mislukt (failed)
    - Open (2e poging) (retry)
    - Ingediend (2e poging) (resubmitted)
    - Definitief mislukt (permanently_failed)
    - Gecorrigeerd (incasso geannuleerd) (settled_cancelled)
    - Gecorrigeerd (incasso mislukt) (settled_failed)

Transaction
- Name: Transactie opwaardering
- Internal name: transaction
- Data type: Linked Entity
- Linked entity type: Transaction
- Required: Yes
- Include as search filter: Yes

Settlement
- Name: Correctie
- Internal name: settlement
- Data type: Single selection
- Include as search filter: Yes
- Possible values (including the internal name):
    - Alsnog betaald via bank (paid)
    - Opwaardering teruggeboekt (revoked)

IBAN
- Name: IBAN
- Internal name: settlement_iban
- Data type: Single line text
- Validation script: check IBAN

Settlement Transaction
- Name: Transactie terugboeking
- Internal name: settlement_transaction
- Data type: Linked entity
- Linked entity type: Transaction

Comment
- Name: Commentaar
- Internal name: comments
- Data type: Multiple line text

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

Nr of transactions
- Name: Aantal transacties
- Internal name: nrOfTrxs
- Data type: Integer
- Show in results: Yes

Total amount
- Name: Totaalbedrag
- Internal name: totalAmount
- Data type: Decimal
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
- Included libraries: utils Library
- Parameters: `Library.properties` (adjust the values to the real values)
- Script code: `direct_debits\Library.groovy`

## Custom field validation script

- Name: directDebit Check Topup amount
- Script code executed when the custom operation is executed: `direct_debits\FieldValidation_TopupAmount.groovy`

## Custom operation script

- Name: directDebit Topup
- Included libraries: directDebit Library
- Script code executed when the custom operation is executed: `direct_debits\Operation_InternalTopup.groovy`

## Custom operation script

- Name: directDebit Download PAIN.008
- Script code: `direct_debits\Operation_RecordDownloadPAIN_008.groovy`

## Custom operation script

- Name: directDebit Manager
- Included libraries: directDebit Library
- Script code when operation is executed: `Operation_UserRecordManager.groovy`
- Script code to determine whether operation is available: `direct_debits\Operation_UserRecordManagerVisibility.groovy`

## Recurring task script

- Name: directDebit Generate PAIN.008
- Included libraries: directDebit Library
- Script code: `direct_debits\ScheduledTask_GeneratePain008.groovy`

## Extension point script

- Name: directDebit Create record at topup
- Script code executed when the data is saved: `direct_debits\ExtensionPoint_TopupCreateDirectDebit.groovy`

# Custom operations

## directDebit Topup

- Name: Opwaarderen via incasso
- Internal name: topupViaDirectDebit
- Enabled for Channels: Main, Mobile app
- Scope: Internal
- Script: directDebit Topup
- Result type: Notification
- Show form: Always
- Information text: {As decided by stakeholders}
- Confirmation script execution message: {As decided by stakeholders}

Form fields:

Gewenst bedrag
- Internal name: amount
- Data type: Decimal
- Default value: 50
- Required: Yes
- Validation script: directDebit Check Topup amount
- Validation script parameters:
minimum_amount_particulieren=10
minimum_amount_bedrijven=50
maximum_amount_particulieren=150
maximum_amount_bedrijven=500

User
- Internal name: user
- Data type: Linked entity
- Linked entity type: User
- Required: Yes

After saving the Custom operation, change its order so it is just above the 'Incassomachtiging' internal Custom operation.

## Download PAIN.008

- Name: Download PAIN.008 incassobestand (can be changed)
- Label: Download PAIN.008 bestand (XML)
- Enabled for channels: Main
- Scope: Record
- Record type: Incassobestand
- Script: directDebit Download PAIN.008
- Result type: File download

## Cancel directDebit

- Name: Incasso annuleren (can be changed)
- Enabled for channels: Main
- Scope: Record
- Record type: Incasso
- Script: directDebit Manager
- Script parameters: action=cancel
- Result type: Notification
- Show form: Always

Form fields:

Comments
- Name: Commentaar
- Internal name: comments
- Data type: Multiple line text

## Mark directDebit as failed

- Name: Incasso markeren als mislukt (can be changed)
- Enabled for channels: Main
- Scope: Record
- Record type: Incasso
- Script: directDebit Manager
- Script parameters: action=fail
- Result type: Notification
- Show form: Always

Form fields:

Comments
- Name: Commentaar
- Internal name: comments
- Data type: Multiple line text

## Retry directDebit

- Name: Incasso opnieuw indienen (can be changed)
- Enabled for channels: Main
- Scope: Record
- Record type: Incasso
- Script: directDebit Manager
- Script parameters: action=retry
- Result type: Notification
- Show form: Always

Form fields:

Comments
- Name: Commentaar
- Internal name: comments
- Data type: Multiple line text

## Settle directDebit via bank payment

- Name: Mislukte incasso afhandelen (bank) (can be changed)
- Label: Mislukte incasso afhandelen door bankoverschrijving in te voeren (can be changed)
- Enabled for channels: Main
- Scope: Record
- Record type: Incasso
- Script: directDebit Manager
- Script parameters: action=settle_paid
- Result type: Notification
- Show form: Always

Form fields:

IBAN
- Name: IBAN
- Internal name: iban
- Data type: Single line text
- Required: Yes

Note: the Validation script 'check IBAN' is already set on the IBAN field of the directDebit user record, so there is no need to set it on the IBAN field of the operation as well.

Comments
- Name: Commentaar
- Internal name: comments
- Data type: Multiple line text

## Settle directDebit by revoking the topup

- Name: Mislukte incasso afhandelen (terugboeking) (can be changed)
- Label: Mislukte incasso afhandelen door opwaardering te laten intrekken (can be changed)
- Enabled for channels: Main
- Scope: Record
- Record type: Incasso
- Script: directDebit Manager
- Script parameters: action=settle_revoked
- Result type: Notification
- Show form: Always

Form fields:

Comments
- Name: Commentaar
- Internal name: comments
- Data type: Multiple line text

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
- Transfer types: 'Debiet rekening - Opwaarderen met incasso'
- Events: Confirm
- Script: directDebit Create record at topup

# Permissions

## Member Product

- [General] Records: Set the new Incasso record to 'Enable'.
- [General] Custom operations: Enable the five new Record operations for managing directDebit user records.

## Network Administrators Group

- [System] System records: Set the new Incassobestand record to 'View'.
- [System] Run system custom operations: Enable the 'Download PAIN.008 incassobestand' custom operation.
- [User data] User records: Set the new Incasso record to 'View'.

## Financial Administrators Group

- [System] System records: Set the new Incassobestand record to 'View'.
- [System] Run system custom operations: Enable the 'Download PAIN.008 incassobestand' custom operation.
- [User management] Run custom operations over users: Enable the five new Record operations for managing directDebit user records.
- [User data] User records: Set the new Incasso record to 'View'.
