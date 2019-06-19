package ru.nahk.folio.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Helper class to work with {@link Calendar} type.
 */
public final class CalendarHelper {
    /**
     * Timezone to use for all conversions.
     */
    private static final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("UTC");

    /**
     * Compares two {@link Calendar} objects.
     * @param left First {@link Calendar} object.
     * @param right Second {@link Calendar} object.
     * @return Zero if both objects are equal, negative value if left is less than right
     * and positive value otherwise.
     */
    public static int compare(Calendar left, Calendar right) {
        if (left == null && right == null) {
            return 0;
        } else if (left == null) {
            return -1;
        } else if (right == null) {
            return 1;
        } else {
            return left.compareTo(right);
        }
    }

    /**
     * Creates {@link Calendar} from date components.
     * @param year Year.
     * @param month Month.
     * @param date Date.
     * @return {@link Calendar} instance representing specified date.
     */
    public static Calendar fromComponents(int year, int month, int date) {
        Calendar result = Calendar.getInstance(UTC_TIMEZONE);
        result.set(year, month, date);
        return result;
    }

    /**
     * Creates {@link Calendar} from date and time components.
     * @param year Year.
     * @param month Month.
     * @param date Date.
     * @param hour Hour.
     * @param minute Minute.
     * @return {@link Calendar} instance representing specified date and time.
     */
    public static Calendar fromComponents(int year, int month, int date, int hour, int minute) {
        Calendar result = Calendar.getInstance(UTC_TIMEZONE);
        result.set(year, month, date, hour, minute);
        return result;
    }

    /**
     * Converts epoch timestamp in milliseconds to {@link Calendar} instance.
     * @param epoch Epoch time in milliseconds.
     * @return {@link Calendar} instance for the given epoch timestamp.
     */
    public static Calendar fromEpochInMillis(long epoch) {
        Calendar result = Calendar.getInstance(UTC_TIMEZONE);
        result.setTimeInMillis(epoch);
        return result;
    }

    /**
     * Converts epoch timestamp in seconds to {@link Calendar} instance.
     * @param epoch Epoch time in seconds.
     * @return {@link Calendar} instance for the given epoch timestamp.
     */
    public static Calendar fromEpochInSeconds(long epoch) {
        return fromEpochInMillis(epoch * 1000);
    }

    /**
     * Parses date string into {@link Calendar} object.
     * @param date Date string to parse.
     * @return Calendar object representing specified date.
     */
    public static Calendar parse(String date) {
        if (date == null) {
            return null;
        }

        Calendar result = Calendar.getInstance(UTC_TIMEZONE);

        try {
            result.setTime(DateFormat.getDateInstance(DateFormat.SHORT).parse(date));
        } catch (ParseException parseException) {
            return null;
        }

        return result;
    }

    /**
     * Formats the {@link Calendar} object value using short format.
     * @param date Date to format.
     * @return Formatted date.
     */
    public static String toString(Calendar date) {
        return toString(date, DateFormat.SHORT);
    }

    /**
     * Formats the {@link Calendar} object value using given style.
     * @param date Date to format.
     * @param style Format style.
     * @return Formatted date.
     */
    public static String toString(Calendar date, int style) {
        DateFormat dateFormat = DateFormat.getDateInstance(style);
        return dateFormat.format(date.getTime());
    }

     /**
      * Converts UTC {@link Calendar} to local time and formats with month-day format.
      * @param timestamp Timestamp to format.
      * @return Formatted date and time.
      */
    public static String toLocalTimeString(Calendar timestamp) {
        @SuppressWarnings("SimpleDateFormat")
        SimpleDateFormat timestampFormat = new SimpleDateFormat(
            android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMhhmm"));
        return timestampFormat.format(timestamp.getTime());
    }
}
