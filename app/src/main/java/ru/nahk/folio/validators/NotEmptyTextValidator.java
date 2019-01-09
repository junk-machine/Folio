package ru.nahk.folio.validators;

import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;

/**
 * Validator that checks, if text is not empty.
 */
public class NotEmptyTextValidator extends TextValidatorBase {
    /**
     * Identifier of the error message string resource.
     */
    private int mErrorMessageId;

    /**
     * Creates new {@link NotEmptyTextValidator} instance for the given {@link TextInputLayout}
     * with the given error message resource identifier.
     * @param inputLayout Text input layout control.
     * @param errorMessageId Identifier of the error message string resource.
     */
    public NotEmptyTextValidator(TextInputLayout inputLayout, int errorMessageId) {
        super(inputLayout);

        mErrorMessageId = errorMessageId;
    }

    /**
     * Validates that text is not empty.
     * @param text Text to validate.
     * @return True if text is empty, otherwise false.
     */
    @Override
    protected boolean validate(CharSequence text) {
        return validateNonEmptyText(text);
    }

    /**
     * Generates error message, if text is empty.
     * @param text Text to validate.
     * @return Error message or null.
     */
    @Override
    protected String getErrorMessage(CharSequence text) {
        return validateNonEmptyText(text) ? null : getString(mErrorMessageId);
    }

    /**
     * Validate that text is not empty.
     * @param text Text to validate.
     * @return True if text is empty, otherwise false.
     */
    private boolean validateNonEmptyText(CharSequence text) {
        return !TextUtils.isEmpty(text);
    }
}
