<?php
/**
 * File with functions for registration. 
 */

include_once 'registration_strings.php';

################################## RETRIEVING/STORING SERVER DATA  ###############################
#################################################################################

/**
 * Gets a list of groups that are open for registration.
 * 
 * @return array	Array with communitynames as keys each containing an array with 'bedrijven' or 'particulieren' and its Cyclos group id.
 */
function _getCommunities() {
	$url = "/api/users/groups-for-registration";
	$result = restRequest($url, array(), "json", false);
	handleErrors($result, '200');
	$groups = $result['response'];

	// Build an array of communites with their name as key and an array of their bedrijven/particulieren group id's as value.
	$communities = array();
	foreach ($groups as $group){
		$groupNameParts = explode(' - ', $group['name']);
		if (count($groupNameParts) != 2) {
			// This Cyclos group does not comply with the naming convention for group names: GroupName - [Bedrijven|Particulieren]. So skip this group.
			continue;
		}
		list($communityName, $bedrijfOrParticulier) = $groupNameParts;
		if ($bedrijfOrParticulier != 'Bedrijven' && $bedrijfOrParticulier != 'Particulieren'){
			// This Cyclos group does not comply with the naming convention, so skip this group.
			continue;
		}
		$communities[$communityName][strtolower($bedrijfOrParticulier)] = $group['id'];
	}

	// If there are no communities at all, something is wrong with the server.
	if (empty($communities)) raiseError('error.noServerContact');

	return $communities;
}
/**
 * Gets the names of the communities that are open for registration.
 * 
 * @return array	Array with community names.
 */
function getCommunityNames() {
	$communities = _getCommunities();
	
	return array_keys($communities);
}

/**
 * Gets the group id of both the particulieren and bedrijven group for the given communityname.
 *
 * @param string community	The communityname, like "Zwolse Pepermunt", case-insensitive.
 * @return array			Array containing the ids of the bedrijven and/or the particulieren group.
 */
function getGroupIds($community) {
	// Get the communities, with lowercase communitynames as keys.
	$communities = array_change_key_case(_getCommunities());
	// Convert the given communityname to lowercase as well.
	$communityLowerCase = strtolower($community);

	if (!isset($communities[$communityLowerCase])) {
		raiseError(null, sprintf(lang('error.unknownCommunity'), htmlspecialchars($community), REDIRECT_COMMUNITY));
	}

	return $communities[$communityLowerCase];
}

/**
 * Gets the forNew data from the Cyclos server for the given group id.
 *
 * @param string groupId	The id of the group to retrieve the data for.
 * @return array			Array indicating which data is needed to register a new user in the given group.
 */
function getForNewResponse($groupId) {
	$url = "/api/users/data-for-new";
	$result = restRequest($url, array('group' => $groupId), "json", false);
	handleErrors($result, '200');
	return $result['response'];
}

/**
 * Uploads the user picture to Cyclos.
 * 
 * @return string	The picture id returned by Cyclos, or null if no picture was uploaded by the user.
 */
 
function uploadPicture() {
	$picSpecified = array_key_exists("uploadedPic", $_FILES) && !empty($_FILES["uploadedPic"]['tmp_name']);
	if (!$picSpecified) {
		return null;
	}
	$curlFile = new CURLFile($_FILES["uploadedPic"]['tmp_name'], 'image/png', "image");
	$data = array(
			"name" => basename($_FILES["uploadedPic"]['tmp_name']),
			"image" => $curlFile
	);
	$url = "/api/images/temp";
	$result = restRequest($url, null, "multipart", true, $data);
	handleErrors($result, '201');
	return $result['response'];
}

/**
 * Creates a user in Cyclos.
 *
 * @param json		Should be in the format specified in the api docs.
 * @return array	Array with the user response from Cyclos.
 */
function createUser($json){
	$url = "/api/users";
	$result = restRequest($url, null, "json", true, $json);
	handleErrors($result, '201');
	return $result['response']['user'];
}

/**
 * Gets the Mollie payment URL of the given Cyclos user.
 * 
 * @param user		String with Cyclos user JSON as returned by createUser().
 * @return string	The payment URL.
 */
function getPaymentUrl($user) {
	$url = "/api/users/" . $user['id'];
	$result = restRequest($url, array('fields' => 'customValues'), "json", false);
	handleErrors($result, '200');
	
	$profileFields = $result['response']['customValues'];
	// Loop through the custom values to find the payment_url.
	foreach ($profileFields as $profileField) {
		if ('payment_url' == $profileField['field']['internalName']) {
			// Return the string value of the payment url field.
			return $profileField['stringValue'];
		}
	}
	// If no profileField payment_url was found, raise an error.
	raiseError('error.missingProfileField');
}

