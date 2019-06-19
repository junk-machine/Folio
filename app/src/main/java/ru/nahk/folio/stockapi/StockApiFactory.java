package ru.nahk.folio.stockapi;

/**
 * Provides access to {@link StockApi} implementations.
 */
public final class StockApiFactory {
    /**
     * Synchronization object for current stock API initialization process.
     */
    private static final Object initializationSyncObj = new Object();

    /**
     * Singleton instance of the current {@link StockApi} implementation.
     */
    private static StockApi apiInstance;

    /**
     * Obtains an instance of the {@link StockApi} implementation.
     * @return An instance of the {@link StockApi} implementation.
     */
    public static StockApi getApi() {
        if (apiInstance == null) {
            synchronized (initializationSyncObj) {
                if (apiInstance == null) {
                    apiInstance = new YahooFinanceApi();
                }
            }
        }

        return apiInstance;
    }
}
