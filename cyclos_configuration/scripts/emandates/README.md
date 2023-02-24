eMandates integration
=====================

# Deployment

Additional resources are required to deploy the solution on the Cyclos installation directory:

- *eMandates.jks*: Copy it to WEB-INF/classes folder
- *eMandates-config.xml: Copy it to WEB-INF/classes folder
- *emandates.jar*: From the eMandates github page, copy the net.emandates.merchant.library.jar to WEB-INF/lib
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

Validation reference
- Internal name: validationReference
- Data type: Single line text

Withdrawn by user
- Internal name: isWithdrawn
- Data type: Boolean

Raw message
- Internal name: rawMessage
- Data type: Multi line text
- Maximum size for words: empty
- Ignore value sanitization: Yes

Enter the Dutch translation for the recordtype. Go to Content > [Content management] Data translation > Circuit Nederland > [Records] Record types. Enter the following translations:
- eMandate: Digitale machtiging
- eMandates: Digitale machtigingen

Enter the Dutch translation for the recordtype fields. Go to Content > [Content management] Data translation > Circuit Nederland > [Records] Record fields. In the eMandate section enter the following translations:
- User: Deelnemer
- Bank name: Bank naam
- Status date: Status datum
- Transaction ID: Transactie ID
- Account name: Rekeninghouder
- Signer name: Ondertekenaar
- Withdrawn: Ingetrokken

Enter the Dutch translation for the possible values of the recordtype fields. Go to Content > [Content management] Data translation > Circuit Nederland > [Records] Record fields possible values. In the 'eMandate Status' section enter the following translations:
- Cancelled: Geannuleerd
- Expired: Verlopen
- Failure: Mislukt
- Pending: In behandeling
- Success: Succesvol

# Scripts

The following scripts will be used:

## eMandates Library

The library holds most of the logic.

- Included libraries: utils Library
- Script: `Library.groovy`
- Parameters: `Library.properties`

## eMandates Update Banklist

Custom scheduled task script which updates the possible values and categories of the custom operation fields reflecting the available banks.

- Included libraries: eMandates Library
- Script: `ScheduledTask_UpdateBanklist.groovy`

## eMandates Check Pending

Custom scheduled task script which checks whether eMandates of status pending were procesed.

- Included libraries: eMandates Library
- Script: `ScheduledTask_CheckPending.groovy`

## eMandates Check Open

Custom scheduled task script which requests a new status for eMandates of status open.

- Included libraries: eMandates Library
- Script: `ScheduledTask_CheckOpen.groovy`

## eMandates Create

Custom operation script to create an eMandate.

- Run with all permissions: Yes
- Included libraries: eMandates Library
- Script when operation is executed: `Operation_InternalCreate.groovy`
- Script when the external site redirects: `Operation_InternalCreate_Callback.groovy`

## eMandates Update

Custom operation script to amend an eMandate.

- Run with all permissions: Yes
- Included libraries: eMandates Library
- Script when operation is executed: `Operation_InternalUpdate.groovy`
- Script when the external site redirects: `Operation_InternalUpdate_Callback.groovy`

## eMandates Withdraw by user

Custom operation script to let the user withdraw or re-activate their emandate.

- Run with all permissions: Yes
- Included libraries: eMandates Library
- Script when operation is executed: `Operation_InternalToggleWithdraw.groovy`

## eMandates Main

Custom operation script to let the user start a create or update.

- Run with all permissions: Yes
- Included libraries: eMandates Library
- Script when operation is executed: `Operation_Main.groovy`

## eMandates Block by admin

Custom operation script to let financial admins block or deblock a user's emandate.

- Run with all permissions: Yes
- Included libraries: eMandates Library
- Script when operation is executed: `Operation_InternalToggleBlock.groovy`

## eMandates Manager

Custom operation script to let financial admins manage the emandate of a user.

- Run with all permissions: Yes
- Included libraries: eMandates Library
- Script when operation is executed: `Operation_Manager.groovy`

## eMandates Generic Callback

Custom web service responds to the *eMandates.Merchant.ReturnUrl* setting in the *emandates-config.xml* file.

- Included libraries: eMandates Library
- Script: `WebService_GenericCallback.groovy`

# Scheduled tasks

## eMandates Update Banklist

The documentation requires us to call this no more than once per week. So the period should be set to 7 days. But it needs to be executed run manually to update the banks initially.

## eMandates Check Pending

If an eMandate needs to be signed by multiple parties, the approval won't be online. Instead, the eMandate is returned in pending status and needs to be checked periodically. The documentation requires us to call this no more than once per day per eMandate. So the period should be set to 1 day.

