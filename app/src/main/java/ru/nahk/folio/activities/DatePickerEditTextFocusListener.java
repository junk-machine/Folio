package ru.nahk.folio.activities;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.support.design.widget.TextInputEditText;
import android.text.Editable;
import android.view.View;
import android.widget.DatePicker;

import java.util.Calendar;

import ru.nahk.folio.utils.CalendarHelper;

/**
 * Listener for {@link TextInputEditText} focus change events that displays date picker dialog.
 */
public class DatePickerEditTextFocusListener implements View.OnFocusChangeListener {
    /**
     * Handler for the editor focus change events.
     * @param v Text editor view.
     * @param hasFocus Flag that indicates whether view has the focus.
     */
    @Override
    public void onFocusChange(final View v, boolean hasFocus) {
        if (!hasFocus || !(v instanceof TextInputEditText)) {
            return;
        }

        final TextInputEditText editText = (TextInputEditText) v;

        Calendar currentDate = null;

        Editable existingText = editText.getText();
        if (existingText != null && existingText.length() > 0) {
            currentDate = CalendarHelper.parse(existingText.toString());
        }

        if (currentDate == null) {
            currentDate = Calendar.getInstance();
        }

        DatePickerDialog datePickerDialog =
            new DatePickerDialog(
                v.getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editText.setText(
                            CalendarHelper.toString(CalendarHelper.fromComponents(year, month, dayOfMonth)));

                        View nextView = v.focusSearch(View.FOCUS_DOWN);

                        if (nextView != null) {
                            nextView.requestFocus();
                        } else {
                            v.clearFocus();
                        }
                    }
                },
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DATE)
            );

        datePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                View previousView = v.focusSearch(View.FOCUS_UP);

                if (previousView != null) {
                    previousView.requestFocus();
                } else {
                    v.clearFocus();
                }
            }
        });

        datePickerDialog.show();
    }
}
