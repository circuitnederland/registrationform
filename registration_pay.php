<?php

include_once 'registration_strings.php';
include_once 'registration_functions.php';

session_start();
storePostValuesInSession();

// If something is wrong, always redirect the user back to register page containing the form.
$nextPageUrl = $_SESSION['backURL'];
try {
	// Check the captcha.
	$captchaError = failCaptcha();
	if ($captchaError) {
		raiseError($captchaError, null, 'validation');
	}

	// Handle picture upload.
	$pictureId = uploadPicture();

	// Create the Cylos user.
	$dataAsJson = getDataFromSession($pictureId);
	$createdUser = createUser($dataAsJson);

	// Get the payment URL of the newly created Cyclos user.
	$paymentUrl = getPaymentUrl($createdUser);

	// Redirect the user to the payment URL.
	if (!empty($paymentUrl)) {
		$nextPageUrl = $paymentUrl;
	}
} catch (Exception $e) {
	// Set the error array if it was not set already.
	if (empty($_SESSION['errors'])) {
		$_SESSION['errors'] = array('errorType' => 'fatal', 'msg' => 'Onbekende fout');
	}
}
// Send the customer off to complete the payment.
header("Location: " . $nextPageUrl);
exit;


?>

<!DOCTYPE html>
<html>
	<head>
		<title><?php echo lang('betaling')?></title>
		<link rel="stylesheet" type="text/css" href="style.css">
	</head>
<body>

<div id="formContainer">
    <div id="wrap">
        <div class="item">
            <div class="spinner"></div>
            <h5><?php echo lang('connect.cyclos')?><span class="dot">.</span></h5>
        </div>
    </div>
</div>
	
</body>
</html> 
