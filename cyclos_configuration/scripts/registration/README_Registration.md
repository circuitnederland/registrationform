Setup Registration C3NL
==

We use the Cyclos wizard functionality for the Circuit Nederland (C3NL) registration process.

# Scripts

1. Type: Wizard
- Name: registration Wizard
- 'Script code executed when the wizard finishes': add // because this code block is required but we don't need it to do anything.
- 'Script code executed on transitions between steps': paste the contents of Wizard_PrefillData_StepTransition.groovy.

2. Type: Custom web service
- Name: registration Wizard Preparation
- Script code: paste the contents of WebService_PrepareRegistrationWizard.groovy.

3. Type: Load custom field values
- Name: contribution scales
- Run with all permissions: No
- Script code that returns the possible values when either creating or editing an entity: paste the contents of ../loadCustomFieldValues_ContributionScales.groovy.

4. Type: Custom field validation
- Name: check DateOfBirth
- Run with all permissions: No
- Script code: paste the contents of ../customFieldValidation_DateOfBirth.groovy.

5. Type: Load custom field values
- Name: restrict community groups in registration
- Run with all permissions: No
- Script code that returns the possible values when either creating or editing an entity: paste the contents of LoadCustomFieldValues_WizardType.groovy.

# Profile fields

Change the Information text of profile fields to use formal instead of informal Dutch:
- Website (also remove the remark about required http(s), which is no longer needed now we use the wizard)
- K.v.K. nummer
- Diensten/producten

Change the existing 'Aankoop saldo' profile field:
- Information text: (use text as decided on by stakeholders)
- Allowed values range: change the maximum value from 10000,00 to 50,00.

Change the existing 'Geboortedatum' profile field:
- Validation script: check DateOfBirth
- Validation script parameters:
```
minimumLeeftijd = 18
teJongMelding = De minimum leeftijd om te kunnen deelnemen is #minimumLeeftijd# jaar.
```

Remove the permission to edit the own profile field for the 'K.v.K. nummer' profile field in the Products 'Algemeen voor bedrijven (behalve UE)' and 'Algemeen United Economy'.

Add a new profile field for authorized signatories: System > [User configuration] 'Profile fields' > New.
- Display name: Tekeningsbevoegde
- Internal name: authorized_signatory
- Required: Yes

After saving the new profile field, use the arrows to move the field up, just above the 'Contactpersoon bedrijf' field.

Add permissions for the new profile field for authorized signatories:
- Group 'Administrateurs C3-Nederland (Netwerk)' > 'Profile fields of other users': Add Visible for 'Tekeningsbevoegde'.
- Group 'Administrateurs financieel - Circuit Nederland' > 'Profile fields of other users': Add Visible and Editable for 'Tekeningsbevoegde'.
- Product 'Algemeen voor bedrijven (behalve UE)' > 'My profile fields': Add Enabled, At registration and Visible for 'Tekeningsbevoegde'.
- Product 'Algemeen United Economy' > 'My profile fields': Add Enabled, At registration and Visible for 'Tekeningsbevoegde'.

Add a new profile field for the contribution: System > [User configuration] 'Profile fields' > New.
- Display name: Lidmaatschapsbijdrage
- Internal name: lidmaatschapsbijdrage
- Data type: Single selection
- Load values script: contribution scales
- Required: Yes
- Include in account history print (PDF): No
- Hidden by default: Yes

After saving the new profile field, add the Possible values for the default company and consumer contribution scales:
- Value: {Use the texts as requested by the business, making sure the amount is the first number in the string}
- Internal name: standaard_bedrijven_1 / standaard_bedrijven_2 etc or standaard_particulieren_1 / standaard_particulieren_2 etc

After creating the new profile field, use the arrows to move the field up, just above the 'Actiecode' field.

