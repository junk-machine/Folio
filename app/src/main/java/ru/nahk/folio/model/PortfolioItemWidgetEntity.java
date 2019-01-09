package ru.nahk.folio.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Entity to store configuration for portfolio item widgets.
 */
@Entity(
    tableName = PortfolioItemWidgetEntity.TABLE_NAME
)
public class PortfolioItemWidgetEntity {
    /**
     * Name of the portfolio item widget configuration table.
     */
    static final String TABLE_NAME = "portfolio_item_widgets";

    /**
     * Name of the app widget identifier column.
     */
    static final String ID_COLUMN = "id";

    /**
     * Name of the referenced portfolio item type column.
     */
    static final String ITEM_TYPE_COLUMN = "item_type";

    /**
     * Name of the referenced portfolio item identifier column.
     */
    static final String ITEM_ID_COLUMN = "item_id";

    /**
     * Positions group item type.
     */
    public static final int ITEM_TYPE_GROUP = 0;

    /**
     * Portfolio position item type.
     */
    public static final int ITEM_TYPE_POSITION = 1;

    /**
     * Unique identifier of the app widget.
     */
    @PrimaryKey
    @ColumnInfo(name = ID_COLUMN)
    public int id;

    /**
     * Type of the referenced portfolio item: group or position.
     */
    @ColumnInfo(name = ITEM_TYPE_COLUMN)
    public int itemType;

    /**
     * Identifier of the referenced portfolio item.
     */
    @ColumnInfo(name = ITEM_ID_COLUMN)
    public long itemId;

    /**
     * Creates new instance of the {@link LotEntity} class.
     */
    public PortfolioItemWidgetEntity() { }

    /**
     * Creates new instance of the {@link LotEntity} class
     * with the given values.
     * @param id Unique identifier of the widget.
     * @param itemType Type of the referenced portfolio item.
     * @param itemId Identifier of the referenced portfolio item.
     */
    @Ignore
    public PortfolioItemWidgetEntity(int id, int itemType, long itemId) {
        this.id = id;
        this.itemType = itemType;
        this.itemId = itemId;
    }
}
