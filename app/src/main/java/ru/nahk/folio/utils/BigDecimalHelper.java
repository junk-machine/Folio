package ru.nahk.folio.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Helper class to work with {@link BigDecimal} values.
 */
public final class BigDecimalHelper {
    /**
     * Number of allowed fraction digits. Stocks below $1 can be priced with 0.0001 increments.
     * This is primarily for storage and internal computations, display format is still using
     * standard currency, which is 2.
     */
    public static final int MAX_FRACTION_DIGITS = 4;

    /**
     * One million.
     */
    private static BigDecimal MILLION = new BigDecimal(1000000L);

    /**
     * One billion.
     */
    private static BigDecimal BILLION = new BigDecimal(1000000000L);

    /**
     * One trillion.
     */
    private static BigDecimal TRILLION = new BigDecimal(1000000000000L);

    /**
     * Format for currency values.
     */
    private static final NumberFormat CURRENCY_FORMAT = getCurrencyFormat();

    /**
     * Format for currency value changes.
     */
    private static final NumberFormat CURRENCY_CHANGE_FORMAT = getCurrencyChangeFormat();

    /**
     * Format for percentage values.
     */
    private static final NumberFormat PERCENTAGE_FORMAT = getPercentageFormat();

    /**
     * Format for percentage values in short form.
     */
    private static final NumberFormat SHORT_PERCENTAGE_FORMAT = getShortPercentageFormat();

    /**
     * Adds two {@link BigDecimal} values.
     * @param accumulator Accumulator to hold the result.
     * @param increment Value to add.
     * @return Updated accumulator value.
     */
    public static BigDecimal add(BigDecimal accumulator, BigDecimal increment) {
        if (accumulator == null) {
            return increment;
        } else if (increment == null) {
            return accumulator;
        }

        return accumulator.add(increment);
    }

    /**
     * Strips trailing zeros from {@link BigDecimal} number.
     * @return New {@link BigDecimal} without trailing zeros.
     */
    public static BigDecimal stripTrailingZeros(BigDecimal number) {
        // Work-around Java bug for zero value
        if (number.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return number.stripTrailingZeros();
    }

    /**
     * Truncates {@link BigDecimal} to be a valid currency value.
     * @param value Value to truncate.
     * @return Valid currency value.
     */
    public static BigDecimal truncateCurrency(BigDecimal value) {
        return
            value.scale() > MAX_FRACTION_DIGITS
                ? value.setScale(MAX_FRACTION_DIGITS, BigDecimal.ROUND_DOWN)
                : value;
    }

    /**
     * Formats currency value.
     * @param value Value to format.
     * @return Formatted currency value.
     */
    public static String formatCurrency(BigDecimal value) {
        return CURRENCY_FORMAT.format(value);
    }

    /**
     * Formats currency value change.
     * @param change Value change to format.
     * @return Formatted currency value change.
     */
    public static String formatCurrencyChange(BigDecimal change) {
        return CURRENCY_CHANGE_FORMAT.format(change);
    }

    /**
     * Formats currency value rounding it to millions, billions or trillions where possible.
     * @param value Value to format.
     * @return Formatted currency value.
     */
    public static String roundFormatCurrency(BigDecimal value) {
        String suffix = null;

        if (TRILLION.compareTo(value) < 0) {
            value = value.divide(TRILLION, 2, RoundingMode.FLOOR);
            suffix = "T";
        } else if (BILLION.compareTo(value) < 0) {
            value = value.divide(BILLION, 2, RoundingMode.FLOOR);
            suffix = "B";
        } else if (MILLION.compareTo(value) < 0) {
            value = value.divide(MILLION, 2, RoundingMode.FLOOR);
            suffix = "M";
        }

        return
            suffix == null
                ? formatCurrency(value)
                : formatCurrency(value) + " " + suffix;
    }

    /**
     * Computes partial value percentage of the full value and formats it to string.
     * @param full Full value.
     * @param part Partial value.
     * @return Formatted percentage value.
     */
    public static String formatPercentage(BigDecimal full, BigDecimal part) {
        return PERCENTAGE_FORMAT.format(computePercentage(full, part));
    }

    /**
     * Computes partial value percentage of the full value and formats it to a short string.
     * @param full Full value.
     * @param part Partial value.
     * @return Formatted percentage value.
     */
    public static String formatPercentageShort(BigDecimal full, BigDecimal part) {
        return SHORT_PERCENTAGE_FORMAT.format(computePercentage(full, part));
    }

    /**
     * Computes partial value percentage of the full value
     * @param full Full value.
     * @param part Partial value.
     * @return Percentage value.
     */
    private static float computePercentage(BigDecimal full, BigDecimal part) {
        return
            full == null || part == null || full.equals(BigDecimal.ZERO)
                ? 0
                : Math.abs(part.floatValue() / full.floatValue());
    }

    /**
     * Retrieves currency format to use when formatting money.
     * @return Currency format to use.
     */
    private static NumberFormat getCurrencyFormat() {
        return NumberFormat.getCurrencyInstance(Locale.US);
    }

    /**
     * Retrieves currency value change format to use when formatting money.
     * @return Currency value change format to use.
     */
    private static DecimalFormat getCurrencyChangeFormat() {
        DecimalFormat format = new DecimalFormat();
        format.setMinimumFractionDigits(CURRENCY_FORMAT.getMinimumFractionDigits());
        format.setMaximumFractionDigits(CURRENCY_FORMAT.getMaximumFractionDigits());
        format.setPositivePrefix("+");
        return format;
    }

    /**
     * Retrieves format to use when formatting percentage.
     * @return Percentage format to use.
     */
    private static NumberFormat getPercentageFormat() {
        NumberFormat format = NumberFormat.getPercentInstance();
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(2);
        return format;
    }

    /**
     * Retrieves short format to use when formatting percentage.
     * @return Short percentage format.
     */
    private static NumberFormat getShortPercentageFormat() {
        NumberFormat format = getPercentageFormat();
        format.setMaximumFractionDigits(0);
        return format;
    }
}
