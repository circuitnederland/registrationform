<?php
/**
 * File with functions for registration. 
 */

require "restRequest.php";
include_once 'registration_strings.php';

/**
 * alternative to debug function var_dump, which is lousy in arrays. 
 * Usage: 		echo $pretty($fieldsParticulieren);
 */
$pretty = function($v='',$c="&nbsp;&nbsp;&nbsp;&nbsp;",$in=-1,$k=null)use(&$pretty){
	$r='';
	if(in_array(gettype($v),array('object','array'))){
		$r.=($in!=-1?str_repeat($c,$in):'').(is_null($k)?'':"$k: ").'<br>';
		foreach($v as $sk=>$vl){
			$r.=$pretty($vl,$c,$in+1,$sk).'<br>';
		}
	}else{
		$r.=($in!=-1?str_repeat($c,$in):'').(is_null($k)?'':"$k: ").(is_null($v)?'&lt;NULL&gt;':"<strong>$v</strong>");
	}return$r;
};

################################## RETRIEVING/STORING SERVER DATA  ###############################
#################################################################################
/**
 * gets the communities from the server. 
 * @return a list with community names.  Communities are defined as group names, but 
 * without the " - particulieren" or " - bedrijven" suffixes. Of course, each community
 * is only counted once when a bedrijven and a particulieren group both are met. 
 */
//tested
function getCommunities($baseUrl) {
	$groupsHeaders = array(
			"Accept: application/json",
			"Content-Type: application/json",
			CYCLOS_ACCESS
	);
	$groupsUrl =  $baseUrl . "/api/users/groups-for-registration";
	$params = 	array('fields'=>'name');
	$groupsRequest = new restRequest($groupsUrl, $groupsHeaders, $params, "json", false, "");
	$communities = array();
	$groups = $groupsRequest->exec();
	if (empty($groups)) {
		return null;
	}
	foreach ($groups as $group) {
		$rawGroupName = $group['name'];
		$bedrijf;
		if (endsWith($rawGroupName, 'bedrijven', false)) {
			$bedrijf = true;
		} else if (endsWith($rawGroupName, 'particulieren', false)) {
			$bedrijf = false;
		} else {
			continue; //skip, no bedrijf and no particulier
		}
		$communityName = extractCommunityName($rawGroupName, !$bedrijf);
		//check if it already exists
		if (!in_array($communityName, $communities)) {
			array_push($communities, $communityName);
		}
	}
	return $communities;
}

/**
 * gets the system record with mollie access data from cyclos, via a rest call.  
 * @return an associative array with the custom values. the key is the internal 
 * name of the field in the record, the value is the value of that field. 
 */
function getMollyRecord($baseUrl) {
	$headers = array(
			"Accept: application/json",
			"Content-Type: application/json",
			CYCLOS_ACCESS
	);
	$url =  $baseUrl . "/api/system/records/mollyConnect";
	$request = new restRequest($url, $headers, array(), "json", false, "");
	$response = $request->exec();
	if (empty($response)) {
		return null;
	}
	return $response[0]['customValues'];
}

/**
 * gets the internal group ids from the community's name. It gets both the particulieren
 * and bedrijven group's id. This is retrieved from server. We cannot use the retrieval
 * which was already done by index.php, because most likely the
 * form doesn't come from there but is called with a param url.
 *
 * @param string $community the community, like "Zwolse Pepermunt".
 *
 * @return an associative array, containing the internal ids which can be used as param
 * in cyclos' rest api. The 'bedrijven' key contains the id of the bedrijven group; the
 * 'particulieren' key of the particulieren group. If some group doesn't exist for this
 * community, the value for that key is null.
 */
function getGroupIds($community, $url) {
	$groupsHeaders = array(
			"Accept: application/json",
			"Content-Type: application/json",
			CYCLOS_ACCESS
	);
	$groupsUrl =  $url . "/api/users/groups-for-registration";
	$groupsRequest = new restRequest($groupsUrl, $groupsHeaders, array(), "json", false, "");
	$result = array();
	$groups = $groupsRequest->exec();
	if (empty($groups)) {
		return null;
	}
	foreach ($groups as $group) {
		$rawGroupName = $group['name'];
		if (startsWith($rawGroupName, $community, false)) {
			if (endsWith($rawGroupName, "bedrijven", false)) {
				$result['bedrijven'] = $group['id'];
			} else if (endsWith($rawGroupName, "particulieren", false)){
				$result['particulieren'] = $group['id'];
			}
		}
		if (count($result) >= 2) {
			break;
		}
	}
	return $result;
}

/**
 * gets the user profile of a user via get request: /api/users/<username>
 * @param unknown $user
 * @param unknown $url
 * @return mixed|unknown|string
 */
function getUserProfile($user, $url) {
	$headers = array(
			"Accept: application/json",
			"Content-Type: application/json",
			CYCLOS_ACCESS
	);
	$realUrl =  $url . "/api/users/" . $user;
	$request = new restRequest($realUrl, $headers, array(), "json", false, "");
	$result = $request->exec();
	return $result;
}


/**
 * gets the forNew data from the cyclos server.
 *
 * @param $group - the group to retrieve the data of.
 * @param $sourceCommunity - string which is the community name. It will be placed in the url.
 */
function getForNewResponse($group, $baseUrl) {
	$forNewHeaders = array(
			"Accept: application/json",
			"Content-Type: application/json",
			CYCLOS_ACCESS
	);
	$forNewUrl = $baseUrl . "/api/users/data-for-new";
	$forNewParams = array(group=>$group);
	$forNewRequest = new restRequest($forNewUrl, $forNewHeaders, $forNewParams, "json", false, "");
	$temp = $forNewRequest->exec();
	if ($temp == null) {
		return array();
	}
	if (is_array($temp) && (count($temp)===1) && array_key_exists("entityType", $temp)) {
		return array();
	}
	return $temp;
}

