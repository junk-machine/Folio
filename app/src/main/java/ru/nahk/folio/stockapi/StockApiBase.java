package ru.nahk.folio.stockapi;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import ru.nahk.folio.model.SymbolEntity;
import ru.nahk.folio.utils.StringUtils;

/**
 * Base class for stock APIs that use cached data for symbols search.
 */
public abstract class StockApiBase implements StockApi {
    /**
     * Searches for symbols matching the given substring.
     * @param context Application or activity context.
     * @param query Part of the symbol or company name.
     * @return List of matching symbols.
     */
    @Override
    public List<SymbolInfo> findSymbols(Context context, String query) {
        // Not all APIs support search queries, so lets just use hard-coded list.
        // If it is new symbol that is not known, then users can still type-in whatever they want.
        List<SymbolInfo> result = new ArrayList<>();

        for (SymbolInfo symbolInfo : SymbolsDatabase.getData(context)) {
            if (symbolInfo.symbol.equalsIgnoreCase(query)) {
                // If there is a full match of the symbol - bring it to the top
                result.add(0, symbolInfo);
            } else if (StringUtils.containsIgnoreCase(symbolInfo.symbol, query)
                || StringUtils.containsIgnoreCase(symbolInfo.companyName, query)) {
                result.add(symbolInfo);
            }
        }

        return result;
    }

    /**
     * Updates symbol information in place.
     * @param symbol Symbol entity to update.
     * @throws StockApiException Thrown when an error occurs during update.
     */
    @Override
    public void updateSymbol(SymbolEntity symbol) throws StockApiException {
        if (symbol == null) {
            return;
        }

        List<SymbolEntity> symbols = new ArrayList<>(1);
        symbols.add(symbol);

        updateSymbols(symbols);
    }
}
