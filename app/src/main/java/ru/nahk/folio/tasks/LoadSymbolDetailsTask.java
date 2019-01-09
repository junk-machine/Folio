package ru.nahk.folio.tasks;

import android.support.annotation.NonNull;

import ru.nahk.folio.activities.SymbolDetailsPresenter;
import ru.nahk.folio.model.SymbolDao;
import ru.nahk.folio.model.SymbolEntity;
import ru.nahk.folio.utils.ProgressHandler;

/**
 * Async task to load symbol details adapter and load initial lots data.
 */
public class LoadSymbolDetailsTask extends UiAsyncTask<SymbolEntity> {
    /**
     * Stock symbol to load details for.
     */
    private String mSymbol;

    /**
     * Persistent data store for symbols data.
     */
    private SymbolDao mSymbolDao;

    /**
     * Presenter for symbol details information.
     */
    private SymbolDetailsPresenter mSymbolDetailsPresenter;

    /**
     * Creates new instance of the {@link LoadSymbolDetailsTask} class
     * with the provided progress handler, stock symbol, symbol data access and
     * symbol details presenter.
     * @param progressHandler Async task progress handler.
     * @param symbol Stock symbol to load detail for.
     * @param symbolDao Persistent data store for symbols data.
     * @param symbolDetailsPresenter Presenter for symbol details information.
     */
    public LoadSymbolDetailsTask(
        @NonNull ProgressHandler progressHandler,
        @NonNull String symbol,
        @NonNull SymbolDao symbolDao,
        @NonNull SymbolDetailsPresenter symbolDetailsPresenter) {

        super(progressHandler);

        mSymbol = symbol;
        mSymbolDao = symbolDao;
        mSymbolDetailsPresenter = symbolDetailsPresenter;
    }

    /**
     * Loads detailed symbol information.
     * @return Loaded symbol entity.
     */
    @Override
    protected SymbolEntity doAsync() {
        return mSymbolDao.get(mSymbol);
    }

    /**
     * Updates details presenter with symbol information.
     * @param symbolEntity Loaded symbol entity.
     */
    @Override
    protected void doAfter(SymbolEntity symbolEntity) {
        if (mSymbolDetailsPresenter != null) {
            mSymbolDetailsPresenter.setData(symbolEntity);
        }
    }
}
