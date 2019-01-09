package ru.nahk.folio.tasks;

import android.support.annotation.NonNull;

import ru.nahk.folio.activities.PositionsListAdapter;
import ru.nahk.folio.model.GroupEntity;
import ru.nahk.folio.model.GroupViewModel;
import ru.nahk.folio.model.PortfolioDatabase;
import ru.nahk.folio.utils.ProgressHandler;

/**
 * Async task to add new group.
 */
public class AddGroupTask extends UiAsyncTask<Long> {
    /**
     * Persistent portfolio data store.
     */
    private PortfolioDatabase mDataStore;

    /**
     * Portfolio positions list adapter.
     */
    private PositionsListAdapter mListAdapter;

    /**
     * Target positions group to add new group to.
     */
    private GroupViewModel mParentGroup;

    /**
     * Name of the new group.
     */
    private String mGroupName;

    /**
     * Creates new asynchronous task class that adds new group.
     * @param progressHandler Async task progress handler.
     * @param dataStore Persistent portfolio data store.
     * @param listAdapter Positions list adapter.
     * @param parentGroup Parent group.
     * @param groupName Name of the group to add.
     */
    public AddGroupTask(
        @NonNull ProgressHandler progressHandler,
        @NonNull PortfolioDatabase dataStore,
        @NonNull PositionsListAdapter listAdapter,
        @NonNull GroupViewModel parentGroup,
        @NonNull String groupName) {

        super(progressHandler);

        mDataStore = dataStore;
        mListAdapter = listAdapter;
        mParentGroup = parentGroup;
        mGroupName = groupName;
    }

    /**
     * Adds new group to the persisted storage and reloads the data.
     * @return Identifier of the newly added group.
     */
    @Override
    protected Long doAsync() {
        long newGroupId =
            mDataStore.groupDao().insert(
                new GroupEntity(mGroupName, true, mParentGroup.id));

        // Expand parent group where child was added
        mDataStore.groupDao()
            .setGroupExpandedState(mParentGroup.id, true);

        // Reload portfolio
        // TODO: Optimize and inject group at specific location?
        mListAdapter.setData(mDataStore.loadPortfolio());

        return newGroupId;
    }

    /**
     * Notifies adapter about the changes.
     * @param newGroupId Identifier of the newly added group entity.
     */
    @Override
    protected void doAfter(Long newGroupId) {
        mListAdapter.notifyDataSetChanged();

        // Ensure new group is visible
        if (newGroupId != null) {
            mListAdapter.scrollToItem(newGroupId);
        }
    }
}
