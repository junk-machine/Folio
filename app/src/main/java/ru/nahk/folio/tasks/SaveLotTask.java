package ru.nahk.folio.tasks;

import android.content.Context;
import android.support.annotation.NonNull;

import ru.nahk.folio.activities.LotsListAdapter;
import ru.nahk.folio.model.LotDao;
import ru.nahk.folio.model.LotEntity;
import ru.nahk.folio.model.PortfolioDatabase;
import ru.nahk.folio.utils.ProgressHandler;

/**
 * Async task to save lot and refresh data in the list adapter.
 */
public class SaveLotTask extends RefreshPortfolioItemWidgetsTask<Long> {
    /**
     * Persisted storage for position lots.
     */
    private LotDao mLotDao;

    /**
     * Lots list adapter.
     */
    private LotsListAdapter mListAdapter;

    /**
     * Lot information to save.
     */
    private LotEntity mLot;

    /**
     * Creates new instance of the {@link SaveLotTask} class
     * with the provided progress handler, activity context, lots data access,
     * list adapter and lot entity.
     * @param progressHandler Async task progress handler.
     * @param context Activity context.
     * @param lotDao Persistent lots data store.
     * @param listAdapter Lots list adapter.
     * @param lot Lot to save.
     */
    public SaveLotTask(
        @NonNull ProgressHandler progressHandler,
        @NonNull Context context,
        @NonNull LotDao lotDao,
        @NonNull LotsListAdapter listAdapter,
        @NonNull LotEntity lot) {

        super(progressHandler, context);

        mLotDao = lotDao;
        mListAdapter = listAdapter;
        mLot = lot;
    }

    /**
     * Saves the lot information and updates data in the lots list adapter.
     * @return Lots list adapter that was updated.
     */
    @Override
    protected Long doAsync() {
        Long modifiedLotId;

        if (mLot.id == PortfolioDatabase.NEW_ENTITY_ID) {
            modifiedLotId = mLotDao.insert(mLot);
        } else {
            mLotDao.update(mLot);
            modifiedLotId = mLot.id;
        }

        mListAdapter.setData(
            mLotDao.getLotsForPosition(mLot.positionId));

        return modifiedLotId;
    }

    /**
     * Notifies lots list adapter about the changes.
     * @param modifiedLotId Identifier of the modified lot.
     */
    @Override
    protected void doAfter(Long modifiedLotId) {
        super.doAfter(modifiedLotId);

        mListAdapter.notifyDataSetChanged();

        if (modifiedLotId != null) {
            mListAdapter.scrollToItem(modifiedLotId);
        }
    }
}