/**
 * Validates a user in Cyclos.
 * 
 * @param string validationKey	The registration validation key the user has received from Cyclos.
 * @return array				Array with the user response from Cyclos.
 */
function validateUser($validationKey){
	$url = "/api/validate/registration/" . $validationKey;
	// Note: a bit strange, but Cyclos requires a POST request, but no post data in this case.
	// Note: another strange thing: this api request requires being called anonymously, so without the secret token.
	$result = restRequest($url, array(), "json", true, null, false);
	handleErrors($result, 200);

	// Return the Cyclos user.
	return $result['response']['user'];
}

/**
 * Gets the login information of the given Cyclos user.
 * 
 * @param user		String with Cyclos user JSON as returned by createUser().
 * @return array	An array with the username and email of the validated user.
 */
function getLoginInformation($user) {
	$url = "/api/users/" . $user['id'];
	$result = restRequest($url, array('fields' => 'username, email'), "json", false);
	handleErrors($result, '200');

	$loginInfo = $result['response'];
	if (empty($loginInfo['username']) || empty($loginInfo['email'])) {
		raiseError('error.missingLoginInfo');
	}

	return $loginInfo;
}

/**
 * verifies the google recaptcha. Returns an empty string if everything is OK, else returns an error string. 
 */
function failCaptcha() {
	$ipUser=$_SERVER["REMOTE_ADDR"];
	$response=$_POST["g-recaptcha-response"];
	if (empty($response)) {
		return "error.captchaForgotten";
	}
	//formulate the request URL to reCaptcha API
	$request =  "https://www.google.com/recaptcha/api/siteverify?secret=" . RECAPTCHA_SECRET . "&response=" . $response . "&remoteip=" . $ipUser;
	//set the recaptcha answer
	$idealanswer="true";
	//finally make and retrieve the request
	$responserecaptcha = file_get_contents($request);
	//Check if the answer is correct
	if((strstr($responserecaptcha,$idealanswer))) {
		return "";
	} 
	return "error.captcha";
}

/**
 * Executes a request to the Cyclos API, via cURL.
 *
 * @param string	$url	The REST URL to send the request to.
 * 							Will be appended to the BASE_URL of Cyclos, so use a path of the form: /api/something.
 * @param array		$params	Array of query parameters to add to the url or empty array if no parameters are needed.
 * @param string	$format	The format of the request. Must be 'json' or 'multipart'.
 * @param boolean	$isPost	Indicates whether the request method is a POST (or GET).
 * @param string	$data	Optional. The data to be posted in case of a POST request.
 * @param boolean	$useToken	Optional. Indicates whether the request should be done with the secret token or anonymously.
 * @return array	The returned array contains a 'httpCode' with the original HTTP Status code and a 'response' with the response body.
 **/
function restRequest($url, $params, $format, $isPost, $data = null, $useToken = true) {
	$headers = array(
		'Content-Type: application/json',
		'Accept: application/json'
	);
	// If we need to do a multipart request, we need different headers.
	if ('multipart' == $format) {
		$headers = array(
			'Content-Type: multipart/form-data'
		);
	}
	if ($useToken) $headers[] = CYCLOS_ACCESS;

	// Create a cURL handle to prepare the request.
	$url = BASE_URL . $url;
	if (!empty($params)) {
		$url .= '?' . http_build_query($params);
	}
	$ch = curl_init($url);
	curl_setopt($ch, CURLOPT_HEADER, 0);
	curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
	if ($isPost) {
		curl_setopt($ch, CURLOPT_POST, true);
		if (!empty($data)) curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
	}

	// Excecute the cURL request and process the response.
	$response = curl_exec($ch);
	$httpCode = curl_getinfo($ch, CURLINFO_RESPONSE_CODE);
	curl_close($ch);
	$result = array(
		'httpCode' => $httpCode
	);
	if ('json' == $format) {
		// If we are supposed to get JSON back, parse it.
		$result['response'] = json_decode($response, true);
	} else {
		// If we get plain text back, just add it as-is.
		$result['response'] = $response;
	}
	return $result;
}

