# Deployment Tasks per release
Things to do manually in the Cyclos production-environment when deploying a new release of the PHP registrationform to production.

## Deployment Tasks for next release

1. Adjust registration_strings.php to reflect the changes in registration_strings-sample.php:
- Add 'RECAPTCHA_SECRET' constant.

	Belongs to #1.

2. Adjust the information-text in the Profilefield 'Aankoop_saldo' to no longer mention a maximum value of 150 euro:

	Go to Systeem > [Gebruikers configuratie] Profielvelden > 'Aankoop saldo' field > 'Informatietekst' and remove the text in brackets: "particulieren mogen maximaal voor 150 euro per keer aan @nder geld kopen".

	Belongs to #7.

3. Change several fields of the mollyConnect systemrecord:

	a. Change the system recordtype: Go to Systeem > [Systeemconfiguratie] Recordtypen > 'Molly configuration (keep safe)'.
	Change the fields so they are the same as on testcyclos (see separate list).

	Note:The maxAankoopPart ('Maximaal aankoop bedrag particulieren') field can be removed. This is only allowed by Cyclos programmers, so instead you can rename its internalName into something like 'maxAankoopPart_to_be_deleted'.

	b. Change the contents of the fields of the systemrecord: Go to Rapporten > 'Systeem records' > 'Molly configuration (keep safe)'.
	Change the contents of the 'BasisURL van het registratieformulier' into the URL of the registrationform on the environment you are deploying to.
	Change the contents of the other fields so they are the same as on testcyclos (see separate list).

	Belongs to #7.

4. Create a new extension script 'createUser' on creating new users:

	Go to Systeem > [Operaties] Scripts: Toevoegen. Choose 'Extensie'. Fill in the form for creating a new script:
	Naam									: createUser
	Uitgevoerd met alle permissies			: Keep checked
	Maak gebruik van bibliotheek (scripts)	: mollie
	Script code uitgevoerd wanneer de gegevens worden opgeslagen: Check this
	Paste the contents of cyclos_configuration\scripts\createUser.groovy in the textarea field that appears.
	
	Go to Systeem > [Operaties] Extensies: Toevoegen. Choose 'Gebruiker'. Fill in the form for creating a new extension:
	Naam: createUser
	// @todo: which groups should be selected? The same 14 groups that are selected in the activateUserCheckPayment extension script at this moment?
	Groepen: Select the Bedrijven and Particulieren groups of Bredageld, Circuit Nederland, De Groninger Gulden, Eurijn, Utrechtse Euro, Vix and Zwolse Pepermunt (14 in total).
	Acties: Select 'Aanmaken'
	Script: Select 'createUser'

	Belongs to #5

5. Create a new profile field 'Payment URL':

	Go to Systeem > [Gebruikers configuratie] Profielvelden > Nieuw. Fill in the values as:
	Weergegeven naam	: Payment URL
	Interne naam		: payment_url
	Grootte				: Gemiddeld
	Standaard verborgen	: Aangevinkt
	
	Go to Systeem > [Gebruikers configuratie] Producten > Algemeen (voor iedereen) > [Algemeen] Mijn profiel velden:
	Check the value 'Ingeschakeld' for the new 'Payment URL' field.
	
	Go to Systeem > [Gebruikers configuratie] Groepen > 'System_admin_registration' > tab Permissies > [Gebruikerbeheer] 'Profiel velden van andere gebruikers':
	Check the value 'Zichtbaar' for the new 'Payment URL' field.
	Also check the value 'Zichtbaar' for the 'Email' field.

	Go to Systeem > [Gebruikers configuratie] Groepen > 'Administrateurs C3-Nederland (Netwerk)' > tab Permissies > [Gebruikerbeheer] 'Profiel velden van andere gebruikers':
	Check the value 'Zichtbaar' for the new 'Payment URL' field.

	Belongs to #5

6. Create a new script for generating a custom validation link:
	
	Go to Systeem > [Operaties] Scripts: Toevoegen. Choose 'Genereren link'. Fill in the form for creating a new script:
	Naam									: Generate custom validationlink
	Maak gebruik van bibliotheek (scripts)	: 
	Parameters: Paste the contents of cyclos_configuration\scripts\generateValidationLink.properties in this textarea.
	Scriptcode: Paste the contents of cyclos_configuration\scripts\generateValidationLink.groovy in this textarea.


	Go to Systeem > [Systeemconfiguratie] Configuraties > 'Default for Nederland': [Sectie 'Tonen'] 'Genereren link'. Click the 'Aanpassen' button next to it.
	Change the 'Link generatie script' to 'Generate custom validationlink'. This leads to a new setting 'Link generatie script parameters'. Leave that empty.
	Save the changes.

	Belongs to #5

7. Adjust the contents of the mollie library script (paste it from cyclos_configuration\scripts\mollie.groovy) and its parameters (paste from cyclos_configuration\scripts\mollie.properties, adjusting paths as necessary for the environment you are deploying to).

	Belongs to #5

8. Change translation:
	Go to Content > Systeemvertaling > Circuit Nederland > [Gebruikers] Gebruikers > validationKey.wrongKey and change it from:
	'Verkeerde validatie key.'
	into:
	'Verkeerde of verlopen validatie key. Neem alstublieft contact op met de administratie.'

9. Allow creating the required transactions from the Web services channel (needed because validation is now called via the REST api instead of via the Cyclos URL):
	Go to Systeem > [Account configuratie] Rekeningtypen > Debiet rekening > tabje 'Betalingstypen' > 'Aankoop Units (Circuit Nederland)'. In 'Kanalen' add 'Web services'.
	Go to Systeem > [Account configuratie] Rekeningtypen > Handelsrekening > tabje 'Betalingstypen' > 'Lidmaatschapsbijdrage'. In 'Kanalen' add 'Web services'.