## eMandates Check Open

When a user issues an eMandate we request the status of the eMandate when the user is redirected to Cyclos. In some unusual situations the redirect could not happen in which case we don't request the status. Or, in unusual situations the bank may not yet respond with a final status which leaves the status of the eMandate open. According to the rules, we have a 'Collection Duty', meaning we must request the status of open eMandates for some time until we receive a final status. This task does this every six hours.

# Web services

## eMandates Generic Callback

Cyclos' custom operations use a dynamic return URL. However, as the Java library for eMandates uses a fixed URL, we'll use this web service to redirect the user to the actual custom operation callback URL.

- HTTP method: Both
- Run as: Guest
- Script: eMandates Generic Callback
- URL mappings: eMandatesCallback

# Profile fields

## Lock emandates field

Add a new user profile field: System > [User configuration] 'Profile fields' > New.

- Display name: Incassomachtiging vergrendeling (can be changed)
- Internal name: emandates_lock
- Data type: Single selection
- Include in file export: No
- Include in account history print (PDF): No
- Hidden by default: Yes

After saving the new profile field, add one Possible value (text value can be changed):

- Geblokkeerd door admin (incasso's te vaak mislukt)

Add the English translation in small caps as the internal name for the possible value: blocked.

# Custom operations

## Create eMandate

- Name: Incassomachtiging afgeven (can be changed)
- Internal name: createEMandate
- Enabled for channels: Main, Mobile app
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
- Display name: Bank (can be changed)
- Internal name: debtorBank
- Data type: Single selection
- Required: Yes

## Amend eMandate

- Name: Incassomachtiging wijzigen (can be changed)
- Internal name: amendEMandate
- Enabled for channels: Main, Mobile app
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
- Display name: Bank (can be changed)
- Internal name: debtorBank
- Data type: Single selection
- Required: Yes

## eMandate Withdrawing

- Name: Incassomachtiging intrekken of herstellen (can be changed)
- Internal name: eMandateWithdrawingByUser
- Label: Incassomachtiging intrekken
- Enabled for channels: Main, Mobile app
- Scope: Internal
- Script: eMandates Withdraw by user
- Result type: Notification

Form fields:

User
- internal name: user
- Data type: Linked entity
- Linked entity type: User
- Required: Yes

## Incassomachtiging

Displays the current eMandate status for a user.

- Name: Incassomachtiging (can be changed)
- Internal name: eMandate
- Enabled for channels: Main, Mobile app
- Scope: Internal
- Script: eMandates Main
- Result type: Rich text

Form fields:

User
- internal name: user
- Data type: Linked entity
- Linked entity type: User
- Required: Yes

Actions:
- Incassomachtiging afgeven (User parameter checked)
- Incassomachtiging wijzigen (User parameter checked)
- Incassomachtiging intrekken of herstellen (User parameter checked)

## eMandate Blocking

- Name: Incassomachtiging (de)blokkeren (can be changed)
- Internal name: eMandateBlockByAdmin
- Label: Incassomachtiging blokkeren
- Enabled for channels: Main
- Scope: Internal
- Script: eMandates Block by admin
- Result type: Notification

Form fields:

User
- internal name: user
- Data type: Linked entity
- Linked entity type: User
- Required: Yes

## Incassomachtiging Beheer

Displays the current eMandate status for a user to financial admins.

- Name: Incassomachtiging beheer (can be changed)
- Internal name: eMandateManager
- Enabled for channels: Main
- Scope: User
- Script: eMandates Manager
- Result type: Rich text

Actions:
- Incassomachtiging (de)blokkeren (User parameter checked)

# Products / Admin permissions

## Member product

During the test phase we will use a separate Product so we can give the eMandates functionality to a selected number of users. Later on, we will move the permissions to a Product that is active for all users.

- In 'My profile fields' set the field 'Incassomachtiging vergrendeling' to Enabled.
- In 'Custom operations' enable the eMandate manager ('Incassomachtiging Beheer') operation.

## Administrator

Change permissions in the Group 'Administrateurs C3-Nederland (Netwerk)':

- [System] 'System records': remove the Create, Edit and Remove permissions for the eMandate system record. Admins should only be allowed to see this record, not change it.
- [User management] 'Profile fields of other users': set the new 'Incassomachtiging vergrendeling' field to Visible.
- [User management] 'Add / remove individual products': Add the new temporary Product.

Change permissions in the Group 'Administrateurs - Financieel':

- [User management] 'Run custom operations over users': add 'Incassomachtiging Beheer'.
