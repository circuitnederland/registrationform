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
		<p class="betaling"><?php printf(lang('payment.done.explain'), htmlspecialchars(strip_tags($_GET['mail']))); ?></p>
	</div>
	
</body>
</html> 