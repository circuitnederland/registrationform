<!DOCTYPE html>
<?php
include_once 'registration_strings.php';
?>
<html>
	<head>
		<title><?php echo lang('payment.done')?></title>
		<link rel="stylesheet" type="text/css" href="style.css">
	</head>
<body>

	<div id="formContainer" class="registrationFinished">
		<h1><?php echo lang('payment.done')?></h1>
		<p class="betaling"><?php
			$from;
			if (isset($_GET['from'])) {
				$from = $_GET['from'];
			}
			if ($from == "repay") {
				echo lang('repayment.done.explain');
			} else {
				echo lang('payment.done.explain');
			}
		?></p>
	</div>
	
</body>
</html> 