package ru.nahk.folio.stockapi;

import android.content.Context;

import java.util.List;

import ru.nahk.folio.model.SymbolEntity;

/**
 * Provides access to market data.
 */
public interface StockApi {
    /**
     * Searches for symbols matching the query.
     * @param context Application or activity context.
     * @param query Part of the symbol or company name.
     * @return List of symbols that match the query.
     * @throws StockApiException Thrown when an error occurs during symbols look-up.
     */
    List<SymbolInfo> findSymbols(Context context, String query) throws StockApiException;

    /**
     * Performs an in-place update of symbol information.
     * @param symbol Symbol entity to update.
     * @throws StockApiException Thrown when an error occurs during update.
     */
    void updateSymbol(SymbolEntity symbol) throws StockApiException;

    /**
     * Performs an in-place update of symbols information.
     * @param symbols Symbol entities to update.
     * @throws StockApiException Thrown when an error occurs during update.
     */
    void updateSymbols(List<SymbolEntity> symbols) throws StockApiException;
}
