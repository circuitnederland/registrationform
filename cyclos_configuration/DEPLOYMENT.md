# Deployment Tasks per release
Things to do manually in the Cyclos production-environment when deploying a new release of the PHP registrationform to production.

## Deployment Tasks for release 1.10.0 (eMandates/directDebits go-live)

1. Create a new Member Product 'Opwaarderen simpel (tijdelijk)'. Set in the permission '[General] Custom operations' the [User] 'Saldo opwaarderen' to Enabled and Run self.

2. Add the new Product to the Group Sets of all communities that should NOT get the emandate functionality yet.

3. Add the Product 'Incassomachtiging / Opwaarderen' to the Group Sets of all communities that should get the emandate functionality.

4. Remove the [User] 'Saldo opwaarderen' of the '[General] Custom operations' permission in the 'Algemeen (handelsrekening)' Product.

When all communities use the emandates functionality, the old/temporary elements can be removed:  
- Remove the 'Opwaarderen simpel (tijdelijk)' Product as it is not enabled in any Group or Group Set anymore.  
- Remove the old 'Opwaarderen' Custom operation and rename the "Circulaire euro's aankopen (Nieuw)" Custom operation to "Circulaire euro's aankopen".  
- Remove the old 'Opwaarderen' Menu floating page and rename the 'Opwaarderen (Nieuw) floating page to 'Opwaarderen'.  
- Find users that still have the 'Incassomachtiging / Opwaarderen' Product individually assigned and remove the Product from them, since they now have it via their Group Set.

When all communities use the emandates functionality, we could transfer all permissions from the 'Incassomachtiging / Opwaarderen' Product into a general Product, for example 'Algemeen (handelsrekening)', remove the 'Incassomachtiging / Opwaarderen' Product assignment from all Group Sets and after that remove the Product itself.

## Deployment Tasks for release 1.9.0

# System record types

## Text Messages

Go to System > [System configuration] 'Record types' > 'Technical details' > Tab 'Fields' and create a new field:  
- Display name: Turn off  
- Internal name: ddTurnedOff  
- Data type: Boolean  
- Information text: {As decided by business}  

After adding the new field, go to the Sections tab, open the 'Direct Debit' section and add the new field in the 'Fields in this section' property.  

Go to System > [System configuration] 'Record types' > 'Text messages' > Tab 'Fields' and create a new field:  
- Display name: eMandates turned off  
- Internal name: bcEMandatesTurnedOff  
- Data type: Multiple line text  
- Ignore value sanitization: Yes

After adding the new field, go to the Sections tab, open the 'Buy credits' section and add the new field in the 'Fields in this section' property.  
Move the 'Buy Credits' section to the top of the three sections, so it is easier to find. Move the new field to right under the 'Buy via bank' field.

Go to System > [System records] 'Text messages' and fill in the new field with the message text as decided by business.

## Application translations

Go to Content > [Content management] Application translation > Circuit Nederland. Filter on 'Translation key': 'addressFields.CITY'. Change the Current translation of both 'MOBILE.ADDRESSES.addressFields.CITY' and 'USERS.ADDRESSES.addressFields.CITY' from 'Woonplaats' into 'Plaats'.

## Profile fields

Go to System > [User configuration] Profile fields > Bijdrage. On the 'Possible values' tab add four new options:  
- Value: € 0,01 - Account zonder Utrechtse Euro betaalrekening (internal name: utrechtseeuro_bedrijven_0)  
- Value: € 50 - bedrijven < 10 werknemers (internal name: utrechtseeuro_bedrijven_1)  
- Value: € 150 - bedrijven < 50 werknemers (internal name: utrechtseeuro_bedrijven_2)  
- Value: € 300 - bedrijven > 50 werknemers (internal name: utrechtseeuro_bedrijven_3)  

Temporarily remove the validation on the Bijdrage profile field: set the Load values script from 'contribution scales' to 'None' on the Bijdrage profile field.  
Go to Users > [Management] Bulk actions. Click 'Run new' > 'Change custom field value'. Run three bulk actions, each time with Group set to 'Utrechtse euro Bedrijven', but with different options for the 'Bijdrage':  
- Filter 'Bijdrage' on the first option '€ 50 - bedrijven < 10 werknemers' > Set Custom field 'Bijdrage' to the '€ 50 - bedrijven < 10 werknemers' more below.  
- Filter 'Bijdrage' on the first option '€ 150 - bedrijven < 50 werknemers' > Set Custom field 'Bijdrage' to the '€ 150 - bedrijven < 50 werknemers' more below.  
- Filter 'Bijdrage' on the first option '€ 300 - bedrijven > 50 werknemers' > Set Custom field 'Bijdrage' to the '€ 300 - bedrijven > 50 werknemers' more below.

Put the validation on the Bijdrage profile field back: set the Load values script back from 'None' to 'contribution scales' on the Bijdrage profile field.

## Deployment Tasks for release 1.8.0

# System record types

## Text Messages

Go to System > [System configuration] 'Record types' > 'Text messages' > Tab 'Fields' and create a new field:  
- Display name: Transactie aan ontvanger geblokkeerd  
- Internal name: circ_payment_blocked  
- Data type: Multiple line text  
- Information text: {As decided by business}  
- Ignore value sanitization: Yes

Go to System > [System records] 'Text messages' and fill in the new field with the message text as decided by business.

# Scripts

## Extension point script
Go to System > [Tools] Scripts and add a new Script of type 'Extension point':  
- Name: blockTransactionToNonTradingUser  
- Included libraries: utils Library  
- Script code executed when the data is validated, but not yet saved: paste the contents of scripts/extensionpoint_blockTransactionToNonTradingUser.groovy.

# Extension points

Go to System > [Tools] Extension points and add a new Extension Point of type Transaction:  
- Name: block transactions to non-trading users  
- Transfer types: Handelsrekening - Handelstransactie  
- Events: Preview  
- Scripts: blockTransactionToNonTradingUser  

## Deployment Tasks for changes to registration form / migration to UnEc - Round 3 (i88) part of release 1.7.0