/**
 * Handles errors: in case the response code in the result does not indicate success, an error is
 * raised (by calling raiseError which throws an Exception). Before that, the response information is
 * parsed to retrieve the correct information to show to the user.
 * Also, the type of error is determined, being either fatal (the user can not retry) or validation (the 
 * user can re-submit the form).
 * 
 * @param result		Array containing the httpCode and Cyclos api response.
 * @param successCode	The HTTP Response Code indicating normal success (200 or 201).
*/
function handleErrors($result, $successCode){
	$httpCode = $result['httpCode'];

	// If the httpCode in the result is equal to the successCode, there is nothing to do.
	if ($successCode == $httpCode) return;

// error_log("result: " . print_r($result, true));
	$response = $result['response'];
	$msgType = '';
	$msg = '';
	$errorType = 'fatal';

	switch ($httpCode) {
		case '401':
		case '403':
		case '500':
			// For httpCodes like 401, 403, 500 there is a default error message in the registration_strings.php.
			$msgType = "error." . $httpCode;
			break;
		case '400':
		case '404':
			// A 400 httpCode probably means a required parameter is missing. Which one is indicated by the entityType.
			// A 404 httpCode can either mean the url is wrong or the requested enity was not found in Cyclos.
			if (isset($response['entityType'])) {
				$entityType = $response['entityType'];
				switch ($entityType) {
					case 'Group':
					case 'User':
						$msgType = "error.unknown$entityType";
						break;
					default:
						$msgType = "error.unknownType";
						break;
				}
			} else {
				$msgType = "error." . $httpCode;
			}
			break;
		case '422':
			// For validation messages, build a custom message instead of using a known msgType.
			$msg = handleValidationError($response);
			// Validation errors should not prevent the normale page content from showing, so use errorType 'validation' instead of 'fatal'.
			$errorType = 'validation';
			break;
		case '0':
			// This means the server is down (or we requested a non-existing api URL).
			$msgType = 'error.noServerContact';
			break;
		default:
			$msgType = 'error.unknown';
	}
	raiseError($msgType, $msg, $errorType);
}
/**
 * Puts the given error information in the session as an 'errors' array and throws an Exception.
 * 
 * @param msgType		If the error is one of the known error situations, use its string as a msgType.
 * @param msg			If the error is not a known error, or requires more custom explanation, use msg.
 * @param errorType		Either fatal or validation. This determines whether the user can retry or not.
 * @throws Exception
*/
function raiseError($msgType = null, $msg = null, $errorType = 'fatal'){
	$_SESSION['errors'] = array(
		'msgType' => $msgType,
		'msg' => $msg,
		'errorType' => $errorType,
	);
	throw new Exception();	
}
/**
 * Builds a custom validationmessage from the information in the response parameter. Used in case of validation
 * (422) errors. These require more custom messages, hence the separate function here.
 * 
 * @param response	The response json from Cyclos.
 * @return string	The custom validationmessage.
*/
function handleValidationError($response){
	$msg = '';
	switch ($response["code"]) {
		case "validation":
			$msg .= "<ul class='error'>";
			if (!empty($response["generalErrors"])) {
				foreach ($response["generalErrors"] as $generalError) {
					$msg .= "<li>" . $generalError;
				}
			}
			if (!empty($response["propertyErrors"])) {
				foreach ($response["propertyErrors"] as $propertyKey => $propertyValue) {
					$msg .= "<li>" . $propertyValue[0];
				}
			}
			if (!empty($response["customFieldErrors"])) {
				foreach ($response["customFieldErrors"] as $customKey => $customValue) {
					$msg .= "<li>" . $customValue[0];
				}
			}
			$msg .= "</ul>";
			break;
		case "maxItems":
			$msg .= lang('error.maxItems');
			if (!empty($response["maxItems"])) {
				$msg .= "<br>" . lang('error.maxItems.max') . " " . $response["maxItems"] . ".";
			}
			break;
		case "queryParse":
			$msg .= lang('error.queryParse');
			if (!empty($response["value"])) {
				$msg .= "<br>" . lang('error.queryParse.text') . " " . $response["value"] . ".";
			}
			break;
		case "dataConversion":
			$msg .= lang('error.dataConverse');
			if (!empty($response["value"])) {
				$msg .= "<br>" . lang('error.dataConverse.item') . " " . $response["value"] . ".";
			}
			break;
		default:
			$msg .= lang('error.unknown');
	}
	return $msg;
}

################################## DATA MANIPULATION ###############################
#################################################################################

function getAankoop() {
	if (isParticulier()) {
		return $_SESSION['aankoop_saldo_particulier'];
	} 
	return $_SESSION['aankoop_saldo_bedrijf'];
}

