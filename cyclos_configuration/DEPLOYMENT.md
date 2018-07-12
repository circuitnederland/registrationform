# Deployment Tasks per release
Things to do manually in the Cyclos production-environment when deploying a new release of the PHP registrationform to production.

## Deployment Tasks for next release

1. Adjust registration_strings.php to reflect the changes in registration_strings-sample.php:
- Add 'RECAPTCHA_SECRET' constant.
- Add 'MOLLIE_WEBHOOK_URL_PART' constant.

	Belongs to #1.

2. Adjust the information-text in the Profilefield 'Aankoop_saldo' to no longer mention a maximum value of 150 euro:

	Go to Systeem > [Gebruikers configuratie] Profielvelden > 'Aankoop saldo' field > 'Informatietekst' and remove the text in brackets: "particulieren mogen maximaal voor 150 euro per keer aan @nder geld kopen".

	Belongs to #7.

3. Rename (or remove but this is only allowed by Cyclos programmers) the maxAankoopPart field in the mollyConnect Record Type:

	Go to Systeem > [Systeemconfiguratie] Recordtypen > 'Molly configuration (keep safe)' > 'Maximaal aankoop bedrag particulieren' field and change the 'Interne naam' from 'maxAankoopPart' into something else, for example 'can_be_deleted'.

	Belongs to #7.