# Change mail addresses from xx@circuitnederland.nl to xx@unitedeconomy.nl
- Mollie system record: Reports > [System records] 'Molly configuration (keep safe)': change the 'Email adres weer te geven in foutmelding', 'Schermmelding aan bezoeker bij openen link activatiemail zonder betaling' and 'Tekst voor het geval Mollie onbereikbaar is'.
- EMandate: System > [System records] Technical details: change the 'Mail admin' and the 'Mail techTeam'.

# Add ip of UnEc server to ip whitelist
- System > [System configuration] Configuration > Default for NL > Tab Channels > Web services: Add the ip-address of the UnEc server to the 'IP address whitelist'.

# Change the location of the registration form
- Move the registration form files (php/css/js) to the UnEc server (test or prd).
- Add a redirect from the old C3NL location to the new UnEc location.
- Mollie system record: Reports > [System records] 'Molly configuration (keep safe)': change the 'BasisURL van het registratieformulier' to the new location.

## Deployment Tasks for changes to registration form / migration to UnEc (i88) part of release 1.7.0

# Change fields

- The display name of the profile field Lidmaatschapsbijdrage: change into 'Bijdrage'.
- The information text of the profile fields Lidmaatschapsbijdrage, Website, Circulaire betalingen and K.v.K. nummer: {text as decided by stakeholders}.
- Go to the Global administration > System > [User configuration] Password types > Login password. Change the 'Public description': {text as decided by stakeholders}.
- In the Product 'Algemeen voor bedrijven (behalve UE)' change the permission 'My profile fields': set 'At registration' to yes for the 'Circulaire betalingen' field.

## Deployment Tasks for release 1.6.0 (changes to profile fields (i84))

# Scripts

1. Type: Load custom field values
- Name: contribution scales
- Run with all permissions: No
- Script code that returns the possible values when either creating or editing an entity: paste the contents of scripts/loadCustomFieldValues_ContributionScales.groovy.

# Remove profile fields

Remove profile fields we no longer wish to use:

- 'Lid van een broodfonds'
- 'Klant bij Triodos'
- 'Bedrijf'

First, go to System > [User configuration] Products. Adjust the following permissions:

- Product 'Algemeen voor bedrijven (behalve UE)':
    - 'Description': Remove the above fields from the list.
    - 'My profile fields': Set 'Enabled' to 'No' for the above fields (this also sets all other columns to No).

- Product 'Algemeen voor iedereen (behalve UE)':
    - 'Profile fields of other users': Set the above fields to Visible 'No' (this also sets all other columns to No).

- Product 'Algemeen United Economy':
    - 'Profile fields of other users': Set the above fields to Visible 'No' (this also sets all other columns to No).
    - 'My profile fields': Set 'Enabled' to 'No' for the above fields (this also sets all other columns to No).

Next, go to System > [User configuration] Groups. Adjust the permissions for the following groups:

- 'Administrateurs C3-Nederland (Netwerk)'
- 'Administrateurs financieel - Circuit Nederland'
- 'All United - Operationeel beheerders'
    - 'Profile fields of other users': Set 'Enabled' to 'No' for the above fields (this also sets all other columns to No).
    - 'Profile fields in simple users search': uncheck 'Bedrijf'.

Finally, go to System > [User configuration] Profile fields. Click the trash icon for each of the above profile fields to remove it.

# Remove address fields

Disable address fields we no longer wish to use:

- Provincie
- Land
Go to System > [Systeem configuration] Configurations > 'Circuit Nederland' and 'Eurijn'. Click the reset icon near 'Define address fields' to stop customizing this value.

Go to System > [Systeem configuration] Configurations > 'Default for Nederland'.
- Change the 'Enabled address fields': de-select 'Region or state' and 'Country'.
- Check the lock icon to lock this value so it can no longer be edited by lower level configurations.

# Change permissions

Remove the permission to edit the own profile field for the 'K.v.K. nummer' profile field in the Products 'Algemeen voor bedrijven (behalve UE)' and 'Algemeen United Economy'.

Add the permission to filter on the accepts circulair payments profilefield in the Product 'Algemeen United Economy': 'Profile fields of other users' > check the 'User filter' column for the 'Circulaire betalingen' field.

# Informal to formal Dutch

Change the Information text of profile fields to use formal instead of informal Dutch:
- IBAN
- Website
- K.v.K. nummer
- Diensten/producten

Go to the Global configuration > System > [User configuration] Password types > 'Login password'. Change the 'Public description' so it uses formal instead of informal Dutch.

Go to Reports > [System records] Molly configuration (keep safe). Change all texts containing informal Dutch to use formal Dutch.

# Change explanation texts of fields

- The information text of the profile field K.v.K. nummer: Change " neem dan contact op met Circuit Nederland" into: " neem dan contact met ons op".
- Go to the Global administration > System > [User configuration] Password types > Login password. Change the 'Public description': remove the line " Dit wachtwoord gebruikt u bij het inloggen op uw online rekening.".

# Add new profile fields

## Authorized signatory (Tekeningsbevoegde)
Add a new profile field for authorized signatories: System > [User configuration] 'Profile fields' > New.
- Display name: Tekeningsbevoegde
- Internal name: authorized_signatory

After saving the new profile field, use the arrows to move the field up, just above the 'Contactpersoon bedrijf' field.

Add permissions for the new profile field for authorized signatories:
- Group 'Administrateurs C3-Nederland (Netwerk)' > 'Profile fields of other users': Add Visible for 'Tekeningsbevoegde'.
- Group 'Administrateurs financieel - Circuit Nederland' > 'Profile fields of other users': Add Visible and Editable for 'Tekeningsbevoegde'.
- Product 'Algemeen voor bedrijven (behalve UE)' > 'My profile fields': Add Enabled, At registration and Visible for 'Tekeningsbevoegde'.
- Product 'Algemeen United Economy' > 'My profile fields': Add Enabled, At registration and Visible for 'Tekeningsbevoegde'.

## Contribution (Lidmaatschapsbijdrage)
First, deploy the PHP files to make sure the registration form is ready to handle the new contribution profile field.

Add a new profile field for the contribution: System > [User configuration] 'Profile fields' > New.
- Display name: Lidmaatschapsbijdrage
- Internal name: lidmaatschapsbijdrage
- Data type: Single selection
- Information text: {as decided by stakeholders}
- Required: Yes
- Default visibility: Hidden for other users

