<?php

################################## INITIALIZATION ###############################
#################################################################################
include_once 'registration_strings.php';
include_once 'registration_functions.php';

define('REDIRECT_PAY', "registration_pay.php");
define('REDIRECT_COMMUNITY', "index.php");

session_start();
header ('Content-type: text/html; charset=utf-8');

// store the present url so we can redirect back to it in case something goes wrong
$_SESSION['backURL'] = $_SERVER['REQUEST_URI'];

################################ COMMUNITY  ##########################################
######################################################################################
/**
 * In this block the form tries to retrieve the community. It does that in the following
 * ways: 
 * 1) First, the form expects community specification via the url. In this way each 
 * community can make their own link to the registration form:
 * E.g.: https://circuitnederland.nl/register.php?community=Utrecht
 * 
 * 2) If no community is specified via the url, then the form expects that it is directed
 * to here from index.php. It tries to read the posted data from 
 * that form, via the "community" field. 
 * 
 * 3) If that doesn't succeed either, the form supposes that index.php
 * was not yet shown, and it redirects to that form.
 * 
 * Beware that the url parameter for Rijn & Vallei needs to be Rijn%20%26%20Vallei.  
 */

// Use the null coalescing operator to get the first non-null case without undefined index notices.
$community = $_GET['community'] ?? $_POST['community'] ?? $_SESSION['community'] ?? '';

if (empty($community)) {
	header("Location: " . REDIRECT_COMMUNITY);
	exit;
} else {
	$_SESSION['community'] = $community;
}


################################## RETRIEVING SERVER DATA  ###############################
#################################################################################
try {
	$_SESSION['groupIds'] = getGroupIds($community);

	$branchesInfo = array();
	$forNewResponseBedrijf = array();
	$lidmaatschapsBedragenBedrijf = array();
	$lidmaatschapsBedragenBedrijfDefaultValue = '';
	$noBedrijven = (!isset($_SESSION['groupIds']['bedrijven']));
	if (!$noBedrijven) {
		$forNewResponseBedrijf = getForNewResponse($_SESSION['groupIds']['bedrijven']);
		foreach ($forNewResponseBedrijf['customFields'] as $customField) {
			switch ($customField['internalName']) {
				// this means that branchesInfo is read only for bedrijven.
				case "branche" :
					$branchesInfo = $customField;
					break;
				case "lidmaatschapbedrijven":
					foreach ($customField['possibleValues'] as $possibleValue) {
						$lidmaatschapsBedragenBedrijf[$possibleValue['internalName']] = $possibleValue['value'];
					}
					$lidmaatschapsBedragenBedrijfDefaultValue = $customField['defaultValue'];
					break;
			}
		}
	}

	$forNewResponseParti = array();
	$lidmaatschapsBedragenParticulier = array();
	$lidmaatschapsBedragenParticulierDefaultValue = '';
	$noParticulieren = (!isset($_SESSION['groupIds']['particulieren']));
	if (!$noParticulieren) {
		$forNewResponseParti = getForNewResponse($_SESSION['groupIds']['particulieren']);
		foreach ($forNewResponseParti['customFields'] as $customField) {
			if ($customField['internalName'] == "lidmaatschapparticulieren") {
				foreach ($customField['possibleValues'] as $possibleValue) {
					$lidmaatschapsBedragenParticulier[$possibleValue['internalName']] = $possibleValue['value'];
				}
				$lidmaatschapsBedragenParticulierDefaultValue = $customField['defaultValue'];
			}
		}
	}

	// create associative arrays which map the internal name to the displayed name and information text.
	$fieldsBedrijven = array();
	$fieldsParticulieren = array();
	// a general list determining the order of custom fields by their internal name
	$customFieldList = array();
	// a general list with all possible passwords - as password order is not defined by cyclos, the order is not important.
	$passwordList = array();
	$_SESSION['passwordsBedrijven'] = array();
	$_SESSION['passwordsParticulieren'] = array();
	$_SESSION['privateFieldsBedrijven'] = array();
	$_SESSION['privateFieldsParticulieren'] = array();
	if (!$noBedrijven) {
		$fieldsBedrijven = fillFieldsArray($forNewResponseBedrijf);
		$_SESSION['fieldsBedrijven'] = getFieldList($forNewResponseBedrijf);
		$_SESSION['passwordsBedrijven'] = array_column($forNewResponseBedrijf['passwordTypes'], 'internalName');
		$_SESSION['privateFieldsBedrijven'] = getPrivacyInfo($forNewResponseBedrijf);
	}
	if (!$noParticulieren) {
		$fieldsParticulieren = fillFieldsArray($forNewResponseParti);
		$_SESSION['fieldsParticulieren'] = getFieldList($forNewResponseParti);
		$_SESSION['passwordsParticulieren'] = array_column($forNewResponseParti['passwordTypes'], 'internalName');
		$_SESSION['privateFieldsParticulieren'] = getPrivacyInfo($forNewResponseParti);
	}
	$customFieldList = fillCustomFieldList($forNewResponseBedrijf, $forNewResponseParti);
	$passwordList = array_unique(array_merge($_SESSION['passwordsBedrijven'], $_SESSION['passwordsParticulieren'] ));
} catch (Exception $e) {
	// Set the error array if it was not set already.
	if (empty($_SESSION['errors'])) {
		$_SESSION['errors'] = array('errorType' => 'fatal', 'msg' => 'Onbekende fout');
	}
}

