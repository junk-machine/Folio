package ru.nahk.folio.validators;

import android.support.design.widget.TextInputLayout;

/**
 * Validator that checks, if number string is not empty and there are not too many decimal digits.
 */
public class DecimalNumberTextValidator extends NotEmptyTextValidator {
    /**
     * Maximum allowed number of decimal digits.
     */
    private int mMaxDecimalDigits;

    /**
     * Identifier of the error message string resource.
     */
    private int mTooManyDecimalDigitsMessageId;

    /**
     * Creates new {@link DecimalNumberTextValidator} instance for the given {@link TextInputLayout}
     * with the given error message resource identifier.
     * @param inputLayout Text input layout control.
     * @param maxDecimalDigits Maximum allowed number of decimal digits.
     * @param emptyStringMessageId Identifier of the error message string resource for an empty text.
     * @param tooManyDecimalDigitsMessageId Identifier of the error message string resource for
     *                                      too many digits error.
     */
    public DecimalNumberTextValidator(
            TextInputLayout inputLayout,
            int maxDecimalDigits,
            int emptyStringMessageId,
            int tooManyDecimalDigitsMessageId) {
        super(inputLayout, emptyStringMessageId);

        mMaxDecimalDigits = maxDecimalDigits;
        mTooManyDecimalDigitsMessageId = tooManyDecimalDigitsMessageId;
    }

    /**
     * Validates that text is not empty and there are not too many decimal digits.
     * @param text Text to validate.
     * @return True if input number string is valid, otherwise false.
     */
    @Override
    protected boolean validate(CharSequence text) {
        return super.validate(text) && validateDecimalPlaces(text);
    }

    /**
     * Validates number of digits after decimal point in the given number string.
     * @param text Number string to validate.
     * @return True if number of decimal digits is less or equal to allowed, otherwise false.
     */
    private boolean validateDecimalPlaces(CharSequence text) {
        for (int charIndex = 0; charIndex < text.length(); ++charIndex) {
            if (text.charAt(charIndex) == '.') {
                return text.length() - charIndex - 2 < mMaxDecimalDigits;
            }
        }

        return true;
    }

    /**
     * Generates error message, if text is empty or there are too many decimal digits.
     * @param text Text to validate.
     * @return Error message or null.
     */
    @Override
    protected String getErrorMessage(CharSequence text) {
        String baseError = super.getErrorMessage(text);
        if (baseError != null) {
            return baseError;
        }

        return validateDecimalPlaces(text)
            ? null
            : getString(mTooManyDecimalDigitsMessageId, mMaxDecimalDigits);
    }
}