After saving the new profile field, add the Possible values for the default company and consumer contribution scales:
- Value: {Use the texts as requested by the business, making sure the amount is the first number in the string}
- Internal name: standaard_bedrijven_1 / standaard_bedrijven_2 etc or standaard_particulieren_1 / standaard_particulieren_2 etc

After creating the new profile field, use the arrows to move the field up, just above the 'Actiecode' field.

Add permissions for the new contribution profile field:
- Group 'Administrateurs C3-Nederland (Netwerk)' > 'Profile fields of other users': Add Visible, Editable and User filter for 'Lidmaatschapsbijdrage'. And 'Profile fields in simple users search': check the 'Lidmaatschapsbijdrage' field.
- Group 'Administrateurs financieel - Circuit Nederland' > 'Profile fields of other users': Add Visible, Editable and User filter for 'Lidmaatschapsbijdrage'. And 'Profile fields in simple users search': check the 'Lidmaatschapsbijdrage' field.
- Product 'Algemeen voor iedereen (behalve UE)' > 'My profile fields': Add Enabled for 'Lidmaatschapsbijdrage'.

Migrate the chosen contribution values from the old profile fields to the new profile field via a set of bulk actions: Users > [Management] Bulk actions > Run new > 'Change custom field value'. Leave the 'Group' filter to the default member groups, set the 'Status' filter to all statusses. Run several bulk actions like this, each with different options:
- Filter 'Lidmaatschapsbijdrage bedrijven' on '50 - bedrijven met minder dan 10 werknemers' > Set Custom field 'Lidmaatschapsbijdrage' to '50 - bedrijven < 10 werknemers'.
- Filter 'Lidmaatschapsbijdrage bedrijven' on '150 - bedrijven met minder dan 50 werknemers' > Set Custom field 'Lidmaatschapsbijdrage' to '150 - bedrijven < 50 werknemers'.
- Filter 'Lidmaatschapsbijdrage bedrijven' on '300 - bedrijven met 50 of meer werknemers' > Set Custom field 'Lidmaatschapsbijdrage' to '300 - bedrijven > 50 werknemers'.
- Filter 'Lidmaatschapsbijdrage particulieren' on '15' > Set Custom field 'Lidmaatschapsbijdrage' to '15 - minimale bijdrage om de kosten te dekken'.
- Filter 'Lidmaatschapsbijdrage particulieren' on '40' > Set Custom field 'Lidmaatschapsbijdrage' to '40 - met deze optie steunt u ons'.
- Filter 'Lidmaatschapsbijdrage particulieren' on '70' > Set Custom field 'Lidmaatschapsbijdrage' to '70 - met deze optie steunt u ons heel erg'.
- Filter 'Lidmaatschapsbijdrage particulieren' on '100' > Set Custom field 'Lidmaatschapsbijdrage' to '100 - u bent een kanjer'.

Some users have been moved from a consumer group to a companies group or vice versa. To find them, filter the users on all 'Bedrijven' groups AND all values for 'Lidmaatschapsbijdrage particulieren'. If you find users, set the Lidmaatschapsbijdrage field so it reflects what they choose in the old field. And do the same vice versa filtering users on all 'Particulieren' groups AND all values for 'Lidmaatschapsbijdrage bedrijven'. On test this resulted in 2 users, for which I set the new Lidmaatschapsbijdrage to the default 40.

After running all bulk actions, set the Load values script 'contribution scales' on the Lidmaatschapsbijdrage profile field.

Add permissions for the new profile field so members can fill in the field at registration and see/change it in their profile.
- Product 'Algemeen voor iedereen (behalve UE)' > 'My profile fields': Add At registration, Visible and Editable for 'Lidmaatschapsbijdrage'.

Make sure no new users were made between running the bulk actions and adding the field to the 'Algemeen voor iedereen (behalve UE)' Product. If there are, manually fill in the new contribution field for those users, based on the chosen option in the old contribution field.

Remove the permissions for the two old contribution fields that were specific for companies and consumers:
- Product 'Algemeen (voor particulieren)' > 'My profile fields': Remove all permissions for 'Lidmaatschapsbijdrage particulieren'.
- Product 'Algemeen voor bedrijven (behalve UE)' > 'My profile fields': Remove all permissions for 'Lidmaatschapsbijdrage bedrijven'.

Next, go to System > [User configuration] Groups. Adjust the permissions for the following groups:

- 'Administrateurs C3-Nederland (Netwerk)'
- 'Administrateurs financieel - Circuit Nederland'
- 'All United - Operationeel beheerders'
    - 'Profile fields in simple users search': uncheck 'lidmaatschapsbijdrage Bedrijven' and 'Lidmaatschapsbijdrage Particulieren'.
    - 'Profile fields of other users': Set 'Enabled' to 'No' for the above fields (this also sets all other columns to No).

Finally, go to System > [User configuration] Profile fields. Click the trash icon for each of the above profile fields to remove it.

## Deployment Tasks for release 1.5.0 (eMandates/directDebits BETA)

# System record types

## Text Messages

- Name: Text messages
- Internal name: textMessages
- Plural name: Text messages
- Display style: Single form
- Main menu: System

Fields:

Create a Multiple line text field (Single line for buttons) for each of the following and add to the corresponding Section after creating those as well. Set 'Ignore value sanitization' to Yes for each field:  
- Without section (no prefix in the field internal names):  
Admin mail salutation / Admin mail closing  
- Section eMandates (use 'em' prefix in the field internal names):  
Description / Details / Different IBAN mail / Error save IBAN mail  
Result open / Result pending / Result success / Result failure / Result expired / Result cancelled / Result success retry  
Status none / Status open / Status pending / Status success / Status failure / Status expired / Status cancelled  
Status blocked / Status withdrawn / Result withdrawn / Result reset / Result blocked / Result unblocked / Result error  
Button withdraw / Button reset / Button block / Button de-block  
Manager status none / Manager status blocked / Manager status withdrawn  
- Section Topup (use 'topup' prefix in the field internal names):  
Topup result success / Topup result wrong amount / PAIN.008 generated mail  
Topup status success / Topup status none / Topup status blocked / Topup status inactive / Topup status wrong IBAN / Topup status withdrawn / Topup status weeklimit / Topup status unsettled
- Section Buy credits (use 'bc' prefix in the field internal names):  
Buy via bank / Button issue eMandate / Button manage eMandate  

