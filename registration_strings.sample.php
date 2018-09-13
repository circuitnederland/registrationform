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
			'connect.mollie' 		=> "Verbinden met iDEAL...",
			'connect.cyclos' 		=> "Verbinden met Circuit Nederland...",
			'error.400'				=> "Technische fout: het formaat van de gegevens bevat een fout.",
			'error.401'				=> "Geen toegang: ontbrekende of onjuiste inloggegevens...", 
			'error.403'				=> "Geen permissie voor deze operatie.",
			'error.404'				=> "Niet gevonden...",
			'error.422'				=> "Http error 422",
			'error.500'				=> "Onverwachte fout.",
			'error.betaald'			=> "Niet echt een fout, maar volgens onze administratie heeft u inmiddels al wel betaald. Alles gaat dus goed, en U kunt (opnieuw) proberen te activeren door de link in de al eerder ontvangen activatie-mail te klikken.",
			'error.captcha.bold'	=> "Geen connectie",
			'error.captcha' 		=> "Kon de captcha-informatie niet ophalen van de server, waardoor het formulier niet verder verwerkt kan worden. Probeert u het later nog eens.",
			'error.captchaForgotten.bold' => "Captcha-validatie",
			'error.captchaForgotten'	=> "U bent vergeten het vakje \"I'm not a robot\" aan te vinken.",
			'error.recaptcha'		=> "Vergeet niet bij het opnieuw versturen ook het vakje \"I'm not a robot\" weer aan te vinken en het wachtwoord opnieuw op te geven.",
			'error.contact'			=> "Heeft u nog vragen? Neem dan contact met ons op via <a class='link' href='mailto:info@circuitnederland.nl?SUBJECT=Melding%20nav%20registratieformulier'>info@circuitnederland.nl</a>.",
			'error.createUser.bold' => "Serverfout",
			'error.createUser'      => "Er is een serverfout opgetreden bij het aanmaken van de nieuwe gebruiker. De foutmelding is als volgt: ",
			'error.dataConverse' 	=> "Er ging iets mis bij data-conversie...",
			'error.dataConverse.item' 	=> "wij snappen de volgende waarde niet:",
			'error.explanation' 	=> "Sommige velden bevatten fouten. Verbeter a.u.b. de fouten en probeer het opnieuw.",
			'error.heading' 		=> "De volgende fouten zijn opgetreden:",
			'error.httpNull'		=> "Geen connectie mogelijk met server.",
			'error.inequalIds'  	=> "Kan uw betalingsgegevens niet valideren. Neem contact op met de administratie.",
			'error.list.heading' 	=> "De volgende validatie-fouten zijn gevonden:",
			'error.maxItems'		=> "Maximum aantal aan te maken items overschreden.",
			'error.maxItems.max'	=> "Maximaal toegestaan aantal is",
			'error.noCustomFieldPermission' => "Geen permissie voor custom fields; neem contact op met de administratie.",
			'error.noMollie'		=> "Kan geen contact maken met de betaalserver. Probeert u het later nog eens. Mocht het probleem zich blijven voordoen, neem dan contact op met de administratie. ",
			'error.noPayment'		=> "Geen betalingsnummer bekend. Er is iets misgegaan met de link van uw mail. Ik verwacht dat er een betalingsnummer gespecificeerd is in die link.",
			'error.noServerContact.bold' => "Geen connectie",
			'error.noServerContact' => "Kan geen verbinding maken met Circuit Nederland. Als dit probleem zich blijft voordoen neem dan a.u.b. contact op met de administratie.", 
			'error.noUser'			=> "Geen gebruiker bekend. Er is iets misgegaan met de link van uw mail. Ik verwacht dat er een gebruiksnaam gespecificeerd is in die link.", 
			'error.pending'			=> "Volgens onze administratie heeft u inmiddels al wel betaald, maar die betaling wordt nog verwerkt door de betaalserver. We raden u aan om over een half uurtje opnieuw te proberen te activeren door de link in de al eerder ontvangen activatie-mail te klikken.",
			'error.queryParse'		=> "Een zoekopdracht bevatte een niet-toegestane text:",
			'error.queryParse.text'	=> "de niet toegestane text is:",
			'error.saveError'		=> "Er is een fout opgetreden bij het wegschrijven van gegevens. Neem a.u.b. contact op met de administratie. Foutcode: ",
			'error.title'			=> "Oeps! Er is iets niet goed gegaan!!",
			'error.unknown'			=> "Onbekende input-fout",
			'error.unknownUser' 	=> "Onbekende gebruiker",
			'error.repay.unknown'   => "Onbekende fout",
			'error.uploadPicture.bold'   => "Afbeelding",
			'error.uploadPicture'   => "Er is iets misgegaan bij het uploaden van de afbeelding. Probeert u het alstublieft opnieuw.",
			'field.address'			=> "Adres",
			'field.agree'			=> "Ik ga akkoord met de algemene voorwaarden van het landelijke Social Trade Circuit Nederland. %s om de algemene voorwaarden te bekijken.",
			'field.city'			=> "Woonplaats",
			'field.community' 		=> "Mijn community",
			'field.email'			=> "E-mail",
			'field.email.confirm'	=> "E-mail bevestigen",
			'field.incasso'			=> "Ik ga akkoord met de automatische incasso van de jaarlijkse lidmaatschapsbijdrage. Uw eerste bijdrage is goed voor de rest van dit jaar en komend jaar.",
			'field.validation'		=> "Om uw inschrijving af te ronden, vinkt u alstublieft hieronder aan dat u geen robot bent en dat u akkoord gaat met de Algemene Voorwaarden en de automatische incasso.",		
			'field.name.bedr'		=> "Bedrijfsnaam",
			'field.name.part'		=> "Volledige naam",
			'field.pic.retail'		=> "Profielfoto",
			'field.pic.org'			=> "Logo",
			'field.upload'			=> "Upload",
			'field.username'		=> "Gebruikersnaam",
			'field.zip'				=> "Postcode",
			'field.username.uitleg'	=> "Vul hieronder uw gebruikersnaam in, via deze gebruikersnaam kunt u betalingen ontvangen van andere leden. Kies een naam die bij u past en die u goed met andere kan delen. Deze naam moet uniek zijn.",			
     		'mollie.api.fail' 		=> "De betaling is niet gelukt. Probeert u het later nog eens.",
			'mollie.general.fail' 	=> "Er is een fout opgetreden bij de betaalserver. Foutmelding is: ",
			'payment.description'   => "Jaarlijkse contributie (€%s) + startsaldo (€%s) voor gebruiker %s.",
			'payment.done'			=> "Bedankt voor uw aanmelding!",
			'payment.done.explain'	=> "Uw circulair geld account staat bijna voor u klaar. Zou u zo vriendelijk willen zijn uw e-mailadres te bevestigen? Gaat u daarvoor naar uw email en open de email die u heeft gekregen van Circuit Nederland. Daarin zit een link die u moet openen om de registratie te voltooien. <br><br>Mocht u de email niet kunnen vinden, kijk dan voor de zekerheid ook even in uw spam box. Wij verifi&euml;ren uw betaling nadat u uw email bevestigd heeft. Mocht onverhoopt uw betaling niet in orde zijn, maakt u zich geen zorgen. In dat geval krijgt u automatisch een e-mail met een nieuwe betaallink nadat u uw e-mailadres bevestigd heeft.",
			'repay.title'			=> "Nabetalingsformulier Circuit Nederland",
			'repay.title.explain'	=> "U ziet dit formulier omdat er iets mis gaat met de nabetaling van uw lidmaatschapsbijdrage voor Circuit Nederland. Vanwege een fout blijkt het niet mogelijk door te verwijzen naar de betalingssite.",
			'repayment.done.explain'=> "Welkom bij het Social Trade Circuit Nederland. Als u heeft betaald, kunt U nu opnieuw de al eerder ontvangen 
										activatiemail openen en opnieuw op de daarin geplaatste link klikken om uw lidmaatschap te activeren.",
			'submit.pay' 			=> "Verder naar betaalpagina",
			'submit.community'		=> "Verder naar inschrijfformulier",
			'title' 				=> "Inschrijfformulier",
			'title.intro' 				=> "Inschrijfformulier Circuit Nederland",
			'title.section1.title'	=> "Kies uw community",
			'title.section1.text'	=> "Binnen Circuit Nederland kennen we verschillende communities (klik <a href='https://www.circuitnederland.nl/community/' target='blank'>hier</a> voor een beschrijving van de communities). Selecteer hieronder de community waar u lid van wilt worden. Als er nog geen community in uw regio actief is, selecteer dan 'Circuit Nederland'. Klik vervolgens op de blauwe knop onderaan deze pagina.",
			'title.section2.title'	=> "Vul uw gegevens in",
			'title.section2.text'	=> "Ga verder met de inschrijving door uw gegevens in te vullen.",
			'title.section3.title'	=> "Betaal de inschrijfkosten",
			'title.section3.text'	=> "Nadat u uw gegevens heeft ingevuld, wordt u automatisch doorgezonden naar een iDEAL betaalpagina. Hier betaalt u de inschrijfkosten plus eventueel aangeschaft @nder geld om te kunnen handelen. ",
			'title.section4.title'	=> "Bevestig uw inschrijving",
			'title.section4.text'	=> "Nadat u betaald heeft, bevestigt u uw registratie door te klikken op de link in de activatie email. Geen email ontvangen? Kijk dan in uw spam folder.",
			'title.section5.title'	=> "Aan de slag",
			'title.section5.text'	=> "Nu kunt u @nder geld besteden bij andere leden, online en via app op de mobiele telefoon beschikbaar voor <a href='https://play.google.com/store/apps/details?id=nl.circuitnederland.mobile' target='_blank'>Android</a>   en <a href='https://itunes.apple.com/nl/app/circuit-nederland/id1071433138' target='blank'>Iphone</a>",
			'community.section1.title'	=> "Introductie inschrijfformulier",
			'community.section1.text'	=> "Leuk dat u zich gaat inschrijven! Hier vindt u de stappen die u doorloopt bij het inschrijven. U start de inschrijving door op de blauwe knop onderaan deze pagina te klikken. Dan komt u op een pagina waar u uw gegevens kunt invullen.",
			'community.section2.title'	=> "Betaal de inschrijfkosten",
			'community.section2.text'	=> "Nadat u uw gegevens heeft ingevuld, wordt u automatisch doorgezonden naar een iDEAL betaalpagina. Hier betaalt u de inschrijfkosten plus eventueel aangeschaft @nder geld om te kunnen handelen. ",
			'community.section3.title'	=> "Bevestig uw inschrijving",
			'community.section3.text'	=> "Nadat u betaald heeft, bevestigt u uw registratie door te klikken op de link in de activatie email. Geen email ontvangen? Kijk dan in uw spam folder.",
			'community.section4.title'	=> "Aan de slag",
			'community.section4.text'	=> "Nu kunt u @nder geld besteden bij andere leden, online en via app op de mobiele telefoon beschikbaar voor <a href='https://play.google.com/store/apps/details?id=nl.circuitnederland.mobile' target='_blank'>Android</a>   en <a href='https://itunes.apple.com/nl/app/circuit-nederland/id1071433138' target='blank'>Iphone</a>",			
			'title.explain' 		=> "U registreert zich door onderstaande velden in te vullen.
									Vervolgens rondt u de inschrijving af door met iDEAL te betalen.
									Verplichte velden zijn gemarkeerd met <span class='red'>*</span>.",
			'validation'			=> "Validatie"
	);
	return (!array_key_exists($phrase,$lang)) ? '???' . $phrase . '???' : $lang[$phrase];
}
 
?>