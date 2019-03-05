# Changelog
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
