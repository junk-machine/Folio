package ru.nahk.folio.activities;

import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

/**
 * Clicks specified button when text editor receives {@code IME_ACTION_DONE}.
 */
public class ImeActionDoneButtonClickDispatcher implements TextView.OnEditorActionListener {
    /**
     * Button to trigger on {@code IME_ACTION_DONE}.
     */
    private Button mButton;

    /**
     * Creates new {@code ImeActionDoneButtonClickDispatcher} object that triggers specified button.
     * @param button Dialog button to trigger on IME_ACTION_DONE.
     */
    ImeActionDoneButtonClickDispatcher(@NonNull Button button) {
        mButton = button;
    }

    /**
     * Handles editor action.
     * @param v Target text view.
     * @param actionId IME action.
     * @param event Key event.
     * @return True if action was IME_ACTION_DONE, false otherwise.
     */
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        // Some vendors always send unspecified action, so we need to check
        // for the <Enter> key explicitly through event argument
        if (actionId == EditorInfo.IME_ACTION_DONE
            || (event != null
                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                && event.hasNoModifiers())) {
            if (mButton.isEnabled()) {
                mButton.performClick();
                return true;
            }
        }

        return false;
    }
}