## Technical details

- Name: Technical details
- Internal name: techDetails
- Plural name: Technical details
- Display style: Single form
- Main menu: System

Create Single line text fields for each of the following and add to the corresponding Section after creating those as well:  
- Without section (no prefix in the field internal names):  
Mail admin / Mail techTeam
- Direct Debit (use 'dd' prefix in the field internal names):  
Creditor name / Creditor IBAN / Creditor BIC / Creditor incassant ID

### Scripts

1. Type: Custom operation
- Name: Saldo ophogen
- Included libraries: eMandates Library, directDebit and utils Library
- Script code: paste the contents of scripts/buyCredits.groovy.

2. Utils Library script: remove the script parameters (they have been moved to the new system records).

### Menu pages

1. Go to Content > [Content management] Menu and pages > Default for Nederland > Add > Floating page.
- Label: Opwaarderen (BETA)
- Title: Opwaarderen (BETA)
- Content layout: Card with regular padding
- Content: {text as decided by stakeholders, explaining the way the user can have their balance upped by transferring money to the C3NL bank account.}

After saving, look up the pageId, for example 6745336155065950096. Use this in the script parameter of the buyViaBank operation below.

### Custom operations

1. Buy via bank
- Name: Aankopen via bankoverschrijving
- Internal name: buyViaBank
- Enabled for Channels: Main, Mobile app
- Scope: Internal
- Script: Menupagina's als custom operatie
- Script parameters:
pageId = {the pageId from the new Floating menu page 'Opwaarderen (BETA)', as created above.}
- Result type: Rich text

After saving the Custom operation, change its order so it is just below the 'Opwaarderen via incasso' internal Custom operation.

2. Buy credits (BETA)
- Name: Circulaire euro’s aankopen (BETA)
- Internal name: buyCredits
- Enabled for Channels: Main, Mobile app
- Scope: User
- Script: Saldo ophogen
- Result type: Rich text

Actions:
- Aankopen via incasso (User parameter checked)
- Aankopen via bankoverschrijving
- Incassomachtiging (User parameter checked)

### Scheduled tasks

1. Enable the scheduled tasks for the eMandates:

- Go to Systeem > [Tools] Scheduled tasks. Open both tasks ('eMandates Check Pending' and 'eMandates Update Banklist') and set them to 'Enabled': Yes.

### Permissions

1. Create and configure a new temporary Product to give eMandate/directDebit functionality to specific users (later on we will move those permissions to all users):

Go to System > [User management] Products > New > Member. Fill in the form:  
- Name: Incassomachtiging (eMandate)  
Permissions:
- [General] My profile fields: check 'Enabled' for 'Incassomachtiging vergrendeling'.
- [General] Records: Set the new Incasso record to 'Enable'.
- [General] Custom operations: check 'Enabled' for 'Incassomachtiging beheer' under User.
- [General] Custom operations: check 'Enabled' and 'Run self' for 'Saldo opwaarderen (BETA)' under User.
- [General] Custom operations: check 'Enabled' for the five new Record operations for managing directDebit user records.

2. Add permissions to admin groups:

'Administrateurs - Netwerk' Group:
- [System] System records: remove the Create, Edit and Remove permissions for the eMandate and Incassobestand system records.
- [System] Run system custom operations: Enable the 'Download PAIN.008 incassobestand' custom operation.
- [User management] Profile fields of other users: set 'Incassomachtiging vergrendeling' to Visible.
- [User management] Add / remove individual products: set 'Incassomachtiging (eMandate)' to checked.
- [User management] Run custom operations over users: add 'Incassomachtiging Beheer'.
- [User data] User records: Set the new Incasso record to 'View'.

'Administrateurs - Financieel' Group:
- [System] System records: Set the new Incassobestand record to 'View', remove the 'View' checkbox for the 'XML' field.
- [System] Run system custom operations: Enable the 'Download PAIN.008 incassobestand' custom operation.
- [User management] Add / remove individual products: set 'Incassomachtiging (eMandate)' to checked.
- [User management] Run custom operations over users: add 'Incassomachtiging Beheer'.
- [User management] Run custom operations over users: Enable the five new Record operations for managing directDebit user records.
- [User data] User records: Set the new Incasso record to 'View'.

## Deployment Tasks for release 1.4.7

### Scripts

1. Type: Library
- Name: utils Library
- Script code: paste the contents of scripts/utilsLibrary.groovy

2. Type: Custom operation
- Name: Bulk actie IBAN conventies
- Run with all permissions: Yes
- Included libraries: utils Library
- Script code: paste the contents of scripts/bulkIbanConventions.groovy.

3. Type: Extension point
- Name: ensure IBAN Conventions
- Run with all permissions: Yes
- Included libraries: utils Library
- Script code executed when the data is saved: paste the contents of scripts/changeUserCheckIbanPattern.groovy.

### Custom operations

1. Bulk IBAN conventions
- Name: Bulk actie IBAN conventies
- Enabled for channels: Main
- Scope: Bulk action
- Script: Bulk actie IBAN conventies

### Bulk actions

1. Run a bulk action on all Bedrijven and Particulieren users to ensure their iban complies to our conventions:
- Add the permission to run the 'Bulk actie IBAN conventies' custom operation on other users to the Network admin Group.
- Go to Users > [Management] Bulk actions > Run new > 'Bulk actie IBAN conventies'. Filter the users:
- Groups: select all C3NL community Groupsets.
- Status: select Active, Access blocked, Pending validation, Disabled.
Click the 'Run over all xx users from search'. The number of affected users should be around 100.

### Extension points

1. IBAN conventions
- Name: ensure IBAN conventions
- Type: User
- Groups: Select all Bedrijven, all Netwerkbouwers and all Particulieren groups.
- Events: Select 'Create' and 'Update'.
- Script: ensure IBAN Conventions

### Profile fields

1. Set the 'Unique' property on the IBAN user profile field:  
- Go to System > [User configuration] 'Profile fields': 'IBAN' and set the 'Unique' property to Yes.

### Workarounds

