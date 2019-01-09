package ru.nahk.folio.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Ignore;
import android.support.annotation.NonNull;

/**
 * Base view-model for portfolio positions list item.
 */
public abstract class PositionsListItemViewModel extends PortfolioItemViewModel {
    /**
     * Name of the item.
     */
    @ColumnInfo(name = "name")
    public String name;

    /**
     * Nesting level of the item.
     */
    @Ignore
    public byte level;

    /**
     * View-model of the parent group.
     */
    @Ignore
    public GroupViewModel parent;

    /**
     * Get the name of the item.
     * @return Name of the item
     */
    @Ignore
    @Override
    @NonNull
    public String toString() {
        return name;
    }
}
