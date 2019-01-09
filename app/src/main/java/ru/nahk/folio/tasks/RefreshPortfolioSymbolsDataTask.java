package ru.nahk.folio.tasks;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import ru.nahk.folio.activities.PositionsListAdapter;
import ru.nahk.folio.utils.ProgressHandler;
import ru.nahk.folio.model.PortfolioDatabase;
import ru.nahk.folio.model.SymbolEntity;
import ru.nahk.folio.stockapi.StockApiFactory;

/**
 * Async task to refresh symbols data for all positions in the portfolio.
 */
public class RefreshPortfolioSymbolsDataTask extends RefreshPortfolioItemWidgetsTask<PositionsListAdapter> {
    /**
     * Persistent portfolio data store.
     */
    private final PortfolioDatabase mDataStore;

    /**
     * Portfolio positions list adapter.
     */
    private final PositionsListAdapter mListAdapter;

    /**
     * Creates new instance of the {@link RefreshPortfolioSymbolsDataTask} class
     * with the provided progress handler, activity context, data store and list adapter.
     * @param progressHandler Async task progress handler.
     * @param context Activity context.
     * @param dataStore Persistent portfolio data store.
     * @param listAdapter Portfolio positions list adapter.
     */
    public RefreshPortfolioSymbolsDataTask(
        @NonNull ProgressHandler progressHandler,
        @NonNull Context context,
        @NonNull PortfolioDatabase dataStore,
        @NonNull PositionsListAdapter listAdapter) {

        super(progressHandler, context);

        mDataStore = dataStore;
        mListAdapter = listAdapter;
    }

    /**
     * Updates all stock symbols detailed information, saves it to the persistent store and
     * updates list adapter.
     * @return Updated portfolio positions list adapter.
     * @throws Exception Thrown if anything goes wrong.
     */
    @Override
    protected PositionsListAdapter doAsync() throws Exception {
        List<SymbolEntity> symbols = mDataStore.symbolDao().get();

        StockApiFactory.getApi().updateSymbols(symbols);

        mDataStore.symbolDao().update(symbols);

        mListAdapter.setData(mDataStore.loadPortfolio());
        return mListAdapter;
    }

    /**
     * Notifies portfolio positions list adapter about the changes.
     * @param adapter Adapter to notify or NULL, if there were no changes.
     */
    @Override
    protected void doAfter(PositionsListAdapter adapter) {
        super.doAfter(adapter);
        adapter.notifyDataSetChanged();
    }
}
