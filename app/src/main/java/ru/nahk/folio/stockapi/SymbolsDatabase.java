package ru.nahk.folio.stockapi;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import ru.nahk.folio.R;

/**
 * Database of well-known symbols.
 */
final class SymbolsDatabase {
    /**
     * Synchronization object used for symbols data initialization.
     */
    private static final Object initializationSyncObj = new Object();

    /**
     * Collection of all well-known symbols.
     */
    private static Iterable<SymbolInfo> symbols;

    /**
     * Gets the collection of all well-known symbols.
     * @param context Application or activity context.
     * @return Collection of all well-known symbols.
     */
    static Iterable<SymbolInfo> getData(Context context) {
        if (symbols == null) {
            synchronized (initializationSyncObj) {
                if (symbols == null) {
                    symbols = loadData(context);
                }
            }
        }

        return symbols;
    }

    /**
     * Loads the list of well-known symbols.
     * @param context Application or activity context.
     * @return Collection of loaded well-known symbols.
     */
    private static Iterable<SymbolInfo> loadData(Context context) {
        ArrayList<SymbolInfo> result = new ArrayList<>(8603);

        try (InputStream symbolsStream = context.getResources().openRawResource(R.raw.symbols)) {
            try (InputStreamReader symbolsStreamReader = new InputStreamReader(symbolsStream)) {
                try (BufferedReader symbolsReader = new BufferedReader(symbolsStreamReader)) {
                    for (String line; (line = symbolsReader.readLine()) != null; ) {
                        String[] symbolInfo = line.split("\\t");

                        if (symbolInfo.length >= 2) {
                            result.add(new SymbolInfo(symbolInfo[0], symbolInfo[1]));
                        }
                    }
                }
            }
        }
        catch (IOException error) {
            // No symbols are known then
        }

        return result;
    }
}