Add permissions for the new contribution profile field:
- Group 'Administrateurs C3-Nederland (Netwerk)' > 'Profile fields of other users': Add Visible, Editable and User filter for 'Lidmaatschapsbijdrage'.
- Group 'Administrateurs financieel - Circuit Nederland' > 'Profile fields of other users': Add Visible, Editable and User filter for 'Lidmaatschapsbijdrage'.
- Product 'Algemeen voor iedereen (behalve UE)' > 'My profile fields': Add Enabled, At registration, Visible and Editable for 'Lidmaatschapsbijdrage'.
- Product 'Algemeen United Economy' > 'My profile fields': Add Enabled, At registration, Visible and Editable for 'Lidmaatschapsbijdrage'.

Remove the permissions for the two old contribution fields that were specific for companies and consumers:
- Product 'Algemeen (voor particulieren)' > 'My profile fields': Remove all permissions for 'Lidmaatschapsbijdrage particulieren'.
- Product 'Algemeen voor bedrijven (behalve UE)' > 'My profile fields': Remove all permissions for 'Lidmaatschapsbijdrage bedrijven'.

Migrate the chosen contribution values from the old profile fields to the new profile field via a set of bulk actions*: Users > [Management] Bulk actions > Run new > 'Change custom field value'. Leave the 'Group' filter to the default member groups, set the 'Status' filter to all statusses. Run several bulk actions like this, each with different options:
- Filter 'Lidmaatschapsbijdrage bedrijven' on '50 - bedrijven met minder dan 10 werknemers' > Set Custom field 'Lidmaatschapsbijdrage' to '50 - bedrijven < 10 werknemers'.
- Filter 'Lidmaatschapsbijdrage bedrijven' on '150 - bedrijven met minder dan 50 werknemers' > Set Custom field 'Lidmaatschapsbijdrage' to '150 - bedrijven < 50 werknemers'.
- Filter 'Lidmaatschapsbijdrage bedrijven' on '300 - bedrijven met 50 of meer werknemers' > Set Custom field 'Lidmaatschapsbijdrage' to '300 - bedrijven > 50 werknemers'.
- Filter 'Lidmaatschapsbijdrage particulieren' on '15' > Set Custom field 'Lidmaatschapsbijdrage' to '15 - minimale bijdrage om de kosten te dekken'.
- Filter 'Lidmaatschapsbijdrage particulieren' on '40' > Set Custom field 'Lidmaatschapsbijdrage' to '40 - met deze optie steunt u ons'.
- Filter 'Lidmaatschapsbijdrage particulieren' on '70' > Set Custom field 'Lidmaatschapsbijdrage' to '70 - met deze optie steunt u ons heel erg'.
- Filter 'Lidmaatschapsbijdrage particulieren' on '100' > Set Custom field 'Lidmaatschapsbijdrage' to '100 - u bent een kanjer'.

Finally, run a bulk action to set the new Lidmaatschapsbijdrage profile field for all United Economy users to their specific value (500): Users > [Management] Bulk actions > Run new > 'Change custom field value':
- Group: 'United Economy' Group set
- Status: All statusses
Set 'Lidmaatschapsbijdrage' to '500 - United Economy bedrijven'.