// No longer use the maxAankoopPart, because there is no maximum for particulieren anymore.
// $maxAankoop = $mollieRecord['maxAankoopPart'];
// if (empty($maxAankoop)) {
	// $maxAankoop = 150;
// }

/*
 * Field validation is done using jQuery validation.
 */

################################## FORM HEADER  ###############################
#################################################################################


?>
<!DOCTYPE html>

<html>
	<head>
		<title><?php echo lang('title')?></title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<link rel="stylesheet" type="text/css" href="style.css">
        <script src="https://code.jquery.com/jquery-3.1.0.min.js" 
				integrity="sha256-cCueBR6CsyA4/9szpPfrX3s49M9vUU5BgtiJj06wt/s=" 
				crossorigin="anonymous"></script>
		<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-validate/1.17.0/jquery.validate.min.js"></script>
		<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-validate/1.17.0/additional-methods.min.js"></script>
		<script src="stro_form_validation.js?"></script>
		<script src="https://www.google.com/recaptcha/api.js" async defer></script>
		<script src="register.js"></script>
		<script src="moment.min.js"></script>
		<script language="javascript" type="text/javascript">
			<!--
				function popitup(url, name) {
					var width = screen.width * 0.7;
					var height = screen.height * 0.7; 
					var left = (screen.width/2)-(width/2);
					var top = (screen.height/2)-(height/2);
					newwindow=window.open(url,name,'toolbar=no,location=no,status=no,menubar=no,width=' + width + ', height=' + height + ', top=' + top + ', left=' + left);
					if (window.focus) {newwindow.focus()}
					return false;
				}
			// -->
		</script>
	</head>
<body>

<div id="formContainer">

	<h2><?php echo lang('title') . " " . $community ?></h2>

	<p class="uitleg">
	<?php echo lang('title.explain')?>
	</p>

<?php

################################## ERROR HANDLING  ###############################
#################################################################################

include 'show_errors.php';


################################## FORM ELEMENTS   ###############################
#################################################################################

?>	
	
