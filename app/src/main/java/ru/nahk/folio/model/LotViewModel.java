package ru.nahk.folio.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Calendar;

/**
 * View-model for the position lot.
 */
public class LotViewModel extends PortfolioItemViewModel {
    /**
     * Shares quantity.
     */
    @ColumnInfo(name = "quantity")
    public int quantity;

    /**
     * Purchase price
     */
    @ColumnInfo(name = "purchase_price")
    public BigDecimal purchasePrice;

    /**
     * Purchase date.
     */
    @ColumnInfo(name = "purchase_date")
    @TypeConverters(DateTypeConverter.class)
    public Calendar purchaseDate;

    /**
     * Purchase commission.
     */
    @ColumnInfo(name = "commission")
    public BigDecimal commission;

    /**
     * Formats shares quantity to string.
     * @return Shares quantity string.
     */
    @Ignore
    @NonNull
    @Override
    public String toString() {
        return NumberFormat.getNumberInstance().format(quantity);
    }
}
