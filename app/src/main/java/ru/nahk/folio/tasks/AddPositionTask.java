package ru.nahk.folio.tasks;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.concurrent.Callable;

import ru.nahk.folio.activities.PositionsListAdapter;
import ru.nahk.folio.model.GroupViewModel;
import ru.nahk.folio.model.PortfolioDatabase;
import ru.nahk.folio.model.PositionEntity;
import ru.nahk.folio.model.SymbolEntity;
import ru.nahk.folio.stockapi.StockApiFactory;
import ru.nahk.folio.utils.ProgressHandler;

/**
 * Async task to add new portfolio position.
 */
public class AddPositionTask extends RefreshPortfolioItemWidgetsTask<Long> {
    /**
     * Persistent portfolio data store.
     */
    private PortfolioDatabase mDataStore;

    /**
     * Portfolio positions list adapter.
     */
    private PositionsListAdapter mListAdapter;

    /**
     * Target positions group to add new position to.
     */
    private GroupViewModel mParentGroup;

    /**
     * Stock symbol for the new position.
     */
    private String mSymbol;

    /**
     * Creates new instance of the {@link AddPositionTask} class
     * with the provided progress handler, activity context, data store,
     * list adapter, parent group and stock symbol.
     * @param progressHandler Async task progress handler.
     * @param context Activity context.
     * @param dataStore Persistent portfolio data store.
     * @param listAdapter Portfolio positions list adapter.
     * @param parentGroup Parent group.
     * @param symbol Stock symbol for the position.
     */
    public AddPositionTask(
        @NonNull ProgressHandler progressHandler,
        @NonNull Context context,
        @NonNull PortfolioDatabase dataStore,
        @NonNull PositionsListAdapter listAdapter,
        @NonNull GroupViewModel parentGroup,
        @NonNull String symbol) {

        super(progressHandler, context);

        mDataStore = dataStore;
        mListAdapter = listAdapter;
        mParentGroup = parentGroup;
        mSymbol = symbol;
    }

    /**
     * Adds new position to the persisted storage and reloads the data.
     * @return Identifier of the newly added group.
     */
    @Override
    protected Long doAsync() {
        final SymbolEntity symbolEntity = new SymbolEntity(mSymbol);

        Long encoded =
            mDataStore.runInTransaction(new Callable<Long>() {
                @Override
                public Long call() {
                    long newSymbolRowNum =
                        mDataStore.symbolDao().ensureSymbol(symbolEntity);

                    long newPositionId = mDataStore.positionDao().insert(
                        new PositionEntity(mSymbol, mParentGroup.id));

                    // Expand group where position was added
                    mDataStore.groupDao()
                        .setGroupExpandedState(mParentGroup.id, true);

                    // Encode isNewSymbol flag and newPositionId into one return value
                    return newPositionId | ((newSymbolRowNum >= 0 ? 1L : 0) << 63);
                }
            });

        boolean isNewSymbol = (encoded >>> 63) == 1;

        if (isNewSymbol) {
            try {
                // New symbol was added - load its details and current price
                StockApiFactory.getApi().updateSymbol(symbolEntity);
                mDataStore.symbolDao().update(symbolEntity);
            } catch (Exception error) {
                // Symbol details refresh is best effort only,
                // we should still update the adapter.
            }
        }

        // Reload portfolio
        mListAdapter.setData(mDataStore.loadPortfolio());

        return encoded << 1 >>> 1;
    }

    /**
     * Notifies adapter about the changes.
     * @param newPositionId Identifier of the newly added position entity.
     */
    @Override
    protected void doAfter(Long newPositionId) {
        super.doAfter(newPositionId);

        mListAdapter.notifyDataSetChanged();

        // Ensure new position is visible
        if (newPositionId != null) {
            mListAdapter.scrollToItem(newPositionId);
        }
    }
}
