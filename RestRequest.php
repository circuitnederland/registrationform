<?php

/**
 * Class provides access via curl to a rest service. 
 * 
 * @author rinke
 *
 */
class restRequest {
	public $url;
	public $headers;
	public $params;
	public $body;
	public $expectedFormat;
	public $method;

	/**
	 * Constructor for the class, loads all the needed properties to make contact and get a response. 
	 * @param string $aUrl - the url of the rest service, without any query strings attached (e.g. no /.../?bla=aap&boe=beer)
	 * @param array $aHeaders - headers to pass to the request, used for options. Usually this defines the content type and the 
	 * authentification. 
	 * @param array $aParams - query params to past in case of a post request; e.g. /.../?param1=aap&param2=beer 
	 * @param string $aFormat - used to specify the expected return format. The default is "json". The other possibility is "xml". 
	 * No other possibilities are supported yet.  
	 * @param string $isPost - false means GET request, true means POST request. 
	 * @param string $aBody - a body to pass with a POST request. Just pass an empty string in case of a GET. 
	 */
	public function restRequest($aUrl, array $aHeaders, array $aParams, $aFormat = "json", $isPost = false, $aBody = "+") {
		$this->url = $aUrl;
		$this->headers = $aHeaders;
		$this->params = $aParams;
		$this->expectedFormat = $aFormat;
		$this->method = ($isPost ? "POST" : "GET");
		$this->body = $aBody;
	}

	/**
	 * sends the request and returns the response from the server. 
	 * 
	 * @return mixed|unknown|string
	 */
	public function exec() {
		$queryStr = "?";
		foreach($this->params as $key=>$val)
			$queryStr .= $key . "=" . $val . "&";

			//trim the last '&'
			$queryStr = rtrim($queryStr, "&");

			$url = $this->url . $queryStr;

			$request = curl_init();
			curl_setopt($request, CURLOPT_URL, $url);
			// don't included headers
			curl_setopt($request, CURLOPT_HEADER, 0);
			curl_setopt($request, CURLOPT_HTTPHEADER, $this->headers);
			curl_setopt($request, CURLOPT_RETURNTRANSFER, 1);
			//curl_setopt($ch, CURLOPT_USERPWD, "$username:$password");

			if($this->method == "POST")	{
				curl_setopt($request, CURLOPT_POST, 1);
				curl_setopt($request, CURLOPT_POSTFIELDS, $this->body);
				//this prevents an additions code 100 from getting returned
				//found it in some forum - seems kind of hacky
				curl_setopt($request, CURLOPT_HTTPHEADER, array("Expect:"));
			}

			$response = curl_exec($request);
			$httpCode = curl_getinfo($request, CURLINFO_HTTP_CODE);
			curl_close($request);
			$result;
			
			if($this->expectedFormat == "json") {
				//parse response
				$result = json_decode($response, true);
				if ($httpCode !== 200) {
					$result['httpCode'] = $httpCode;
				}
			}	elseif($this->expectedFormat == "xml")	{
				return $response;
			}
			return $result;
	}
}


?>