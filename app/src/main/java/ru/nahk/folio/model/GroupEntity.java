package ru.nahk.folio.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Group entity.
 */
@Entity(
    tableName = GroupEntity.TABLE_NAME,
    foreignKeys = @ForeignKey(
        entity = GroupEntity.class,
        parentColumns = GroupEntity.ID_COLUMN,
        childColumns = GroupEntity.PARENT_GROUP_ID_COLUMN,
        onDelete = ForeignKey.CASCADE),
    indices = @Index(GroupEntity.PARENT_GROUP_ID_COLUMN)
)
public class GroupEntity {
    /**
     * Name of the groups table.
     */
    static final String TABLE_NAME = "groups";

    /**
     * Name of the group identifier column.
     */
    static final String ID_COLUMN = "id";

    /**
     * Name of the group name column.
     */
    static final String NAME_COLUMN = "name";

    /**
     * Name of the group expansion state column.
     */
    static final String IS_EXPANDED_COLUMN = "is_expanded";

    /**
     * Name of the parent group identifier column.
     */
    static final String PARENT_GROUP_ID_COLUMN = "parent_group_id";

    /**
     * Group identifier.
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID_COLUMN)
    public long id;

    /**
     * Group name.
     */
    @NonNull
    @ColumnInfo(name = NAME_COLUMN)
    public String name;

    /**
     * Group expansion state.
     */
    @ColumnInfo(name = IS_EXPANDED_COLUMN)
    public boolean isExpanded;

    /**
     * Identifier of the parent group.
     */
    @ColumnInfo(name = PARENT_GROUP_ID_COLUMN)
    public Long parentGroupId;

    /**
     * Creates new instance of the {@link GroupEntity} class.
     */
    public GroupEntity() { }

    /**
     * Creates new instance of the {@link GroupEntity} class
     * with given values.
     */
    @Ignore
    public GroupEntity(@NonNull String name, boolean isExpanded, Long parentGroupId) {
        this.name = name;
        this.isExpanded = isExpanded;
        this.parentGroupId = parentGroupId;
    }
}
