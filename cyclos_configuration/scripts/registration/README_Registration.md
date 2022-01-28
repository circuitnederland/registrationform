Setup Registration C3NL
==

We use the Cyclos wizard functionality for the Circuit Nederland (C3NL) registration process.

# Scripts

@todo

# Wizard

Create a wizard of type 'Registration form': System > [Tools] Wizards > New > 'Registration form'.

## Details

- Name: Registration wizard (can be changed)
- Internal name: registration *
- Show steps progress: Yes

****Note***: The internal name of the wizard (= 'registration') will be used inside the eMandates script to update the dropdown containing the banks the user can choose from. So, if you must change it here, make sure to change it in the eMandates script as well.

## Custom fields

Create a new custom field:
- Display name: Bank (can be changed)
- Internal name: debtorBank *
- Data type: Single selection
- Required: Yes

****Note***: The internal name of the custom field (= 'debtorBank') will be used inside the eMandates script to update the dropdown containing the banks the user can choose from. So, if you must change it here, make sure to change it in the eMandates script as well.


## Steps

Create the following steps:

1. Type: Form fields
- Name: Intro
- Description: Introduction step, containing explanatory text only.
- Title: Welkom bij Circuit Nederland!
- Information text: (use html with explanatory text as decided on by stakeholders)

2. Type: Group selection
- Name: Group selection
- Title: Kies je community
- Information text: (use html with explanatory text as decided on by stakeholders)

3. Type: Form fields
- Name: E-mail activation
- Title: E-mail verificatie
- Information text: (use html with explanatory text as decided on by stakeholders)
- Show profile fields: Shows specific profile fields
- Profile fields to show: E-mail
- Require e-mail validation: Yes
- Show privacy control for fields: Yes

4. Type: Form fields
- Name: eMandate
- Description: Step to request an eMandate. For users with an eMandate we can make a direct debit to cash the contribution amount.
- Title: Digitale machtiging
- Information text: (use html with explanatory text as decided on by stakeholders)
- Show wizard fields: Bank

5. Type: Form fields
- Name: Basic fields
- Description: Step containing the required basic profile fields like username and password.
- Title: Basisgegevens voor jouw account
- Information text: (use html with explanatory text as decided on by stakeholders)
- Show profile fields: Shows specific profile fields
- Profile fields to show: Full name, Login name
- Show password: Yes
- Show security question: Yes
- Show agreement: Yes

6. Type: Form fields
- Name: Profile fields
- Description: All other profile fields we did not retrieve in previous steps.
- Title: Jouw profiel
- Information text: (use html with explanatory text as decided on by stakeholders)
- Show profile fields: Show all remaining profile fields

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