<form method="POST" enctype="multipart/form-data" action="<?php echo REDIRECT_PAY;?>">

	<div class="<?php $class = ($noBedrijven || $noParticulieren) ? " hidden" : "formRow" ; echo $class;?>">
		<div class="label"><?php echo lang('bedrijfpart')?> <span class="red">*</span></div>
		<div class="value radio above">
			<?php
				$preselectParti = isset($_SESSION["customerType"]) && ($_SESSION["customerType"] == "particulier");
				if (!$noBedrijven) {
					echo "<input id='bedrijf' type='radio' name='customerType' value='bedrijf'";
					if (!$preselectParti) {
						echo " checked";
					}
					echo ">";
					echo "<label for='bedrijf'>zakelijk</label>";
				}
				if (!$noParticulieren) {
					echo "<input id='particulier' type='radio' name='customerType' value='particulier'";
					if ($preselectParti) {
						echo " checked";
					}
					echo ">";
					echo "<label for='particulier'>particulier</label>";
				}
			?>
		</div>
	</div>

	<div class="formRow">
	    <!--  Field name is always displayed -->
		<div class="label retail-only"><?php echo lang('field.name.part')?> <span class="red">*</span></div>
		<div class="label organisation-only"><?php echo lang('field.name.bedr')?> <span class="red">*</span></div>
		<div class="value">
			<input class="FormFields" type="text" name="name" maxlength="40" required
				<?php 
					if (isset($_SESSION["name"])) {
						echo " value='" . $_SESSION["name"] . "'"; 
					}; 
				?>
			>
		</div>
	</div>

	<!--  This username field may or may not be displayed -->
	<p class="<?php echo getFieldStyle("username", $forNewResponseBedrijf, $forNewResponseParti, "uitleg")?>">
		<?php echo lang('field.username.uitleg')?>
	</p>
	<div class="<?php echo getFieldStyleFormRow("username", $forNewResponseBedrijf, $forNewResponseParti)?>">
		<div class="label"><?php echo lang('field.username')?> <span class="red">*</span></div>
		<div class="value">
			<input class="FormFields username" type="text" name="username" maxlength="40"
				<?php 
					/* we don't set this field to required in the form, because that is complicated
					 * when it is visible for one group and not for the other. When the user doesn't
					 * fill it in, the cyclos validation will catch that error and report back.
					 */
					if (isset($_SESSION["username"])) {
						echo " value='" . $_SESSION["username"] . "'"; 
					}; 
				?>
			>
		</div>
	</div>

    <!--  Field email is always displayed -->
	<div class="formRow">
		<div class="label"><?php echo lang('field.email')?> <span class="red">*</span></div>
		<div class="value">
			<input class="FormFields" id="emailFirst" type="email" name="email" maxlength="40" required
				<?php 
					if (isset($_SESSION["email"])) {
						echo " value='" . $_SESSION["email"] . "'"; 
					}; 
				?>
			>
		</div>
	</div>
	
