# Deployment Tasks per release
Things to do manually in the Cyclos production-environment when deploying a new release of the PHP registrationform to production.

## Deployment Tasks for release with improved profile field validations

### Scripts

1. Type: Custom field validation
- Name: check IBAN
- Run with all permissions: No
- Script code: paste the contents of scripts/customFieldValidation_Iban.groovy.

2. Type: Custom operation
- Name: Bulk action check user profiles
- Run with all permissions: No
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

## Deployment Tasks for next release
1. Enable the scheduled tasks for the eMandates:

	- Go to Systeem > [Operaties] Geplande taken. Open both tasks ('eMandates Check Pending' and 'eMandates Update Banklist') and set them to 'Ingeschakeld': Yes.

2. Create and configure a new temporary Product to give eMandate functionality to specific users (later on we will move those permissions to all users):

	- Go to Systeem > [Gebruikers configuratie] Producten > Nieuw > Gebruiker. Fill in the form:  
	Naam: Incassomachtiging (eMandate)  
	Beschrijving: {copy-paste from testC3NL}.  
	[Algemeen] 'Operaties': check 'Geactiveerd' and 'Uitvoeren op mezelf' for 'Incassomachtiging'.

- Go to Systeem > [Gebruikers configuratie] Groepen > 'Administrateurs - Netwerk'. At the Permissies tab change [Gebruikerbeheer] 'Toevoegen / verwijderen van afzonderlijke producten' so 'Incassomachtiging (eMandate)' is checked.  

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
