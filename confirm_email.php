<?php
include_once 'registration_strings.php';
include_once 'registration_functions.php';

$loginInfo = array();
try{
	// First check if we have a validationKey request parameter.
	$validationKey = $_GET['validationKey'] ?? null;

	// Let Cyclos validate the user.
	$validatedUser = validateUser($validationKey);

	// Retrieve the login information of the user.
	$loginInfo = getLoginInformation($validatedUser);

} catch(Exception $e) {
	// Set the error array if it was not set already.
	if (empty($_SESSION['errors'])) {
		$_SESSION['errors'] = array('errorType' => 'fatal', 'msg' => 'Onbekende fout');
	}
}

// Activation errors are not validation or fatal errors but a special type. For now, use fatal, because it has the same effect.
// @todo: maybe use a separate errorType for validation and adapt show_errors.php to show different layout??
if (!empty($_SESSION['errors'])) $_SESSION['errors']['errorType'] = 'fatal';
?>

<!DOCTYPE html>
<html>
	<head>
		<title><?php echo lang('confirmation.title')?></title>
		<link rel="stylesheet" type="text/css" href="style.css">
	</head>
<body>
<div id="formContainer">
	<h1><?php echo lang('confirmation.title')?></h1>

	<?php

	include 'show_errors.php';

	?>

	<p class="uitleg"><?php printf(lang('confirmation.explain'), BASE_URL, $loginInfo['username'], $loginInfo['email']); ?></p>

</div>
	
</body>
</html>
