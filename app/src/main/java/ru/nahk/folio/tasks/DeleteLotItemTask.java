package ru.nahk.folio.tasks;

import android.content.Context;
import android.support.annotation.NonNull;

import ru.nahk.folio.activities.LotsListAdapter;
import ru.nahk.folio.model.LotDao;
import ru.nahk.folio.model.LotViewModel;
import ru.nahk.folio.utils.Callback;
import ru.nahk.folio.utils.ProgressHandler;

/**
 * Async task to delete position lot item.
 */
public class DeleteLotItemTask extends RefreshPortfolioItemWidgetsTask<Integer> {
    /**
     * Data access for persistent lots information.
     */
    private LotDao mLotDao;

    /**
     * Lots list adapter.
     */
    private LotsListAdapter mListAdapter;

    /**
     * Lot item to delete.
     */
    private LotViewModel mLotItem;

    /**
     * Callback to process task result.
     */
    private Callback<Integer> mCompletionCallback;

    /**
     * Creates new instance of the {@link DeleteLotItemTask} class
     * with the provided progress handler, activity context,
     * lots data access, list adapter, lot item and completion callback.
     * @param progressHandler Async task progress handler.
     * @param context Activity context.
     * @param lotDao Persistent data store for lots data.
     * @param listAdapter Lots list adapter.
     * @param lotItem Lot item to delete.
     * @param completionCallback Callback to process task results.
     */
    public DeleteLotItemTask(
        @NonNull ProgressHandler progressHandler,
        @NonNull Context context,
        @NonNull LotDao lotDao,
        @NonNull LotsListAdapter listAdapter,
        @NonNull LotViewModel lotItem,
        @NonNull Callback<Integer> completionCallback) {

        super(progressHandler, context);

        mLotDao = lotDao;
        mListAdapter = listAdapter;
        mLotItem = lotItem;
        mCompletionCallback = completionCallback;
    }

    /**
     * Removes lot from the database and list adapter.
     * @return Adapter index of the removed item.
     */
    @Override
    protected Integer doAsync() {
        mLotDao.delete(mLotItem.id);
        return mListAdapter.deleteItem(mLotItem);
    }

    /**
     * Invokes completion callback passing it adapter index of the removed item.
     * @param removedIndex Adapter index of the removed item.
     */
    @Override
    protected void doAfter(Integer removedIndex) {
        super.doAfter(removedIndex);
        mCompletionCallback.run(removedIndex);
    }
}
