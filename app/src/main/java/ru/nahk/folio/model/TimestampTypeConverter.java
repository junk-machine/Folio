package ru.nahk.folio.model;

import android.arch.persistence.room.TypeConverter;

import java.util.Calendar;

import ru.nahk.folio.utils.CalendarHelper;

/**
 * Defines type conversion to store timestamp values as 64-bit integers.
 */
class TimestampTypeConverter {
    /**
     * Converts epoch timestamp representation to {@link Calendar} instance.
     * @param timestamp Epoch timestamp value.
     * @return {@code Calendar} instance for the given timestamp.
     */
    @TypeConverter
    public static Calendar toTimestamp(Long timestamp) {
        return timestamp == null ? null : CalendarHelper.fromEpochInMillis(timestamp);
    }

    /**
     * Converts {@link Calendar} instance to epoch timestamp representation.
     * @param date {@link Calendar} instance.
     * @return Epoch timestamp representation.
     */
    @TypeConverter
    public static Long fromTimestamp(Calendar date) {
        return date == null ? null : date.getTimeInMillis();
    }
}
