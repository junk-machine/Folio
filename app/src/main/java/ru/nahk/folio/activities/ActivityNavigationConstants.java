package ru.nahk.folio.activities;

/**
 * Constants for activity navigation extras.
 */
public class ActivityNavigationConstants {
    /**
     * Intent parameter key for portfolio position identifier.
     */
    public static final String POSITION_ID_KEY = "position-id";

    /**
     * Intent parameter key for position lot identifier.
     */
    static final String LOT_ID_KEY = "lot-id";

    /**
     * Intent parameter key for stock symbol.
     */
    public static final String STOCK_SYMBOL_KEY = "stock-symbol";

    /**
     * Intent parameter key for stocks quantity.
     */
    static final String STOCK_QUANTITY_KEY = "stock-quantity";

    /**
     * Intent parameter key for stocks purchase price.
     */
    static final String STOCK_PURCHASE_PRICE_KEY = "stock-purchase-price";

    /**
     * Intent parameter key for stocks purchase date.
     */
    static final String STOCK_PURCHASE_DATE_KEY = "stock-purchase-date";

    /**
     * Intent parameter key for stocks purchase commission.
     */
    static final String STOCK_PURCHASE_COMMISSION_KEY = "stock-purchase-commission";

    /**
     * Intent parameter key for a flag which indicates that data has changed.
     */
    static final String HAS_CHANGED_KEY = "has-changed";

    /**
     * Intent parameter key to force data refresh of the view.
     */
    static final String FORCE_REFRESH_KEY = "force-refresh";
}
