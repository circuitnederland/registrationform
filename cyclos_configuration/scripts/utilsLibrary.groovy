import groovy.transform.TypeChecked

/**
 * Utils library class containing several helper methods for use in C3NL scripts.
 */

@TypeChecked
class Utils {
    private Binding binding

    public Utils(Binding binding) {
        this.binding = binding
    }

    /**
     * Ensures the given iban complies with the pattern conventions for ibans we use (spacing and uppercase).
     *
     * If the given iban already complies with these conventions, it is returned unchanged.
     * If not, the corrected iban is returned.
     * 
     * Note: we also allow non-Dutch ibans, so the number of characters may vary. This is why we
     * can not use a Cyclos input mask for this.
     *
     * This method does NOT check if the given iban is a valid iban, this is done by the validation script.
     * This is because we can not correct invalid ibans automatically, so we let the validation script throw a validation exception.
     */
    String ibanByConvention(String iban) {
        if( this.isIbanConventionCompliant(iban) ) {
            // The iban pattern is fine, return it as-is.
            return iban
        }
        return this.correctIbanPattern(iban)
    }

    /**
     * Correct the pattern of a given IBAN by putting a space after each block of four characters and using uppercase letters.
     */
    String correctIbanPattern(String iban) {
        String correctedIBAN = ''
        int pos = 0
        iban.replaceAll("\\s",'').each {
            correctedIBAN += it
            pos ++
            if ( pos % 4 === 0 ) {
                correctedIBAN += ' '
            }
        }
        return correctedIBAN.toUpperCase()
    }
    
    /**
     * Returns whether the given IBAN complies to the conventions we use for ibans:
     * - A space after each block of four characters.
     * - Only uppercase letters.
     *
     * Example: NL02 ABNA 0123 4567 89
     */
    Boolean isIbanConventionCompliant(String iban) {
        return iban ==~ /^([A-Z0-9]{4} )*[A-Z0-9]{1,4}$/
    }
}