/**
 * verifies the google recaptcha. Returns an empty string if everything is OK, else returns an error string. 
 */
function failCaptcha() {
	$ipUser=$_SERVER["REMOTE_ADDR"];
	$response=$_POST["g-recaptcha-response"];
	if (empty($response)) {
		return "captchaForgotten";
	}
	$secret="1234abcd";	// @todo: Refactor this into a constant from registration_strings.php instead of hardcoding it here.
	//formulate the request URL to reCaptcha API
	$request =  "https://www.google.com/recaptcha/api/siteverify?secret=" . $secret . "&response=" . $response . "&remoteip=" . $ipUser;
	//set the recaptcha answer
	$idealanswer="true";
	//finally make and retrieve the request
	$responserecaptcha = file_get_contents($request);
	//Check if the answer is correct
	if((strstr($responserecaptcha,$idealanswer))) {
		return "";
	} 
	return "captchaFail";
}

/**
 * general url post function
 */
function httpPost($url, $data) {
	$curl = curl_init($url);
	curl_setopt($curl, CURLOPT_POST, true);
	curl_setopt($curl, CURLOPT_POSTFIELDS, http_build_query($data));
	curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
	$response = curl_exec($curl);
	curl_close($curl);
	return $response;
}

/**
 * uploads the user picture via the get request: /api/images/temp
 * @param unknown $access
 * @return NULL[]|Exception[]|unknown[]|NULL
 */
 
function uploadPicture($access) {
	$picSpecified = array_key_exists("uploadedPic", $_FILES) && !empty($_FILES["uploadedPic"]['tmp_name']);
	if ($picSpecified) {
		$curlFile = new CURLFile($_FILES["uploadedPic"]['tmp_name'], 'image/png', "image");
		$data = array(
				"name" => basename($_FILES["uploadedPic"]['tmp_name']),
				"image" => $curlFile
		);
		try {
			$url = BASE_URL . "/api/images/temp";
			$ch = curl_init($url);
			curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "POST");
			curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
			curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
			curl_setopt($ch, CURLOPT_FOLLOWLOCATION, 1);
			curl_setopt($ch, CURLOPT_HTTPHEADER, array(
					'Content-Type: multipart/form-data' ,
					$access
			));
			
			$result = curl_exec($ch);
			$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
		} catch (Exception $e) {
			return array(result=>$e, httpCode=> NULL);
		} finally {
			curl_close($ch);
		}
		return array(result=>$result, httpCode=>$httpCode);
	}
	return null;
}

/**
 * Creates a user in Cyclos.
 * If Mollie returned a payment URL and an unique payment id continue and register the user in Cyclos.
 * This can be done using our REST API. 
 * The documentation of how to register users can be seen here:
 * https://demo.cyclos.org/api#!/Users/createUser
 *
 * @param $json Should be in the format specified in the api docs.
 * @return an associative array with the following key-value pairs:
 * <ul>
 * <li>result: the result returned by the server.
 * <li>httpCode: the http response.
 * </ul>
 *
 */
// probably it could be made a bit more efficient, via the restFunction of some new function alike. Alas...
function createUser($json, $access){
	try {
		$url = BASE_URL . "/api/users";
		$ch = curl_init($url);
		curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "POST");
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
		curl_setopt($ch, CURLOPT_POSTFIELDS, $json);
		curl_setopt($ch, CURLOPT_FOLLOWLOCATION, 1);
		// You can register users as an admin in Cyclos with an access client, using the given string.
		curl_setopt($ch, CURLOPT_HTTPHEADER, array(
				'Content-Type: application/json',
				'Accept: application/json',
				$access
		));
		
		$result = curl_exec($ch);
		$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
	} catch (Exception $e) {
		return array(result=>$e, httpCode=> NULL);
	} finally {
		curl_close($ch);
	}
	return array(result=>$result, httpCode=>$httpCode);
}

function saveCustomFieldValue($user, $fieldName, $fieldValue, $baseUrl) {
	// first we need to retrieve the server's data-for-edit:
	$headers = array(
			"Accept: application/json",
			"Content-Type: application/json",
			CYCLOS_ACCESS
	);
	$realUrl =  $baseUrl . "/api/users/" . $user . "/data-for-edit";
	$request = new restRequest($realUrl, $headers, array(), "json", false, "");
	$dataForEdit = $request->exec();
	// we only need the version from the old data
	$userData = array('customValues' => array($fieldName => $fieldValue));
	$userData['version'] = $dataForEdit['user']['version'];
	// send it back via rest put /users/user
	$json = json_encode($userData);
	try {
		$putUrl = $baseUrl . "/api/users/" . $user;
		$ch = curl_init($putUrl);
		curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "PUT");
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
		curl_setopt($ch, CURLOPT_POSTFIELDS, $json);
		curl_setopt($ch, CURLOPT_FOLLOWLOCATION, 1);
		curl_setopt($ch, CURLOPT_HTTPHEADER, array(
				'Content-Type: application/json',
				'Accept: application/json',
				CYCLOS_ACCESS
		));
		
		$result = curl_exec($ch);
		$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
	} catch (Exception $e) {
		return $httpCode;
	} finally {
		curl_close($ch);
	}
	if ($httpCode != 204) {
		return $httpCode;
	}
	return null;
}


################################## MOLLIE ###############################
#################################################################################


/**
 * creates the payment object for molly.
 */
function createPayment($access, $amount, $lidbijdrage, $startSaldo, $user, $redirectFinish, $webhookUrl) {
	if (empty($startSaldo)) {
		$startSaldo = 0.0;
	}
	$strLidbijdrage = sprintf('%0.2f', $lidbijdrage);
	$strStartSaldo = sprintf('%0.2f', $startSaldo);
	$mollie = new Mollie_API_Client;
	$mollie->setApiKey($access);
	$payment = $mollie->payments->create(
			array(
					'amount'=> $amount,
					'description' => sprintf(lang('payment.description'), $strLidbijdrage, $strStartSaldo, $user),
					'redirectUrl' => $redirectFinish,
					'webhookUrl'  => $webhookUrl,
					'method'      => 'ideal',
					'metadata' => array(
							'user' => $user
					    )
			));
	return $payment;
}

