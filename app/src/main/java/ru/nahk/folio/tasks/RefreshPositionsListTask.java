package ru.nahk.folio.tasks;

import android.support.annotation.NonNull;

import ru.nahk.folio.activities.PositionsListAdapter;
import ru.nahk.folio.utils.ProgressHandler;
import ru.nahk.folio.model.PortfolioDatabase;

/**
 * Async task to refresh portfolio positions list.
 */
public class RefreshPositionsListTask extends UiAsyncTask<PositionsListAdapter> {
    /**
     * Persistent portfolio data store.
     */
    private PortfolioDatabase mDataStore;

    /**
     * Portfolio positions list adapter.
     */
    private PositionsListAdapter mListAdapter;

    /**
     * Creates new instance of the {@link RefreshPositionsListTask} class
     * with the progress handler, data store and list adapter.
     * @param progressHandler Async task progress handler.
     * @param dataStore Persistent portfolio data store.
     * @param listAdapter Portfolio positions list adapter.
     */
    public RefreshPositionsListTask(
        @NonNull ProgressHandler progressHandler,
        @NonNull PortfolioDatabase dataStore,
        @NonNull PositionsListAdapter listAdapter) {

        super(progressHandler);

        mDataStore = dataStore;
        mListAdapter = listAdapter;
    }

    /**
     * Reloads portfolio positions information and updates the list adapter.
     * @return Updated list adapter.
     */
    @Override
    protected PositionsListAdapter doAsync() {
        mListAdapter.setData(mDataStore.loadPortfolio());
        return mListAdapter;
    }

    /**
     * Notifies adapter about the changes.
     * @param adapter Portfolio positions list adapter to notify.
     */
    @Override
    protected void doAfter(PositionsListAdapter adapter) {
        adapter.notifyDataSetChanged();
    }
}
