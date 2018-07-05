<?php

/* First get the url parameter */
$com = $_GET['community'];

/* First get the url parameter */
include_once 'registration_strings.php';

/* Form redirec */
define('REDIRECT_DATA', "register.php");
?>
<!DOCTYPE html>

<html>
	<head>
		<title><?php echo lang('title')?></title>
		<link rel="stylesheet" type="text/css" href="style.css">
	</head>
<body>
<div id="formContainer">
<form method="POST" enctype="multipart/form-data" action="<?php echo REDIRECT_DATA;?>">
	<input type="hidden" value="<?php echo($com)?>" name="community" />
	<h1><?php echo lang('title').' '.$com ?></h1>
	<div class="section">
		<div class="sectionNumber">1</div>
		<div class="sectionContent">
			<h2 class="sectionTitle"><?php echo lang('community.section1.title')?></h2>
			<div class="sectionText"><p><?php echo lang('community.section1.text')?></p></div>
		</div>		
	</div>
	<div class="section">
		<div class="sectionNumber">2</div>
		<div class="sectionContent">
			<h2 class="sectionTitle"><?php echo lang('community.section2.title')?></h2>
			<div class="sectionText"><p><?php echo lang('community.section2.text')?></p></div>			
		</div>
	</div>
	<div class="section">
		<div class="sectionNumber">3</div>
		<div class="sectionContent">
			<h2 class="sectionTitle"><?php echo lang('community.section3.title')?></h2>
			<div class="sectionText"><p><?php echo lang('community.section3.text')?></p></div>
		</div>
	</div>
		<div class="section">
		<div class="sectionNumber">4</div>
		<div class="sectionContent">
			<h2 class="sectionTitle"><?php echo lang('community.section4.title')?></h2>
			<div class="sectionText"><p><?php echo lang('community.section4.text')?></p></div>
		</div>
	</div>
	<div class="formRow">
		<div class="label"></div>
		<div class="value">
			<input id="button" type="submit" value="<?php echo lang('submit.community')?>">
		</div>
	</div>
	
</form>  
</div> 
</body>
</html> 