/**
 * gets a Mollie payment by its id. 
 */
function getPayment($access, $paymentId) {
	$mollie = new Mollie_API_Client;
	$mollie->setApiKey($access);
	return $mollie->payments->get($paymentId);
}




################################## DATA MANIPULATION ###############################
#################################################################################


/**
 * gets the amount from the form. The amount is the startsaldo + bijdrage.
 */
function getAmount() {
	$bijdrage = getLidmaatschapsBijdrage();
	return $bijdrage + getAankoop();
}

function getAankoop() {
	if (isParticulier()) {
		return $_SESSION['aankoop_saldo_particulier'];
	} 
	return $_SESSION['aankoop_saldo_bedrijf'];
}

function getNullSafeAankoop() {
	return !empty(getAankoop()) ? getAankoop() : "";
}

function getLidmaatschapsBijdrage() {
	if (isParticulier()) {
		return $_SESSION['lidmaatschapsBedragenParticulier'][$_SESSION['lidmaatschapparticulieren']];
	} 
	return $_SESSION['lidmaatschapsBedragenBedrijf'][$_SESSION['lidmaatschapbedrijven']];
}

/**
 * Create an associative array which maps the internal name to the displayed name and information text.
 * It also contains the 'required' boolean.
 * Note that the associative array contains only custom fields and password and phone fields. It does
 * NOT contain other profile fields like name, address, etc.
 */
function fillFieldsArray($forNewResponse) {
	$fields = array();
	foreach ($forNewResponse['customFields'] as $customField) {
		if ($customField['internalName'] != 'betaald') {
			$fields[$customField['internalName']] = array(
					name => $customField['name'],
					description => isset($customField['informationText']) ? $customField['informationText'] : "",
					required => isset($customField['required']) && $customField['required']
			);
		}
	}
	foreach ($forNewResponse['passwordTypes'] as $passwordType) {
		$fields[$passwordType['internalName']] = array(
				name => $passwordType['name'],
				description => isset($passwordType['description']) ? $passwordType['description'] : "",
				required => true
		);
	}
	$fields[$forNewResponse['phoneConfiguration']['mobilePhone']['kind']] = array(
			name => $forNewResponse['phoneConfiguration']['mobilePhone']['name'],
			description => "",
			required => $forNewResponse['phoneConfiguration']['mobileAvailability'] == "required"
	);
	$fields[$forNewResponse['phoneConfiguration']['landLinePhone']['kind']] = array(
			name => $forNewResponse['phoneConfiguration']['landLinePhone']['name'],
			description => "",
			required => $forNewResponse['phoneConfiguration']['landLineAvailability'] == "required"
	);
	//remove the payment_id field, as that is an exception, which should never be shown
	unset($fields['payment_id']);
	return $fields;
}

/**
 * creates a simple list with internal names of custom fields plus phones. 
 * @param unknown $dataForNew
 */
function getFieldList($forNewResponse) {
	$result = array_column($forNewResponse['customFields'], 'internalName');
	if (isset($forNewResponse['phoneConfiguration']['mobilePhone']['kind'])) {
		array_push($result, $forNewResponse['phoneConfiguration']['mobilePhone']['kind']);
	}
	if (isset($forNewResponse['phoneConfiguration']['landLinePhone']['kind'])) {
		array_push($result, $forNewResponse['phoneConfiguration']['landLinePhone']['kind']);
	}
	return $result;
}

/**
 * Create a simple list with internal names of custom fields, which will be used for determining the custom
 * field order. 
 */
function fillCustomFieldList($dataForNew1, $dataForNew2) {
	if (empty($dataForNew1)) {
		return array_column($dataForNew2['customFields'], 'internalName');
	}
	if (empty($dataForNew2)) {
		return array_column($dataForNew1['customFields'], 'internalName');
	}
	$list1 = array_column($dataForNew1['customFields'], 'internalName');
	$list2 = array_column($dataForNew2['customFields'], 'internalName');
	$fields = array();
 	$list2Counter = 0;
 	// loop list 1
 	foreach ($list1 as $field) {
 		// check if element exists in list 2
 		$relevantList2 = array_slice($list2, $list2Counter);
 		$found = array_search($field, $relevantList2);
 		if ($found !== FALSE) {
 			// we search only in relevantList2, so we need to correct for the complete list.
 			$found = $found + $list2Counter;
			if ($found > $list2Counter) {
				// so we missed previous items in list2, add them
				$toAdd = array_slice($list2, $list2Counter, $found - $list2Counter);
				$fields = array_merge($fields, $toAdd);
			}
			$list2Counter = $found + 1;
 		}
 		// place element and next
		array_push($fields, $field);
 	}
 	// at the end of list1, check if there are still elements left over at $list2
 	$toAdd = array_slice($list2, $list2Counter);
 	$fields = array_merge($fields, $toAdd);
	return $fields;
}

/**
 * gets the data from the session and prepares it to the format as expected bij the Cyclos createUser endpoint.
 */
