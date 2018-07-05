<?php

################################## INITIALIZATION ###############################
#################################################################################
include_once 'registration_strings.php';
include_once 'registration_functions.php';

session_start();
header ('Content-type: text/html; charset=utf-8');
require "vendor/autoload.php";

define('REDIRECT_FINISH',   "http://" . $_SERVER['HTTP_HOST'] . pathinfo($_SERVER['SCRIPT_NAME'], PATHINFO_DIRNAME) . '/' . "registration_finished.php");

// store the present url so we can redirect back to it in case something goes wrong
$_SESSION['backURL'] = $_SERVER['REQUEST_URI'];
if (!isset($_SESSION['errors'])) {
	$_SESSION['errors'] = array();
}

################################## MAIN CODE  ###############################
#################################################################################

// read the url params
$user = '';
if (isset($_GET['user'])) {
	$user = $_GET['user'];
} else {
	$_SESSION['errors'][] = "noUser";
}
$oldPayId = '';
if (isset($_GET['payment'])) {
	$oldPayId = $_GET['payment'];
} else {
	$_SESSION['errors'][] = "noPayment";
}

$noServerContact = false;
$lidmaatschapParticulieren;
$lidmaatschapBedrijven;
$aankoopSaldo;
$storedPaymentId;
$betaald;
$amount;
$payment;
$httpError;
$username;

// get the user's data via a rest call to cyclos.
$profile = getUserProfile($user, BASE_URL );

if (empty($profile)) {
	$noServerContact = true;
} else if ($profile['httpCode']) {
	if ($profile['httpCode'] == 404) {
		$_SESSION['errors'][] = "unknownUser";
	} else {
		$_SESSION['errors'][] = "repayError"; 
	}
} else if (empty($profile['customValues'])) {
	$_SESSION['errors'][] = "noCustFieldPermission";
} else {
	$username=$profile['display'];
	// read the profile fields
	foreach ($profile['customValues'] as $customValue) {
		switch ($customValue['field']['internalName']) {
			case "lidmaatschapparticulieren" :
				$lidmaatschapParticulieren = $customValue['enumeratedValues'][0]['value'];
				break;
			case "lidmaatschapbedrijven":
				$lidmaatschapBedrijven = $customValue['enumeratedValues'][0]['value'];
				break;
			case "aankoop_saldo" :
				$aankoopSaldo = $customValue['decimalValue'];
				break;
			case "payment_id" :
				$storedPaymentId = $customValue['stringValue'];
				break;
			case "betaald" :
				$betaald = $customValue['enumeratedValues'][0]['internalName'];
				break;
		}
	}

	if (isset($betaald) && ($betaald == "betaald")) {
		$_SESSION['errors'][] = "betaald";
	} else if ($oldPayId != $storedPaymentId) {
		// could not validate the paymentId against the profile fields
		$_SESSION['errors'][] = "inequalIds";
	} else {
		$mollyAccess = getMollyRecord(BASE_URL)['accessKey'];
		// check the status: maybe he paid in the meantime?
		$oldPayment = getPayment($mollyAccess, $oldPayId);
		if (empty($oldPayment)) {
			$_SESSION['errors'][] = "noMollie";				
		} else {
			if ($oldPayment->isPaid()) {
				$_SESSION['errors'][] = "betaald";
			} elseif ($oldPayment->isPending()) {
				$_SESSION['errors'][] = "pending";
			}  else {
				$amount = $lidmaatschapParticulieren + $lidmaatschapBedrijven + $aankoopSaldo;
				//contact mollie, retrieve a new payment object via createPayment. Description includes amounts + username
				$payment = createPayment($mollyAccess, $amount, $lidmaatschapParticulieren + $lidmaatschapBedrijven, $aankoopSaldo, $username, REDIRECT_FINISH . "?from=repay");
				// save payment id to the user's profile with a rest call put /users/{user}.
				$saveResult = saveCustomFieldValue($user, "payment_id", $payment->id, BASE_URL);
				if ($saveResult != null) {
					$_SESSION['errors'][] = "SaveError";
					$httpError = $saveResult;
				} else {
					// forward to payment url of mollie, retrieved from the retrieved payment object.
					header("Location: " . $payment->getPaymentUrl());
					exit;
				}
			}
		}
	}
}


################################## FORM HEADER  ###############################
#################################################################################


?>
<!DOCTYPE html>

<html>
	<head>
		<title><?php echo lang('repay.title')?></title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<link rel="stylesheet" type="text/css" href="style.css">
        <script src="https://code.jquery.com/jquery-3.1.0.min.js" 
				integrity="sha256-cCueBR6CsyA4/9szpPfrX3s49M9vUU5BgtiJj06wt/s=" 
				crossorigin="anonymous"></script>
		<script src="https://ajax.aspnetcdn.com/ajax/jquery.validate/1.15.0/jquery.validate.min.js"></script>
		<script src="https://ajax.aspnetcdn.com/ajax/jquery.validate/1.15.0/additional-methods.min.js"></script>
		<script src="stro_form_validation.js"></script>
		<script src="moment.min.js"></script>
	</head>
<body>

	<h2><?php echo lang('repay.title')?></h2>

	<p class="uitleg">
	<?php echo lang('repay.title.explain')?>
	</p>
	

<?php 

################################## ERROR HANDLING  ###############################
#################################################################################
if (!empty($_SESSION['errors'])) {
	$errors = $_SESSION['errors'];
	unset($_SESSION['errors']);
	echo "<hr>";
	echo "<h3><span class='red'>" . lang('error.title') . "</span></h3>";
	echo "<p class='uitleg error'>";
	
	foreach ($errors as $tempError) {
		switch ($tempError) {
			case "noUser"    : echo lang('error.noUser') . "<br><br>"; break; 
			case "noPayment" : echo lang('error.noPayment') . "<br><br>"; break;
			case "inequalIds": echo lang('error.inequalIds') . "<br><br>"; break;
			case "noMollie"  : echo lang('error.noMollie') . "<br><br>"; break;
			case "SaveError" : echo lang('error.saveError') . $httpError . "<br><br>"; break;
			case "betaald"   : echo lang('error.betaald') . "<br><br>"; break;
			case "pending"   : echo lang('error.pending') . "<br><br>"; break;
			case "unknownUser": echo lang('error.unknownUser') . "<br><br>"; break;
			case "noCustFieldPermission": echo lang('error.noCustomFieldPermission') . "<br><br>"; break;
			default : echo lang('error.repay.unknown') . "<br><br>"; break;;
		}
	}
	
	echo "<br><br>". lang('error.contact');
	echo "</p><hr>";
}




?>	
	

</body>
</html> 