package ru.nahk.folio.model;

import android.arch.persistence.room.TypeConverter;

import java.math.BigDecimal;

import ru.nahk.folio.utils.BigDecimalHelper;

/**
 * Defines type conversion to store {@link BigDecimal} dollar values as long number of pennies.
 */
class MoneyTypeConverter {
    /**
     * Converts pennies value to {@link BigDecimal} dollar representation.
     * @param pennies Pennies amount.
     * @return Dollar amount.
     */
    @TypeConverter
    public static BigDecimal penniesToDollars(Long pennies) {
        return pennies == null
            ? null
            : new BigDecimal(pennies).scaleByPowerOfTen(-BigDecimalHelper.MAX_FRACTION_DIGITS);
    }

    /**
     * Converts {@link BigDecimal} dollar representation to pennies.
     * @param dollars Dollar amount.
     * @return Pennies amount.
     */
    @TypeConverter
    public static Long dollarsToPennies(BigDecimal dollars) {
        return dollars == null
            ? null
            : dollars.scaleByPowerOfTen(BigDecimalHelper.MAX_FRACTION_DIGITS).longValue();
    }
}