function getDataFromSession($pictureId, $paymentId){
	$fieldsUsed = isParticulier() ? $_SESSION['fieldsParticulieren'] : $_SESSION['fieldsBedrijven'];
	$mobilePhones = array();
	if (in_array('mobile', $fieldsUsed) && !empty(getNullSafeValue('mobile'))) {	
		array_push($mobilePhones, array(
				"name" => "Standaard mobiel telefoonnummer",
				"number" => getNullSafeValue('mobile')));
	}
	$landLinePhones = array();
	if (in_array('landLine', $fieldsUsed) && !empty(getNullSafeValue('landLine'))) {
		array_push($landLinePhones, array(
				"name" => "Standaard vast telefoonnummer",
				"number" => getNullSafeValue('landLine')));
	}
	$groupId = $_SESSION['groupIds'][getCustomerType()];

	$customValues = array();
	foreach ($fieldsUsed as $customField) {
		switch ($customField) {
			case "broodfonds":
			case "triodosklant":
				$customValues[$customField] = boxChecked($customField);
				break;
			case "aankoop_saldo":
				$customValues[$customField] = getNullSafeAankoop();
				break;
			case "geboortedatum":
				$customValues[$customField] = getNullSafeDate($customField);
				break;
			case "mobile":
			case "landLine":
				break;
			default: 
				$customValues[$customField] = getNullSafeValue($customField);
		}
	}
	$customValues['payment_id'] = $paymentId;

	$passwordsUsed = isParticulier() ? $_SESSION['passwordsParticulieren'] : $_SESSION['passwordsBedrijven'];
	$passwords = array(); 
	foreach ($passwordsUsed as $password) {
		$passwords[] = array(
				"type" => $password,
				"value" => getNullSafeValue($password),
				"forceChange" => "false"
		);
	}
	
	$data = array(
			"name" => getNullSafeValue('name'),
			"email" => getNullSafeValue('email'),
			"customValues" => $customValues,
			"group" => $groupId,
			"addresses" => array(
					array(
							"name" => "Standaard adres",
							"addressLine1" => getNullSafeValue('address'),
							"zip" => getNullSafeValue('zip'),
							"city" => getNullSafeValue('city'),
							"defaultAddress" => "true",
							"hidden" => "false")),
			"mobilePhones" => $mobilePhones,
			"landLinePhones" => $landLinePhones,
			"passwords" => $passwords,
			"images" => (empty($pictureId)) ? null : array($pictureId),
			"acceptAgreement" => "true",
			"skipActivationEmail" => "false"
		);
		if (array_key_exists("username", $_SESSION)) {
			$data['username'] = getNullSafeValue('username');
		}
					
		// if no picture was added, it has an empty value for the images key. So remove in that case
		if (empty($pictureId)) {
			unset($data['images']);
		}
		
		// json encode the data
		$result = json_encode($data);
		return $result;
}


/**
 * returns either $fieldsBedrijven or $fieldsParticulieren, whichever is not empty.
 * If both are not empty $fieldsParticulieren is returned.
 */
function getFields($fieldsBedrijven, $fieldsParticulieren) {
	return (empty($fieldsParticulieren)) ? $fieldsBedrijven : $fieldsParticulieren;
}


/**
 * Stores posted values in session with as key the post key and value the post value.
 */
function storePostValuesInSession() {
	foreach ($_POST as $key => $value) {
		$_SESSION[$key] = $value;
	}
}

/**
 * Returns the value of the key specified in the session or an empty string if it does not exist.
 *
 * @param $key string The key to look for in the session.
 * @return string Value belonging to key or empty string.
 */
function getNullSafeValue($key) {
	return array_key_exists($key, $_SESSION) ? $_SESSION[$key] : "";
}

/**
 * As getNullSafeValue, but converts a date format from DD-MM-YYYY (input) to 
 * YYYY-MM-DD (return).
 *
 */
function getNullSafeDate($key) {
	if (array_key_exists($key, $_SESSION)) {
		$wrongFormattedDate = $_SESSION[$key];
		$a = explode("-", $wrongFormattedDate);
		return $a[2].'-'.$a[1].'-'.$a[0];
	}
	return "";
}

/**
 * Returns the value of a key representing a checkbox. Note that this function expects that a checkbox input is checked if
 * it and only if returns a value "on".
 *
 * @param $key string The key to look for in the session.
 * @return boolean Value true if the checkbox belonging to the key was checked, false if the checkbox was NOT checked.
 */
function boxChecked($key) {
	return array_key_exists($key, $_SESSION) ? $_SESSION[$key] == "on" : false;
}

/**
 * Determine customer type by obtaining type of customer from the session. Seems like Cyclos API uses
 * "particulieren" and "bedrijven" instead of "particulier "and "bedrijf".
 *
 * @return string Either "particulieren" or "bedrijven".
 */
function getCustomerType() {
	return (array_key_exists('customerType', $_SESSION) && $_SESSION['customerType'] == "particulier") ? "particulieren" : "bedrijven";
}

function isParticulier() {
	return (getCustomerType() == "particulieren");
}




################################## RENDERING FUNCTIONS ###############################
#################################################################################
/**
 * displays the informationText for field $internalName with style $style.
 * If the field does not exist, or if no informationText is specified, nothing is shown. Uses the $fields array to
 * check if the informationText is set on the field.
 *
 * @param unknown $internalName
 * @param array $fields an array with all fields retrieved from cyclos. Each entry contains an associate array with internalName as key, and
 *   an array with name and description properties.
 * @param unknown $style
 */
function showInformationText($internalName, $fields, $style) {
	if (!empty($fields[$internalName]) && !empty($fields[$internalName]['description'])) {
		if (!empty($style)) {
			echo "<div class='" . $style . "'>";
		}
		echo "<p class='uitleg'>" . $fields[$internalName]['description'] . "</p>";
		if (!empty($style)) {
			echo "</div>";
		}
	}
}

/**
 * gets the style for a non-custom field. The field is shown if it appears in the profileFieldActions array
 * inside the forNewResponse array.
 * 
 * @param string fieldName the internal name of the field as it appears in profileFieldActions 
 * @param unknown $forNewResponseBedrijf
 * @param unknown $forNewResponseParti
 * @return either hidden, formRow, or organisation-only/retail-only + formRow
 */
function getFieldStyleFormRow($fieldName, $forNewResponseBedrijf, $forNewResponseParti) {
	return getFieldStyle($fieldName, $forNewResponseBedrijf, $forNewResponseParti, "formRow");
}

