<?php
/*
 * test form for the custom web server which is called by the mollie webhook after paying. 
 */

include_once 'registration_strings.php';
include_once 'registration_functions.php';

$url = "https://your-webhook-url";	// @todo: Refactor this into constants from registration_strings.php instead of hardcoding it here (URL and data id below).
$data = array(
		'id'=>'abcd1234'
);
$response = httpPost($url, $data);
	
var_dump($response);
?>