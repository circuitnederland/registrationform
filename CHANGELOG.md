# Changelog
## 1.7.0 (2023-11-24)
### Changed
<ul>
	<li>Migration from Circuit Nederland to United Economy.</li>
</ul>

## 1.6.0 (2023-09-28)
### Improved
<ul>
	<li>Changes to profile fields.</li>
</ul>

## 1.5.0 (2023-08-31)
### Added
<ul>
	<li>Added BETA phase of emandates functionality, so designated users with a special Product can issue an emandate and topup their balance, leading to a direct debit from their bank account.</li>
</ul>

### Improved
<ul>
	<li>Comply with Cyclos 4.16 scripting API which contains changes that are not backwards compatible.</li>
</ul>

## 1.4.7 (2022-12-09)
### Improved
<ul>
	<li>IBAN profile field is now unique and corrected if not complying to our conventions for spaces and uppercase letters.</li>
</ul>

## 1.4.6 (2022-10-04)
### Improved
<ul>
	<li>DoB profile field is no longer required to avoid validation problems. It is required during registration in the PHP form.</li>
</ul>

### Added
<ul>
	<li>Added block/withdraw functionality to the new eMandate (not live yet).</li>
</ul>

## 1.4.5 (2022-09-20)
### Improved
<ul>
	<li>Improved profile fields validation.</li>
</ul>

## 1.4.4 (2022-09-01)
### Added
<ul>
	<li>Added scripts for the new eMandate functionality (not live yet).</li>
</ul>

### Bugfixes
<ul>
	<li>Update the moment library to 2.29.4.</li>
</ul>

## 1.4.3 (2022-01-13)
### Added
<ul>
	<li>Allow easy invoice amount to be optional.</li>
	<li>Imported some C3NL scripts that were not in the git repo yet.</li>
</ul>

## 1.4.2 (2021-06-15)
### Bugfixes
<ul>
	<li>Fix logo upload</li>
	<li>Fix for AgreementLogService API changes in Cyclos 4.13</li>
</ul>

## 1.4.1 (2020-04-30)
### Changed
<ul>
	<li>Removed the options for users to buy circular euro's via iDEAL.</li>
</ul>

## 1.4.0 (2019-07-09)
### Added
<ul>
	<li>Added a custom period to the MT940 export, next to the existing month/quarter/year options, so uses can choose a custom start and end date.</li>
</ul>

## 1.3.0 (2019-03-05)
### Added
<ul>
	<li>Added topup functionality, so users can now buy credits via iDEAL.</li>
</ul>

### Changed
<ul>
	<li>Changed user identification in Mollie payments to use userId instead of username.</li>
	<li>Set PaymentId fields to be unique in Cyclos within profile fields and record fields.</li>
	<li>Improved check on payment age: it should not be older than the user itself.</li>
	<li>Let mollieWebhook use recordService so changes in the idealDetail records become visible in the record history.</li>
</ul>

### Bugfixes
<ul>
	<li>Changed iban comparison to avoid warning e-mails to admin when ibans differ only in whitespace.</li>
</ul>

## 1.2.0 (2019-01-08)
### Added
<ul>
	<li>New checkbox in manual validation script for users with alternative payments: admin can now indicate whether the user already accepted the agreements or not.</li>
	<li>Changes by webhook are now visible in profile history.</li>
</ul>

### Changed
<ul>
	<li>Improved message when user clicks confirmation link twice.</li>
	<li>Small improvements: red oops bar removed, proper encodig of email address in screen after registration, link to login page and links to mobile apps in screen after activation.</li>
</ul>

### Bugfixes
<ul>
	<li>Better checks on payments during validation. User can now also pay an older payment they might have still open.</li>
	<li>Improved activation for users created by brokers.</li>
	<li>Operators now receive the original Cyclos URL to validate.</li>
	<li>Uploading non-image files as logo is now prevented and generates a proper validation message.</li>
	<li>Improved validation for aankoop_saldo field.</li>
</ul>

## 1.1.1 (2018-11-15)
### Added
<ul>
	<li>Added custom operation for financial admins to validate users with alternative payment methods.</li>
</ul>

### Changed
<ul>
	<li>Changed description of debit to user payment.</li>
	<li>Changed Mollie description to look better when truncated.</li>
</ul>

### Bugfixes
<ul>
	<li>Fix missing consumerName in iDEAL records.</li>
</ul>

## 1.1.0 (2018-10-30)
### Added
<ul>
	<li>Added fields to the iDEAL user record.</li>
	<li>Added marking fields as private for 'particulieren'.</li>
	<li>Removed maximum 'aankoop saldo' for 'particulieren'.</li>
</ul>

### Changed
<ul>
	<li>Refactored the flow so more logic is done within Cyclos instead of in the php.</li>
	<li>Updated jquery validation library version to 1.17.0.</li>
	<li>Updated Mollie API version to v2.</li>
	<li>Changed PHP code to be used on PHP 7.</li>
	<li>Changed texts to use informal vocative (je/jouw) instead of formal (u/uw).</li>
</ul>

## 1.0.0 (2018-07-05)
### Added
<ul>
	<li>Imported current live version</li>
	<li>Added .gitignore and sample version of registration_strings</li>
</ul>

### Changed
<ul>
	<li>Removed domain-specific information from registration_functions</li>
</ul>