/**
 * gets the style for a non-custom field. The field is shown if it appears in the profileFieldActions array
 * inside the forNewResponse array.
 * 
 * @param string fieldName the internal name of the field as it appears in profileFieldActions 
 * @param unknown $forNewResponseBedrijf
 * @param unknown $forNewResponseParti
 * @param string a list of styles that should be used when shown.
 * @return either hidden, formRow, or organisation-only/retail-only + formRow
 */
function getFieldStyle($fieldName, $forNewResponseBedrijf, $forNewResponseParti, $style) {
	$show = showField($fieldName, $forNewResponseBedrijf, $forNewResponseParti);
	switch ($show) {
		case "none": return "hidden";
		case "both": return $style;
		default: 
			return $show . " " . $style;
	}
}

/**
 * should a non-custom field be shown? The field is shown if it appears in the profileFieldActions array
 * inside the forNewResponse array.
 * 
 * @param string fieldName the internal name of the field as it appears in profileFieldActions 
 * @param unknown $forNewResponseBedrijf
 * @param unknown $forNewResponseParti
 * @return string none/both/organisation-only/retail-only
 */
function showField($fieldName, $forNewResponseBedrijf, $forNewResponseParti) {
	$bedrijfStyle = (array_key_exists($fieldName, $forNewResponseBedrijf['profileFieldActions'])) ? "organisation-only" : "";
	$particulierStyle = (array_key_exists($fieldName, $forNewResponseParti['profileFieldActions'])) ? "retail-only" : "";
	if (empty($bedrijfStyle) && empty($particulierStyle)) {
		return "none";
	}
	if (!empty($bedrijfStyle) && !empty($particulierStyle)) {
		return "both";
	}
	return $bedrijfStyle . $particulierStyle;
}

/**
 * Shows 2 edits for any passwordtype. To be placed in a loop of passwordTypes. 
 * @param unknown $fieldName
 * @param unknown $fieldsBedrijven
 * @param unknown $fieldsParticulieren
 */
function showPasswordType($fieldName, $fieldsBedrijven, $fieldsParticulieren) {
	$bedrijfStyle = array_key_exists($fieldName, $fieldsBedrijven) ? "organisation-only" : "";
	$particulierStyle = array_key_exists($fieldName, $fieldsParticulieren) ? "retail-only" : "";
	$outerStyle = $bedrijfStyle . $particulierStyle;
	if (!empty($bedrijfStyle) && !empty($particulierStyle)) {
		$outerStyle = "both";
	}
	
	if ($outerStyle != "both") {
		echo "<div class='" . $outerStyle . "'>";
	}

	// class for the input was also pwcheckminlength8 for login and pwchecklength6onlylowercase for paypassword,
	$classStr = "";
	$maxLength = "20";
	switch ($fieldName) {
		case "login":
			$classStr = "FormFields pwcheckminlength8";
			break;
		case "betaalwachtwoord":
			$classStr = "FormFields pwchecklength6onlylowercase";
			$maxLength = "6";
			break;
		case "pin":
			$classStr = "FormFields pwchecklength4onlynumbers";
			$maxLength = "4";
			break;
		default:
			$classStr = "FormFields";
	}
	
	if ($fieldName == "login") {
		echo "<h2>Inlogwachtwoord</h2>";
	} else {
		echo "<h2>" . $fieldName . "</h2>";
	}

	// first item
	showInformationText($fieldName, getFields($fieldsBedrijven, $fieldsParticulieren), ""); 
	echo '<div class="formRow">';	
	echo '    <div class="label above">';
	$fields = getFields($fieldsBedrijven, $fieldsParticulieren);
	echo $fields[$fieldName]['name'];
	showRequired($fieldName, $fields);
	echo '    </div>';
	echo '    <div class="value">';
	echo '        <input id="' . $fieldName . '" class="' . $classStr . '" type="password" size="25" name="' . $fieldName . '" maxlength="20" required';
// 	if (isset($_SESSION[$fieldName])) {
// 		echo " value='" . $_SESSION[$fieldName] . "'";
// 	};
	echo '>';  // close of input
	echo '    </div>';
	echo '</div>';

	// confirm edit
	echo '<div class="formRow">';
	echo '    <div class="label">';
	echo $fields[$fieldName]["name"];
	echo " " . lang('confirm');
	showRequired($fieldName, $fields);
	echo '    </div>';
	echo '    <div class="value">';
	echo '        <input class="FormFields" type="password" size="25" name="' . $fieldName . '" maxlength="' . $maxLength . '" required equalTo="#' . $fieldName . '"';
// 	if (isset($_SESSION[$fieldName])) {
// 		echo " value='" . $_SESSION[$fieldName] . "'";
// 	};
	echo '>'; // close of input
	echo '    </div>';
	echo '</div>';
	
	if ($outerStyle != "both") {
		// closing tag for outermost div
		echo "</div>";
	}
}

/**
 * gets the style to be used, depending if it is to be displayed for bedrijven,
 * for particulieren, for none, or for both.
 */
function getStyle($definedBedrijven, $definedParticulieren, $style) {
	if ($definedBedrijven || $definedParticulieren) {
		$newStyle;
		if ($definedBedrijven) {
			if ($definedParticulieren) {
				// both bedrijven and particulieren
				$newStyle = $style;
			} else {
				//only bedrijven
				if (!empty($style)) {
					$newStyle = "organisation-only " . $style;
				} else {
					$newStyle = "organisation-only";
				}
			}
		} else if ($definedParticulieren) {
			// only particulieren
			if (!empty($style)) {
				$newStyle = "retail-only " . $style;
			} else {
				$newStyle = "retail-only";
			}
		}
		return $newStyle;
	}
	return "";
}

/**
 * shows the informationText either for bedrijven or particulieren, or none, or both.
 */
