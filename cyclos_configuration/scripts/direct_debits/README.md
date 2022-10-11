Direct debits
==============

# Transfer types

## Topup

Create a new 'Payment transfer type' in the Account type 'Debiet rekening':

- Name: Opwaarderen
- Internal name: topup
- From: Debiet rekening
- To: Handelsrekening

# User record types

## Direct debit

- Name: Incasso
- Internal name: directDebit
- Plural name: Incasso's

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

# Scripts

## Extension point script

- Name: directDebit Create record at topup
- Script code executed when the data is saved: `direct_debits\ExtensionPoint_TopupCreateDirectDebit.groovy`

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

- [User data] User records: Set the new Incasso record to 'View' and only the fields 'Status' and 'Comment' to 'Edit'.
