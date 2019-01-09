package ru.nahk.folio.tasks;

import android.content.Context;
import android.support.annotation.NonNull;

import ru.nahk.folio.activities.PositionsListAdapter;
import ru.nahk.folio.utils.ProgressHandler;
import ru.nahk.folio.model.GroupDao;
import ru.nahk.folio.model.GroupViewModel;

/**
 * Async task to rename an existing group.
 */
public class RenameGroupTask extends RefreshPortfolioItemWidgetsTask<Integer> {
    /**
     * Data access for persistent positions group information.
     */
    private GroupDao mGroupDao;

    /**
     * Portfolio positions list adapter.
     */
    private PositionsListAdapter mListAdapter;

    /**
     * View-model of the group to rename.
     */
    private GroupViewModel mGroup;

    /**
     * New group name.
     */
    private String mNewName;

    /**
     * Creates new instance of the {@link RenameGroupTask} class
     * with the provided progress handler, activity context, group data access,
     * list adapter, group view-model and new group name.
     * @param progressHandler Async task progress handler.
     * @param context Activity context.
     * @param groupDao Persistent groups data store.
     * @param listAdapter Positions list adapter.
     * @param group Group to rename.
     * @param newName New group name.
     */
    public RenameGroupTask(
        @NonNull ProgressHandler progressHandler,
        @NonNull Context context,
        @NonNull GroupDao groupDao,
        @NonNull PositionsListAdapter listAdapter,
        @NonNull GroupViewModel group,
        @NonNull String newName) {

        super(progressHandler, context);

        mGroupDao = groupDao;
        mListAdapter = listAdapter;
        mGroup = group;
        mNewName = newName;
    }

    /**
     * Renames the group and updates positions list adapter.
     * @return Index of the renamed group item.
     */
    @Override
    protected Integer doAsync() {
        mGroupDao.rename(mGroup.id, mNewName);
        mGroup.name = mNewName;
        return mListAdapter.getItemIndex(mGroup);
    }

    /**
     * Notifies adapter about the changes.
     * @param itemIndex Index of the changed item.
     */
    @Override
    protected void doAfter(Integer itemIndex) {
        super.doAfter(itemIndex);

        if (itemIndex != null) {
            mListAdapter.notifyItemChanged(itemIndex);
        }
    }
}
