package ru.nahk.folio.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import static android.arch.persistence.room.ColumnInfo.NOCASE;
import static android.arch.persistence.room.ForeignKey.CASCADE;
import static android.arch.persistence.room.ForeignKey.RESTRICT;

/**
 * Portfolio position entity.
 */
@Entity(
    tableName = PositionEntity.TABLE_NAME,
    foreignKeys = {
        @ForeignKey(
            entity = GroupEntity.class,
            parentColumns = GroupEntity.ID_COLUMN,
            childColumns = PositionEntity.PARENT_GROUP_ID_COLUMN,
            onDelete = CASCADE),
        @ForeignKey(
            entity = SymbolEntity.class,
            parentColumns = SymbolEntity.ID_COLUMN,
            childColumns = PositionEntity.SYMBOL_ID_COLUMN,
            onDelete = RESTRICT)
    },
    indices = {
        @Index(PositionEntity.PARENT_GROUP_ID_COLUMN),
        @Index(PositionEntity.SYMBOL_ID_COLUMN)
    }
)
public class PositionEntity {
    /**
     * Name of the portfolio positions table.
     */
    static final String TABLE_NAME = "positions";

    /**
     * Name of the portfolio position identifier column.
     */
    static final String ID_COLUMN = "id";

    /**
     * Name of the parent group identifier column.
     */
    static final String PARENT_GROUP_ID_COLUMN = "parent_group_id";

    /**
     * Name of the stock symbol column.
     */
    static final String SYMBOL_ID_COLUMN = "symbol_id";

    /**
     * Portfolio position identifier.
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID_COLUMN)
    public long id;

    /**
     * Identifier of the parent group.
     */
    @NonNull
    @ColumnInfo(name = PARENT_GROUP_ID_COLUMN)
    public Long parentGroupId;

    /**
     * Stock symbol.
     */
    @NonNull
    @ColumnInfo(name = SYMBOL_ID_COLUMN, collate = NOCASE)
    public String symbolId;

    /**
     * Creates new instance of the {@link PositionEntity} class.
     */
    public PositionEntity() { }

    /**
     * Creates new instance of the {@link PositionEntity} class
     * with the given values.
     * @param symbolId Stock symbol.
     * @param parentGroupId Identifier of the parent group.
     */
    @Ignore
    public PositionEntity(@NonNull String symbolId, @NonNull Long parentGroupId) {
        this.symbolId = symbolId;
        this.parentGroupId = parentGroupId;
    }
}
