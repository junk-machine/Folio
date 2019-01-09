package ru.nahk.folio.activities;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import ru.nahk.folio.R;
import ru.nahk.folio.model.PortfolioItemWidgetEntity;
import ru.nahk.folio.tasks.AddPortfolioItemWidgetTask;
import ru.nahk.folio.tasks.LoadExpandedPortfolioTask;
import ru.nahk.folio.utils.Callback;
import ru.nahk.folio.utils.ProgressHandler;

/**
 * Activity that configures portfolio item widget.
 */
public class PortfolioItemWidgetConfigureActivity
    extends
        DataAccessActivityBase
    implements
        ProgressHandler,
        PositionsListAdapter.OnGroupItemClickListener,
        PositionsListAdapter.OnPositionItemClickListener {

    /**
     * Unique identifier of the widget that is being configured.
     */
    private int mAppWidgetId;

    /**
     * Layout control that displays loading progress.
     * There is no swipe-to-refresh in this activity, we just reuse the control
     * to display spinner to have same look and feel as everywhere else.
     */
    private SwipeRefreshLayout mSwipeRefresh;

    /**
     * Initializes the activity.
     * @param savedInstanceState Previous activity state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio_item_widget_configure);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // By default cancel widget creation, if user presses back button
        setResult(
            RESULT_CANCELED,
            new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId));

        // Cancel configuration, if original intent is missing widget identifier
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        mSwipeRefresh = findViewById(R.id.swipe_refresh);
        mSwipeRefresh.setEnabled(false);

        final RecyclerView positionsList = findViewById(R.id.positions_list_view);
        positionsList.setHasFixedSize(true);
        positionsList.setLayoutManager(new LinearLayoutManager(this));

        new LoadExpandedPortfolioTask(
            this,
            getDataStore(),
            this,
            this,
            new Callback<PositionsListAdapter>() {
                @Override
                public void run(PositionsListAdapter adapter) {
                    positionsList.setAdapter(adapter);
                }
        }).execute();
    }

    /**
     * Associates group item with the widget.
     * @param viewHolder View holder for the group item that was clicked.
     */
    @Override
    public void onGroupClick(@NonNull PositionsListAdapter.GroupViewHolder viewHolder) {
        saveWidget(
            new PortfolioItemWidgetEntity(
                mAppWidgetId,
                PortfolioItemWidgetEntity.ITEM_TYPE_GROUP,
                viewHolder.getBoundData().id));
    }

    /**
     * Associates position item with the widget.
     * @param viewHolder View holder for the position item that was clicked.
     */
    @Override
    public void onPositionClick(@NonNull PositionsListAdapter.PositionViewHolder viewHolder) {
        saveWidget(
            new PortfolioItemWidgetEntity(
                mAppWidgetId,
                PortfolioItemWidgetEntity.ITEM_TYPE_POSITION,
                viewHolder.getBoundData().id));
    }

    /**
     * Persists widget association information in the data store.
     * @param widget Portfolio item widget to save.
     */
    private void saveWidget(final PortfolioItemWidgetEntity widget) {
        new AddPortfolioItemWidgetTask(
            this,
            this,
            getDataStore().widgetDao(),
            widget,
            new Runnable() {
                @Override
                public void run() {
                    // Report successful configuration
                    setResult(
                        RESULT_OK,
                        new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId));
                    finish();
                }
            }).execute();
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
