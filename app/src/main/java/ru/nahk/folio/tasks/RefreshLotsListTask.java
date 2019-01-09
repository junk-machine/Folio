package ru.nahk.folio.tasks;

import android.support.annotation.NonNull;

import ru.nahk.folio.activities.LotsListAdapter;
import ru.nahk.folio.utils.ProgressHandler;
import ru.nahk.folio.model.LotDao;

/**
 * Async task to refresh lots data in the list adapter.
 */
public class RefreshLotsListTask extends UiAsyncTask<LotsListAdapter> {
    /**
     * Data access for persistent lots information.
     */
    private LotDao mLotDao;

    /**
     * Lots list adapter.
     */
    private LotsListAdapter mListAdapter;

    /**
     * Identifier of the position to reload lots for.
     */
    private long mPositionId;

    /**
     * Creates new instance of the {@link RefreshLotsListTask} class
     * with the provided progress handler, lot data access, list adapter,
     * list adapter and position identifier.
     * @param progressHandler Async task progress handler.
     * @param lotDao Persistent data store for lots data.
     * @param listAdapter Lots list adapter.
     * @param positionId Identifier of the position to reload lots for.
     */
    public RefreshLotsListTask(
        @NonNull ProgressHandler progressHandler,
        @NonNull LotDao lotDao,
        @NonNull LotsListAdapter listAdapter,
        long positionId) {

        super(progressHandler);

        mLotDao = lotDao;
        mListAdapter = listAdapter;
        mPositionId = positionId;
    }

    /**
     * Reloads lots information and updates the list adapter.
     * @return Updated list adapter.
     */
    @Override
    protected LotsListAdapter doAsync() {
        mListAdapter.setData(
            mLotDao.getLotsForPosition(mPositionId));

        return mListAdapter;
    }

    /**
     * Notifies adapter about the changes.
     * @param adapter Lots list adapter to notify.
     */
    @Override
    protected void doAfter(LotsListAdapter adapter) {
        adapter.notifyDataSetChanged();
    }
}
