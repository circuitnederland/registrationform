Setup Registration C3NL
==

We use the Cyclos wizard functionality for the Circuit Nederland (C3NL) registration process.

# I. Preparation phase

# Scripts

1. Type: Wizard
- Name: registration Wizard
- Included libraries: eMandates Library, utils Library
- 'Script code executed when the wizard finishes': paste the contents of Wizard_Finish.groovy.
- 'Script code executed on transitions between steps': paste the contents of Wizard_PrefillData_StepTransition.groovy.
- 'Script code executed before the user is redirected to an external site': paste the contents of Wizard_ExternalRedirect.groovy.
- 'Script code executed when the external site redirects the user back to Cyclos': paste the contents of Wizard_Callback.groovy.

2. Type: Custom web service
- Name: registration Wizard Preparation
- Script code: paste the contents of WebService_PrepareRegistrationWizard.groovy.

3. Type: Load custom field values
- Name: restrict community groups in registration
- Run with all permissions: No
- Script code that returns the possible values when either creating or editing an entity: paste the contents of LoadCustomFieldValues_WizardType.groovy.

4. Type: Custom field validation
- Name: check DateOfBirth
- Run with all permissions: No
- Included libraries: utils Library
- Script code: paste the contents of FieldValidation_DateOfBirth.groovy.

# Text messages

Add a new field to the system record 'Text messages':
- Display name: Registration Too Young
- Internal name: regTooYoung
- Data type: Multiple line text
- Ignore value sanitization: Yes

Add a new Section to the system record 'Text messages': 'Registration' and add the new regTooYoung field to it.

After this, go to System > Text messages and fill in the desired text message in the new regTooYoung field, using #minimumAge# as a dynamic variable.

# Profile fields

Change the Information text of profile fields:
- Website: remove the remark about required http(s), which is no longer needed now we use the wizard.

Change the existing 'Aankoop saldo' profile field:
- Information text: (use text as decided on by stakeholders)
- Allowed values range: change the maximum value from 10000,00 to 50,00.

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

7. Authorized signatories Companies:
- Display name: Tekeningsbevoegde
- Internal name: authorized_signatory
- Data type: Single line text (= default)
- Required: Yes

8. Date of Birth:
- Display name: Geboortedatum
- Internal name: date_of_birth
- Data type: Date
- Required: Yes
- Validation script: check DateOfBirth
- Validation parameters: minimumAge = 18

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
- Internal name: eMandate_companies
- Description: Step to request an eMandate. For users with an eMandate we can make a direct debit to cash the contribution amount.
- Title: Digitale machtiging
- Information text: (use html with explanatory text as decided on by stakeholders)
- Show only for specific groups: (select all Bedrijven groups)
- Show profile fields: Show specific profile fields
- Profile fields to show: Lidmaatschapsbijdrage, Actiecode, Aankoop saldo
- Show wizard fields: Bank, Tekeningsbevoegde
- Show agreement: Yes
- This step performs an external redirect: Yes

8. Type: Form fields
- Name: eMandate Consumers
- Internal name: eMandate_consumers
- Description: Step to request an eMandate. For users with an eMandate we can make a direct debit to cash the contribution amount.
- Title: Digitale machtiging
- Information text: (use html with explanatory text as decided on by stakeholders)
- Show only for specific groups: (select all Consumer groups)
- Show profile fields: Show specific profile fields
- Profile fields to show: Lidmaatschapsbijdrage, Actiecode, Aankoop saldo
- Show wizard fields: Bank
- Show agreement: Yes
- This step performs an external redirect: Yes

9. Type: Form fields
- Name: Profile fields Companies
- Internal name: profilefields_companies
- Description: Profile fields for companies.
- Title: Registratiegegevens
- Information text: (use html with explanatory text as decided on by stakeholders)
- Show only for specific groups: (select all Bedrijven groups)
- Show profile fields: Show specific profile fields
- Profile fields to show: Contactpersoon Bedrijf, K.v.K. nummer
- Show wizard fields: Geboortedatum

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
- Internal name: contactfields_consumers
- Description: Contact fields for consumers.
- Title: Contactgegevens
- Information text: (use html with explanatory text as decided on by stakeholders)
- Show only for specific groups: (select all Particulieren groups)
- Show profile fields: Show specific profile fields
- Profile fields to show: Phone, Address
- Phone numbers to show: Both mobile and land-line phones
- Show privacy control for fields: Yes
- Show wizard fields: Profielfoto, Geboortedatum

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

# Themes

Go to Content > [Content management] Themes > 'Default for Nederland' > 'Circuit Nederland' theme and add the following to the 'Custom style' section:

```
/* Wizard step Information text color, added July 2022, SB */
.wizardstep {
    color: #333;
}
```

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
- APP.TRANSLATIONS.address-line1: Add a custom translation: "Adres".
- APP.TRANSLATIONS.address-region: Add a custom translation: "Provincie".
- APP.TRANSLATIONS.address-city: Add a custom translation: "Plaats".
- CONTENT_MANAGEMENT.EMAILS.activated.body.template: {Add a line with links to the apps as decided on by stakeholders}

# 2. Go-live phase

# Configuration

Set the registration wizard in the configuration: System > [System configuration] Configurations > 'Default for Nederland', under the [Data visible to guests] section:
- Possible groups for public registration: (select the bedrijven and particulieren groups of the active communities - make sure you don't select broker groups)
- Registration wizard for large screens: Registration wizard
- Registration wizard for medium screens: Registration wizard
- Registration wizard for small screens: Registration wizard

# Changes to left-overs from the old registration process

## Remove extension points

Remove the extension points we used in the old registration process, involving an iDeal/Mollie payment: System > [Tools] > Extension points. Open the following extension points and uncheck the 'Enabled' checkbox:
- activateUserCheckPayment
- createUser
