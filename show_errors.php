<?php 

/*
Parse the error information stored in the session - if available.
*/

if (empty($_SESSION['errors'])) return;

$errors = $_SESSION['errors'];
unset($_SESSION['errors']);
$isFatal = isset($errors['errorType'])? ('fatal' == $errors['errorType']) : true;
$captchaForgotten = false;
$errorMessage = "Onbekende fout";
if (!empty($errors['msg'])) {
	$errorMessage = $errors['msg'];
} elseif (!empty($errors['msgType'])) {
	$msgType = $errors['msgType'];
	// @todo: improve the entire lang() construction, especially with this way of checking for .bold keys.
	$typesWithBoldPart = array('error.captcha', 'error.captchaForgotten', 'error.createUser', 'error.noServerContact', 'error.uploadPicture');
	if (in_array($msgType, $typesWithBoldPart)) {
		$errorMessage = sprintf('<ul><li><span class="redError">%s:</span> %s</li></ul>', lang($msgType . ".bold"), lang($msgType));
		$captchaForgotten = ('error.captchaForgotten' == $msgType);
	} else {
		$errorMessage = lang($msgType);
	}
}
?>
	<div class="schadow">
		<div class="errorTop"><h3><?php echo lang('error.title'); ?></h3></div>
		<div class="errorBottom">
			<p class="textError"><?php echo lang('error.heading'); ?></p>
			<?=$errorMessage?>
			<?php
			// Only show the reminder to re-enter the captcha if that was not the error in itself and we do show it again (no fatal).
			if (!$isFatal && !$captchaForgotten): ?>
				<p class="textError"><?php echo lang('error.recaptcha'); ?></p>
			<?php endif; ?>
		   	<p class="textError"><?php echo lang('error.contact'); ?></p>
		</div>
	</div>

<?php
// If this is a fatal error, stop showing anything else.
if($isFatal) {
	// End html tags that were opened before this php file was included.
	?>
</div>
</body>
</html>
	<?php
	exit();
}
