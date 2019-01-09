package ru.nahk.folio.stockapi;

import android.content.Context;
import android.util.JsonReader;
import android.util.JsonToken;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.nahk.folio.BuildConfig;
import ru.nahk.folio.model.SymbolEntity;
import ru.nahk.folio.utils.BigDecimalHelper;
import ru.nahk.folio.utils.CalendarHelper;
import ru.nahk.folio.utils.StringUtils;

/**
 * Provides access to market data using IEX Trading API.
 */
public class IexStockApi implements StockApi {
    /**
     * URI template to query latest symbols information.
     */
    private final static String QUOTE_API_URL =
        "https://api.iextrading.com/1.0/stock/market/quote?symbols=%s&filter=symbol,companyName,open,openTime,latestPrice,latestUpdate,close,closeTime,extendedPrice,extendedPriceTime,previousClose,high,low,week52High,week52Low";

    /**
     * Searches for symbols matching the given substring.
     * @param context Application or activity context.
     * @param query Part of the symbol or company name.
     * @return List of matching symbols.
     */
    @Override
    public List<SymbolInfo> findSymbols(Context context, String query) {
        // Since IEX APIs don't support search queries - just use hard-coded list.
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

    /**
     * Updates symbols information in-place.
     * @param symbols Symbol entities to update.
     * @throws StockApiException Thrown when an error occurs during update.
     */
    @Override
    public void updateSymbols(List<SymbolEntity> symbols) throws StockApiException {
        if (symbols == null || symbols.size() <= 0) {
            return;
        }

        Map<String, SymbolEntity> symbolsMap = new HashMap<>();
        StringBuilder symbolNames = new StringBuilder();

        for (SymbolEntity symbol : symbols) {
            if (symbolNames.length() > 0) {
                symbolNames.append(',');
            }

            try {
                symbolNames.append(URLEncoder.encode(symbol.id, "UTF-8"));
            }
            catch (UnsupportedEncodingException encodingError) {
                throw new StockApiException(
                    "Failed to prepare request URL: " + encodingError.getMessage(),
                    encodingError);
            }

            symbolsMap.put(symbol.id, symbol);
        }

        HttpURLConnection connection = null;

        try {
            connection =
                (HttpURLConnection) new URL(String.format(QUOTE_API_URL, symbolNames.toString()))
                    .openConnection();
            connection.setRequestProperty("User-Agent", "android/folio-" + BuildConfig.VERSION_NAME);
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new StockApiException("Invalid API response code: " + connection.getResponseCode());
            }

            StockDetails stockDetails = new StockDetails();

            try (JsonReader reader = new JsonReader(new InputStreamReader(connection.getInputStream()))) {
                reader.beginArray();

                while (reader.peek() == JsonToken.BEGIN_OBJECT) {
                    stockDetails.reset();

                    readDetailsObject(reader, stockDetails);

                    if (stockDetails.symbol != null) {
                        SymbolEntity symbolEntity = symbolsMap.get(stockDetails.symbol);

                        if (symbolEntity != null) {
                            if (stockDetails.companyName != null) {
                                symbolEntity.displayName = stockDetails.companyName;
                            }

                            if (stockDetails.openPrice != null
                                && isNewer(stockDetails.openTime, symbolEntity.openTime)) {
                                symbolEntity.openPrice = BigDecimalHelper.truncateCurrency(stockDetails.openPrice);
                                symbolEntity.openTime = stockDetails.openTime;
                            }

                            if (stockDetails.latestPrice != null
                                && isNewer(stockDetails.latestTime, symbolEntity.latestTime)) {
                                symbolEntity.latestPrice = BigDecimalHelper.truncateCurrency(stockDetails.latestPrice);
                                symbolEntity.latestTime = stockDetails.latestTime;
                            }

                            if (stockDetails.closePrice != null
                                && isNewer(stockDetails.closeTime, symbolEntity.closeTime)) {
                                symbolEntity.closePrice = BigDecimalHelper.truncateCurrency(stockDetails.closePrice);
                                symbolEntity.closeTime = stockDetails.closeTime;
                            }

                            if (stockDetails.extendedPrice != null
                                && isNewer(stockDetails.extendedTime, symbolEntity.extendedTime)) {
                                symbolEntity.extendedPrice = BigDecimalHelper.truncateCurrency(stockDetails.extendedPrice);
                                symbolEntity.extendedTime = stockDetails.extendedTime;
                            }

                            if (stockDetails.previousClosePrice != null) {
                                symbolEntity.previousClosePrice = BigDecimalHelper.truncateCurrency(stockDetails.previousClosePrice);
                            }

                            if (stockDetails.dayHigh != null && stockDetails.dayLow != null) {
                                symbolEntity.dayHigh = BigDecimalHelper.truncateCurrency(stockDetails.dayHigh);
                                symbolEntity.dayLow = BigDecimalHelper.truncateCurrency(stockDetails.dayLow);
                            }

                            if (stockDetails.week52High != null && stockDetails.week52Low != null) {
                                symbolEntity.week52High = BigDecimalHelper.truncateCurrency(stockDetails.week52High);
                                symbolEntity.week52Low = BigDecimalHelper.truncateCurrency(stockDetails.week52Low);
                            }
                        }
                    }
                }

                reader.endArray();
            }
        }
        catch (IOException ioError) {
            throw new StockApiException(ioError.getMessage(), ioError);
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Populates given {@link StockDetails} object with the information from {@link JsonReader}.
     * @param reader JSON reader to read the stock information from.
     * @param stockDetails Resulting object to populate.
     * @throws IOException Thrown if JSON stream is invalid.
     */
    private void readDetailsObject(JsonReader reader, StockDetails stockDetails) throws IOException {
        try {
            reader.beginObject();

            while (reader.peek() == JsonToken.NAME) {
                String propertyName = reader.nextName();

                if (propertyName.equalsIgnoreCase("symbol")) {
                    stockDetails.symbol = reader.nextString();
                } else if (propertyName.equalsIgnoreCase("companyName")) {
                    stockDetails.companyName = reader.nextString();
                } else if (propertyName.equalsIgnoreCase("open")) {
                    stockDetails.openPrice = new BigDecimal(reader.nextString());
                } else if (propertyName.equalsIgnoreCase("openTime")) {
                    stockDetails.openTime = CalendarHelper.fromEpochInMillis(reader.nextLong());
                } else if (propertyName.equalsIgnoreCase("latestPrice")) {
                    stockDetails.latestPrice = new BigDecimal(reader.nextString());
                } else if (propertyName.equalsIgnoreCase("latestUpdate")) {
                    stockDetails.latestTime = CalendarHelper.fromEpochInMillis(reader.nextLong());
                } else if (propertyName.equalsIgnoreCase("close")) {
                    stockDetails.closePrice = new BigDecimal(reader.nextString());
                } else if (propertyName.equalsIgnoreCase("closeTime")) {
                    stockDetails.closeTime = CalendarHelper.fromEpochInMillis(reader.nextLong());
                } else if (propertyName.equalsIgnoreCase("extendedPrice")) {
                    stockDetails.extendedPrice = new BigDecimal(reader.nextString());
                } else if (propertyName.equalsIgnoreCase("extendedPriceTime")) {
                    stockDetails.extendedTime = CalendarHelper.fromEpochInMillis(reader.nextLong());
                } else if (propertyName.equalsIgnoreCase("previousClose")) {
                    stockDetails.previousClosePrice = new BigDecimal(reader.nextString());
                } else if (propertyName.equalsIgnoreCase("high")) {
                    stockDetails.dayHigh = new BigDecimal(reader.nextString());
                } else if (propertyName.equalsIgnoreCase("low")) {
                    stockDetails.dayLow = new BigDecimal(reader.nextString());
                } else if (propertyName.equalsIgnoreCase("week52High")) {
                    stockDetails.week52High = new BigDecimal(reader.nextString());
                } else if (propertyName.equalsIgnoreCase("week52Low")) {
                    stockDetails.week52Low = new BigDecimal(reader.nextString());
                } else {
                    // Skip unknown property
                    reader.skipValue();
                }
            }

            reader.endObject();
        } catch (IOException ioError) {
            // Try to recover
            reader.skipValue();
        }
    }

    /**
     * Compares if new {@link Calendar} value is greater than current one.
     * @param newValue New timestamp value.
     * @param currentValue Current timestamp value.
     * @return True if new value is greater than the old one.
     */
    private static boolean isNewer(Calendar newValue, Calendar currentValue) {
        if (newValue == null) {
            return false;
        } else if (currentValue == null) {
            return true;
        } else {
            return newValue.compareTo(currentValue) > 0;
        }
    }

    /**
     * Contains information about the symbol from remote API.
     */
    private class StockDetails {
        /**
         * Stock symbol.
         */
        String symbol;

        /**
         * Company name.
         */
        String companyName;

        /**
         * Share price at trade opening.
         */
        BigDecimal openPrice;

        /**
         * Trade opening time.
         */
        Calendar openTime;

        /**
         * Latest share price.
         */
        BigDecimal latestPrice;

        /**
         * Latest trade time.
         */
        Calendar latestTime;

        /**
         * Share price aat trade closing.
         */
        BigDecimal closePrice;

        /**
         * Trade closing time.
         */
        Calendar closeTime;

        /**
         * Extended share price.
         */
        BigDecimal extendedPrice;

        /**
         * Extended trade time.
         */
        Calendar extendedTime;

        /**
         * Share price at previos closing.
         */
        BigDecimal previousClosePrice;

        /**
         * Day's highest share price.
         */
        BigDecimal dayHigh;

        /**
         * Day's lowest share price.
         */
        BigDecimal dayLow;

        /**
         * 52 weeks range highest share price.
         */
        BigDecimal week52High;

        /**
         * 52 weeks range lowest share price.
         */
        BigDecimal week52Low;

        /**
         * Resets all values to NULL.
         */
        void reset() {
            symbol = null;
            companyName = null;
            latestPrice = null;
            latestTime = null;
            closePrice = null;
            closeTime = null;
            extendedPrice = null;
            extendedTime = null;
            previousClosePrice = null;
            dayHigh = null;
            dayLow = null;
            week52High = null;
            week52Low = null;
        }
    }
}
