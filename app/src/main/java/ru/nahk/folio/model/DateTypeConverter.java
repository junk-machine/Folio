package ru.nahk.folio.model;

import android.arch.persistence.room.TypeConverter;

import java.util.Calendar;

import ru.nahk.folio.utils.CalendarHelper;

/**
 * Defines type conversion to store date values as integers.
 */
class DateTypeConverter {
    /**
     * Converts integer date representation to {@link Calendar} instance.
     * @param date Date stored as integer.
     * @return {@link Calendar} instance for the given date.
     */
    @TypeConverter
    public static Calendar toDate(int date) {
        int year = date / 10000;
        date -= year * 10000;

        int month = date / 100;
        date -= month * 100;

        return CalendarHelper.fromComponents(year, month, date);
    }

    /**
     * Converts {@link Calendar} instance to integer date representation.
     * @param date {@link Calendar} instance.
     * @return Integer date representation.
     */
    @TypeConverter
    public static int fromDate(Calendar date) {
        return
            date.get(Calendar.YEAR) * 10000 +
            date.get(Calendar.MONTH) * 100 +
            date.get(Calendar.DAY_OF_MONTH);
    }
}