function showInformationText2($internalName, $fieldsBedrijven, $fieldsParticulieren, $style) {
	$definedBedrijven = !empty($fieldsBedrijven[$internalName]) && !empty($fieldsBedrijven[$internalName]['description']);
	$definedParticulieren = !empty($fieldsParticulieren[$internalName]) && !empty($fieldsParticulieren[$internalName]['description']);
	if ($definedBedrijven || $definedParticulieren) {
		$newStyle = getStyle($definedBedrijven, $definedParticulieren, $style);
		if (!empty($newStyle)) {
			echo "<div class='" . $newStyle . "'>";
		}
		$description = $definedBedrijven ? $fieldsBedrijven[$internalName]['description'] : $fieldsParticulieren[$internalName]['description'];
		echo "<p class='uitleg'>" . $description . "</p>";
		if (!empty($newStyle)) {
			echo "</div>";
		}
	}
}

/**
 * displays the class of the div tag which is either hidden if the field does not exist, or
 * the specified style if it does exist.
 */
function divHideIfNonExisting($internalName, $fields, $style) {
	if (empty($fields[$internalName])) {
		echo "'hidden'";
	} else {
		echo "'" . $style . "'";
	}
}

/**
 * shows the label name + required star from the internal name, either retrieved from
 * particulieren or bedrijven. As fieldnames and required is not dependent on group,
 * it just uses the first $fields array which is not empty.
 *
 * @param unknown $internalName
 * @param unknown $fieldsBedrijven
 * @param unknown $fieldsParticulieren
 */
function showLabel($internalName, $fieldsBedrijven, $fieldsParticulieren) {
	$fields = getFields($fieldsBedrijven, $fieldsParticulieren);
	echo $fields[$internalName]['name'];
	echo showRequired($internalName, $fields);
}

/**
 * Shows the custom field, taking into account if it is to be shown for bedrijven, for
 * particulieren, for both, or for none.
 *
 * @param string $internalName - the internal name for the field, as dictated by cyclos.
 * @param associative array $fieldsBedrijven - the list of custom fields to be shown for bedrijven.
 * @param associative array $fieldsParticulieren - the list of custom fields to be shown for particulieren.
 * @param string $inputExtraClass - extra css class which is to be used for the input tag, besides
 *                the "FormFields" class which is always used.
 * @param string $type the type of the input. this method can handle text (default, used if empty), checkbox,
 * 				  textarea, and probably others too.
 * @param unknown $maxLength - the maxlength to be used on a text input.
 * @param unknown $extraAttribs - extra attributes to be used on the input.
 */
function showCustomField($internalName, $fieldsBedrijven, $fieldsParticulieren, $inputExtraClass, $type, $maxLength, $extraAttribs) {
	showInformationText2($internalName, $fieldsBedrijven, $fieldsParticulieren, "");
	$definedBedrijven = !empty($fieldsBedrijven[$internalName]);
	$definedParticulieren = !empty($fieldsParticulieren[$internalName]);
	$none = !$definedBedrijven && !$definedParticulieren;
	$style = ($none) ? "hidden" : getStyle($definedBedrijven, $definedParticulieren, "formRow");
	$fields = ($definedBedrijven) ? $fieldsBedrijven : $fieldsParticulieren;
	
	echo "<div class='" . $style . "'>"; // surrounding div tag determining if and when it is shown
	// 4 lines of label block
	echo "    <div class='label'>";
	echo $fields[$internalName]['name'];
	echo showRequired($internalName, $fields);
	echo "    </div>";
	
	echo "        <div class='value'>";
	
	if ($type == "textarea") {
		// textarea needs not the input tag, but a textarea tag
		echo "            <textarea class='FormFieldsGroot' ";
	} else {
		if ($type == "checkbox") {
			/*
			 * This is a hack. Normally, a checkbox is only added to the session if it is checked. Checkboxes aren't
			 * added to the post and session if they are not checked. This creates a problem: when a form's checkbox
			 * is checked the first time it is rendered, and unchecked the second time the form is rendered (showing
			 * errors), then because the box is unchecked, it will NOT override the on value of the first rendering in
			 * the session, as unchecked boxes don't add anything.
			 * The trick is to add a hidden field with the same name. If the checkbox is checked, php uses the last
			 * value set to the broodfonds session var (being the "on" of the checked checkbox, thus ignoring the
			 * hidden field). If the checkbox is NOT checked, php ignores the checkbox line, thus using the hidden
			 * field's value "off", overriding any existing previous values.
			 * TESTED: it works.
			 */
			echo "			<input type='hidden' value='off' name='" . $internalName . "'>";
		}
		// input tag for all other types except textarea
		echo "            <input ";
		echo "                class='FormFields" . ((empty($inputExtraClass)) ? "" : (" " . $inputExtraClass)) . "'";
		if (empty($type)) {
			echo "                type='text'";
		} else if ($type == "checkbox") {
			echo "                type='checkbox' value='on'";
		} else {
			echo "                type='" . $type . "'";
		}
	}
	
	echo "                name='" . $internalName . "'"; // name is always done like this, for all types
	
	if (!empty($maxLength)) {
		echo "                maxlength='" . $maxLength . "'"; //maxlength is always done like this. It should not be specified for checkbox
	}
	
	if (!empty($extraAttribs)) {
		echo "                " . $extraAttribs;
	}
	
	if (isRequired($internalName, $fields) && ($type != "checkbox")) {
		echo "                required";
	}
	
	if ($type == "textarea") {
		// for textarea, the session var must be set as content between start and endtag.
		echo ">";
		if (isset($_SESSION[$internalName])) {
			echo $_SESSION[$internalName];
		}
		echo "</textarea>";
	} else {
		// all other types but textarea have the session var specified as attribute
		if (isset($_SESSION[$internalName])) {
			if ($type == "checkbox") {
				// for checkboxes the attribute is "checked"
				if ($_SESSION[$internalName] == "on") {
					echo " checked";
				}
			} else {
				// for other types the session var attribute is "value"
				echo "                value='" . $_SESSION[$internalName] . "'";
			}
		}
		echo "           >"; // end tag for input, inside the else clause, as it is not to be used together with textarea.
	}
	echo "        </div>";
	
	echo "</div>";
}