Some existing users share the same IBAN. If this is legitimate and there is no possibility for them to use different IBANs, the financial admin could consider giving one of them a fake IBAN. They should use TEST as the bank code to clearly indicate that the IBAN is not a real IBAN. This way, the user will not be able to buy or swap circular euro's, but at least their user profile will be valid, so updating the user profile is possible without errors.

## Deployment Tasks for release 1.4.6

### Profile fields

1. Remove the required property for the Date of Birth user profile field:  
- Go to System > [User configuration] 'Profile fields': 'Geboortedatum' and uncheck the 'Required' property.

## Deployment Tasks for release 1.4.5

### Scripts

1. Type: Custom field validation
- Name: check IBAN
- Run with all permissions: No
- Script code: paste the contents of scripts/customFieldValidation_Iban.groovy.

2. Type: Custom operation
- Name: Bulk action check user profiles
- Run with all permissions: Yes
- Script code: paste the contents of scripts/bulkCheckUserProfiles.groovy.

### Profile fields

1. Change the existing 'K.v.K. nummer' profile field:
- Pattern: 00000000

2. Change the existing 'IBAN' profile field:
- Min / max length: remove the minimum of 18 characters. It is no longer needed now we have a validation script and would refuse Belgian ibans without spaces.
- Validation script: check IBAN

### Custom operation

1. Name: Bulk actie deelnemer controles
- Enabled for channels: Main
- Scope: Bulk action
- Script: Bulk action check user profiles

### Groups

1. Group 'Network Administrators', tab Permissions:
- Run custom operations over users: Add 'Bulk actie deelnemer controles'

## Deployment Tasks for release 1.4.1
1. Remove the option for users to buy extra units during registration:

	- Go to Systeem > [Gebruikers configuratie] Producten > 'Algemeen voor iedereen (behalve UE)' > 'Aanpassen' > [Algemeen] 'Mijn profiel velden': Uncheck the value 'Ingeschakeld' for 'Aankoop saldo'.

2. Remove the aankoopsaldo and total lines from the warning when a user opens the activationmail without having paid first:

	- Go to Systeem > Rapporten > [Systeem records] Molly configuration (keep safe) > 'Schermmelding aan bezoeker bij openen link activatiemail zonder betaling'. Remove the lines:  
	Aankoopsaldo: #aankoop#  
	Totaal: #totaal#

3. Adjust the Mollie description visible in the IDEAL screen (mollie_payment.description):

	- Go to System > Scripts and edit the mollie library script parameters (paste from cyclos_configuration\scripts\mollie.properties).

4. Adjust the contents of the registration e-mail:

	- Go to Content > Systeemvertaling > Circuit Nederland > Content Management > Emails > 'Content management > E-mails > activated.body.singlePrincipal'. Remove the text from 'Let op'.

5. Create a new payment type from Debiet to Circuit Beheer:

	- Go to Systeem > [Account configuratie] Rekeningtypen > Debiet rekening > Tab Betalingstypen > Nieuw > Choose 'Betalingstype'. Fill in the form:  
	Naam			: Storting lidmaatschapsbijdrage  
	Interne naam	: lidmaatschapsbijdrage  
	Kanalen			: Check the 'Web services' channel (besides the already checked Main channel).

6. Instead of creating two transactions to/from the user, create one transaction directly from debit to Circuit beheer for the contribution:

	- Go to Systeem > [Operaties] Scripts: click the existing 'mollie' library script and change:  
	Script parameters: Paste the contents of cyclos_configuration\scripts\mollie.properties.  
	Script code uitgevoerd wanneer de operatie wordt uitgevoerd: Paste the contents of cyclos_configuration\scripts\mollie.groovy in the textarea field.

