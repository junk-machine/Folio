package ru.nahk.folio.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.Calendar;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Position lot entity.
 */
@Entity(
    tableName = LotEntity.TABLE_NAME,
    foreignKeys = @ForeignKey(
        entity = PositionEntity.class,
        parentColumns = PositionEntity.ID_COLUMN,
        childColumns = LotEntity.POSITION_ID_COLUMN,
        onDelete = CASCADE),
    indices = @Index(LotEntity.POSITION_ID_COLUMN)
)
public class LotEntity {
    /**
     * Name of the lots table.
     */
    static final String TABLE_NAME = "lots";

    /**
     * Name of the lot identifier column.
     */
    static final String ID_COLUMN = "id";

    /**
     * Name of the parent position identifier column.
     */
    static final String POSITION_ID_COLUMN = "position_id";

    /**
     * Name of the shares quantity column.
     */
    static final String QUANTITY_COLUMN = "quantity";

    /**
     * Name of the purchase price column.
     */
    static final String PURCHASE_PRICE_COLUMN = "purchase_price";

    /**
     * Name of the purchase date column.
     */
    static final String PURCHASE_DATE_COLUMN = "purchase_date";

    /**
     * Name of the purchase commission column.
     */
    static final String COMMISSION_COLUMN = "commission";

    /**
     * Lot identifier.
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID_COLUMN)
    public long id;

    /**
     * Identifier of the parent position.
     */
    @ColumnInfo(name = POSITION_ID_COLUMN)
    public long positionId;

    /**
     * Shares quantity.
     */
    @ColumnInfo(name = QUANTITY_COLUMN)
    public int quantity;

    /**
     * Purchase price.
     */
    @NonNull
    @ColumnInfo(name = PURCHASE_PRICE_COLUMN)
    public BigDecimal purchasePrice;

    /**
     * Purchase date.
     */
    @NonNull
    @ColumnInfo(name = PURCHASE_DATE_COLUMN)
    @TypeConverters(DateTypeConverter.class)
    public Calendar purchaseDate;

    /**
     * Purchase commission.
     */
    @NonNull
    @ColumnInfo(name = COMMISSION_COLUMN)
    public BigDecimal commission;

    /**
     * Creates new instance of the {@link LotEntity} class.
     */
    public LotEntity() { }

    /**
     * Creates new instance of the {@link LotEntity} class
     * with given values.
     */
    @Ignore
    public LotEntity(
            long positionId,
            int quantity,
            @NonNull BigDecimal purchasePrice,
            @NonNull Calendar purchaseDate,
            @NonNull BigDecimal commission) {
        this.positionId = positionId;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.purchaseDate = purchaseDate;
        this.commission = commission;
    }
}
