package ru.nahk.folio.stockapi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.nahk.folio.BuildConfig;
import ru.nahk.folio.model.SymbolEntity;
import ru.nahk.folio.utils.CalendarHelper;

/**
 * Provides access to market data using Yahoo Finance v7 API.
 */
public class YahooFinanceApi extends StockApiBase {
    /**
     * URI template to query latest symbols information.
     */
    private final static String QUOTE_API_URL =
        "https://query2.finance.yahoo.com/v6/finance/quote?symbols=%s&fields=symbol,shortName,regularMarketOpen,regularMarketPrice,regularMarketTime,postMarketPrice,postMarketTime,regularMarketPreviousClose,marketCap,regularMarketDayHigh,regularMarketDayLow,fiftyTwoWeekHigh,fiftyTwoWeekLow";

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
        StringBuilder response = new StringBuilder();

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

            // Read entire response
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String responseLine;
                while ((responseLine = reader.readLine()) != null) {
                    response.append(responseLine);
                    response.append(System.lineSeparator());
                }
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

        if (response.length() == 0) {
            throw new StockApiException("Empty response from API");
        }

        try {
            // Parse response
            JSONObject jsonResponse = new JSONObject(response.toString());

            // Check for request errors
            if (jsonResponse.has("description")) {
                throw new StockApiException(jsonResponse.getString("description"));
            }

            JSONArray quotes =
                jsonResponse
                    .getJSONObject("quoteResponse")
                    .getJSONArray("result");

            for (int quoteIndex = 0; quoteIndex < quotes.length(); ++quoteIndex) {
                JSONObject quote = quotes.getJSONObject(quoteIndex);

                SymbolEntity symbolEntity = symbolsMap.get(quote.getString("symbol"));

                if (symbolEntity != null) {
                    symbolEntity.displayName =
                        quote.optString("shortName", symbolEntity.displayName);

                    if (quote.has("regularMarketTime")) {
                        Calendar latestPriceTime =
                            CalendarHelper.fromEpochInSeconds(quote.getLong("regularMarketTime"));

                        if (isNewer(latestPriceTime, symbolEntity.latestTime)) {
                            if (quote.has("regularMarketPrice")) {
                                // Set close time and price equal to latest.
                                // Yahoo Finance API doesn't provide separate value for closing price
                                // and technically close price is the latest price at the time of
                                // exchange closing.
                                // Lets just adjust close price along with latest and let UI choose
                                // which one to show based on extended trading timestamp.
                                symbolEntity.latestTime =
                                    symbolEntity.closeTime =
                                        latestPriceTime;

                                symbolEntity.latestPrice =
                                    symbolEntity.closePrice =
                                        new BigDecimal(quote.getString("regularMarketPrice"));
                            }

                            if (quote.has("regularMarketOpen")) {
                                // Round up open time to 13:30 GMT (9:30 EST)
                                // TODO: Is this affected by daylight saving time?
                                symbolEntity.openTime =
                                    CalendarHelper.fromComponents(
                                        latestPriceTime.get(Calendar.YEAR),
                                        latestPriceTime.get(Calendar.MONTH),
                                        latestPriceTime.get(Calendar.DATE),
                                        13, 30);
                                symbolEntity.openPrice =
                                    new BigDecimal(quote.getString("regularMarketOpen"));
                            }
                        }
                    }

                    if (quote.has("postMarketTime") && quote.has("postMarketPrice")) {
                        symbolEntity.extendedTime =
                            CalendarHelper.fromEpochInSeconds(quote.getLong("postMarketTime"));
                        symbolEntity.extendedPrice =
                            new BigDecimal(quote.getString("postMarketPrice"));
                    }

                    if (quote.has("regularMarketPreviousClose")) {
                        symbolEntity.previousClosePrice =
                            new BigDecimal(quote.getString("regularMarketPreviousClose"));
                    }

                    if (quote.has("marketCap")) {
                        symbolEntity.marketCap =
                            new BigDecimal(quote.getString("marketCap"));
                    }

                    if (quote.has("regularMarketDayHigh") && quote.has("regularMarketDayLow")) {
                        symbolEntity.dayHigh =
                            new BigDecimal(quote.getString("regularMarketDayHigh"));
                        symbolEntity.dayLow =
                            new BigDecimal(quote.getString("regularMarketDayLow"));
                    }

                    if (quote.has("fiftyTwoWeekHigh") && quote.has("fiftyTwoWeekLow")) {
                        symbolEntity.week52High =
                            new BigDecimal(quote.getString("fiftyTwoWeekHigh"));
                        symbolEntity.week52Low =
                            new BigDecimal(quote.getString("fiftyTwoWeekLow"));
                    }
                }
            }
        }
        catch (JSONException jsonException) {
            throw new StockApiException(jsonException.getMessage(), jsonException);
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
}
