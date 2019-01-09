package ru.nahk.folio.tasks;

import android.support.annotation.NonNull;

import ru.nahk.folio.activities.PositionsListAdapter;
import ru.nahk.folio.model.PortfolioDatabase;
import ru.nahk.folio.utils.Callback;
import ru.nahk.folio.utils.ProgressHandler;

/**
 * Async task that loads portfolio with all groups in expanded state.
 */
public class LoadExpandedPortfolioTask extends UiAsyncTask<PositionsListAdapter> {
    /**
     * Persisted portfolio data storage.
     */
    private final PortfolioDatabase mDataStore;

    /**
     * Listener for positions group item click events.
     */
    private final PositionsListAdapter.OnGroupItemClickListener mGroupItemClickListener;

    /**
     * Listener for portfolio position item click events.
     */
    private final PositionsListAdapter.OnPositionItemClickListener mPositionItemClickListener;

    /**
     * Callback to process task result.
     */
    private final Callback<PositionsListAdapter> mCompletionCallback;

    /**
     * Creates new instance of the {@link LoadExpandedPortfolioTask} class
     * with the provided progress handler, persisted portfolio storage,
     * group item click listener, position item click listener and
     * task completion callback.
     * @param progressHandler Async task progress handler.
     * @param dataStore Persistent portfolio data store.
     * @param groupItemClickListener Listener for group items click events.
     * @param positionItemClickListener Listener for position items click events.
     * @param completionCallback Callback to process task result.
     */
    public LoadExpandedPortfolioTask(
        @NonNull ProgressHandler progressHandler,
        @NonNull PortfolioDatabase dataStore,
        @NonNull PositionsListAdapter.OnGroupItemClickListener groupItemClickListener,
        @NonNull PositionsListAdapter.OnPositionItemClickListener positionItemClickListener,
        @NonNull Callback<PositionsListAdapter> completionCallback) {

        super(progressHandler);

        mGroupItemClickListener = groupItemClickListener;
        mPositionItemClickListener = positionItemClickListener;
        mDataStore = dataStore;
        mCompletionCallback = completionCallback;
    }

    /**
     * Creates {@link PositionsListAdapter} and populates it with stored portfolio data.
     * @return Newly created list adapter.
     */
    @Override
    protected PositionsListAdapter doAsync() {
        PositionsListAdapter adapter = new PositionsListAdapter();

        adapter.setOnGroupClickListener(mGroupItemClickListener);
        adapter.setOnPositionClickListener(mPositionItemClickListener);

        adapter.setData(mDataStore.loadPortfolio(true));

        return adapter;
    }

    /**
     * Invokes completion callback passing it created adapter.
     * @param adapter Adapter that was created.
     */
    @Override
    protected void doAfter(PositionsListAdapter adapter) {
        mCompletionCallback.run(adapter);
    }
}