/**
 * Shows the aankoop_saldo custom field. This needs a separate function, as it is to have a max attribute only for 
 * particulieren. 
 *
 * @param associative array $fieldsBedrijven - the list of custom fields to be shown for bedrijven.
 * @param associative array $fieldsParticulieren - the list of custom fields to be shown for particulieren.
 * @param string $custType - either bedrijf or particulier
 */
function showAankoop($fieldsBedrijven, $fieldsParticulieren, $maxAankoop) {
	$definedBedrijven = !empty($fieldsBedrijven['aankoop_saldo']);
	$definedParticulieren = !empty($fieldsParticulieren['aankoop_saldo']);
	$none = !$definedBedrijven && !$definedParticulieren;
	$outerStyle = ($none) ? "hidden" : getStyle($definedBedrijven, $definedParticulieren, "both");
	if ($outerStyle != "both") {
		echo "<div class='" . $outerStyle . "'>";
	}
	echo "	<h2>Saldo aankopen</h2>";
	if ($outerStyle != "both") {
		echo "</div>";
	}
	$internalName = "aankoop_saldo";
	showInformationText2($internalName, $fieldsBedrijven, $fieldsParticulieren, "");
	$style = ($none) ? "hidden" : getStyle($definedBedrijven, $definedParticulieren, "formRow");
	$fields = ($definedBedrijven) ? $fieldsBedrijven : $fieldsParticulieren;
	$custType = (empty($maxAankoop)) ? "bedrijf" : "particulier";
	$fieldName = $internalName . "_" . $custType;
	
	echo "<div class='" . $style . "'>"; // surrounding div tag determining if and when it is shown
	// 4 lines of label block
	echo "    <div class='label'>";
	echo $fields[$internalName]['name'];
	echo showRequired($internalName, $fields);
	echo "    </div>";
	echo "        <div class='value'>";
	echo "            <input class='FormFields aankoop aankoop" . $custType ."'";
	echo "                name='" . $fieldName . "'"; 
	echo "                maxlength='40' size='25'";

/**	if ($custType=="particulier") {
		echo "                max='" . $maxAankoop . "'";
	} 
Commented by Roder and Andre because this caused a double validation. In case for example a dot is typed the business message appeared.
**/
	if (isRequired($internalName, $fields)) {
		echo "                required";
	}
	if (isset($_SESSION[$fieldName])) {
		echo "                value='" . $_SESSION[$fieldName] . "'";
	}
	echo "           >"; // end tag for input, inside the else clause, as it is not to be used together with textarea.
	echo "        </div>";
	echo "</div>";
}

/**
 * shows the lidmaatschap edits, either for particulieren or bedrijven. 
 * @param unknown $key a string with the internal name of the field, either lidmaatschapparticulieren or lidmaatschapbedrijven
 * @param unknown $fields either $fieldsParticulieren or $fieldsBedrijven.
 * @param $noServerContact a boolean which is true if there was no server contact.
 * @param $bedragen an associative array with the custom field values for the field.
 * @param $defaultValue the default value for the fiekd  
 */
function showLidmaatschap($key, $fields, $noServerContact, $bedragen, $defaultValue) {
	$style = ($key == 'lidmaatschapparticulieren') ? "retail-only" : "organisation-only";
	showInformationText($key, $fields, $style);
	echo "<div class='formRow " . $style . "'>";
	echo "   <div class='label above bijdrage'>";
	echo $fields[$key]['name']; 
	showRequired($key, $fields);
	echo "</div>";
	echo "		<div class='value radio above' >";
	/**
	 * run over all possible values for lidmaatschap en place a radio button for each of them. 
	 */
	if (!$noServerContact) {
		$counter = 1;
		foreach ($bedragen as $bedragName => $bedragValue) {
			echo "<input id='" . $bedragName . "' type='radio' name='" . $key . "' value='" . $bedragName . "'";
			if (isset($_SESSION[$key])) {
				if ($_SESSION[$key] == $bedragName) {
					echo " checked"; // if selected previously, select it again
				}
			} else if (!empty($defaultValue)) {
				if ($defaultValue == $bedragName) {
					echo " checked"; //check default if nothing selected
				}
			} else if ($counter == 2) {
				echo " checked"; //if none is selected and no default known, select second item.
			};
			echo ">";
			echo "<label for='" . $bedragName . "'>&euro; " . $bedragValue . "</label>";
			$counter++;
		}
	}
	echo "	</div>";
	echo "</div>";
}

/**
 * shows the branche information. The function can determine if it is to be shown for
 * bedrijven, for particulieren, for both, or for none.
 *
 * @param unknown $fieldsBedrijven
 * @param unknown $fieldsParticulieren
 * @param unknown $branchesInfo
 */
