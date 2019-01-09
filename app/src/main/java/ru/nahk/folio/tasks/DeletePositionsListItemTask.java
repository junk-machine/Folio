package ru.nahk.folio.tasks;

import android.content.Context;
import android.support.annotation.NonNull;

import ru.nahk.folio.activities.PositionsListAdapter;
import ru.nahk.folio.model.GroupDao;
import ru.nahk.folio.model.GroupViewModel;
import ru.nahk.folio.model.PortfolioDatabase;
import ru.nahk.folio.model.PositionViewModel;
import ru.nahk.folio.model.PositionsListItemViewModel;
import ru.nahk.folio.utils.ProgressHandler;

/**
 * Async task to delete portfolio positions list item.
 */
public class DeletePositionsListItemTask extends RefreshPortfolioItemWidgetsTask<PositionsListAdapter> {
    /**
     * Persistent portfolio data store.
     */
    private PortfolioDatabase mDataStore;

    /**
     * Portfolio positions list adapter.
     */
    private PositionsListAdapter mListAdapter;

    /**
     * Portfolio positions list item to delete.
     */
    private PositionsListItemViewModel mPositionsListItem;

    /**
     * Creates new instance of the {@link AddPositionTask} class
     * with the provided progress handler, activity context, data store,
     * list adapter, parent group and stock symbol.
     * @param progressHandler Async task progress handler.
     * @param context Activity context.
     * @param dataStore Persistent portfolio data store.
     * @param listAdapter Portfolio positions list adapter.
     * @param positionsListItem Portfolio positions list item to delete.
     */
    public DeletePositionsListItemTask(
        @NonNull ProgressHandler progressHandler,
        @NonNull Context context,
        @NonNull PortfolioDatabase dataStore,
        @NonNull PositionsListAdapter listAdapter,
        @NonNull PositionsListItemViewModel positionsListItem) {

        super(progressHandler, context);

        mDataStore = dataStore;
        mListAdapter = listAdapter;
        mPositionsListItem = positionsListItem;
    }

    /**
     * Deletes positions llist item and reloads the data.
     * @return Portfolio positions list adapter if item was deleted, otherwise NULL.
     */
    @Override
    protected PositionsListAdapter doAsync() {
        if (mPositionsListItem instanceof GroupViewModel) {
            // Delete group
            if (mPositionsListItem.id == GroupDao.ROOT_GROUP_ID) {
                mDataStore.clearPortfolio();
            }
            else {
                mDataStore.groupDao().delete(mPositionsListItem.id);
            }
        } else if (mPositionsListItem instanceof PositionViewModel) {
            // Delete position
            mDataStore.runInTransaction(new Runnable() {
                @Override
                public void run() {
                    mDataStore.positionDao().delete(mPositionsListItem.id);
                    mDataStore.symbolDao().cleanupSymbols();
                }
            });
        } else {
            // No action will be taken, thus don't need to refresh
            return null;
        }

        // TODO: Optimize deletion of an empty group? No need to reload everything.
        mListAdapter.setData(mDataStore.loadPortfolio());
        return mListAdapter;
    }

    /**
     * Notifies adapter about the changes.
     * @param adapter Positions list adapter.
     */
    @Override
    protected void doAfter(PositionsListAdapter adapter) {
        super.doAfter(adapter);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
