// Document ready
$(function() {
    
    /** Define some CSS selectors used on the page. */
    var retailRadioButtonSelector = '#particulier';
    var retailSpecificEntriesSelector = '.retail-only';
    var organisationRadioButtonSelector = '#bedrijf';
    var organisationSpecificEntriesSelector = '.organisation-only';
    
    /**
     * Displays form related to retail (particulier) customer by hiding unrelated form entries.
     */
    function showRetailForm() {
        $(retailSpecificEntriesSelector).show();
        $(organisationSpecificEntriesSelector).hide();
    }
    
    /**
     * Displays form related to organisational (bedrijf) customer by hiding unrelated form entries.
     */
    function showOrganisationForm() {
        $(retailSpecificEntriesSelector).hide();
        $(organisationSpecificEntriesSelector).show();
    }

    /**
     * Checks if a radio button is checked or not.
     * @param selector The CSS selector.
     * @returns {*|jQuery} True if selected, false if not.
     */
    function isChecked(selector) {
        return $(selector).is(':checked');
    }
    
    function setFormForCustomerType() { 
        if(isChecked(retailRadioButtonSelector)) { 
            showRetailForm();
        } 
        else if (isChecked(organisationRadioButtonSelector)) {
            showOrganisationForm();
        } 
    }

    /**
     * Runs callback function on page load and when selector is clicked.
     * @param selectors An array of HTMl selectors.
     * @param callback The callback function to execute.
     */
    function runOnLoadAndOnClick(selectors, callback) {
        callback(); // on load
        selectors.forEach(function(selector) { // on click
            $(selector).click(function() {
                callback();
            });
        })
    }
    
    /**
     * Display the appropriate form for a retail and organisational customer on page load and on change of radio button.
     */
    runOnLoadAndOnClick([retailRadioButtonSelector, organisationRadioButtonSelector], setFormForCustomerType);
});

function captchaRefresh() {
	$.ajax({
	      type: "get",
	      url: "captcha_reload.php",
	      dataType: "text",
	      success: function (data, textStatus) {
	    	  // as I don't seem to be able to retrieve the data without http headers, we have to extract the url
	    	  var index = data.indexOf("http");
	    	  var url = data.substr(index);
	    	  $("#captcha_image").attr('src', url);
	      }
	});
}




