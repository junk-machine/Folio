package ru.nahk.folio.tasks;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ru.nahk.folio.activities.LotsListAdapter;
import ru.nahk.folio.model.PortfolioDatabase;
import ru.nahk.folio.model.SymbolEntity;
import ru.nahk.folio.stockapi.StockApiFactory;
import ru.nahk.folio.utils.ProgressHandler;

/**
 * Async task to refresh symbol data for position lots.
 */
public class RefreshLotsSymbolDataTask extends RefreshPortfolioItemWidgetsTask<LotsListAdapter> {
    /**
     * Stock symbol to refresh details for.
     */
    private String mSymbol;

    /**
     * Identifier of the position, that holds lots for the symbol.
     */
    private long mPositionId;

    /**
     * Persistent portfolio data store.
     */
    private PortfolioDatabase mDataStore;

    /**
     * Lots list adapter.
     */
    private LotsListAdapter mListAdapter;

    /**
     * Creates new instance of the {@link RefreshLotsSymbolDataTask} class
     * with the provided progress handler, activity context, stock symbol,
     * position identifier, data store and list adapter.
     * @param progressHandler Async task progress handler.
     * @param context Activity context.
     * @param symbol Stock symbol to refresh.
     * @param positionId Identifier of the position holding lots for the symbol.
     * @param dataStore Persistent portfolio data store.
     * @param listAdapter Lots list adapter.
     */
    public RefreshLotsSymbolDataTask(
        @NonNull ProgressHandler progressHandler,
        @NonNull Context context,
        @NonNull String symbol,
        long positionId,
        @NonNull PortfolioDatabase dataStore,
        @NonNull LotsListAdapter listAdapter) {

        super(progressHandler, context);

        mSymbol = symbol;
        mPositionId = positionId;
        mDataStore = dataStore;
        mListAdapter = listAdapter;
    }

    /**
     * Updates stock symbol detailed information, saves it to the persistent store and
     * updates list adapter.
     * @return Updated lots list adapter.
     * @throws Exception Thrown if anything goes wrong.
     */
    @Override
    protected LotsListAdapter doAsync() throws Exception {
        SymbolEntity symbolEntity =
            mDataStore.symbolDao().get(mSymbol);

        if (symbolEntity == null) {
            return null;
        }

        List<SymbolEntity> symbols = new ArrayList<>(1);
        symbols.add(symbolEntity);

        StockApiFactory.getApi().updateSymbols(symbols);

        mDataStore.symbolDao().update(symbols);

        mListAdapter.setData(
            mDataStore.lotDao().getLotsForPosition(mPositionId));

        return mListAdapter;
    }

    /**
     * Notifies lots list adapter about the changes.
     * @param adapter Adapter to notify or NULL, if there were no changes.
     */
    @Override
    protected void doAfter(LotsListAdapter adapter) {
        super.doAfter(adapter);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
