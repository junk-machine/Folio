package ru.nahk.folio.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import java.math.BigDecimal;

import ru.nahk.folio.R;
import ru.nahk.folio.activities.ActivityNavigationConstants;
import ru.nahk.folio.activities.LotsListActivity;
import ru.nahk.folio.activities.MainActivity;
import ru.nahk.folio.activities.SymbolDetailsActivity;
import ru.nahk.folio.model.GroupViewModel;
import ru.nahk.folio.model.PortfolioDatabase;
import ru.nahk.folio.model.PortfolioItemWidgetEntity;
import ru.nahk.folio.model.PositionViewModel;
import ru.nahk.folio.model.PositionsListItemViewModel;
import ru.nahk.folio.utils.BigDecimalHelper;

/**
 * Manages portfolio item widgets UI.
 */
public final class PortfolioItemWidgetManager {
    /**
     * Action name for pending intent to navigate to positions group.
     */
    private static final String NAVIGATE_TO_GROUP = "NAVIGATE_TO_GROUP";

    /**
     * Activity name for pending intent to navigate to position lots.
     */
    private static final String NAVIGATE_TO_POSITION = "NAVIGATE_TO_POSITION";

    /**
     * Activity name for pending intent to navigate to symbol details.
     */
    private static final String NAVIGATE_TO_SYMBOL_DETAILS = "NAVIGATE_TO_SYMBOL_DETAILS";

    /**
     * Refreshes specified widget view.
     * @param context Activity context.
     * @param widgetId Unique identifier of the widget.
     */
    public static void updateWidget(@NonNull Context context, int widgetId) {
        PortfolioDatabase database =
            PortfolioDatabase.getInstance(context.getApplicationContext());

        database.beginTransaction();
        try {
            updateWidgetView(
                context,
                database.widgetDao().loadPortfolioItemWidget(widgetId),
                database,
                AppWidgetManager.getInstance(context));

            database.setTransactionSuccessful();
        }
        finally {
            database.endTransaction();
        }
    }

    /**
     * Refreshes all widgets views.
     * @param context Activity context.
     */
    public static void updateAllWidgets(@NonNull Context context) {
        PortfolioDatabase database =
            PortfolioDatabase.getInstance(context.getApplicationContext());

        database.beginTransaction();
        try {
            AppWidgetManager widgetManager =
                AppWidgetManager.getInstance(context);

            for (PortfolioItemWidgetEntity widget:
                database.widgetDao().loadAllPortfolioItemWidgets()) {

                updateWidgetView(context, widget, database, widgetManager);
            }

            database.setTransactionSuccessful();
        }
        finally {
            database.endTransaction();
        }
    }

    /**
     * Updates single portfolio item widget.
     * @param context Activity context.
     * @param widget Widget data.
     * @param database Portfolio persistent storage.
     * @param widgetManager Application widgets manager.
     */
    private static void updateWidgetView(
        @NonNull Context context,
        @NonNull PortfolioItemWidgetEntity widget,
        @NonNull PortfolioDatabase database,
        @NonNull AppWidgetManager widgetManager) {

        RemoteViews view = null;

        if (widget.itemType == PortfolioItemWidgetEntity.ITEM_TYPE_GROUP) {
            view = getGroupWidgetView(
                context,
                database.groupDao().loadWithValue(widget.itemId));
        } else if (widget.itemType == PortfolioItemWidgetEntity.ITEM_TYPE_POSITION) {
            view = getPositionWidgetView(
                context,
                database.positionDao().load(widget.itemId));
        }

        if (view != null) {
            widgetManager.updateAppWidget(widget.id, view);
        }
    }

    /**
     * Updates group widget view.
     * @param context Current context.
     * @param group Positions group item.
     * @return Remote view for the widget.
     */
    private static RemoteViews getGroupWidgetView(Context context, GroupViewModel group) {
        RemoteViews view = createWidgetView(context, group);

        if (group != null) {
            // Open main activity when widget is clicked
            view.setOnClickPendingIntent(
                R.id.widget_layout_root,
                PendingIntent.getActivity(
                    context,
                    (int) group.id,
                    new Intent(context, MainActivity.class)
                        .setAction(NAVIGATE_TO_GROUP),
                    PendingIntent.FLAG_IMMUTABLE));
        } else {
            view.setOnClickPendingIntent(R.id.widget_layout_root, null);
        }

        return view;
    }

