eMandates integration
=====================

# Deployment

Additional resources are required to deploy the solution on the Cyclos installation directory:

- *eMandates.jks*: Copy it to WEB-INF/classes folder
- *eMandates-config.xml: Copy it to WEB-INF/classes folder
- *jaxb-runtime*: Download the [jar from maven](https://mvnrepository.com/artifact/org.glassfish.jaxb/jaxb-runtime/2.3.4) and put it in WEB-INF/lib
- *istack-commons-runtime*: Download the [jar from maven](https://mvnrepository.com/artifact/com.sun.istack/istack-commons-runtime/3.0.12) and put it in WEB-INF/lib

# System record type

This system record will be used to store the eMandate. It cannot be of scope user because it will be also used on registration wizard, and needs to be created before the user registration is finished.

- Name: eMandate (can be changed)
- internalName: eMandate
- Plural name: eMandates (can be changed)
- Use separated view / edit pages: Yes

Setup the following fields:

User
- Internal name: owner (user is reserved)
- Data type: Linked entity
- Linked entity type: User
- Show in results: Yes
- Include as search filter: Yes

Bank id
- Internal name: bankId
- Data type: Single line text
- Required: Yes

Bank name
- Internal name: bankName
- Data type: Single line text
- Required: Yes
- Show in results: Yes

Status
- Internal name: status
- Data type: Single selection
- Required: Yes
- Show in results: Yes
- Include as search filter: Yes (this is required for the task that checks for pending)
- Possible values: internal names must be those in lowercase. Values can be changed:
    - Success
    - Cancelled
    - Expired
    - Failure
    - Open (set this one as default value)
    - Pending

Status date
- Internal name: statusDate
- Data type: Date
- Show in results: Yes

Transaction ID
- Internal name: transactionId
- Data type: Single line text
- Use exact matching on search filters: Yes
- Unique: Yes
- Include as search filter: Yes (the library will search, even if not needed on page)

IBAN
- Internal name: iban
- Data type: Single line text

Account name
- Internal name: accountName
- Data type: Single line text

Signer name
- Internal name: signerName
- Data type: Single line text

Raw message
- Internal name: rawMessage
- Data type: Multi line text

# Scripts

The following scripts will be used:

## eMandates Library

The library holds most of the logic.

- Script: `Library.groovy`
- Parameters: `Library.properties`

## eMandates Update Banklist

Custom scheduled task script which updates the possible values and categories of the custom operation fields reflecting the available banks.

- Script: `ScheduledTask_UpdateBanklist.groovy`

## eMandates Check Pending

Custom scheduled task script which checks whether eMandates of status pending were procesed.

- Script: `ScheduledTask_CheckPending.groovy`

## eMandates Create

Custom operation script to create an eMandate.

- Script when operation is executed: `Operation_InternalCreate.groovy`
- Script when the external site redirects: `Operation_InternalCrud_Callback.groovy`

## eMandates Update

Custom operation script to amend an eMandate.

- Script when operation is executed: `Operation_InternalUpdate.groovy`
- Script when the external site redirects: `Operation_InternalCrud_Callback.groovy`

## eMandates Main

Custom operation script to let the user start a create or update.

- Script when operation is executed: `Operation_Main.groovy`

## eMandates Generic Callback

Custom web service responds to the *eMandates.Merchant.ReturnUrl* setting in the *emandates-config.xml* file.

- Script: `WebService_GenericCallback.groovy`

# Scheduled tasks

## eMandates Update Banklist

The documentation requires us to call this no more than once per week. So the period should be set to 7 days. But it needs to be executed run manually to update the banks initially.

## eMandates Check Pending

If an eMandate needs to be signed by multiple parties, the approval won't be online. Instead, the eMandate is returned in pending status and needs to be checked periodically. The documentation requires us to call this no more than once per day per eMandate. So the period should be set to 1 day.

# Web services

## eMandates Generic Callback

Cyclos' custom operations use a dynamic return URL. However, as the Java library for eMandates uses a fixed URL, we'll use this web service to redirect the user to the actual custom operation callback URL.

- HTTP method: Both
- Run as: Guest
- Script: eMandates Generic Callback
- URL mappings: eMandatesCallback

# Custom operations

## Create eMandate

- Name: Incassomachtiging afgeven (can be changed)
- Internal name: createEMandate
- Enabled for channels: Main
- Scope: Internal
- Script: eMandates Create
- Result type: External redirect

Form fields:

User
- internal name: user
- Data type: Linked entity
- Linked entity type: User
- Required: Yes

Debtor bank
- internal name: debtorBank
- Data type: Single selection
- Required: Yes

## Amend eMandate

- Name: Incassomachtiging wijzigen (can be changed)
- Internal name: amendEMandate
- Enabled for channels: Main
- Scope: Internal
- Script: eMandates Update
- Result type: External redirect

Form fields:

User
- internal name: user
- Data type: Linked entity
- Linked entity type: User
- Required: Yes

Debtor bank
- internal name: debtorBank
- Data type: Single selection
- Required: Yes

## Incassomachtiging

Displays the current eMandate status for a user.

- Name: Incassomachtiging (can be changed)
- Internal name: eMandate
- Enabled for channels: Main
- Scope: User
- Script: eMandates Main
- Result type: Rich text

Actions:
- Incassomachtiging afgeven (User parameter checked)
- Incassomachtiging wijzigen (User parameter checked)

# Products / Admin permissions

## Member product

During the test phase we will use a separate Product so we can give the eMandates functionality to a selected number of users. Later on, we will move the permissions to a Product that is active for all users.

- In custom operations, enable and allow the eMandate ('Incassomachtiging') operation to 'Run self'.

## Administrator

Change permissions in the Group 'Administrateurs C3-Nederland (Netwerk)':

- [System] 'System records': remove the Create, Edit and Remove permissions for the eMandate system record. Admins should only be allowed to see this record, not change it.
- [User management] 'Add / remove individual products': Add the new temporary Product.
