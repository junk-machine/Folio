package ru.nahk.folio.widgets;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;

/**
 * Job service that refreshes symbols data and updates all portfolio item widgets.
 */
public class PortfolioItemWidgetUpdateJob extends JobService {
    /**
     * Identifier of the job.
     */
    public static int JOB_ID = 1;

    /**
     * Instance of the currently running update task.
     */
    private PortfolioItemWidgetUpdateTask mUpdateTask;

    /**
     * Starts async task to refresh symbols data and update widgets.
     * @param params Job parameters.
     * @return True if new task was started, otherwise false.
     */
    @Override
    public boolean onStartJob(final JobParameters params) {
        PortfolioItemWidgetUpdateTask previousTask = mUpdateTask;

        if (previousTask != null
                && previousTask.getStatus() != AsyncTask.Status.FINISHED) {
            // Previous task is still running, let it finish
            return false;
        }

        mUpdateTask = new PortfolioItemWidgetUpdateTask(this, params);
        mUpdateTask.execute();

        return true;
    }

    /**
     * Cancels active async task.
     * @param params Job parameters.
     * @return False to indicate that job does not need to be rescheduled.
     */
    @Override
    public boolean onStopJob(JobParameters params) {
        PortfolioItemWidgetUpdateTask activeTask = mUpdateTask;

        if (activeTask != null) {
            activeTask.cancel(true);
            mUpdateTask = null;
        }

        return false;
    }
}
