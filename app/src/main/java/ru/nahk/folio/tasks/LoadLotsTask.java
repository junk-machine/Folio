package ru.nahk.folio.tasks;

import android.support.annotation.NonNull;

import ru.nahk.folio.activities.LotsListAdapter;
import ru.nahk.folio.utils.ProgressHandler;
import ru.nahk.folio.model.LotDao;
import ru.nahk.folio.utils.Callback;

/**
 * Async task to create lots list adapter and load initial lots data.
 */
public class LoadLotsTask extends UiAsyncTask<LotsListAdapter> {
    /**
     * Data access for persistent lots information.
     */
    private LotDao mLotDao;

    /**
     * Identifier of the parent position.
     */
    private long mPositionId;

    /**
     * Listener for lot item click events.
     */
    private LotsListAdapter.OnLotItemClickListener mLotItemClickListener;

    /**
     * Listener for lot item long click events.
     */
    private LotsListAdapter.OnLotItemLongClickListener mLotItemLongClickListener;

    /**
     * Callback to process task result.
     */
    private Callback<LotsListAdapter> mCompletionCallback;

    /**
     * Creates new instance of the {@link LoadLotsTask} class
     * with the provided progress handler, lots data access,
     * parent position identifier, lot item click listener and
     * task completion callback.
     * @param progressHandler Async task progress handler.
     * @param lotDao Persistent data store for lots data.
     * @param positionId Identifier of the position to load lots for.
     * @param lotItemClickListener Listener for position lots click events.
     * @param lotItemLongClickListener Listener for position lots long click events.
     * @param completionCallback Callback to process task results.
     */
    public LoadLotsTask(
        @NonNull ProgressHandler progressHandler,
        @NonNull LotDao lotDao,
        long positionId,
        @NonNull LotsListAdapter.OnLotItemClickListener lotItemClickListener,
        @NonNull LotsListAdapter.OnLotItemLongClickListener lotItemLongClickListener,
        @NonNull Callback<LotsListAdapter> completionCallback) {

        super(progressHandler);

        mLotDao = lotDao;
        mPositionId = positionId;
        mLotItemClickListener = lotItemClickListener;
        mLotItemLongClickListener = lotItemLongClickListener;
        mCompletionCallback = completionCallback;
    }

    /**
     * Creates and populates lots list adapter.
     * @return Newly created list adapter.
     */
    @Override
    protected LotsListAdapter doAsync() {
        LotsListAdapter adapter = new LotsListAdapter();

        adapter.setOnLotClickListener(mLotItemClickListener);
        adapter.setOnLotLongClickListener(mLotItemLongClickListener);

        adapter.setData(mLotDao.getLotsForPosition(mPositionId));

        return adapter;
    }

    /**
     * Invokes completion callback passing it created adapter.
     * @param adapter Adapter that was created.
     */
    @Override
    protected void doAfter(LotsListAdapter adapter) {
        mCompletionCallback.run(adapter);
    }
}
