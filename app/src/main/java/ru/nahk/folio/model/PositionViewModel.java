package ru.nahk.folio.model;

import android.arch.persistence.room.ColumnInfo;

import java.math.BigDecimal;

/**
 * View-model for the portfolio position.
 */
public class PositionViewModel extends PositionsListItemViewModel {
    /**
     * Stock symbol.
     */
    @ColumnInfo(name = "symbol_id")
    public String symbol;

    /**
     * Shares quantity.
     */
    @ColumnInfo(name = "quantity")
    public int quantity;

    /**
     * Value of single one share.
     */
    @ColumnInfo(name = "symbol_value")
    public BigDecimal symbolValue;

    /**
     * Recent change in share's value.
     */
    @ColumnInfo(name = "symbol_value_change")
    public BigDecimal symbolValueChange;
}
