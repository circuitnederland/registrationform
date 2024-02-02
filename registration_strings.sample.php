<?php
/**
 * File with all language strings. 
 */

/**
 * The base url for your community instance of cyclos. 
 */
define('BASE_URL', "https://your-cyclos-domain.com");

/* Authorization code is always: "Authorization: Basic <code>==", where
 * <code> is the string <username>:<password> encoded according to base64 ecoding.
 * Username and password must of course belong to an admin which has permission to
 * retrieve this information.
 * You can encode your string via https://www.base64encode.org/
 */
define('CYCLOS_ACCESS', "Access-Client-Token: abcd1234");

/*
 * The secret key for Google's recaptcha.
 */
define('RECAPTCHA_SECRET', 'xxx');

/**
 * The language function. Pass a key and the translated string will be returned. 
 * @param string $phrase the key under which the translated string is stored
 * @return string the translated string. If not found, the origingal phrase is returned, pre- and postfixed by three question marks. 
 */
 
function lang($phrase){
	static $lang = array(
			'bedrijfpart'		    => "Zakelijk of Particulier?",
			'betaling' 				=> "Betaling",
			'click.here'			=> "Klik hier",
			'confirm'				=> "bevestigen",
			'connect.cyclos' 		=> "Verbinden met United Economy...",
			'error.400'				=> "Technische fout: het formaat van de gegevens bevat een fout.",
			'error.401'				=> "Geen toegang: ontbrekende of onjuiste inloggegevens...", 
			'error.403'				=> "Geen permissie voor deze operatie.",
			'error.404'				=> "Niet gevonden...",
			'error.422'				=> "Http error 422",
			'error.500'				=> "Onverwachte fout.",
			'error.betaald'			=> "Niet echt een fout, maar volgens onze administratie heeft u inmiddels al wel betaald. Alles gaat dus goed, en u kunt (opnieuw) proberen te activeren door de link in de al eerder ontvangen activatie-mail te klikken.",
			'error.captcha.bold'	=> "Geen connectie",
			'error.captcha' 		=> "Kon de captcha-informatie niet ophalen van de server, waardoor het formulier niet verder verwerkt kan worden. Probeer het later nog eens.",
			'error.captchaForgotten.bold' => "Captcha-validatie",
			'error.captchaForgotten'	=> "U bent vergeten het vakje \"I'm not a robot\" aan te vinken.",
			'error.recaptcha'		=> "Vergeet niet bij het opnieuw versturen ook het vakje \"I'm not a robot\" weer aan te vinken en het wachtwoord opnieuw op te geven.",
			'error.contact'			=> "Heeft u nog vragen? Neem dan contact met ons op via <a class='link' href='mailto:info@unitedeconomy.nl?SUBJECT=Melding%20nav%20registratieformulier'>info@unitedeconomy.nl</a>.",
			'error.createUser.bold' => "Serverfout",
			'error.createUser'      => "Er is een serverfout opgetreden bij het aanmaken van de nieuwe gebruiker. De foutmelding is als volgt: ",
			'error.dataConverse' 	=> "Er ging iets mis bij data-conversie...",
			'error.dataConverse.item' 	=> "wij snappen de volgende waarde niet:",
			'error.explanation' 	=> "Sommige velden bevatten fouten. Verbeter alstublieft de fouten en probeer het opnieuw.",
			'error.heading' 		=> "De volgende fouten zijn opgetreden:",
			'error.httpNull'		=> "Geen connectie mogelijk met server.",
			'error.inequalIds'  	=> "Kan uw betalingsgegevens niet valideren. Neem contact op met de administratie.",
			'error.list.heading' 	=> "De volgende validatie-fouten zijn gevonden:",
			'error.maxItems'		=> "Maximum aantal aan te maken items overschreden.",
			'error.maxItems.max'	=> "Maximaal toegestaan aantal is",
			'error.missingProfileField'	=> "De profielvelden staan niet goed ingesteld. Neem contact op met de administratie.",
			'error.noCustomFieldPermission' => "Geen permissie voor custom fields; neem contact op met de administratie.",
			'error.noMollie'		=> "Kan geen contact maken met de betaalserver. Probeer het later nog eens. Mocht het probleem zich blijven voordoen, neem dan contact op met de administratie. ",
			'error.noPayment'		=> "Geen betalingsnummer bekend. Er is iets misgegaan met de link van uw mail. Ik verwacht dat er een betalingsnummer gespecificeerd is in die link.",
			'error.noServerContact.bold' => "Geen connectie",
			'error.noServerContact' => "Kan geen verbinding maken met United Economy. Als dit probleem zich blijft voordoen neem dan alstublieft contact op met de administratie.", 
			'error.noUser'			=> "Geen gebruiker bekend. Er is iets misgegaan met de link van uw mail. Neem alstublieft contact op met de administratie.", 
			'error.pending'			=> "Volgens onze administratie heeft u inmiddels al wel betaald, maar die betaling wordt nog verwerkt door de betaalserver. We raden u aan om over een half uurtje opnieuw te proberen te activeren door de link in de al eerder ontvangen activatie-mail te klikken.",
			'error.queryParse'		=> "Een zoekopdracht bevatte een niet-toegestane text:",
			'error.queryParse.text'	=> "de niet toegestane text is:",
			'error.saveError'		=> "Er is een fout opgetreden bij het wegschrijven van gegevens. Neem alstublieft contact op met de administratie. Foutcode: ",
			'error.unknownType'		=> "Onbekende input-fout",
			'error.unknownGroup' 	=> "Onbekende groep",
			'error.unknownUser' 	=> "Onbekende gebruiker",
			'error.unknownCommunity'=> "Community '%s' is niet bekend of niet correct geconfigureerd.<br />Ga naar het <a href=\"%s\">registratie startscherm</a> om uw community te selecteren, of neem alstublieft contact op met de administratie.",
			'error.unknown'   		=> "Onbekende fout. Neem alstublieft contact op met de administratie.",
			'error.uploadPicture.bold'   => "Afbeelding",
			'error.uploadPicture'   => "Er is iets misgegaan bij het uploaden van de afbeelding. Probeer het alstublieft opnieuw.",
			'error.missingLoginInfo'=> "Uw inschrijving is verwerkt, maar er is een probleem met het weergeven van uw inloggegevens.",
			'field.address'			=> "Adres",
			'field.agree'			=> "Ik ga akkoord met de algemene voorwaarden van United Economy. %s om de algemene voorwaarden te bekijken.",
			'field.city'			=> "Woonplaats",
			'field.community' 		=> "Mijn community",
			'field.email'			=> "E-mail",
			'field.email.confirm'	=> "E-mail bevestigen",
			'field.incasso'			=> "Ik ga akkoord met de automatische incasso van de jaarlijkse lidmaatschapsbijdrage. Uw eerste bijdrage is goed voor de rest van dit jaar en komend jaar.",
			'field.validation'		=> "Om uw inschrijving af te ronden, vink alstublieft hieronder aan dat u geen robot bent en dat u akkoord gaat met de Algemene Voorwaarden.",
			'field.name.bedr'		=> "Bedrijfsnaam",
			'field.name.part'		=> "Volledige naam",
			'field.pic.retail'		=> "Profielfoto",
			'field.pic.org'			=> "Logo",
			'field.upload'			=> "Upload",
			'field.username'		=> "Gebruikersnaam",
			'field.zip'				=> "Postcode",
			'field.username.uitleg'	=> "Vul hieronder uw gebruikersnaam in. Via uw gebruikersnaam kunt u betalingen ontvangen van andere leden. Kies daarom een naam die bij u past en die u met anderen kunt delen. De gebruikersnaam moet uniek zijn en tussen de 2 en 16 tekens lang zijn.",			
     		'mollie.api.fail' 		=> "De betaling is niet gelukt. Probeer het later nog eens.",
			'mollie.general.fail' 	=> "Er is een fout opgetreden bij de betaalserver. Foutmelding is: ",
			'confirmation.title'	=> "Bevestiging van uw inschrijving",
			'confirmation.explain'	=> "<p>Gefeliciteerd, uw account is geactiveerd.</p><p>U kunt nu inloggen in uw <a href=\"%s\" target=\"_blank\">United Economy account</a>.</p><p>Om in te loggen gebruikt u uw gebruikersnaam (%s) of e-mailadres (%s) en het door u gekozen wachtwoord.</p><p>Gebruik ook onze gemakkelijke United Economy app op uw smartphone! U kunt er mee betalen, betaalverzoeken sturen, en zien welke bedrijven er meedoen. Download de app uit de <a href=\"https://play.google.com/store/apps/details?id=nl.circuitnederland.mobile&hl=nl/\" target=\"_blank\">Google Play store</a> of <a href=\"https://apps.apple.com/nl/app/united-economy/id1071433138\" target=\"_blank\">Apple App store</a>.</p>",
			'confirmation.error'	=> "Er is iets misgegaan bij het activeren van uw account. Neem alstublieft even contact op met de administratie.",
			'payment.done'			=> "Bedankt voor uw aanmelding!",
			'payment.done.explain'	=> "Uw circulair geld account staat bijna voor u klaar. Zou u zo vriendelijk willen zijn uw e-mailadres te bevestigen en daarmee uw registratie te voltooien? Dit doet u door te klikken op de link in de mail die United Economy gestuurd heeft naar uw opgegeven e-mailadres (%s).<br><br>Mocht u de e-mail niet kunnen vinden, kijk dan voor de zekerheid ook even in uw spam box. Wij verifi&euml;ren uw betaling nadat u uw e-mail bevestigd hebt. Mocht onverhoopt uw betaling niet gelukt zijn, maakt u zich dan geen zorgen. In dat geval krijgt u automatisch een nieuwe betaallink nadat u uw e-mailadres bevestigd hebt.",
			'submit.pay' 			=> "Verder naar betaalpagina",
			'submit.community'		=> "Verder naar inschrijfformulier",
			'title' 				=> "Inschrijfformulier",
			'title.intro' 				=> "Inschrijfformulier United Economy",
			'title.section1.title'	=> "Uw community kiezen",
			'title.section1.text'	=> "Binnen United Economy kennen we verschillende communities (klik <a href='https://www.unitedeconomy.nl/lokale-communities/' target='blank'>hier</a> voor een beschrijving van de communities). Selecteer hieronder de community waar u lid van wilt worden. Als er nog geen community in uw regio actief is, selecteer dan 'United Economy'. Klik vervolgens op de blauwe knop onderaan deze pagina.",
			'title.section2.title'	=> "Gegevens invullen",
			'title.section2.text'	=> "Ga verder met de inschrijving door uw gegevens in te vullen.",
			'title.section3.title'	=> "Inschrijfkosten betalen",
			'title.section3.text'	=> "Nadat u uw gegevens hebt ingevuld, wordt u doorgezonden naar een iDEAL betaalpagina. Hier betaalt u de inschrijfkosten. ",
			'title.section4.title'	=> "Inschrijving bevestigen",
			'title.section4.text'	=> "Nadat u betaald hebt, bevestigt u uw registratie door te klikken op de link in de activatie email. Geen email ontvangen? Kijk dan in uw spamfolder.",
			'title.section5.title'	=> "Aan de slag",
			'title.section5.text'	=> "Nu kunt u circulair geld besteden bij andere leden, online en via de app op de mobiele telefoon, beschikbaar voor <a href='https://play.google.com/store/apps/details?id=nl.circuitnederland.mobile' target='_blank'>Android</a> en <a href='https://apps.apple.com/nl/app/united-economy/id1071433138' target='blank'>Iphone</a>.",
			'community.section1.title'	=> "Gegevens invullen",
			'community.section1.text'	=> "U start de inschrijving door op de knop hieronder te klikken. Als eerste stap vult u uw gegevens in.",
			'community.section2.title'	=> "Inschrijfkosten betalen",
			'community.section2.text'	=> "Nadat u uw gegevens hebt ingevuld, wordt u doorgezonden naar een iDEAL betaalpagina. Hier betaalt u de inschrijfkosten. ",
			'community.section3.title'	=> "Inschrijving bevestigen",
			'community.section3.text'	=> "Nadat u betaald hebt, bevestigt u uw registratie door te klikken op de link in de activatie email. Geen email ontvangen? Kijk dan in uw spamfolder.",
			'community.section4.title'	=> "Aan de slag",
			'community.section4.text'	=> "Nu kunt u circulair geld besteden bij andere leden, online en via de app op de mobiele telefoon, beschikbaar voor <a href='https://play.google.com/store/apps/details?id=nl.circuitnederland.mobile' target='_blank'>Android</a> en <a href='https://apps.apple.com/nl/app/united-economy/id1071433138' target='blank'>Iphone</a>.",			
			'title.explain' 		=> "Vul alstublieft hieronder uw gegevens in. Verplichte velden zijn gemarkeerd met <span class='red'>*</span>.",
			'validation'			=> "Validatie"
	);
	return (!array_key_exists($phrase,$lang)) ? '???' . $phrase . '???' : $lang[$phrase];
}
 
?>