package ru.nahk.folio.tasks;

import android.content.Context;
import android.support.annotation.NonNull;

import ru.nahk.folio.activities.PositionsListAdapter;
import ru.nahk.folio.model.GroupViewModel;
import ru.nahk.folio.model.PortfolioDatabase;
import ru.nahk.folio.model.PositionViewModel;
import ru.nahk.folio.model.PositionsListItemViewModel;
import ru.nahk.folio.utils.ProgressHandler;

/**
 * Async task to move item in the portfolio positions list.
 */
public class MovePositionsListItemTask extends RefreshPortfolioItemWidgetsTask<Long> {
    /**
     * Persistent portfolio data store.
     */
    private final PortfolioDatabase mDataStore;

    /**
     * Portfolio positions list adapter.
     */
    private final PositionsListAdapter mListAdapter;

    /**
     * Positions list item to move.
     */
    private final PositionsListItemViewModel mPositionsListItem;

    /**
     * Target positions group to move item to.
     */
    private final GroupViewModel mTargetGroup;

    /**
     * Creates new instance of the {@link MovePositionsListItemTask} class
     * with the provided progress handler, activity context, data store,
     * list adapter, list item and target group.
     * @param progressHandler Async task progress handler.
     * @param context Activity context.
     * @param dataStore Persistent portfolio data store.
     * @param listAdapter Portfolio positions list adapter.
     * @param positionsListItem Positions list item to move.
     * @param targetGroup Target positions group.
     */
    public MovePositionsListItemTask(
        @NonNull ProgressHandler progressHandler,
        @NonNull Context context,
        @NonNull PortfolioDatabase dataStore,
        @NonNull PositionsListAdapter listAdapter,
        @NonNull PositionsListItemViewModel positionsListItem,
        @NonNull GroupViewModel targetGroup) {

        super(progressHandler, context);

        mDataStore = dataStore;
        mListAdapter = listAdapter;
        mTargetGroup = targetGroup;
        mPositionsListItem = positionsListItem;
    }

    /**
     * Moves item to a new parent.
     * @return Identifier of the moved item or NULL, if nothing was moved.
     */
    @Override
    protected Long doAsync() {
        if (mPositionsListItem.parent == null
            || mPositionsListItem.parent.id == mTargetGroup.id) {
            // Cannot move root group and nothing to do when moving to the same parent
            return null;
        }

        if (mPositionsListItem instanceof GroupViewModel) {
            if (mPositionsListItem.id == mTargetGroup.id) {
                // Cannot move group to itself
                return null;
            }

            // Move group
            mDataStore.groupDao().move(mPositionsListItem.id, mTargetGroup.id);
        } else if (mPositionsListItem instanceof PositionViewModel) {
            // Move position
            mDataStore.positionDao().move(mPositionsListItem.id, mTargetGroup.id);
        } else {
            // No action will be taken, thus don't need to refresh
            return null;
        }

        // TODO: Optimize move of an empty group? No need to reload everything.
        mListAdapter.setData(mDataStore.loadPortfolio());

        return mPositionsListItem.id;
    }

    /**
     * Notifies list adapter about the changes.
     * @param movedItemId Identifier of the moved item.
     */
    @Override
    protected void doAfter(Long movedItemId) {
        super.doAfter(movedItemId);

        if (movedItemId != null) {
            mListAdapter.notifyDataSetChanged();
            mListAdapter.scrollToItem(movedItemId);
        }
    }
}
