// Document ready
$(function() {
    
    /** https://github.com/jzaefferer/jquery-validation/blob/master/src/localization/messages_nl.js */

    /*
     * Translated default messages for the jQuery validation plugin.
     * Locale: NL (Dutch; Nederlands, Vlaams)
     */
    $.extend( $.validator.messages, {
        required: "Dit is een verplicht veld.",
        remote: "Controleer dit veld.",
        email: "Vul hier een geldig e-mailadres in.",
        url: "Vul hier een geldige URL in.",
        date: "Vul hier een geldige datum in.",
        equalTo: "Vul hier dezelfde waarde in.",
        dateISO: "Vul hier een geldige datum in (ISO-formaat).",
        number: "Vul hier een geldig getal in.",
        digits: "Vul hier alleen getallen in.",
        creditcard: "Vul hier een geldig creditcardnummer in.",
        extension: "Vul hier een waarde in met een geldige extensie.",
        maxlength: $.validator.format( "Vul hier maximaal {0} tekens in." ),
        minlength: $.validator.format( "Vul hier minimaal {0} tekens in." ),
        rangelength: $.validator.format( "Vul hier een waarde in van minimaal {0} en maximaal {1} tekens." ),
        range: $.validator.format( "Vul hier een waarde in van minimaal {0} en maximaal {1}." ),
        max: $.validator.format( "Vul hier een waarde in kleiner dan of gelijk aan {0}." ),
        min: $.validator.format( "Vul hier een waarde in groter dan of gelijk aan {0}." ),

        // For validations in additional-methods.js
        username: "Vul hier een geldige gebruikersnaam in.",
        dateNL: "Vul hier een geldige datum in.",
        phoneNL: "Vul hier een geldig Nederlands telefoonnummer in.",
        mobileNL: "Vul hier een geldig Nederlands mobiel telefoonnummer in.",
        postalcodeNL: "Vul hier een geldige postcode in.",
        bankaccountNL: "Vul hier een geldig bankrekeningnummer in.",
        giroaccountNL: "Vul hier een geldig gironummer in.",
        bankorgiroaccountNL: "Vul hier een geldig bank- of gironummer in.",
        iban: "Vul hier een geldig IBAN in."
    });

    // username has 2 - 16 characters 
    $.validator.addMethod("username", function (value) {
        return /^[a-zA-Z0-9]{2,16}$/.test(value)
    }, "Gebruikersnaam moet minimaal 2 en maximaal 16 letters en/of cijfers lang zijn.");
    
    // check op geldigheid datum 
    $.validator.addMethod("dateNl", function (value) {
    	// test if pattern is valid
    	var patternOK = /^\d{2}-\d{2}-\d{4}$/.test(value);
    	if (!patternOK) {
    	  return false;
    	}
    	// test if date is valid
    	return moment(value, "DD-MM-YYYY").isValid()
    }, "Ongeldige datum, moet van vorm DD-MM-JJJJ zijn.");
    
    // Postcode format is 9999 XX 
    $.validator.addMethod("postcode", function (value) {
        return /^[1-9][0-9]{3} ?[a-zA-Z]{2}$/.test(value)
    }, "Postcode moet vier cijfers en twee letters bevatten, bijvoorbeeld 1234AA.");
    
    // mobile phone
    $.validator.addMethod("mobile", function (value) {
        return /^(((\\+31|0|0031)6){1}[1-9]{1}[0-9]{7})$/.test(value) || (!value || !value.length)
    }, "Vul a.u.b. geldig 06-nummer in (gebruik geen spaties of koppeltekens).");
    
    // landline phone
    $.validator.addMethod("landline", function (value) {
        return /^(((0)[1-9]{2}[0-9][-]?[1-9][0-9]{5})|((\\+31|0|0031)[1-9][0-9][-]?[1-9][0-9]{6}))$/.test(value) || (!value || !value.length)
    }, "Vul a.u.b. geldig vast telefoonnummer in (gebruik geen spaties of koppeltekens).");
    
    // KvKnummer is 8 cijfers 
    $.validator.addMethod("kvkNummer", function (value) {
    	if (!value || !value.length) {
    		// always validate on empty, because it may not be required.
    		return true;
    	}
        return /([0-9]{8})|^$/.test(value)
    }, "K.v.K. nummer moet een 8-cijferig nummer zijn.");
    
    // aankoop moet positief of leeg zijn, alleen hele getallen particulieren maximaal 10000 
    $.validator.addMethod("aankoopparticulier", function (value) {
    	if (!value || !value.length) {
    		// always validate on empty, because it may not be required.
    		return true;
    	}
    	// test if pattern is valid
    	var patternOK = /^\d{1,5}(?:,\d{2})?$/.test(value)
    	if (!patternOK) {
    	  return false;
    	}
    	// test if not too big, but first replace , by . because parseFloat only takes decimal point.
    	if (parseFloat(value.replace(',', '.')) > 10000) {
    		return false;
    	}
    	return true;
    }, "Vul a.u.b. een heel bedrag in (zonder komma of punt). Dit bedrag mag niet hoger zijn dan 10000 euro.");
    
	
    // aankoop moet positief of leeg zijn, alleen hele getallen bedrijven maximaal 10000 
    $.validator.addMethod("aankoopbedrijf", function (value) {
    	if (!value || !value.length) {
    		// always validate on empty, because it may not be required.
    		return true;
    	}
    	// test if pattern is valid
    	var patternOK = /^\d{1,5}(?:,\d{2})?$/.test(value)
    	if (!patternOK) {
    	  return false;
    	}
    	// test if not too big, but first replace , by . because parseFloat only takes decimal point.
    	if (parseFloat(value.replace(',', '.')) > 10000) {
    		return false;
    	}
    	return true;
    }, "Vul a.u.b. een heel bedrag in (zonder komma of punt). Dit bedrag mag niet hoger zijn dan 10000 euro.");
	
	
    // Het inlogwachtwoord moet minimaal 8 tekens lang zijn en minstens 1 kleine letter, 1 hoofdletter en 1 cijfer bevatten
    $.validator.addMethod("pwcheckminlength8", function (value) {    
        return /[a-z]/.test(value) && /[0-9]/.test(value) && /[A-Z]/.test(value) && /.{8,}/.test(value)
    }, "Het inlogwachtwoord moet minimaal 8 tekens lang zijn en minstens 1 kleine letter, 1 hoofdletter en 1 cijfer bevatten.");
    
    // Het wachtwoord moet 6 tekens lang zijn en mag alleen uit kleine letters bestaan: abcdefghijklmnopqrstuvwxyz 
    $.validator.addMethod("pwchecklength6onlylowercase", function (value) {
        return /[a-z]{6}/.test(value)
    }, "Het wachtwoord moet 6 tekens lang zijn en mag alleen uit kleine letters bestaan.");
    
    // Het wachtwoord moet 4 tekens lang zijn en mag alleen uit cijfers bestaan 
    $.validator.addMethod("pwchecklength4onlynumbers", function (value) {
        return /[0-9]{4}/.test(value)
    }, "Het wachtwoord moet 4 tekens lang zijn en mag alleen uit cijfers bestaan.");
    
    // Setup jQuery validator rules
    $("form").validate({

        // Fix for placing error messages over checkbox text.
        // See https://www.sitepoint.com/community/t/positioning-jquery-validation-errors-after-checkbox-value/22587
        errorPlacement: function(label, element) {
            if(element.attr("id") === "checkincasso" || element.attr("id") === "checkVoorwaarde") {
                element.parent().append(label); // append after label
            } else {
                label.insertAfter( element ); // standard behaviour
            }
        } 
    });
});