<!--  Field email.confirm is always displayed -->
	<div class="formRow">
		<div class="label"><?php echo lang('field.email.confirm')?> <span class="red">*</span></div>
		<div class="value">
			<input class="FormFields" type="email" name="email" maxlength="40" required equalTo='#emailFirst'
				<?php 
					if (isset($_SESSION["email"])) {
						echo " value='" . $_SESSION["email"] . "'"; 
					}; 
				?>
			>
		</div>
	</div>

    <!--  Field mobile is always displayed -->
	<div class="formRow">
	    <div class="label"><?php showLabel("mobile", $fieldsBedrijven, $fieldsParticulieren) ?></div>
		<div class="value">
			<input class="FormFields mobile" name="mobile" maxlength="40" 
				<?php
					// as required is not depending on particulieren or bedrijven, we can use either one
					// of the fields configuration. We only need to check if the fields array is empty, 
					// in case one of both types does not exist.
					if (isRequired("mobile", getFields($fieldsBedrijven, $fieldsParticulieren))) {
						echo " required";
					}
					if (isset($_SESSION["mobile"])) {
						echo " value='" . $_SESSION["mobile"] . "'"; 
					}; 
				?>
			>
		</div>
	</div>
	
    <!--  Field landline is always displayed -->
	<div class="formRow">
	    <div class="label"><?php showLabel("landLine", $fieldsBedrijven, $fieldsParticulieren) ?></div>
		<div class="value">
			<input class="FormFields landline" name="landLine" maxlength="40"
				<?php 
					if (isRequired("landLine", getFields($fieldsBedrijven, $fieldsParticulieren))) {
						echo " required";
					}
					if (isset($_SESSION["landLine"])) {
						echo " value='" . $_SESSION["landLine"] . "'"; 
					}; 
				?>
			>
		</div>
	</div>
		
    <!--  Field address is always displayed -->
	<div class="formRow">
		<div class="label"><?php echo lang('field.address')?> <span class="red">*</span></div>
		<div class="value">
			<input class="FormFields" type="text" name="address" maxlength="40" required
				<?php 
					if (isset($_SESSION["address"])) {
						echo " value='" . $_SESSION["address"] . "'"; 
					}; 
				?>
			>
		</div>
	</div>	
		
    <!--  Field zip is always displayed -->
	<div class="formRow">
		<div class="label"><?php echo lang('field.zip')?> <span class="red">*</span></div>
		<div class="value">
			<input class="FormFields postcode" type="text" name="zip" maxlength="6" placeholder="Bijvoorbeeld 1234AA" required
				<?php 
					if (isset($_SESSION["zip"])) {
						echo " value='" . $_SESSION["zip"] . "'"; 
					}; 
				?>
			>
		</div>
	</div>	
	
    <!--  Field city is always displayed -->
	<div class="formRow">
		<div class="label"><?php echo lang('field.city')?> <span class="red">*</span></div>
		<div class="value">
			<input class="FormFields" type="text" name="city" maxlength="40" required
				<?php 
					if (isset($_SESSION["city"])) {
						echo " value='" . $_SESSION["city"] . "'"; 
					}; 
				?>
			>
		</div>
	</div>	

	<div class="<?php echo getFieldStyleFormRow("image", $forNewResponseBedrijf, $forNewResponseParti)?>">
		<div class="label retail-only"><?php echo lang('field.pic.retail')?></div>
		<div class="label organisation-only"><?php echo lang('field.pic.org')?></div>
		<div class="value">
			<!-- this is the edit with the choose file text. It will show the file chosen. Disabled, because it is only there 
			to show the text to the user. -->
			<input id="uploadFile" class="FormFields" placeholder="Choose File" disabled="disabled" />
			<div class="fileUpload">
				<!--  This is the button text "upload" -->
				<div class="uploadText"><?php echo lang('field.upload')?></div>
				<!--  This is the actual file edit, but it has the uploadtext displayed over it.
				This edit takes care that the file is actually uploaded. Therefore, it MUST have a name attribute. -->
				<input id="uploadBtn" name="uploadedPic" type="file" class="upload" accept="image/png,image/jpeg,image/gif" />
			</div>
            <div id="uploadFileFeedback" style="display:none;"><p class="error">Ondersteunde bestandstypen: *.png, *.jpg, *.jpeg, *.jfif, *.pjpeg, *.pjp, *.gif</p></div>
		</div>
	</div>

	
	<!--  ############################################ CUSTOM FIELDS #################################################### -->
	<!--  ############################################################################################################### -->
	
	<!-- Custom fields must be placed in the order dictated by cyclos configuration -->
	
	<?php
		$lidmaatschapHeadingShown = False; 
		foreach ($customFieldList as $customField) {
			switch ($customField) {
				
				case "contactpersoon":
					showCustomField($customField, $fieldsBedrijven, $fieldsParticulieren, "", "", 100, "");
					break;

				case "website": 
					showCustomField($customField, $fieldsBedrijven, $fieldsParticulieren, "", "url", 40, "");
					break;
				
				case "iban":
					showCustomField($customField, $fieldsBedrijven, $fieldsParticulieren, "", "", 40, "data-rule-iban='true'");
					break;
				
				case "kvknummer":
					showCustomField($customField, $fieldsBedrijven, $fieldsParticulieren, "kvkNummer", "", 40, "");
					//TODO X: add field for uploading the kvk bewijs van inschrijving. Await decision by Roder/Gerben/Henk
					break;
				
				case "broodfonds":
				case "triodosklant":
					showCustomField($customField, $fieldsBedrijven, $fieldsParticulieren, "", "checkbox", 40, "");
					break;
				
				case "branche":
					showBrancheField($fieldsBedrijven, $fieldsParticulieren, $branchesInfo);
					break;
					
				case "omschrijving":
					showCustomField($customField, $fieldsBedrijven, $fieldsParticulieren, "", "textarea", 350, "rows='4'");
					break;
					
				case "geboortedatum":
					showCustomField($customField, $fieldsBedrijven, $fieldsParticulieren, "dateNl", "", 10, "placeholder='DD-MM-JJJJ'");
					break;

				case "circ_payments":
					showAcceptPayments($customField, $fieldsBedrijven, 'bedrijven');
					break;
					
				case "lidmaatschapsbijdrage":
					echo "<hr>";
					echo "<h2>Bijdrage</h2>";
					showLidmaatschapStaffels($customField, $fieldsParticulieren, 'particulieren');
					showLidmaatschapStaffels($customField, $fieldsBedrijven, 'bedrijven');
					break;
					
				case "lidmaatschapparticulieren":
					if ($lidmaatschapHeadingShown === False) {
						$lidmaatschapHeadingShown = True;
						echo "<hr>";
						echo "<h2>Bijdrage</h2>";
					}
					showLidmaatschap($customField, $fieldsParticulieren, $lidmaatschapsBedragenParticulier, $lidmaatschapsBedragenParticulierDefaultValue);
					break;
					
				case "lidmaatschapbedrijven":
					if ($lidmaatschapHeadingShown === False) {
						$lidmaatschapHeadingShown = True;
						echo "<hr>";
						echo "<h2>Bijdrage</h2>";
					}
					showLidmaatschap($customField, $fieldsBedrijven, $lidmaatschapsBedragenBedrijf, $lidmaatschapsBedragenBedrijfDefaultValue);
					break;
					
				case "aankoop_saldo":
					showAankoop(array(), $fieldsParticulieren, 'no_maximum');
					showAankoop($fieldsBedrijven, array(), null);
					break;
					
				default: // for actiecode,   
					showCustomField($customField, $fieldsBedrijven, $fieldsParticulieren, "", "", 40, "");
					
			}
		}
	?>
	
