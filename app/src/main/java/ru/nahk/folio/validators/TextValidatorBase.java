package ru.nahk.folio.validators;

import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

/**
 * Base class for text validators.
 */
public abstract class TextValidatorBase implements TextWatcher, View.OnFocusChangeListener {
    /**
     * Interface for validation state change event listeners.
     */
    public interface OnValidationStateChangedListener {
        /**
         * Callback to invoke when validity state changes.
         * @param isValid New validity state.
         */
        void onValidationStateChanged(boolean isValid);
    }

    /**
     * Text input layout control.
     */
    private TextInputLayout mInputLayout;

    /**
     * Flag that contains last validity state reported to listener.
     */
    private Boolean mLastReportedState;

    /**
     * Actual validity flag.
     */
    private Boolean mIsValid;

    /**
     * Listener for validation state changed events.
     */
    private OnValidationStateChangedListener mValidationStateChangedListener;

    /**
     * Creates new instance of {@link TextValidatorBase} class for the given {@link TextInputLayout}.
     * @param inputLayout Text input layout control.
     */
    TextValidatorBase(TextInputLayout inputLayout) {
        mInputLayout = inputLayout;

        // Hook to text change events
        EditText editText = inputLayout.getEditText();
        if (editText != null) {
            editText.addTextChangedListener(this);
            editText.setOnFocusChangeListener(this);
        }
    }

    /**
     * Gets the current validity state.
     * @return Flag that indicates whether current input is valid.
     */
    public boolean isValid() {
        if (mIsValid == null) {
            // Initialize validity state
            EditText editText = mInputLayout.getEditText();
            if (editText != null) {
                mIsValid = validate(editText.getText());
            }
        }

        return mIsValid;
    }

    /**
     * Sets listener for validation state change events.
     * @param listener New listener for validation state change events.
     */
    public void setOnValidationStateChangedListener(OnValidationStateChangedListener listener) {
        mValidationStateChangedListener = listener;

        // Notify listener about current state
        if (listener != null) {
            notifyListener(isValid());
        } else {
            mLastReportedState = null;
        }
    }

    /**
     * Does nothing.
     * @param s Original text.
     * @param start Start position.
     * @param count Number of changed characters.
     * @param after Length of the new text.
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    /**
     * Does nothing.
     * @param s New text.
     * @param start Change start position.
     * @param before Length of original text.
     * @param count Number of changed characters.
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) { }

    /**
     * Performs validation when text in the edit field changes.
     * @param s New text.
     */
    @Override
    public void afterTextChanged(Editable s) {
        String newError = getErrorMessage(s);
        mIsValid = newError == null;

        if (newError != mInputLayout.getError()) {
            mInputLayout.setError(newError);
        }

        notifyListener(mIsValid);
    }

    /**
     * Performs validation when focus moves away from the control.
     * @param v Target edit control.
     * @param hasFocus Flag that indicates whether control is losing focus.
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus && v instanceof EditText) {
            EditText editText = (EditText) v;
            afterTextChanged(editText.getText());
        }
    }

    /**
     * Performs validation of the input text.
     * @param text Text to validate.
     * @return True if text is valid, otherwise false.
     */
    protected abstract boolean validate(CharSequence text);

    /**
     * Performs validation of the input text and returns error message, if any.
     * @param text Text to validate.
     * @return Error message or null.
     */
    protected abstract String getErrorMessage(CharSequence text);

    /**
     * Notifies listener with the current validity state.
     * @param isValid Flag that indicates whether current input is valid.
     */
    private void notifyListener(boolean isValid) {
        if (mValidationStateChangedListener != null) {
            if (mLastReportedState == null || isValid != mLastReportedState) {
                mLastReportedState = isValid;

                mValidationStateChangedListener
                    .onValidationStateChanged(isValid);
            }
        }
    }

    /**
     * Retrieves string resource by its identifier.
     * @param id String resource identifier.
     * @return String resource.
     */
    protected String getString(int id, Object... formatArgs) {
        return mInputLayout.getResources().getString(id, formatArgs);
    }
}
