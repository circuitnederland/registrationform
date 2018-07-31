<?php

/**
 * This is the first page of the registration form. It is responsible for choosing
 * the community. If you know the community already then start with the
 * register.php form.
 *
 */

################################## INITIALIZATION ###############################
#################################################################################
session_start();
include_once 'registration_strings.php';
include_once 'registration_functions.php';
define('REDIRECT_DATA', "register.php");

################################## RETRIEVING SERVER DATA  ###############################
#################################################################################
$noServerContact = false;
$communitiesFromServer = getCommunities(BASE_URL);
if (empty($communitiesFromServer)) {
	$noServerContact = true;
}

################################## FORM HEADER  ###############################
#################################################################################


?>
<!DOCTYPE html>

<html>
	<head>
		<title><?php echo lang('title')?></title>
		<link rel="stylesheet" type="text/css" href="style.css">
	</head>
<body>
<div id="formContainer">

<?php 

################################## ERROR HANDLING  ###############################
#################################################################################

if ($noServerContact) {
	echo '<div class="schadow">';
	echo '	<div class="errorTop">';
	echo '  	<h3>' . lang('error.title') . '</h3>';
	echo '	</div>';
	echo '	<div class="errorBottom">';
	
	echo '		<p class="textError">' . lang('error.heading') . ' 		</p>';
	echo ' 		<ul>';
	
	if ($noServerContact) {
		showErrorLi('error.noServerContact');
	}
	echo '      </ul>';
	echo '   	<p class="textError">' . lang('error.contact') . '</p>';
	echo '	</div>';
	echo '</div>';
}


################################## FORM ELEMENTS   ###############################
#################################################################################

?>	
	<form method="POST" enctype="multipart/form-data" action="<?php echo REDIRECT_DATA;?>">
	<h1><?php echo lang('title.intro')?></h1>
	<div class="section">
		<div class="sectionNumber">1</div>
		<div class="sectionContent">
			<h2 class="sectionTitle"><?php echo lang('title.section1.title')?></h2>
			<div class="sectionText"><p><?php echo lang('title.section1.text')?></p></div>
			<div class="value styled-select communitySelect">
				<select name="community">
				<?php
					/**
					 * Create a drop down option for each community. Communities are retrieved from 
					 * group names ending in " - particulieren"  or " - bedrijven". Circuit Nederland
					 * will be selected as default community.
					 */
					foreach ($communitiesFromServer as $community) {
						if ($community == "Circuit Nederland") {
							echo "<option selected='selected' class='selectOption' value='Circuit Nederland'>Circuit Nederland</option>";
						} else {
							echo "<option class='selectOption' value='" . $community . "'" .">" . $community . "</option>";
						}
					}
				?>
				</select>
			</div>
		</div>		
	</div>
	<div class="section">
		<div class="sectionNumber">2</div>
		<div class="sectionContent">
			<h2 class="sectionTitle"><?php echo lang('title.section2.title')?></h2>
			<div class="sectionText"><p><?php echo lang('title.section2.text')?></p></div>			
		</div>
	</div>
	<div class="section">
		<div class="sectionNumber">3</div>
		<div class="sectionContent">
			<h2 class="sectionTitle"><?php echo lang('title.section3.title')?></h2>
			<div class="sectionText"><p><?php echo lang('title.section3.text')?></p></div>
		</div>
	</div>
		<div class="section">
		<div class="sectionNumber">4</div>
		<div class="sectionContent">
			<h2 class="sectionTitle"><?php echo lang('title.section4.title')?></h2>
			<div class="sectionText"><p><?php echo lang('title.section4.text')?></p></div>
		</div>
	</div>
	<div class="section">
		<div class="sectionNumber">5</div>
		<div class="sectionContent">
			<h2 class="sectionTitle"><?php echo lang('title.section5.title')?></h2>
			<div class="sectionText"><p><?php echo lang('title.section5.text')?></p></div>
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