    /**
     * Updates portfolio position widget view.
     * @param context Current context.
     * @param position Portfolio position item.
     * @return Remote view for the widget.
     */
    private static RemoteViews getPositionWidgetView(Context context, PositionViewModel position) {
        RemoteViews view = createWidgetView(context, position);

        if (position != null) {
            // If there are lots for this position - open lots list activity when widget is clicked,
            // otherwise open symbol details
            view.setOnClickPendingIntent(
                R.id.widget_layout_root,
                PendingIntent.getActivity(
                    context,
                    (int) position.id,
                    new Intent(
                            context,
                            position.quantity > 0 ? LotsListActivity.class : SymbolDetailsActivity.class)
                        .setAction(position.quantity > 0 ? NAVIGATE_TO_POSITION : NAVIGATE_TO_SYMBOL_DETAILS)
                        .putExtra(ActivityNavigationConstants.POSITION_ID_KEY, position.id)
                        .putExtra(ActivityNavigationConstants.STOCK_SYMBOL_KEY, position.symbol),
                    PendingIntent.FLAG_IMMUTABLE));

            if (TextUtils.isEmpty(position.name)) {
                // For positions show symbol, if company name was not loaded
                view.setTextViewText(R.id.position_name, position.symbol);
            }

            if (position.quantity > 0 && position.symbolValueChange != null) {
                int symbolValueChangeDirection =
                    position.symbolValueChange.compareTo(BigDecimal.ZERO);

                view.setViewVisibility(R.id.symbol_value_change_icon, View.VISIBLE);

                view.setImageViewResource(
                    R.id.symbol_value_change_icon,
                    symbolValueChangeDirection > 0
                        ? R.drawable.arrow_up
                        : (symbolValueChangeDirection < 0
                            ? R.drawable.arrow_down
                            : R.drawable.circle));
            } else {
                view.setViewVisibility(R.id.symbol_value_change_icon, View.GONE);
            }
        } else {
            view.setOnClickPendingIntent(R.id.widget_layout_root, null);
            view.setViewVisibility(R.id.symbol_value_change_icon, View.GONE);
        }

        return view;
    }

    /**
     * Creates remote view layout for the widget to display given positions list item.
     * @param context Current context.
     * @param item Positions list item that holds the data.
     * @return Newly created remote view.
     */
    @NonNull
    private static RemoteViews createWidgetView(Context context, PositionsListItemViewModel item) {
        RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget_portfolioitem);

        int valueChangeColorResId = R.color.noChange;

        if (item != null) {
            view.setTextViewText(R.id.position_name, item.name);

            view.setTextViewText(
                R.id.position_current_value,
                item.currentValue != null
                    ? BigDecimalHelper.formatCurrency(item.currentValue)
                    : context.getString(R.string.unknown_value));

            if (item.currentValue != null && item.baseValue != null) {
                BigDecimal valueChange = item.currentValue.subtract(item.baseValue);
                int valueChangeDirection = item.currentValue.compareTo(item.baseValue);

                view.setTextViewText(
                    R.id.position_value_change,
                    context.getString(R.string.extended_value_change_format,
                        BigDecimalHelper.formatCurrencyChange(valueChange),
                        BigDecimalHelper.formatPercentageShort(item.baseValue, valueChange)));

                valueChangeColorResId =
                    valueChangeDirection > 0
                        ? R.color.positiveChange
                        : (valueChangeDirection < 0
                            ? R.color.negativeChange
                            : R.color.noChange);
            } else {
                view.setTextViewText(R.id.position_value_change, null);
            }
        } else {
            view.setTextViewText(
                R.id.position_name,
                context.getString(R.string.unknown_value));

            view.setTextViewText(
                R.id.position_current_value,
                context.getString(R.string.unknown_value));

            view.setTextViewText(R.id.position_value_change, null);
        }

        int valueChangeColor = context.getColor(valueChangeColorResId);
        view.setTextColor(R.id.position_value_change, valueChangeColor);
        view.setInt(R.id.position_level_stripes, "setColorFilter", valueChangeColor);

        return view;
    }
}