function getNullSafeAankoop() {
	return !empty(getAankoop()) ? getAankoop() : "";
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
					'name' => $customField['name'],
					'description' => isset($customField['informationText']) ? $customField['informationText'] : "",
					'required' => isset($customField['required']) && $customField['required']
			);
		}
	}
	foreach ($forNewResponse['passwordTypes'] as $passwordType) {
		$fields[$passwordType['internalName']] = array(
				'name' => $passwordType['name'],
				'description' => isset($passwordType['description']) ? $passwordType['description'] : "",
				'required' => true
		);
	}
	$fields[$forNewResponse['phoneConfiguration']['mobilePhone']['kind']] = array(
			'name' => $forNewResponse['phoneConfiguration']['mobilePhone']['name'],
			'description' => "",
			'required' => $forNewResponse['phoneConfiguration']['mobileAvailability'] == "required"
	);
	$fields[$forNewResponse['phoneConfiguration']['landLinePhone']['kind']] = array(
			'name' => $forNewResponse['phoneConfiguration']['landLinePhone']['name'],
			'description' => "",
			'required' => $forNewResponse['phoneConfiguration']['landLineAvailability'] == "required"
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
 * Gets the privacy information from the data-for-new json.
 */
function getPrivacyInfo($forNewResponse){
	return [
		"email" => in_array('email', $forNewResponse['user']['hiddenFields']),
		"addresses" => $forNewResponse['addressConfiguration']['address']['hidden'],
		"mobile" => $forNewResponse['phoneConfiguration']['mobilePhone']['hidden'],
		"landLine" => $forNewResponse['phoneConfiguration']['landLinePhone']['hidden'],
		];
}

/**
 * gets the data from the session and prepares it to the format as expected bij the Cyclos createUser endpoint.
 */
function getDataFromSession($pictureId){
	$fieldsUsed = isParticulier() ? $_SESSION['fieldsParticulieren'] : $_SESSION['fieldsBedrijven'];
	$privateFields = isParticulier() ? $_SESSION['privateFieldsParticulieren'] : $_SESSION['privateFieldsBedrijven'];
	$mobilePhones = array();
	if (in_array('mobile', $fieldsUsed) && !empty(getNullSafeValue('mobile'))) {	
		array_push($mobilePhones, array(
				"name" => "Standaard mobiel telefoonnummer",
				"number" => getNullSafeValue('mobile'),
				"hidden" => $privateFields['mobile']
			));
	}
	$landLinePhones = array();
	if (in_array('landLine', $fieldsUsed) && !empty(getNullSafeValue('landLine'))) {
		array_push($landLinePhones, array(
				"name" => "Standaard vast telefoonnummer",
				"number" => getNullSafeValue('landLine'),
				"hidden" => $privateFields['landLine']
			));
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
							"hidden" => $privateFields['addresses'])),
			"mobilePhones" => $mobilePhones,
			"landLinePhones" => $landLinePhones,
			"passwords" => $passwords,
			"images" => (empty($pictureId)) ? null : array($pictureId),
			"acceptAgreement" => "true",
			"skipActivationEmail" => "false"
		);
		if ($privateFields['email']) {
			$data['hiddenFields'] = ["email"];
		}
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
	if (!isset($fields[$internalName])) return;
	
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
 * @param $bedragen an associative array with the custom field values for the field.
 * @param $defaultValue the default value for the fiekd  
 */
function showLidmaatschap($key, $fields, $bedragen, $defaultValue) {
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
	
	$currentCategory = "";
	$isOptgroupOpen = false;
	foreach ($branchesInfo['possibleValues'] as $brancheValue) {
		$categoryName = $brancheValue['category']['name'] ?? '';
		if ($categoryName != $currentCategory) {
			if ($isOptgroupOpen) {
				echo "</optgroup>";
				$isOptgroupOpen = false;
			}
			echo "<optgroup class='Optiongroup' label='" . $categoryName . "'>";
			$currentCategory = $categoryName;
            $isOptgroupOpen = true;
		}
		$internalBrancheName = $brancheValue['internalName'] ?? '';
		echo "<option class='selectOption' value='" . $internalBrancheName . "'";
		$selectedBranche = $_SESSION['branche'] ?? '';
		if ($selectedBranche == $internalBrancheName) {
			echo " selected ";
		}
		echo ">" . $brancheValue['value'] . "</option>";
	}
	if ($isOptgroupOpen) {
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
