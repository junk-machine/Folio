package ru.nahk.folio.widgets;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import ru.nahk.folio.model.PortfolioDatabase;

/**
 * Job service that deletes widgets configuration from persistent storage.
 */
public class PortfolioItemWidgetDeleteJob extends JobIntentService {
    /**
     * Unique identifier for this job.
     */
    private static final int JOB_ID = 2;

    /**
     * Key for intent extra that holds array of widget identifiers.
     */
    private static final String APP_WIDGET_IDS_EXTRA = "app-widget-ids";

    /**
     * Schedules the job.
     * @param context Application context.
     * @param widgetIds Identifiers of the widgets to delete.
     */
    static void enqueueWork(Context context, int[] widgetIds) {
        enqueueWork(
            context,
            PortfolioItemWidgetDeleteJob.class,
            JOB_ID,
            new Intent().putExtra(APP_WIDGET_IDS_EXTRA, widgetIds));
    }

    /**
     * Deletes configuration for specified widgets from the persistent storage.
     * @param intent Job intent.
     */
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        int[] appWidgetIds = intent.getIntArrayExtra(APP_WIDGET_IDS_EXTRA);

        if (appWidgetIds != null) {
            PortfolioDatabase
                .getInstance(getApplicationContext())
                .widgetDao()
                .deletePortfolioItemWidgets(appWidgetIds);
        }
    }
}