function showBrancheField($fieldsBedrijven, $fieldsParticulieren, $branchesInfo) {
	showInformationText2("branche", $fieldsBedrijven, $fieldsParticulieren, "");
	$definedBedrijven = !empty($fieldsBedrijven["branche"]);
	$definedParticulieren = !empty($fieldsParticulieren["branche"]);
	$none = !$definedBedrijven && !$definedParticulieren;
	$style = ($none) ? "hidden" : getStyle($definedBedrijven, $definedParticulieren, "formRow");
	$fields = ($definedBedrijven) ? $fieldsBedrijven : $fieldsParticulieren;
	
	echo "<div class='" . $style . "'>";
	echo "    <div class='label'>";
	echo $fields["branche"]['name'];
	echo showRequired("branche", $fields);
	echo "    </div>";
	
	echo "    <div class='value'>";
	echo "        <select name='branche'" . (isRequired("branche", $fields) ? " required" : "") . ">";
	
	$lastGroup = "";
	$firstLoop = true;
	foreach ($branchesInfo['possibleValues'] as $brancheValue) {
		$categoryName = $brancheValue['category']['name'];
		if ($categoryName != $lastGroup) {
			if (!$firstLoop) {
				$firstLoop = false;
				echo "</optgroup>";
			}
			echo "<optgroup class='Optiongroup' label='" . $categoryName . "'>";
			$lastGroup = $categoryName;
		}
		$internalBrancheName = $brancheValue['internalName'];
		echo "<option class='selectOption' value='" . $internalBrancheName . "'";
		if ($_SESSION['branche'] == $internalBrancheName) {
			echo " selected ";
		}
		echo ">" . $brancheValue['value'] . "</option>";
	}
	if (!$firstLoop) {
		$firstLoop = false;
		echo "</optgroup>";
	}
	
	echo "        </select>";
	echo "    </div>";
	echo "</div>";
}

/**
 * checks if a field is required according to the fields array which contains data from server.
 * returns false if the field is unknown.
 * Works with custom fields, password and phone fields.
 */
function isRequired($internalName, $fields) {
	if (!empty($fields[$internalName]) && !empty($fields[$internalName]['required'])) {
		return $fields[$internalName]['required'];
	} else {
		return false;
	}
}

/**
 * checks if the customField is required, and if so, prints the red *.
 * Also works with password and phone fields, not with address and other fields.
 */
function showRequired($internalName, $fields) {
	if (isRequired($internalName, $fields)) {
		echo ' <span class="red">*</span>';
	}
}

function showHttpCode($httpCode, $body) {
	switch ($httpCode) {
		case 401:
		case 403:
		case 500:
			echo lang("error." . $httpCode);
			if (!empty($body)) {
				$errorResult = json_decode($body, true);
				if (!empty($errorResult["exceptionMessage"])) {
					echo ": " . $errorResult["exceptionMessage"];
				}
			}
			break;
		case 404:
			echo lang("error.404" . " - ");
			if (!empty($body)) {
				$errorResult = json_decode($body, true);
				if (!empty($errorResult["entityType"])) {
					echo ": " . $errorResult["entityType"];
				}
			}
			break;
		case 422:
			if (!empty($body)) {
				handle422($body);
			} else {
				lang("error.422");
			}
			break;
		case NULL:
			echo lang('error.httpNull') . "<br>";
			break;
		default:
	}
}

/**
 * shows an error in a list row.
 * It shows the error as follows:
 * <li><span class="redError">error keyword:</span> error explanation...</li>.
 * The $langKey parameter is the language key for the explanation. Besides that,
 * the same language key but with a ".bold" needs to be present for the keyword.
 * Of course, this must be nested in an <ul> or <ol> tag, probably together with subsequent other calls
 * to this same function.
 * @param unknown $langKey
 */
function showErrorLi($langKey) {
	echo '<li><span class="redError">';
	echo lang($langKey . ".bold");
	echo ':</span> ';
	echo lang($langKey);
	echo '</li>';
}

function handle422($rawErrorResult) {
	//echo lang('error.explanation');
	$errorResult = json_decode($rawErrorResult, true);
	switch ($errorResult["code"]) {
		case "validation":
			//echo lang('error.list.heading');
			echo "<ul class='error'>";
			if (!empty($errorResult["generalErrors"])) {
				foreach ($errorResult["generalErrors"] as $generalError) {
					echo "<li>" . $generalError;
				}
			}
			if (!empty($errorResult["propertyErrors"])) {
				foreach ($errorResult["propertyErrors"] as $propertyKey => $propertyValue) {
					echo "<li>" . $propertyValue[0];
				}
			}
			if (!empty($errorResult["customFieldErrors"])) {
				foreach ($errorResult["customFieldErrors"] as $customKey => $customValue) {
					echo "<li>" . $customValue[0];
				}
			}
			echo "</ul>";
			break;
		case "maxItems":
			echo lang('error.maxItems');
			if (!empty($errorResult["maxItems"])) {
				echo "<br>" . lang('error.maxItems.max') . " " . $errorResult["maxItems"] . ".";
			}
			break;
		case "queryParse":
			echo lang('error.queryParse');
			if (!empty($errorResult["value"])) {
				echo "<br>" . lang('error.queryParse.text') . " " . $errorResult["value"] . ".";
			}
			break;
		case "dataConversion":
			echo lang('error.dataConverse');
			if (!empty($errorResult["value"])) {
				echo "<br>" . lang('error.dataConverse.item') . " " . $errorResult["value"] . ".";
			}
			break;
		default:
			echo lang('error.unknown');
	}
	echo "</ul>";
}


################################## UTILITY FUNCTIONS ###############################
#################################################################################

/**
 * Reads the fullGroupName, removes "particulieren" or "bedrijven" from the end, and then removes trailing whitespace, -, _, or :
 */
//Tested
function extractCommunityName($fullGroupName, $isParticulier) {
	$remove = ($isParticulier) ? strlen("particulieren") : strlen("bedrijven");
	$halfStripped = substr($fullGroupName, 0, 0 - $remove);
	return rtrim($halfStripped, ' -_:');
}

/**
 * tests if a string ends with a substring
 */
//tested
function endsWith($haystack, $needle, $case=true) {
	$len = strlen($needle);
	if ($len == 0) {
		return true;
	} else {
		if ($case)
			return (substr($haystack, -$len) === $needle);
			else
				return (strcasecmp(substr($haystack, -$len), $needle) === 0);
	}
}

/**
 * tests if a string starts with a substring
 */
function startsWith($haystack, $needle, $case=true) {
	$len = strlen($needle);
	if ($len == 0) {
		return true;
	} else {
		if ($case)
			return (strpos($haystack, $needle) === 0);
			else
				return (strncasecmp($haystack, $needle, $len) === 0);
	}
}



?>
