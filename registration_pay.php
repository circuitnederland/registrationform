<?php

include_once 'registration_strings.php';
include_once 'registration_functions.php';

//////////// constants ///////////////////////////////////////////////////////////////////////////
// beware that this only goes well if the files are in the "inschrijfformulier" directory. 
define('REDIRECT_FINISH',   "http://" . $_SERVER['HTTP_HOST'] . pathinfo($_SERVER['SCRIPT_NAME'], PATHINFO_DIRNAME) . '/' . "registration_finished.php");
define('WEBHOOK_URL', BASE_URL . MOLLIE_WEBHOOK_URL_PART);


session_start();
storePostValuesInSession();

// autoload dependencies.
// Better to place this in index.php.
// If you are not using composer reference Mollie_API_Client in another way.
require "vendor/autoload.php"; 

// redirect user back to first page
$nextPageUrl = $_SESSION['backURL'];
//first do the captcha
$captchaError = failCaptcha();
if ($captchaError) {
	$_SESSION['errors'] = $captchaError;
} else {
	try {
		$picture = uploadPicture(CYCLOS_ACCESS);
		if (!empty($picture) && $picture["httpCode"] != 201) {
			// picture upload failed, return with error message
			$result['type'] = "uploadPicture";
			$_SESSION['errors'] = $result;
		} else {
			$paymentUser = (empty(getNullSafeValue('username'))) ? getNullSafeValue('name') : getNullSafeValue('username');
			$payment = createPayment($_SESSION['MOLLY_ACCESS'], getAmount(), getLidmaatschapsBijdrage(), getNullSafeAankoop(), $paymentUser, REDIRECT_FINISH, WEBHOOK_URL);
			$pictureId = (empty($picture)) ? null : $picture['result'];
			// Create the user
			$dataAsJson = getDataFromSession($pictureId, $payment->id);
			$result = createUser($dataAsJson, CYCLOS_ACCESS);
			
	 		if ($result["httpCode"] == 201) {
	 			// redirect user to payment page
	 			$nextPageUrl = $payment->getPaymentUrl();
	 		} else {
	 			// redirect user back to first page
	 			$nextPageUrl = $_SESSION['backURL'];
	 			// and pass the error, so the first page can display it.
	 			$result['type'] = "createUser";
	 			$_SESSION['errors'] = $result;
	 		}
		}
	} catch (Mollie_API_Exception $e) {
		$result['type'] = "mollie";
		$result['msg'] = lang('mollie.api.fail') ;
		$_SESSION['errors'] = $result;
	} catch (Exception $e) {
		$result['type'] = "mollie";
		$result['msg'] = lang('mollie.general.fail') . htmlspecialchars($e->getMessage());
		$_SESSION['errors'] = $result;
	}
}
//Send the customer off to complete the payment.
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
            <h5><?php echo lang('connect.mollie')?><span class="dot">.</span></h5>
        </div>
    </div>
</div>
	
</body>
</html> 
