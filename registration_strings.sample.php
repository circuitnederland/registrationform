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
			'bedrijfpart'		    => "Bedrijf of Particulier?",
			'betaling' 				=> "Betaling",
			'click.here'			=> "Klik hier",
			'confirm'				=> "bevestigen",
			'connect.cyclos' 		=> "Verbinden met Circuit Nederland...",
			'error.400'				=> "Technische fout: het formaat van de gegevens bevat een fout.",
			'error.401'				=> "Geen toegang: ontbrekende of onjuiste inloggegevens...", 
			'error.403'				=> "Geen permissie voor deze operatie.",
			'error.404'				=> "Niet gevonden...",
			'error.422'				=> "Http error 422",
			'error.500'				=> "Onverwachte fout.",
			'error.betaald'			=> "Niet echt een fout, maar volgens onze administratie heb je inmiddels al wel betaald. Alles gaat dus goed, en je kunt (opnieuw) proberen te activeren door de link in de al eerder ontvangen activatie-mail te klikken.",
			'error.captcha.bold'	=> "Geen connectie",
			'error.captcha' 		=> "Kon de captcha-informatie niet ophalen van de server, waardoor het formulier niet verder verwerkt kan worden. Probeer het later nog eens.",
			'error.captchaForgotten.bold' => "Captcha-validatie",
			'error.captchaForgotten'	=> "Je bent vergeten het vakje \"I'm not a robot\" aan te vinken.",
			'error.recaptcha'		=> "Vergeet niet bij het opnieuw versturen ook het vakje \"I'm not a robot\" weer aan te vinken en het wachtwoord opnieuw op te geven.",
			'error.contact'			=> "Heb je nog vragen? Neem dan contact met ons op via <a class='link' href='mailto:info@circuitnederland.nl?SUBJECT=Melding%20nav%20registratieformulier'>info@circuitnederland.nl</a>.",
			'error.createUser.bold' => "Serverfout",
			'error.createUser'      => "Er is een serverfout opgetreden bij het aanmaken van de nieuwe gebruiker. De foutmelding is als volgt: ",
			'error.dataConverse' 	=> "Er ging iets mis bij data-conversie...",
			'error.dataConverse.item' 	=> "wij snappen de volgende waarde niet:",
			'error.explanation' 	=> "Sommige velden bevatten fouten. Verbeter alsjeblieft de fouten en probeer het opnieuw.",
			'error.heading' 		=> "De volgende fouten zijn opgetreden:",
			'error.httpNull'		=> "Geen connectie mogelijk met server.",
			'error.inequalIds'  	=> "Kan je betalingsgegevens niet valideren. Neem contact op met de administratie.",
			'error.list.heading' 	=> "De volgende validatie-fouten zijn gevonden:",
			'error.maxItems'		=> "Maximum aantal aan te maken items overschreden.",
			'error.maxItems.max'	=> "Maximaal toegestaan aantal is",
			'error.missingProfileField'	=> "De profielvelden staan niet goed ingesteld. Neem contact op met de administratie.",
			'error.noCustomFieldPermission' => "Geen permissie voor custom fields; neem contact op met de administratie.",
			'error.noMollie'		=> "Kan geen contact maken met de betaalserver. Probeer het later nog eens. Mocht het probleem zich blijven voordoen, neem dan contact op met de administratie. ",
			'error.noPayment'		=> "Geen betalingsnummer bekend. Er is iets misgegaan met de link van je mail. Ik verwacht dat er een betalingsnummer gespecificeerd is in die link.",
			'error.noServerContact.bold' => "Geen connectie",
			'error.noServerContact' => "Kan geen verbinding maken met Circuit Nederland. Als dit probleem zich blijft voordoen neem dan alsjeblieft contact op met de administratie.", 
			'error.noUser'			=> "Geen gebruiker bekend. Er is iets misgegaan met de link van je mail. Neem alsjeblieft contact op met de administratie.", 
			'error.pending'			=> "Volgens onze administratie heb je inmiddels al wel betaald, maar die betaling wordt nog verwerkt door de betaalserver. We raden je aan om over een half uurtje opnieuw te proberen te activeren door de link in de al eerder ontvangen activatie-mail te klikken.",
			'error.queryParse'		=> "Een zoekopdracht bevatte een niet-toegestane text:",
			'error.queryParse.text'	=> "de niet toegestane text is:",
			'error.saveError'		=> "Er is een fout opgetreden bij het wegschrijven van gegevens. Neem alsjeblieft contact op met de administratie. Foutcode: ",
			'error.unknownType'		=> "Onbekende input-fout",
			'error.unknownGroup' 	=> "Onbekende groep",
			'error.unknownUser' 	=> "Onbekende gebruiker",
			'error.unknownCommunity'=> "Community '%s' is niet bekend of niet correct geconfigureerd.<br />Ga naar het <a href=\"%s\">registratie startscherm</a> om je community te selecteren, of neem alsjeblieft contact op met de administratie.",
			'error.unknown'   		=> "Onbekende fout. Neem alsjeblieft contact op met de administratie.",
			'error.uploadPicture.bold'   => "Afbeelding",
			'error.uploadPicture'   => "Er is iets misgegaan bij het uploaden van de afbeelding. Probeer het alsjeblieft opnieuw.",
			'error.missingLoginInfo'=> "Je inschrijving is verwerkt maar er is een probleem met het weergeven van je inlog gegevens.",
			'field.address'			=> "Adres",
			'field.agree'			=> "Ik ga akkoord met de algemene voorwaarden van het landelijke Social Trade Circuit Nederland. %s om de algemene voorwaarden te bekijken.",
			'field.city'			=> "Woonplaats",
			'field.community' 		=> "Mijn community",
			'field.email'			=> "E-mail",
			'field.email.confirm'	=> "E-mail bevestigen",
			'field.incasso'			=> "Ik ga akkoord met de automatische incasso van de jaarlijkse lidmaatschapsbijdrage. Je eerste bijdrage is goed voor de rest van dit jaar en komend jaar.",
			'field.validation'		=> "Om je inschrijving af te ronden, vink alsjeblieft hieronder aan dat je geen robot bent en dat je akkoord gaat met de Algemene Voorwaarden en de automatische incasso.",
			'field.name.bedr'		=> "Bedrijfsnaam",
			'field.name.part'		=> "Volledige naam",
			'field.pic.retail'		=> "Profielfoto",
			'field.pic.org'			=> "Logo",
			'field.upload'			=> "Upload",
			'field.username'		=> "Gebruikersnaam",
			'field.zip'				=> "Postcode",
			'field.username.uitleg'	=> "Vul hieronder je gebruikersnaam in, via deze gebruikersnaam kun je betalingen ontvangen van andere leden. Kies een naam die bij je past en die je goed met anderen kan delen. Deze naam moet uniek zijn.",			
     		'mollie.api.fail' 		=> "De betaling is niet gelukt. Probeer het later nog eens.",
			'mollie.general.fail' 	=> "Er is een fout opgetreden bij de betaalserver. Foutmelding is: ",
			'confirmation.title'	=> "Bevestiging van je inschrijving",
			'confirmation.explain'	=> "<p>Gefeliciteerd, je account is geactiveerd.</p><p>Je kunt nu inloggen in je <a href=\"%s\" target=\"_blank\">Circuit Nederland account</a>.</p><p>Om in te loggen gebruik je jouw gebruikersnaam (%s) of e-mailadres (%s) en het door jou gekozen wachtwoord.</p><p>Gebruik ook onze gemakkelijke Circuit Nederland app op je smart-phone! Je kunt er mee betalen, betaalverzoeken sturen, en zien welke bedrijven er meedoen. Download de app uit de <a href=\"https://play.google.com/store/apps/details?id=nl.circuitnederland.mobile&hl=nl/\" target=\"_blank\">Google Play store</a> of <a href=\"https://itunes.apple.com/nl/app/circuit-nederland/id1071433138?mt=8\" target=\"_blank\">Apple App store</a>.</p>",
			'confirmation.error'	=> "Er is iets misgegaan bij het activeren van je account. Neem alsjeblieft even contact op met de administratie.",
			'payment.done'			=> "Bedankt voor je aanmelding!",
			'payment.done.explain'	=> "Je circulair geld account staat bijna voor je klaar. Zou je zo vriendelijk willen zijn je e-mailadres te bevestigen en daarmee je registratie te voltooien? Dit doe je door te klikken op de link in de mail die Circuit Nederland gestuurd heeft naar jouw opgegeven emailadres (%s).<br><br>Mocht je de email niet kunnen vinden, kijk dan voor de zekerheid ook even in je spam box. Wij verifi&euml;ren je betaling nadat je jouw email bevestigd hebt. Mocht onverhoopt je betaling niet gelukt zijn, maak je geen zorgen. In dat geval krijg je automatisch een nieuwe betaallink nadat je jouw e-mailadres bevestigd hebt.",
			'submit.pay' 			=> "Verder naar betaalpagina",
			'submit.community'		=> "Verder naar inschrijfformulier",
			'title' 				=> "Inschrijfformulier",
			'title.intro' 				=> "Inschrijfformulier Circuit Nederland",
			'title.section1.title'	=> "Kies je community",
			'title.section1.text'	=> "Binnen Circuit Nederland kennen we verschillende communities (klik <a href='https://www.circuitnederland.nl/community/' target='blank'>hier</a> voor een beschrijving van de communities). Selecteer hieronder de community waar je lid van wilt worden. Als er nog geen community in jouw regio actief is, selecteer dan 'Circuit Nederland'. Klik vervolgens op de blauwe knop onderaan deze pagina.",
			'title.section2.title'	=> "Vul je gegevens in",
			'title.section2.text'	=> "Ga verder met de inschrijving door je gegevens in te vullen.",
			'title.section3.title'	=> "Betaal de inschrijfkosten",
			'title.section3.text'	=> "Nadat je jouw gegevens hebt ingevuld, word je automatisch doorgezonden naar een iDEAL betaalpagina. Hier betaal je de inschrijfkosten plus eventueel aangeschaft circulair geld om te kunnen handelen. ",
			'title.section4.title'	=> "Bevestig je inschrijving",
			'title.section4.text'	=> "Nadat je betaald hebt, bevestig je jouw registratie door te klikken op de link in de activatie email. Geen email ontvangen? Kijk dan in je spam folder.",
			'title.section5.title'	=> "Aan de slag",
			'title.section5.text'	=> "Nu kun je circulair geld besteden bij andere leden, online en via app op de mobiele telefoon beschikbaar voor <a href='https://play.google.com/store/apps/details?id=nl.circuitnederland.mobile' target='_blank'>Android</a>   en <a href='https://itunes.apple.com/nl/app/circuit-nederland/id1071433138' target='blank'>Iphone</a>",
			'community.section1.title'	=> "Introductie inschrijfformulier",
			'community.section1.text'	=> "Leuk dat je je gaat inschrijven! Hier vind je de stappen die je doorloopt bij het inschrijven. Je start de inschrijving door op de blauwe knop onderaan deze pagina te klikken. Dan kom je op een pagina waar je jouw gegevens kunt invullen.",
			'community.section2.title'	=> "Betaal de inschrijfkosten",
			'community.section2.text'	=> "Nadat je jouw gegevens hebt ingevuld, word je automatisch doorgezonden naar een iDEAL betaalpagina. Hier betaal je de inschrijfkosten plus eventueel aangeschaft circulair geld om te kunnen handelen. ",
			'community.section3.title'	=> "Bevestig je inschrijving",
			'community.section3.text'	=> "Nadat je betaald hebt, bevestig je jouw registratie door te klikken op de link in de activatie email. Geen email ontvangen? Kijk dan in je spam folder.",
			'community.section4.title'	=> "Aan de slag",
			'community.section4.text'	=> "Nu kun je circulair geld besteden bij andere leden, online en via app op de mobiele telefoon beschikbaar voor <a href='https://play.google.com/store/apps/details?id=nl.circuitnederland.mobile' target='_blank'>Android</a>   en <a href='https://itunes.apple.com/nl/app/circuit-nederland/id1071433138' target='blank'>Iphone</a>",			
			'title.explain' 		=> "Je registreert je door onderstaande velden in te vullen.
									Vervolgens rond je de inschrijving af door met iDEAL te betalen.
									Verplichte velden zijn gemarkeerd met <span class='red'>*</span>.",
			'validation'			=> "Validatie"
	);
	return (!array_key_exists($phrase,$lang)) ? '???' . $phrase . '???' : $lang[$phrase];
}
 
?>