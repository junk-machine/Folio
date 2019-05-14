package ru.nahk.folio.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.Calendar;

import static android.arch.persistence.room.ColumnInfo.NOCASE;

/**
 * Stock symbol entity.
 */
@Entity(
    tableName = SymbolEntity.TABLE_NAME
)
public class SymbolEntity {
    /**
     * Name of the stock symbols table.
     */
    static final String TABLE_NAME = "symbols";

    /**
     * Name of the stock symbol column.
     */
    static final String ID_COLUMN = "id";

    /**
     * Name of the company name column.
     */
    static final String DISPLAY_NAME_COLUMN = "display_name";

    /**
     * Name of the share price at open column.
     */
    static final String OPEN_PRICE_COLUMN = "open_price";

    /**
     * Name of the trading open time column.
     */
    static final String OPEN_TIME_COLUMN = "open_time";

    /**
     * Name of the latest share price column.
     */
    static final String LATEST_PRICE_COLUMN = "latest_price";

    /**
     * Name of the latest share trade time column.
     */
    static final String LATEST_TIME_COLUMN = "latest_time";

    /**
     * Name of the share price at close column.
     */
    static final String CLOSE_PRICE_COLUMN = "close_price";

    /**
     * Name of the trading close time column.
     */
    static final String CLOSE_TIME_COLUMN = "close_time";

    /**
     * Name of the extended share price column.
     */
    static final String EXTENDED_PRICE_COLUMN = "extended_price";

    /**
     * Name of the extended share trade time column.
     */
    static final String EXTENDED_TIME_COLUMN = "extended_time";

    /**
     * Name of the share price at previous close column.
     */
    static final String PREVIOUS_CLOSE_PRICE_COLUMN = "previous_close_price";

    /**
     * Name of the market capitalization column.
     */
    static final String MARKET_CAP_COLUMN = "market_cap";

    /**
     * Name of the day's highest price column.
     */
    static final String DAY_HIGH_COLUMN = "day_high";

    /**
     * Name of the day's lowest price column.
     */
    static final String DAY_LOW_COLUMN = "day_low";

    /**
     * Name of the 52 weeks range highest price column.
     */
    static final String WEEK_52_HIGH_COLUMN = "week_52_high";

    /**
     * Name of the 52 weeks range lowest price column.
     */
    static final String WEEK_52_LOW_COLUMN = "week_52_low";

    /**
     * Stock symbol.
     */
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = ID_COLUMN, collate = NOCASE)
    public String id;

    /**
     * Company name.
     */
    @ColumnInfo(name = DISPLAY_NAME_COLUMN)
    public String displayName;

    /**
     * Share price at trade opening.
     */
    @ColumnInfo(name = OPEN_PRICE_COLUMN)
    public BigDecimal openPrice;

    /**
     * Trade opening time.
     */
    @ColumnInfo(name = OPEN_TIME_COLUMN)
    public Calendar openTime;

    /**
     * Latest share price.
     */
    @ColumnInfo(name = LATEST_PRICE_COLUMN)
    public BigDecimal latestPrice;

    /**
     * Latest trade time.
     */
    @ColumnInfo(name = LATEST_TIME_COLUMN)
    public Calendar latestTime;

    /**
     * Share price at trade closing.
     */
    @ColumnInfo(name = CLOSE_PRICE_COLUMN)
    public BigDecimal closePrice;

    /**
     * Trade closing time.
     */
    @ColumnInfo(name = CLOSE_TIME_COLUMN)
    public Calendar closeTime;

    /**
     * Extended share price, e.g. after hours.
     */
    @ColumnInfo(name = EXTENDED_PRICE_COLUMN)
    public BigDecimal extendedPrice;

    /**
     * Extended trade time.
     */
    @ColumnInfo(name = EXTENDED_TIME_COLUMN)
    public Calendar extendedTime;

    /**
     * Share price at previous closing.
     */
    @ColumnInfo(name = PREVIOUS_CLOSE_PRICE_COLUMN)
    public BigDecimal previousClosePrice;

    /**
     * Market capitalization.
     */
    @ColumnInfo(name = MARKET_CAP_COLUMN)
    public BigDecimal marketCap;

    /**
     * Day's highest price.
     */
    @ColumnInfo(name = DAY_HIGH_COLUMN)
    public BigDecimal dayHigh;

    /**
     * Day's lowest price.
     */
    @ColumnInfo(name = DAY_LOW_COLUMN)
    public BigDecimal dayLow;

    /**
     * 52 weeks range highest price.
     */
    @ColumnInfo(name = WEEK_52_HIGH_COLUMN)
    public BigDecimal week52High;

    /**
     * 52 weeks range lowest price.
     */
    @ColumnInfo(name = WEEK_52_LOW_COLUMN)
    public BigDecimal week52Low;

    /**
     * Creates new instance of the {@link SymbolEntity} class.
     */
    public SymbolEntity() { }

    /**
     * Creates new instance of the {@link SymbolEntity} class
     * with the given stock symbol.
     */
    @Ignore
    public SymbolEntity(@NonNull String id) {
        this.id = id;
    }
}
