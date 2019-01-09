package ru.nahk.folio.tasks;

import android.support.annotation.NonNull;

import ru.nahk.folio.activities.PositionsListAdapter;
import ru.nahk.folio.utils.ProgressHandler;
import ru.nahk.folio.model.PortfolioDatabase;
import ru.nahk.folio.utils.Callback;

/**
 * Async task to create portfolio list adapter and load initial portfolio data.
 */
public class LoadPortfolioTask extends UiAsyncTask<PositionsListAdapter> {
    /**
     * Persisted portfolio data storage.
     */
    private final PortfolioDatabase mDataStore;

    /**
     * Listener for positions list item long click events.
     */
    private final PositionsListAdapter.OnItemLongClickListener mItemLongClickListener;

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
     * Creates new instance of the {@link LoadPortfolioTask} class
     * with the provided progress handler, persisted portfolio storage,
     * list item long click listener, group item click listener,
     * position item click listener and task completion callback.
     * @param progressHandler Async task progress handler.
     * @param dataStore Persistent portfolio data store.
     * @param itemLongClickListener Listener for list item long click events.
     * @param groupItemClickListener Listener for group items click events.
     * @param positionItemClickListener Listener for position items click events.
     * @param completionCallback Callback to process task result.
     */
    public LoadPortfolioTask(
        @NonNull ProgressHandler progressHandler,
        @NonNull PortfolioDatabase dataStore,
        @NonNull PositionsListAdapter.OnItemLongClickListener itemLongClickListener,
        @NonNull PositionsListAdapter.OnGroupItemClickListener groupItemClickListener,
        @NonNull PositionsListAdapter.OnPositionItemClickListener positionItemClickListener,
        @NonNull Callback<PositionsListAdapter> completionCallback) {

        super(progressHandler);

        mDataStore = dataStore;
        mItemLongClickListener = itemLongClickListener;
        mGroupItemClickListener = groupItemClickListener;
        mPositionItemClickListener = positionItemClickListener;
        mCompletionCallback = completionCallback;
    }

    /**
     * Creates {@link PositionsListAdapter} and populates it with stored portfolio data.
     * @return Newly created list adapter.
     */
    @Override
    protected PositionsListAdapter doAsync() {
        PositionsListAdapter adapter = new PositionsListAdapter();

        adapter.setOnItemLongClickListener(mItemLongClickListener);
        adapter.setOnGroupClickListener(mGroupItemClickListener);
        adapter.setOnPositionClickListener(mPositionItemClickListener);

        adapter.setData(mDataStore.loadPortfolio());

        return adapter;
    }

    /**
     * Invokes completion callback passing it created adapter.
     * @param adapter Adapter that was created.
     */
    @Override
    protected void doAfter(PositionsListAdapter adapter) {
        super.doAfter(adapter);
        mCompletionCallback.run(adapter);
    }
}