<hr>

	<?php
		foreach ($passwordList as $passwordType) {
			showPasswordType($passwordType, $fieldsBedrijven, $fieldsParticulieren);
		}
	?>

<hr>

	<h2>Inschrijving bevestigen</h2>
	<p class="uitleg"><?php echo lang('field.validation')?></p>
<div class="formRow validatie">
		<div class="label above"><?php echo lang('validation')?> <span class="red">*</span></div>
		<div class="value">
			<div class="g-recaptcha" data-sitekey="6LcfwDMUAAAAAFbNenr1DHNg40gXM5FQG-bY7sCj"></div>
		</div>
</div>	
	
<hr>
	<div class="formRow checkbox">
		<div class="value incasso">
			<input id="checkVoorwaarde" type="checkbox" name="checkVoorwaarde" value="checkVoorwaarde" required
				<?php 
					if (isset($_SESSION["checkVoorwaarde"])) {
						echo " checked"; 
					}; 
				?>
			>
			<label id="checkincassolabel" for="checkVoorwaarde"><?php
				$onClickText = 'return popitup("https://www.circuitnederland.nl/algemene-voorwaarden/", "Voorwaarden")';
				$linkAsText = "<a href='https://www.circuitnederland.nl/algemene-voorwaarden/' onclick='" . $onClickText . "'>" . lang('click.here') . "</a>";
				echo sprintf(lang('field.agree'), $linkAsText);
			?></label>
		</div>	
	</div>
	
	<div class="formRow">
		<div class="label"></div>
		<div class="value">
			<input id="button" type="submit" value="<?php echo lang('submit.pay')?>">
		</div>
	</div>
	
	</form>  
</div>

<script>
	document.getElementById("uploadBtn").onchange = function () {
		var uploadPathName = this.value.replace("C:\\fakepath\\", "");
		var uploadedFileExtension = uploadPathName.split('.').pop();
		var allowedExtensions = ['png', 'jpg', 'jpeg', 'jfif', 'pjpeg', 'pjp', 'gif'];
		var uploadedFileNameDiv = document.getElementById("uploadFile");
        var fileUploadFeedback = document.getElementById("uploadFileFeedback");
		if (allowedExtensions.indexOf(uploadedFileExtension) != -1){ 
			uploadedFileNameDiv.value = uploadPathName;
			fileUploadFeedback.style.display = "none";
		} else {
            // Don't set the file if it is not the proper type.
			uploadedFileNameDiv.value = "";
            this.value = "";
			fileUploadFeedback.style.display = "block";
		}
	}
</script>


</body>
</html> 