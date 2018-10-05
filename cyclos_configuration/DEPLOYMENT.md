# Deployment Tasks per release
Things to do manually in the Cyclos production-environment when deploying a new release of the PHP registrationform to production.

## Deployment Tasks for next release

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
