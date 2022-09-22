/* 
 * Validates the iban user profile field, to check if the value is a valid iban (Dutch or foreign).
*/

import org.apache.commons.validator.routines.checkdigit.IBANCheckDigit

return IBANCheckDigit.IBAN_CHECK_DIGIT.isValid(value.replaceAll("\\s", ""))
