package ru.nahk.folio.controls;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.util.AttributeSet;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

/**
 * Text input view that supports auto-complete functionality and can be used with
 * {@link TextInputLayout}.
 */
public class TextInputAutoCompleteTextView extends AppCompatAutoCompleteTextView {
    /**
     * Creates new instance of the {@link TextInputAutoCompleteTextView} class
     * with the given activity context.
     * @param context Parent activity context.
     */
    public TextInputAutoCompleteTextView(Context context) {
        super(context);
    }

    /**
     * Creates new instance of the {@link TextInputAutoCompleteTextView} class
     * with the given activity context and XML attributes.
     * @param context Parent activity context.
     * @param attrs XML attributes.
     */
    public TextInputAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Creates new instance of the {@link TextInputAutoCompleteTextView} class
     * with the given activity context, XML attributes and default styles.
     * @param context Parent activity context.
     * @param attrs XML attributes.
     * @param defStyleAttr Default style attributes.
     */
    public TextInputAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Connects text input with parent {@link TextInputLayout}.
     * @param outAttrs Editor attributes.
     * @return Input connection.
     */
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        final InputConnection inputConnection =
            super.onCreateInputConnection(outAttrs);

        if (inputConnection != null && outAttrs.hintText == null) {
            // If there is no hintText, try use one from the parent
            final ViewParent parent = getParent();
            if (parent instanceof TextInputLayout) {
                outAttrs.hintText = ((TextInputLayout) parent).getHint();
            }
        }

        return inputConnection;
    }
}
