package ru.nahk.folio.widgets;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;

/**
 * Provider for portfolio item widget.
 */
public class PortfolioItemWidgetProvider extends AppWidgetProvider {
    /**
     * Widget refresh interval.
     */
    private final static int REFRESH_INTERVAL = 30 * 60 * 1000; // 30 minutes

    /**
     * Schedules widgets update job when widgets update is requested.
     * This is not expected to be called as we use configuration activity and
     * updatePeriodMillis is set to 0, but some launchers still call this when
     * application is updated/reinstalled.
     * @param context Application context.
     * @param appWidgetManager Application widget manager.
     * @param appWidgetIds Identifiers of the widgets to update.
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        scheduleUpdateJob(context);
    }

    /**
     * Schedules widgets update job when first widget is added or device rebooted.
     * @param context Application context.
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        scheduleUpdateJob(context);
    }

    /**
     * Deletes given widgets from the persistent store.
     * @param context Application context.
     * @param appWidgetIds Unique identifers of the widgets to remove.
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        PortfolioItemWidgetDeleteJob.enqueueWork(context, appWidgetIds);
        super.onDeleted(context, appWidgetIds);
    }

    /**
     * Stops the refresh job.
     * @param context Application context.
     */
    @Override
    public void onDisabled(Context context) {
        // All widgets removed - stop the job
        context.getSystemService(JobScheduler.class)
            .cancel(PortfolioItemWidgetUpdateJob.JOB_ID);

        super.onDisabled(context);
    }

    /**
     * Schedules periodic job to refresh prices and update widget views.
     * @param context Application context.
     */
    private void scheduleUpdateJob(Context context) {
        // First widget created or device rebooted - kick-off the job
        JobScheduler scheduler =
            context.getSystemService(JobScheduler.class);

        scheduler.schedule(
            new JobInfo
                .Builder(
                    PortfolioItemWidgetUpdateJob.JOB_ID,
                    new ComponentName(context, PortfolioItemWidgetUpdateJob.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(REFRESH_INTERVAL)
                .build());
    }
}
