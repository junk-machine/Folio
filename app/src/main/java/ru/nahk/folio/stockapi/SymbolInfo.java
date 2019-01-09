package ru.nahk.folio.stockapi;

import android.support.annotation.NonNull;

/**
 * Basic information about the stock symbol.
 */
public final class SymbolInfo {
    /**
     * Stock symbol.
     */
    public final String symbol;

    /**
     * Name of the company.
     */
    public final String companyName;

    /**
     * Creates a new instance of the {@link SymbolInfo} class
     * with the provided symbol and company name.
     * @param symbol Stock symbol.
     * @param companyName Company name.
     */
    SymbolInfo(String symbol, String companyName) {
        this.symbol = symbol;
        this.companyName = companyName;
    }

    /**
     * Gets the stock symbol.
     * @return Stock symbol.
     */
    @NonNull
    @Override
    public String toString() {
        return symbol;
    }
}
