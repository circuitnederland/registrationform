Deployment Tasks for next release

Things to do manually in the Cyclos production-environment when deploying the future 1.1.0 release of the PHP registrationform to production.

1. Adjust registration_strings.php to reflect the changes in registration_strings-sample.php:
- Add 'RECAPTCHA_SECRET' constant.
- Add 'MOLLIE_WEBHOOK_URL_PART' constant.
Belongs to #1.
