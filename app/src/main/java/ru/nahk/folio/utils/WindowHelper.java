package ru.nahk.folio.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Window;
import android.view.WindowManager;

/**
 * Defines helper methods for {@link Window} class.
 */
public class WindowHelper {
    /**
     * Displays soft keyboard, if no keyboard is shown.
     * @param context Parent activity context.
     * @param window Window to display the keyboard for.
     */
    public static void ensureKeyboard(Context context, Window window) {
        // For some reason Configuration.keyboardHidden returns NO even when no keyboard is shown,
        // so check hardware keyboard explicitly and enforce software keyboard, if hard not present.
        if (context != null && window != null &&
            context.getResources().getConfiguration().hardKeyboardHidden !=
                Configuration.HARDKEYBOARDHIDDEN_NO) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }
}
