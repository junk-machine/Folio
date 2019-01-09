package ru.nahk.folio.tasks;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ru.nahk.folio.activities.SymbolDetailsPresenter;
import ru.nahk.folio.model.SymbolDao;
import ru.nahk.folio.model.SymbolEntity;
import ru.nahk.folio.stockapi.StockApiFactory;
import ru.nahk.folio.utils.ProgressHandler;

/**
 * Async task to refresh stock symbol detailed information.
 */
public class RefreshSymbolDataTask extends RefreshPortfolioItemWidgetsTask<SymbolEntity> {
    /**
     * Stock symbol.
     */
    private String mSymbol;

    /**
     * Persistent data store for stock symbols data.
     */
    private SymbolDao mSymbolDao;

    /**
     * Presenter for symbol details information.
     */
    private SymbolDetailsPresenter mSymbolDetailsPresenter;

    /**
     * Creates new instance of the {@link RefreshSymbolDataTask} class
     * with the provided progress provider, stock symbol, symbol data access and
     * symbol details presenter.
     * @param progressHandler Async task progress handler.
     * @param context Activity context.
     * @param symbol Stock symbol to load detail for.
     * @param symbolDao Persistent data store for symbols data.
     * @param symbolDetailsPresenter Presenter for symbol details information.
     */
    public RefreshSymbolDataTask(
        @NonNull ProgressHandler progressHandler,
        @NonNull Context context,
        @NonNull String symbol,
        @NonNull SymbolDao symbolDao,
        @NonNull SymbolDetailsPresenter symbolDetailsPresenter) {

        super(progressHandler, context);

        mSymbol = symbol;
        mSymbolDao = symbolDao;
        mSymbolDetailsPresenter = symbolDetailsPresenter;
    }

    /**
     * Updates stock symbol detailed information and saves it to the persistent store.
     * @return Updated stock symbol entity.
     * @throws Exception Thrown if anything goes wrong.
     */
    @Override
    protected SymbolEntity doAsync() throws Exception {
        SymbolEntity symbolEntity =
            mSymbolDao.get(mSymbol);

        if (symbolEntity == null) {
            return null;
        }

        List<SymbolEntity> symbols = new ArrayList<>(1);
        symbols.add(symbolEntity);

        StockApiFactory.getApi().updateSymbols(symbols);

        mSymbolDao.update(symbols);

        return symbolEntity;
    }

    /**
     * Updates symbol details presenter with new symbol data.
     * @param symbolEntity Updated symbol entity.
     */
    @Override
    protected void doAfter(SymbolEntity symbolEntity) {
        super.doAfter(symbolEntity);

        if (mSymbolDetailsPresenter != null) {
            mSymbolDetailsPresenter.setData(symbolEntity);
        }
    }
}