* Note: The bulk actions to set the new profilefield only work when I temporarily remove the 'Load values script' ('contribution scales) from the profile field and put it back afterwards.

Some users have been moved from a consumer group to a companies group or vice versa. To find them, create a new Bulk action and filter on all 'Bedrijven' groups AND all values for 'Lidmaatschapsbijdrage particulieren'. If you find users, set the Lidmaatschapsbijdrage field so it reflects what they choose in the old field. And do the same vice versa filtering users on all 'Particulieren' groups AND all values for 'Lidmaatschapsbijdrage bedrijven'. On test this resulted in 2 users, for which I set the new Lidmaatschapsbijdrage to the default 40.
And finally, there is one user in United Economy Particulieren. Fix his Lidmaatschapsbijdrage value to 40.
Don't forget to set the Load values script back on the Lidmaatschapsbijdrage profile field after you have run all bulk actions.

# Products

Change the Product 'Algemeen (handelsrekening)':
- My profile fields: Set 'Enabled' and 'At registration' to 'Yes' for the profile field 'Aankoop saldo'.
- Description: add a line to describe the Aankoop saldo field is added at registration.

# Wizard

Create a wizard of type 'Registration form': System > [Tools] Wizards > New > 'Registration form'.

## Details

- Name: Registration wizard (can be changed)
- Internal name: registration *
- Script: registration Wizard

****Note***: The internal name of the wizard (= 'registration') will be used inside the eMandates script to update the dropdown containing the banks the user can choose from. So, if you must change it here, make sure to change it in the eMandates script as well.

## Custom fields

1. Bank:
- Display name: Bank (can be changed)
- Internal name: debtorBank *
- Data type: Single selection
- Required: Yes

****Note***: The internal name of the custom field (= 'debtorBank') will be used inside the eMandates script to update the dropdown containing the banks the user can choose from. So, if you must change it here, make sure to change it in the eMandates script as well.

2. Community:
- Display name: Community (can be changed)
- Internal name: community
- Data type: Single selection
- Size: Medium
- Required: Yes

After saving the Community field, add Possible values for each of the current Group sets, i.e. All United, Arnhems Hert, etc. Open each value after saving and fill in the internal name with the community name without spaces in small caps, i.e. allunited, arnhemshert, utrechtseeuro, etc.

3. Type:
- Display name: Inschrijven als (can be changed)
- Internal name: type
- Data type: Single selection
- Load values script: restrict community groups in registration
- Load values script parameters: {the type to show for each community that should not have both types visible, for example:}
```
allunited = particulieren
unitedeconomy = bedrijven
```
- Size: Medium
- Required: Yes

After saving the Type field, add Possible values 'Bedrijf' (internal name 'bedrijven') and 'Particulier' (internal name 'particulieren').

4. Name Companies:
- Display name: Bedrijfsnaam (can be changed)
- Internal name: company_name
- Data type: Single line text (= default)
- Required: Yes

5. Name Consumers:
- Display name: Volledige naam (can be changed)
- Internal name: consumer_name
- Data type: Single line text (= default)
- Required: Yes

6. Image Companies:
- Display name: Bedrijfslogo (can be changed)
- Internal name: company_image
- Data type: Image

6. Image Consumers:
- Display name: Profielfoto (can be changed)
- Internal name: consumer_image
- Data type: Image

## Steps

Create the following steps (use a surrounding `<div class="wizardstep"></div>` in each Information text):

1. Type: Form fields
- Name: Intro
- Description: Introduction step, containing explanatory text only.
- Title: Welkom bij Circuit Nederland
- Information text: (use html with explanatory text as decided on by stakeholders)

2. Type: Form fields
- Name: Community
- Internal name: community
- Title: Kies uw community
- Information text: (use html with explanatory text as decided on by stakeholders)
- Show wizard fields: Community

3. Type: Form fields
- Name: Type
- Internal name: type
- Title: Schrijft u zich in als bedrijf of als particulier?
- Information text: (use html with explanatory text as decided on by stakeholders)
- Show wizard fields: Type

4. Type: Form fields
- Name: E-mail activation
- Internal name: email
- Title: E-mail verificatie
- Information text: (use html with explanatory text as decided on by stakeholders)
- Show profile fields: Shows specific profile fields
- Profile fields to show: E-mail
- Require e-mail validation: Yes
- Show privacy control for fields: Yes

5. Type: Form fields
- Name: Accountinfo Companies
- Internal name: accountinfo_companies
- Description: Step containing the required account fields like username and password.
- Title: Accountgegevens
- Information text: (use html with explanatory text as decided on by stakeholders)
- Show only for specific groups: (select all Bedrijven groups)
- Show profile fields: Shows specific profile fields
- Profile fields to show: Login name
- Show wizard fields: Bedrijfsnaam
- Show password: Yes
- Show security question: Yes

6. Type: Form fields
- Name: Accountinfo Consumers
- Internal name: accountinfo_consumers
- Description: Step containing the required account fields like username and password.
- Title: Accountgegevens
- Information text: (use html with explanatory text as decided on by stakeholders)
- Show only for specific groups: (select all Particulieren groups)
- Show profile fields: Shows specific profile fields
- Profile fields to show: Login name
- Show wizard fields: Volledige naam
- Show password: Yes
- Show security question: Yes

7. Type: Form fields
- Name: eMandate Companies
- Description: Step to request an eMandate. For users with an eMandate we can make a direct debit to cash the contribution amount.
- Title: Digitale machtiging
- Information text: (use html with explanatory text as decided on by stakeholders)
- Show only for specific groups: (select all Bedrijven groups)
- Show profile fields: Show specific profile fields
- Profile fields to show: Tekeningsbevoegde, Lidmaatschapsbijdrage, Actiecode, Aankoop saldo
- Show wizard fields: Bank
- Show agreement: Yes

8. Type: Form fields
- Name: eMandate Consumers
- Description: Step to request an eMandate. For users with an eMandate we can make a direct debit to cash the contribution amount.
- Title: Digitale machtiging
- Information text: (use html with explanatory text as decided on by stakeholders)
- Show only for specific groups: (select all Consumer groups)
- Show profile fields: Show specific profile fields
- Profile fields to show: Lidmaatschapsbijdrage, Actiecode, Aankoop saldo
- Show wizard fields: Bank
- Show agreement: Yes

9. Type: Form fields
- Name: Profile fields Companies
- Description: Profile fields for companies.
- Title: Registratiegegevens
- Information text: (use html with explanatory text as decided on by stakeholders)
- Show only for specific groups: (select all Bedrijven groups)
- Show profile fields: Show specific profile fields
- Profile fields to show: Contactpersoon Bedrijf, Geboortedatum, K.v.K. nummer

10. Type: Form fields
- Name: Contact fields Companies
- Description: Contact fields for companies.
- Title: Contactgegevens
- Information text: (use html with explanatory text as decided on by stakeholders)
- Show only for specific groups: (select all Bedrijven groups)
- Show profile fields: Show specific profile fields
- Profile fields to show: Phone, Address
- Phone numbers to show: Both mobile and land-line phones
- Show privacy control for fields: Yes

11. Type: Form fields
- Name: Contact fields Consumers
- Description: Contact fields for consumers.
- Title: Contactgegevens
- Information text: (use html with explanatory text as decided on by stakeholders)
- Show only for specific groups: (select all Particulieren groups)
- Show profile fields: Show specific profile fields
- Profile fields to show: Phone, Address, Geboortedatum
- Phone numbers to show: Both mobile and land-line phones
- Show privacy control for fields: Yes
- Show wizard fields: Profielfoto

12. Type: Form fields
- Name: Company profile
- Description: All other profile fields we did not retrieve in previous steps.
- Title: Bedrijfsprofiel
- Information text: (use html with explanatory text as decided on by stakeholders)
- Show only for specific groups: (select all Bedrijven groups)
- Show profile fields: Show specific profile fields
- Profile fields to show: Website, Branche informatie, Diensten/producten
- Show wizard fields: Bedrijfslogo

13. Type: Form fields *
- Name: Ending

* Note: this last empty step is not needed anymore when Cyclos release containing fix for CYCLOS-9653 is deployed to C3NL.

# Custom web service

- Name: registration Wizard Startup
- Description: Start a registration wizard, optionally with community and type already filled in if these are given in the request URL.
- Http method: GET
- Run as: Guest
- Script: registration Wizard Preparation
- Url mappings:  
inschrijven  
inschrijven/{community}/{type}  
inschrijven/{community}


# Configuration

Set the registration wizard in the configuration: System > [System configuration] Configurations > 'Default for Nederland', under the [Data visible to guests] section:
- Possible groups for public registration: (select the bedrijven and particulieren groups of the active communities - make sure you don't select broker groups)
- Registration wizard for large screens: Registration wizard
- Registration wizard for medium screens: Registration wizard
- Registration wizard for small screens: Registration wizard

# Groups

Correct the internal names of all groups that are open for public registration. The internal name should follow the convention {community}_{type}. For example utrechtseeuro_bedrijven, fryskeeuro_particulieren, etc.

# Agreements

Add a new agreement for United Economy:
- Name: Ledencontract United Economy
- Content: {as supplied by UnEc.}

Add the new United Economy agreement to the Product 'Algemeen United Economy'. And also the general 'Algemene Voorwaarden' agreement.

# Themes

Go to Content > [Content management] Themes > 'Default for Nederland' > 'Circuit Nederland' theme and add the following to the 'Custom style' section:

```
/* Wizard step Information text color, added July 2022, SB */
.wizardstep {
    color: #333;
}
```

# Changes to left-overs from the old registration process

## Remove profile fields

We remove profile fields we no longer wish to use:

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
- 'All United - Content beheerders'
    - 'Profile fields of other users': Set 'Enabled' to 'No' for the above fields (this also sets all other columns to No).
    - 'Profile fields in simple users search': uncheck 'Bedrijf'.

Finally, go to System > [User configuration] Profile fields. Click the trash icon for each of the above profile fields to remove it.

## Remove extension points

Remove the extension points we used in the old registration process, involving an iDeal/Mollie payment: System > [Tools] > Extension points. Open the following extension points and uncheck the 'Enabled' checkbox:
- activateUserCheckPayment
- createUser

## Remove unused address field

Remove the region (= Provincie) address field:

Go to System > [System configuration] Configurations > 'Default for Nederland' > [Addresses] 'Enabled address fields': remove the checkbox for 'Region or state'.

## Change group properties

In the groups 'United Economy - Bedrijven' and 'United Economy - Particulieren' remove the property 'Name on registration', which was set to resp. 'Bedrijven' and 'Particulieren'. Make those empty, so the group name is shown instead, in the group selection step in the wizard.

# Workarounds

## Translations

Adjust some of the translations via Content > [Content management] Application translation > Circuit Nederland:

- Translation key USERS.USERS.publicRegistration.securityQuestion.question: change "Vraag" into "Beveiligingsvraag"
- APP.TRANSLATIONS.user-securityQuestion.empty: Change "Laat dit veld leeg" into "Selecteer een optie"
- USERS.USERS.publicRegistration.securityQuestion.leaveBlank: Change "Laat dit veld leeg" into "Selecteer een optie"
- APP.TRANSLATIONS.securityQuestion-answer:  Change "Uw antwoord" into "Antwoord op beveiligingsvraag"
- USERS.USERS.publicRegistration.securityQuestion.answer: Change "Antwoord" into "Antwoord op beveiligingsvraag"
- APP.TRANSLATIONS.field-privacy-public.tooltip: Change "Dit veld is zichtbaar door anderen. Klik op maken." into "Dit veld is zichtbaar voor anderen. Klik op dit icoontje om het privé te maken."
- APP.TRANSLATIONS.field-privacy-private.tooltip: Change "Dit veld is privé. Klik op toestaan om het te bekijken." into "Dit veld is privé. Klik op dit icoontje om het voor andere deelnemers zichtbaar te maken."
- USERS.PHONES.type.LANDLINE: Remove the current translation "Vast telefoonnummer" by clicking the button 'Restore original translation' (which is "Vaste telefoon").
- APP.TRANSLATIONS.phone-landLine: Add a custom translation: "Vaste telefoon".
- APP.TRANSLATIONS.address-line1: Add a custom translation: "Adres".
- APP.TRANSLATIONS.address-poBox: Add a custom translation: "Postbus".
- APP.TRANSLATIONS.address-region: Add a custom translation: "Provincie".
- USERS.ADDRESSES.addressFields.CITY: Add a custom translation: "Plaats".
- APP.TRANSLATIONS.address-city: Add a custom translation: "Plaats".
- APP.TRANSLATIONS.user-title-images: Add a custom translation: "Afbeeldingen".
- CONTENT_MANAGEMENT.EMAILS.activated.body.template: {Add a line with links to the apps as decided on by stakeholders}
