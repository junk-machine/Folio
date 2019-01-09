package ru.nahk.folio.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.Calendar;

import ru.nahk.folio.R;
import ru.nahk.folio.model.SymbolEntity;
import ru.nahk.folio.tasks.LoadSymbolDetailsTask;
import ru.nahk.folio.tasks.RefreshSymbolDataTask;
import ru.nahk.folio.utils.BigDecimalHelper;
import ru.nahk.folio.utils.CalendarHelper;
import ru.nahk.folio.utils.ProgressHandler;

/**
 * Activity that displays detailed information about the symbol.
 */
public class SymbolDetailsActivity
    extends
        SymbolsDataTrackingActivity
    implements
        ProgressHandler,
        SwipeRefreshLayout.OnRefreshListener,
        SymbolDetailsPresenter {

    /**
     * Layout control that enables swipe-to-refresh functionality.
     */
    private SwipeRefreshLayout mSwipeRefresh;

    /**
     * Layout control that groups all tiles.
     */
    private View mContentLayout;

    /**
     * Control that displays company name.
     */
    private TextView mCompanyNameView;

    /**
     * Control that groups all latest price controls.
     */
    private ViewGroup mLatestPriceTile;

    /**
     * Control that displays latest price.
     */
    private TextView mLatestPriceView;

    /**
     * Control that displays latest price change.
     */
    private TextView mLatestChangeView;

    /**
     * Control that displays latest price time.
     */
    private TextView mLatestTimeView;

    /**
     * Control that groups all extended price controls.
     */
    private ViewGroup mExtendedPriceTile;

    /**
     * Control that displays extended price.
     */
    private TextView mExtendedPriceView;

    /**
     * Control that displays extended price change.
     */
    private TextView mExtendedChangeView;

    /**
     * Control that displays extended price time.
     */
    private TextView mExtendedTimeView;

    /**
     * Control that displays open price.
     */
    private TextView mOpenPriceView;

    /**
     * Control that displays open price change.
     */
    private TextView mOpenChangeView;

    /**
     * Control that displays open price time.
     */
    private TextView mOpenTimeView;

    /**
     * Control that groups all close price controls.
     */
    private ViewGroup mClosePriceTile;

    /**
     * Control that displays close price.
     */
    private TextView mClosePriceView;

    /**
     * Control that displays close price change.
     */
    private TextView mCloseChangeView;

    /**
     * Control that displays close price time.
     */
    private TextView mCloseTimeView;

    /**
     * Control that displays previous close price.
     */
    private TextView mPreviousClosePriceView;

    /**
     * Control that groups all day price range controls.
     */
    private ViewGroup mDayRangeTile;

    /**
     * Control that displays day highest price.
     */
    private TextView mDayHighView;

    /**
     * Control that displays day lowest price.
     */
    private TextView mDayLowView;

    /**
     * Control that groups all 52-weeks price range controls.
     */
    private ViewGroup mWeek52RangeTile;

    /**
     * Control that displays 52-weeks highest price.
     */
    private TextView mWeek52HighView;

    /**
     * Control that displays 52-weeks lowest price.
     */
    private TextView mWeek52LowView;

    /**
     * Identifier of the parent position.
     */
    private long mPositionId;

    /**
     * Stock symbol to display details for.
     */
    private String mStockSymbol;

    /**
     * Flag that indicates a change in symbol details.
     */
    private boolean mDetailsChanged;

    /**
     * Initializes activity view.
     * @param savedInstanceState Saved activity state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symbol_details);

        if (!initializeState(savedInstanceState)
                && (getIntent() == null || !initializeState(getIntent().getExtras()))) {
            finish();
            return;
        }

        mContentLayout = findViewById(R.id.content_layout);

        mCompanyNameView = findViewById(R.id.company_name);

        mLatestPriceTile = findViewById(R.id.latest_price_tile);
        mLatestPriceView = findViewById(R.id.latest_price);
        mLatestChangeView = findViewById(R.id.latest_change);
        mLatestTimeView = findViewById(R.id.latest_time);

        mExtendedPriceTile = findViewById(R.id.extended_price_tile);
        mExtendedPriceView = findViewById(R.id.extended_price);
        mExtendedChangeView = findViewById(R.id.extended_change);
        mExtendedTimeView = findViewById(R.id.extended_time);

        mOpenPriceView = findViewById(R.id.open_price);
        mOpenChangeView = findViewById(R.id.open_change);
        mOpenTimeView = findViewById(R.id.open_time);

        mClosePriceTile = findViewById(R.id.close_price_tile);
        mClosePriceView = findViewById(R.id.close_price);
        mCloseChangeView = findViewById(R.id.close_change);
        mCloseTimeView = findViewById(R.id.close_time);

        mPreviousClosePriceView = findViewById(R.id.previous_close_price);

        mDayRangeTile = findViewById(R.id.day_range_tile);
        mDayHighView = findViewById(R.id.day_high);
        mDayLowView = findViewById(R.id.day_low);

        mWeek52RangeTile = findViewById(R.id.week_52_range_tile);
        mWeek52HighView = findViewById(R.id.week_52_high);
        mWeek52LowView = findViewById(R.id.week_52_low);

        mSwipeRefresh = findViewById(R.id.swipe_refresh);
        mSwipeRefresh.setOnRefreshListener(this);

        // Kick-off the task to load symbol details
        new LoadSymbolDetailsTask(
            this,
            mStockSymbol,
            getDataStore().symbolDao(),
            this).execute();
    }

    /**
     * Updates current intent that was used to launch this activity.
     * @param intent New intent.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != getIntent()) {
            setIntent(intent);

            if (intent == null || !initializeState(intent.getExtras())) {
                finish();
                return;
            }

            onSymbolsDataChanged();
        }
    }

    /**
     * Saves the activity state.
     * @param outState Bundle to save the state to.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(ActivityNavigationConstants.POSITION_ID_KEY, mPositionId);
        outState.putString(ActivityNavigationConstants.STOCK_SYMBOL_KEY, mStockSymbol);
        outState.putBoolean(ActivityNavigationConstants.HAS_CHANGED_KEY, mDetailsChanged);
    }

    /**
     * Creates the intent to return to parent activity.
     * @return Intent to return to parent activity.
     */
    @Override
    public Intent getParentActivityIntent() {
        Intent parentIntent = super.getParentActivityIntent();

        if (parentIntent != null) {
            parentIntent.putExtra(ActivityNavigationConstants.POSITION_ID_KEY, mPositionId);
            parentIntent.putExtra(ActivityNavigationConstants.STOCK_SYMBOL_KEY, mStockSymbol);
            parentIntent.putExtra(ActivityNavigationConstants.HAS_CHANGED_KEY, mDetailsChanged);
        }

        return parentIntent;
    }

    /**
     * Initializes activity from the given state bundle.
     * This can be intent's extra or previously saved state.
     * @param state State bundle.
     * @return True if intent had all required data, otherwise false.
     */
    private boolean initializeState(Bundle state) {
        if (state != null) {
            mPositionId = state.getLong(ActivityNavigationConstants.POSITION_ID_KEY, -1);
            mStockSymbol = state.getString(ActivityNavigationConstants.STOCK_SYMBOL_KEY);
            mDetailsChanged = state.getBoolean(ActivityNavigationConstants.HAS_CHANGED_KEY, false);

            if (mPositionId != -1 && !TextUtils.isEmpty(mStockSymbol)) {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(
                        getString(R.string.symbol_details_title_format, mStockSymbol));
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Populates all controls with the data from the given {@link SymbolEntity}.
     * @param symbolEntity Symbol details.
     */
    @Override
    public void setData(SymbolEntity symbolEntity) {
        mContentLayout.setVisibility(View.VISIBLE);

        if (symbolEntity == null) {
            mCompanyNameView.setText(R.string.unknown_value);

            setPriceChangeAndTime(
                mLatestPriceView, mLatestChangeView, mLatestTimeView,
                null, null, null);

            mExtendedPriceTile.setVisibility(View.GONE);
            setPriceChangeAndTime(
                mExtendedPriceView, mExtendedChangeView, mExtendedTimeView,
                null, null, null);

            setPriceChangeAndTime(
                mOpenPriceView, mOpenChangeView, mOpenTimeView,
                null, null, null);

            mClosePriceTile.setVisibility(View.GONE);
            setPriceChangeAndTime(
                mClosePriceView, mCloseChangeView, mCloseTimeView,
                null, null, null);

            setPrice(mPreviousClosePriceView, null);

            mDayRangeTile.setVisibility(View.GONE);
            mDayHighView.setText(null);
            mDayLowView.setText(null);

            mWeek52RangeTile.setVisibility(View.GONE);
            mWeek52HighView.setText(null);
            mWeek52LowView.setText(null);
        }
        else {
            if (symbolEntity.displayName == null) {
                mCompanyNameView.setText(R.string.unknown_value);
            } else {
                mCompanyNameView.setText(symbolEntity.displayName);
            }

            if (CalendarHelper.compare(symbolEntity.extendedTime, symbolEntity.latestTime) >= 0) {
                // If extended trading is happening, then replace latest price with close price
                // and extended price
                mLatestPriceTile.setVisibility(View.GONE);
                setPriceChangeAndTime(
                    mLatestPriceView, mLatestChangeView, mLatestTimeView,
                    null, null, null);

                mExtendedPriceTile.setVisibility(View.VISIBLE);
                setPriceChangeAndTime(
                    mExtendedPriceView, mExtendedChangeView, mExtendedTimeView,
                    symbolEntity.extendedPrice, symbolEntity.previousClosePrice, symbolEntity.extendedTime);

                mClosePriceTile.setVisibility(View.VISIBLE);
                setPriceChangeAndTime(
                    mClosePriceView, mCloseChangeView, mCloseTimeView,
                    symbolEntity.closePrice, symbolEntity.previousClosePrice, symbolEntity.closeTime);
            } else {
                // If trading during normal hours, then display latest price
                // and hide extended and close prices
                mLatestPriceTile.setVisibility(View.VISIBLE);
                setPriceChangeAndTime(
                    mLatestPriceView, mLatestChangeView, mLatestTimeView,
                    symbolEntity.latestPrice, symbolEntity.previousClosePrice, symbolEntity.latestTime);

                mExtendedPriceTile.setVisibility(View.GONE);
                setPriceChangeAndTime(
                    mExtendedPriceView, mExtendedChangeView, mExtendedTimeView,
                    null, null, null);

                mClosePriceTile.setVisibility(View.GONE);
                setPriceChangeAndTime(
                    mClosePriceView, mCloseChangeView, mCloseTimeView,
                    null, null, null);
            }

            setPriceChangeAndTime(
                mOpenPriceView, mOpenChangeView, mOpenTimeView,
                symbolEntity.openPrice, symbolEntity.previousClosePrice, symbolEntity.openTime);

            setPrice(mPreviousClosePriceView, symbolEntity.previousClosePrice);

            if (symbolEntity.dayLow == null || symbolEntity.dayHigh == null) {
                mDayRangeTile.setVisibility(View.GONE);
                mDayHighView.setText(null);
                mDayLowView.setText(null);
            } else {
                mDayHighView.setText(BigDecimalHelper.formatCurrency(symbolEntity.dayHigh));
                mDayLowView.setText(BigDecimalHelper.formatCurrency(symbolEntity.dayLow));
            }

            if (symbolEntity.week52Low == null || symbolEntity.week52High == null) {
                mWeek52RangeTile.setVisibility(View.GONE);
                mWeek52HighView.setText(null);
                mWeek52LowView.setText(null);
            } else {
                mWeek52RangeTile.setVisibility(View.VISIBLE);
                mWeek52HighView.setText(BigDecimalHelper.formatCurrency(symbolEntity.week52High));
                mWeek52LowView.setText(BigDecimalHelper.formatCurrency(symbolEntity.week52Low));
            }
        }
    }

    /**
     * Updates price and time controls with given values.
     * @param priceView Control to display price.
     * @param changeView Control to display price change.
     * @param timeView Control to display time.
     * @param priceValue Price value.
     * @param baseValue Base price value.
     * @param timeValue Time value.
     */
    private void setPriceChangeAndTime(
        TextView priceView,
        TextView changeView,
        TextView timeView,
        BigDecimal priceValue,
        BigDecimal baseValue,
        Calendar timeValue) {

        setPrice(priceView, priceValue);

        if (baseValue == null || priceValue == null) {
            changeView.setVisibility(View.GONE);
            changeView.setText(null);
        } else {
            BigDecimal valueChange = priceValue.subtract(baseValue);
            int valueChangeDirection = valueChange.compareTo(BigDecimal.ZERO);

            changeView.setVisibility(View.VISIBLE);
            changeView.setTextAppearance(
                valueChangeDirection > 0
                    ? R.style.TextAppearance_DetailsTile_Value_Positive
                    : (valueChangeDirection < 0
                        ? R.style.TextAppearance_DetailsTile_Value_Negative
                        : R.style.TextAppearance_DetailsTile_Value_NoChange));
            changeView.setText(
                getString(
                    R.string.detail_value_change_format,
                    BigDecimalHelper.formatCurrencyChange(valueChange),
                    BigDecimalHelper.formatPercentage(baseValue, valueChange)));
        }

        if (timeValue == null) {
            timeView.setVisibility(View.GONE);
            timeView.setText(null);
        } else {
            timeView.setVisibility(View.VISIBLE);
            timeView.setText(CalendarHelper.toLocalTimeString(timeValue));
        }
    }

    /**
     * Updates price control with the given value.
     * @param priceView Control to display price.
     * @param priceValue Price value.
     */
    private void setPrice(TextView priceView, BigDecimal priceValue) {
        if (priceValue == null) {
            priceView.setText(R.string.unknown_value);
        } else {
            priceView.setText(BigDecimalHelper.formatCurrency(priceValue));
        }
    }

    /**
     * Refreshes symbol details.
     */
    @Override
    public void onRefresh() {
        mDetailsChanged = true;

        new RefreshSymbolDataTask(
            this,
            this,
            mStockSymbol,
            getDataStore().symbolDao(),
            this).execute();
    }

    /**
     * Reloads symbol details from persistent store and updates the view.
     */
    @Override
    void onSymbolsDataChanged() {
        new LoadSymbolDetailsTask(
            this,
            mStockSymbol,
            getDataStore().symbolDao(),
            this).execute();
    }

    /**
     * Handles start of the async operation.
     */
    @Override
    public void progressStart() {
        mSwipeRefresh.setRefreshing(true);
    }

    /**
     * Handles completion of the async operation.
     */
    @Override
    public void progressEnd() {
        mSwipeRefresh.setRefreshing(false);
    }

    /**
     * Handles errors during async operation.
     * @param error Exception from async operation.
     */
    @Override
    public void progressError(Exception error) {
        Snackbar.make(findViewById(android.R.id.content), error.getMessage(), Snackbar.LENGTH_LONG)
            .show();
    }
}
