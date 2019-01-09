package ru.nahk.folio.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Ignore;

import java.util.ArrayList;
import java.util.List;

/**
 * View-model for the positions group.
 */
public class GroupViewModel extends PositionsListItemViewModel {
    /**
     * Group expansion state.
     */
    @ColumnInfo(name = "is_expanded")
    public boolean isExpanded;

    /**
     * Group children.
     */
    @Ignore
    public final List<PositionsListItemViewModel> children;

    /**
     * Creates new instance of the {@link GroupViewModel} class.
     */
    public GroupViewModel() {
        children = new ArrayList<>();
    }
}
