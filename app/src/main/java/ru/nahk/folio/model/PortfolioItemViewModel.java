package ru.nahk.folio.model;

import android.arch.persistence.room.ColumnInfo;

import java.math.BigDecimal;

/**
 * Base view-model for all portfolio items.
 */
public abstract class PortfolioItemViewModel {
    /**
     * Identifier of the item.
     */
    @ColumnInfo(name = "id")
    public long id;

    /**
     * Current value of the item.
     */
    @ColumnInfo(name = "current_value")
    public BigDecimal currentValue;

    /**
     * Base value of the item.
     */
    @ColumnInfo(name = "base_value")
    public BigDecimal baseValue;
}