7. Remove the option for users to topup (see the 1.3.0 changelog for the previous settings of the topup operation):

	- Go to Content > Menu en pagina's > Default for Nederland > Click Toevoegen, choose 'Contentpagina'. Fill in the form:  
	Label	: Opwaarderen  
	Content	: {fill in the explanation text}  
	Take note of the pageId in the URL, right above the Content area. For example: 6745336155068047248

	- Go to Systeem > [Operaties] Operaties > Opwaarderen. Change the following fields:  
	Script			: Menupagina's als custom operatie (instead of: topup)  
	Scriptparameters: pageId = 6745336155068047248 (use the pageId from the 'Contentpagina' above)  
	Resultaattype	: RTF (instead of: Externe redirect)  
	Informatie tekst: empty (instead of the old text explaining iDEAL and the minimum amount of 25 euro's)  
	Tab Formuliervelden: remove the old Bedrag field.

## Deployment Tasks for release 1.4.0
1. Changes to add custom period and year options to the MT940 export functionality:  
	- Go to Systeem > [Operaties] Scripts: 'Toevoegen'. Choose 'Operatie'. Fill in the form for creating a new script:  
	Naam									: MT940 selecteer periode  
	Uitgevoerd met alle permissies			: Uncheck  
	Script code uitgevoerd wanneer de operatie wordt uitgevoerd: Paste the contents of cyclos_configuration\scripts\mt940_select_period.groovy in the textarea field.

	- Go to Systeem > [Operaties] Scripts: click the existing 'MT940 export' script and change:  
	Script code uitgevoerd wanneer de operatie wordt uitgevoerd: Paste the contents of cyclos_configuration\scripts\mt940_export.groovy in the textarea field.

	- Go to Systeem > [Operaties] Scripts: click the existing 'MT940 genereer periodes' script and change:  
	Uitgevoerd met alle permissies			: Uncheck  
	Parameters	: Paste the contents of cyclos_configuration\scripts\mt940_generate_periods.properties in the textarea field.  
	Script code uitgevoerd wanneer de operatie wordt uitgevoerd: Paste the contents of cyclos_configuration\scripts\mt940_generate_periods.groovy in the textarea field.

	- Go to Systeem > [Operaties] Operaties: Toevoegen. Fill in the form for creating a new operation:  
	[Detail tab]:  
	Naam			: MT940 Export  
	Interne naam	: mt940_export  
	Label			: Selecteer periode  
	Ingeschakeld voor kanalen: select 'Main'  
	Omvang			: Interne  
	Script			: MT940 export  
	Resultaattype	: Bestand downloaden  
	
	[Formuliervelden tab]:  
	Create the following two 'formuliervelden':  
		Weergegeven naam: Begin/Eind  
		Interne naam: begin/end  
		Datatype: Datum  
		Verplicht: Check this  

	- Go to Systeem > [Operaties] Operaties: open the existing 'Transacties exporteren (MT940)' and 'Transacties kredietrekening exporteren (MT940)' operations and change:  
	[Detail tab]:  
	Script			: Change 'MT940 export' into 'MT940 selecteer periode'  
	Resultaattype	: Change 'Bestand downloaden' into 'Onopgemaakte tekst'  
	[Acties tab]: Add a new Actie (Toevoegen) and fill in: 'MT940 Export'. Click on the checkbox next to both Begin and End parameters and choose 'Gedefinieerd door het script'.

## Deployment Tasks for release 1.3.0
1. Add a new operation for the topup functionality:
	- Go to Systeem > [Operaties] Scripts: 'Toevoegen'. Choose 'Operatie'. Fill in the form for creating a new script:
	Naam									: topup  
	Uitgevoerd met alle permissies			: Keep checked  
	Maak gebruik van bibliotheek (scripts)	: mollie  
	Script code uitgevoerd wanneer de operatie wordt uitgevoerd: Paste the contents of cyclos_configuration\scripts\topup.groovy in the textarea field.
	Script code uitgevoerd wanneer de externe website de gebruiker terug naar Cyclos stuurt: Paste the contents of cyclos_configuration\scripts\topup_redirect.groovy in the textarea field.

	- Go to Systeem > [Operaties] Operaties: Toevoegen. Fill in the form for creating a new operation:  
	[Detail tab]:  
	Naam			: Opwaarderen  
	Interne naam	: topup  
	Beschrijving	: Met deze operatie kan een gebruiker zijn saldo opwaarderen.  
	Label			: Saldo opwaarderen  
	Ingeschakeld voor kanalen: select 'Main' and 'Mobiele App'  
	Pictogram		: Select the creditcard icon on the second row  
	Aangepaste toestemmingslabel: Opwaarderen  
	Omvang			: Gebruiker  
	Script			: topup  
	Resultaattype	: Externe redirect  
	Hoofdmenu		: Geldzaken  
	Gebruikers management sectie: Geldzaken  
	Enabled for active users: Check this  
	Enabled for pending users: Uncheck this  
	Informatie tekst: (Fill in the text from the test environment, using HTML-mode to get the correct font-color.)
	
		[Formuliervelden tab]:  
		Create the following 'formulierveld':
			Weergegeven naam: Bedrag  
			Interne naam: amount  
			Datatype: Decimale  
			Decimalen: 2  
			Verplicht: Check this  
			Bereik: 25,00 tot 10000,00  

	- Go to Systeem > [Gebruikers configuratie] Producten > 'Algemeen (handelsrekening)'. Under [Algemeen] change 'Operaties' so 'Opwaarderen' has both 'Geactiveerd' and 'Uitvoeren op mezelf' checked.

2. Set the payment_id field to be unique within idealDetail userrecords:
	- Go to Systeem > [Systeemconfiguratie] Recordtypen > 'iDEAL transacties' and click the 'Fields' tab. Open the paymentId field and check its 'Uniek' property.

3. Set the payment_id profilefield to be unique:
	- Go to Systeem > [Gebruikers configuratie] Profielvelden > 'Payment id'. Check its 'Uniek' property.

## Deployment Tasks for release 1.2.0
1. Add a field to the alternativePaymentValidation Operation:
	- Go to Systeem > [Operaties] Operaties > 'Validatie afwijkende betaalmethode'. Click the tab 'Formuliervelden' > 'Toevoegen'. Fill in the form for creating a new form field:  
	Naam			: Algemene voorwaarden zijn door gebruiker al geaccepteerd  
	Interne naam	: agreements_accepted  
	Datatype		: Boolean  
	Informatietekst	: Vink dit hokje alleen aan als de algemene voorwaarden en automatische incasso door de gebruiker zelf al zijn geaccepteerd op dit moment, bijvoorbeeld middels een handtekening op een papieren formulier dat in het archief op te zoeken is. Is dit niet zo, laat het hokje dan leeg; de gebruiker krijgt dan bij de eerste inlogpoging alsnog de mogelijkheid om de voorwaarden te accepteren.

2. Change the system translation message when a user clicks the activation link again after having activated already:
	- Go to Content > [Content Beheer] Systeemvertaling > Circuit Nederland.  
	Search for Vertaalsleutel: wrongKey  
	This should give 1 result: USERS.USERS.validationKey.wrongKey  
	Change the translation of this key from: Verkeerde of verlopen validatie key. Neem alsjeblieft contact op met de administratie.  
	into: Verkeerde of verlopen validatie key. Misschien heb je jouw account al geactiveerd? Als je account al geactiveerd is, heb je daar een e-mail over ontvangen. Neem anders alsjeblieft contact op met de administratie.

3. Add validation to the aankoop saldo profilefield:
	- Go to Systeem > [Gebruikers configuratie] Profielvelden > Aankoop saldo. Fill in the 'Bereik van toegestane waarden' from 0,00 to 10000,00.

## Deployment Tasks for release 1.1.1
1. Create a new operation script on pending users:
	- Go to Systeem > [Operaties] Scripts: Toevoegen. Choose 'Operatie'. Fill in the form for creating a new script:  
	Naam									: Validatie afwijkende betaalmethode  
	Uitgevoerd met alle permissies			: Keep checked  
	Maak gebruik van bibliotheek (scripts)	: mollie  
	Script code uitgevoerd wanneer de operatie wordt uitgevoerd: Paste the contents of cyclos_configuration\scripts\alternativePaymentValidation.groovy in the textarea field.

	- Go to Systeem > [Operaties] Operaties: Toevoegen. Fill in the form for creating a new operation:  
	[Detail tab]:  
	Naam		: Validatie met afwijkende betaalmethode 
	Interne naam	: alternativePaymentValidation  
	Beschrijving	: Met deze operatie kan een financieel beheerder een gebruiker valideren die niet via iDEAL heeft betaald maar op een andere manier.  
	Label		: Valideren met afwijkende betaalmethode  
	Ingeschakeld voor kanalen: select 'Main' only  
	Aangepaste toestemmingslabel: Gebruiker valideren  
	Omvang		: Gebruiker  
	Script		: Validatie afwijkende betaalmethode  
	Resultaattype: URL  
	Hoofdmenu	: Gebruikers  
	Gebruikers management sectie: Gebruikersbeheer  
	Enabled for active users: Uncheck this  
	Enabled for pending users: Check this  
	Informatie tekst: Fill in the text from the test environment, using HTML-mode to get the correct font-color.
	
		[Formuliervelden tab]:  
		Create the following 'formuliervelden':
		- E-mail (email), Gemiddeld, Verplicht
		- IBAN (iban), Gemiddeld, Verplicht
		- Naam rekeninghouder (accountName), Gemiddeld, Verplicht
		- Bedrag (amount), Decimale, Verplicht
		- Betaalmethode (method), Enkelvoudige selectie, Veldtype Knop, Verplicht  
			-> After saving, create the following 'Waarden' for this field:  
			Reeds handmatig overgeboekt (handmatig), Standaard  
			Reeds betaald via pin (pin)  
			Reeds geïncasseerd (via incasso) (incasso)  
			Reeds cash betaald (kopie bankpas is gemaakt!) (cash)  

	- Go to Systeem > [Gebruikers configuratie] Producten > 'Algemeen (voor iedereen)'. Under [Algemeen] change 'Operaties' so 'Valideren met afwijkende betaalmethode' is set to 'Geactiveerd'.

	- Go to Systeem > [Gebruikers configuratie] Groepen > 'Administrateurs C3-Nederland (Netwerk)'. At the Permissies tab change [Gebruikerbeheer] 'Uitvoeren operaties (op gebruikers)' so 'Valideren met afwijkende betaalmethode' is checked.  
	Do the same for the 'Administrateurs financieel - Circuit Nederland' group.

## Deployment Tasks for release 1.1.0
1. Adjust registration_strings.php to reflect the changes in registration_strings-sample.php:

	- Add 'RECAPTCHA_SECRET' constant.

	Belongs to #1.

2. Adjust the information-text in the Profilefield 'Aankoop_saldo' to no longer mention a maximum value of 150 euro:

	- Go to Systeem > [Gebruikers configuratie] Profielvelden > 'Aankoop saldo' field > 'Informatietekst' and remove the text in brackets: "particulieren mogen maximaal voor 150 euro per keer aan @nder geld kopen".

	Belongs to #7.

3. Change several fields of the mollyConnect systemrecord:

	a. Change the system recordtype: Go to Systeem > [Systeemconfiguratie] Recordtypen > 'Molly configuration (keep safe)':

	- Change the fields so they are the same as on testcyclos (see separate list).

	Note: The maxAankoopPart ('Maximaal aankoop bedrag particulieren') field can be removed. This is only allowed by Cyclos programmers, so instead you can rename its internalName into something like 'maxAankoopPart_to_be_deleted'.

	b. Change the contents of the fields of the systemrecord: Go to Rapporten > 'Systeem records' > 'Molly configuration (keep safe)':

	- Change the contents of the 'BasisURL van het registratieformulier' into the URL of the registrationform on the environment you are deploying to.
	- Change the contents of the other fields so they are the same as on testcyclos (see separate list).

	Belongs to #7 and #5.

4. Create a new extension script 'createUser' on creating new users:

	- Go to Systeem > [Operaties] Scripts: Toevoegen. Choose 'Extensie'. Fill in the form for creating a new script:  
	Naam									: createUser  
	Uitgevoerd met alle permissies			: Keep checked  
	Maak gebruik van bibliotheek (scripts)	: mollie  
	Script code uitgevoerd wanneer de gegevens worden opgeslagen: Check this  
	Paste the contents of cyclos_configuration\scripts\createUser.groovy in the textarea field that appears.
	
	- Go to Systeem > [Operaties] Extensies: Toevoegen. Choose 'Gebruiker'. Fill in the form for creating a new extension:  
	Naam: createUser  
	Groepen: Select the Bedrijven and Particulieren groups of Arnhems Hert, Brabantse Parel, Circuit Nederland, De Groninger Gulden, Eurijn, Locoo, Utrechtse Euro, Vix and Zwolse Pepermunt (18 in total).  
	Acties: Select 'Aanmaken'  
	Script: Select 'createUser'

	- Go to Systeem > [Operaties] Extensies > activateUserCheckPayment.  
	Groepen: Add the missing groups (Arnhems Hert and Locoo) so they are the same 18 as above.

	Belongs to #5

5. Create a new profile field 'Payment URL':

	- Go to Systeem > [Gebruikers configuratie] Profielvelden > Nieuw. Fill in the values as:  
	Weergegeven naam	: Payment URL  
	Interne naam		: payment_url  
	Grootte				: Gemiddeld  
	Standaard verborgen	: Aangevinkt
	
	- Go to Systeem > [Gebruikers configuratie] Producten > Algemeen (voor iedereen) > [Algemeen] Mijn profiel velden. Check the value 'Ingeschakeld' for the new 'Payment URL' field.
	
	- Go to Systeem > [Gebruikers configuratie] Groepen > 'System_admin_registration' > tab Permissies > [Gebruikerbeheer] 'Profiel velden van andere gebruikers'. 	Check the value 'Zichtbaar' for the new 'Payment URL' field.  
	Also check the value 'Zichtbaar' for the 'Email' field.

	- Go to Systeem > [Gebruikers configuratie] Groepen > 'Administrateurs C3-Nederland (Netwerk)' > tab Permissies > [Gebruikerbeheer] 'Profiel velden van andere gebruikers'. Check the value 'Zichtbaar' for the new 'Payment URL' field.

	Belongs to #5

6. Create a new script for generating a custom validation link:
	
	- Go to Systeem > [Operaties] Scripts: Toevoegen. Choose 'Genereren link'. Fill in the form for creating a new script:  
	Naam									: Generate custom validationlink  
	Maak gebruik van bibliotheek (scripts)	: (leave empty)   
	Parameters: Paste the contents of cyclos_configuration\scripts\generateValidationLink.properties in this textarea.  
	Scriptcode: Paste the contents of cyclos_configuration\scripts\generateValidationLink.groovy in this textarea.

	- Go to Systeem > [Systeemconfiguratie] Configuraties > 'Default for Nederland': [Sectie 'Tonen'] 'Genereren link'. Click the 'Aanpassen' button next to it. 	Change the 'Link generatie script' to 'Generate custom validationlink'.  
	This leads to a new setting 'Link generatie script parameters'. Leave that empty.  
	Save the changes.

	Belongs to #5

7. Adjust the contents of the mollie library script (paste it from cyclos_configuration\scripts\mollie.groovy) and its parameters (paste from cyclos_configuration\scripts\mollie.properties.

	Adjust the contents of the activateUserCheckPayment script (paste it from cyclos_configuration\scripts\activateUserCheckPayment.groovy). Empty the script parameters of the activateUserCheckPayment script (they have been moved to the mollie library script).

	Belongs to #5

8. Change translation:

	- Go to Content > Systeemvertaling > Circuit Nederland > [Gebruikers] Gebruikers > validationKey.wrongKey and change it from:  
	'Verkeerde validatie key.'  
	into:  
	'Verkeerde of verlopen validatie key. Neem alstublieft contact op met de administratie.'

9. Allow creating the required transactions from the Web services channel (needed because validation is now called via the REST api instead of via the Cyclos URL):

	- Go to Systeem > [Account configuratie] Rekeningtypen > Debiet rekening > tabje 'Betalingstypen' > 'Aankoop Units (Circuit Nederland)'. In 'Kanalen' add 'Web services'.

	- Go to Systeem > [Account configuratie] Rekeningtypen > Handelsrekening > tabje 'Betalingstypen' > 'Lidmaatschapsbijdrage'. In 'Kanalen' add 'Web services'.

10. Add fields to the user recordtype idealDetail:

	- Go to Systeem > [Systeemconfiguratie] Recordtypen > 'iDEAL transacties' (Interne naam: 'idealDetail'). Add the following fields (under 'Aangepaste velden' click 'Nieuw'):  
		- paymentId, method (Enkele tekst regel)
		- transaction ('Verbonden velden' with 'Gelinkt eniteitstype': 'Transactie')
		- amount (Decimale)
		- paid (Boolean)
		- source (Enkelvoudige selectie). After saving add 'Waarden (opties)': 'registration' and 'topup'. After saving the waarde, also add the internal name with the same value.

		Only fill in the 'Weergegeven naam' and 'Interne naam' with these values and set the Datatype. Leave all other settings on their defaults.

	- Make the new fields visible and not changeable for admin groups:  
		- Go to System > [Gebruikers configuratie] Groepen > 'Administrateurs C3-Nederland (Netwerk)', Tab 'Permissies'. Change the ['Gebruikergegevens'] 'Gebruikers records' so the new fields under 'iDEAL transacties' have 'Bekijken' selected and 'Nieuw' and 'Aanpasbaar' not selected.
		- Go to System > [Gebruikers configuratie] Groepen > 'Administrateurs financieel - Circuit Nederland', tab 'Permissies'. Change the ['Gebruikergegevens'] 'Gebruikers records' so the new fields under 'iDEAL transacties' have 'Bekijken' selected.

		Belongs to #12

11. Add a new custom web service for the mollie webhook:

	- Go to Systeem > [Operaties] Scripts and click 'Toevoegen' > 'Custom web service'. Fill in:  
	Naam: mollieWebhook  
	Uitgevoerd met alle permissies: (leave this checked)  
	Maak gebruik van bibliotheek (scripts): check 'mollie'  
	Scriptcode: paste the contents of cyclos_configuration\scripts\mollieWebhook.groovy in this textarea.

	- Go to Systeem > [Operaties] 'Custom web services' and click 'Toevoegen'. Fill in:  
	Naam: mollie webhook  
	Interne naam: mollieWebhook  
	Beschrijving: mollie contacteert dit met een post request na elke wijziging in payment status. De webhook service wordt gebruikt om het veld "betaald" te updaten.  
	Http-methode: POST  
	Uitvoeren als: Gast  
	Script: Select 'mollieWebhook'  
	Url-toewijzing: paid

12. Change settings:

	- Go to Systeem > [Gebruikers configuratie] Groepen > 'Administrateurs financieel - Circuit Nederland' > tab 'Permissies' > [Gebruikerbeheer] 'Profiel velden van andere gebruikers'. Deselect 'Aanpasbaar' for the 'Betaald' field.
	- Do the same for the groups 'Administrateurs Circuit Nederland (gebruikers en content beheer)' and 'Administrateurs C3-Nederland (Netwerk)'.
	- Go to Systeem > [Gebruikers configuratie] Groepen > 'System_admin_registration' > tab 'Permissies' > [Gebruikerbeheer] 'Profiel velden van andere gebruikers'. Select 'Zichtbaar' for the 'Gebruikersnaam' field. Deselect 'Zichtbaar' and 'Aanpasbaar' for the 'Payment id' field. Deselect 'Zichtbaar' for the 'Betaald' field.
	- Go to Systeem > [Gebruikers configuratie] Producten > 'Algemeen (voor iedereen)' > [Algemeen] 'Mijn profiel velden'. Deselect 'Bij inschrijving' for the 'Payment id' field.
	- Remove group registration settings:
		- Go to Systeem > [Gebruikers configuratie] Groepen > 'Circuit Nederland - Bedrijven'. On the 'Details' tab, empty the fields 'Naam op registratie' and 'Beschrijving op registratie'.
		- Do the same for: 'Circuit Nederland - Particulieren', 'Zwolse Pepermunt - Bedrijven' and 'Zwolse Pepermunt - Particulieren'.
	- Go to Syseem > [Gebruikers configuratie] Profielvelden. For the following profilefields change their 'Informatie tekst' so 'u/uw' is converted to 'je' and '@nder geld' is changed to 'circulair geld' (can be copied from test): IBAN, Website, K.v.K. nummer, Diensten/producten, Lid van een broodfonds, Klant bij Triodos, Lidmaatschapsbijdrage particulieren, Lidmaatschapsbijdrage bedrijven, Aankoop saldo.
	- Change to the global environment (click 'Overschakelen naar Globale administratie' ) and there go to System > ['User configuration'] 'Password types' > 'Login password'. Change the contents of the 'Public description' field replacing 'u/uw' by 'je' (can be copied from test).
