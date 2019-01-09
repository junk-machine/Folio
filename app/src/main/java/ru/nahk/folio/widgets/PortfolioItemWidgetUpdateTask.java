package ru.nahk.folio.widgets;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.List;

import ru.nahk.folio.broadcasts.SymbolsDataChangedBroadcast;
import ru.nahk.folio.model.PortfolioDatabase;
import ru.nahk.folio.model.SymbolEntity;
import ru.nahk.folio.stockapi.StockApiException;
import ru.nahk.folio.stockapi.StockApiFactory;

/**
 * Asynchronous task that refreshes symbols data and updates all widget views.
 */
class PortfolioItemWidgetUpdateTask extends AsyncTask<Void, Void, JobService> {
    /**
     * Parent job service.
     */
    private final WeakReference<JobService> mJobService;

    /**
     * Parent job parameters.
     */
    private final JobParameters mJobParameters;

    /**
     * Creates new instance of the {@link PortfolioItemWidgetUpdateTask} class
     * with the provided parent job service and job parameters.
     * @param jobService Parent job service.
     * @param jobParameters Parameters of the job.
     */
    PortfolioItemWidgetUpdateTask(
        @NonNull JobService jobService,
        @NonNull JobParameters jobParameters) {

        mJobService = new WeakReference<>(jobService);
        mJobParameters = jobParameters;
    }

    /**
     * Refreshes symbols data and updates all widget views.
     * @param aVoid Nothing.
     * @return Parent job service.
     */
    @Override
    protected JobService doInBackground(Void[] aVoid) {
        JobService jobService = mJobService.get();

        if (jobService == null) {
            return null;
        }

        PortfolioDatabase database =
            PortfolioDatabase.getInstance(jobService.getApplicationContext());

        database.beginTransaction();
        try {
            // Refresh all symbols
            List<SymbolEntity> symbols = database.symbolDao().get();
            StockApiFactory.getApi().updateSymbols(symbols);
            database.symbolDao().update(symbols);

            // Notify app that symbols data has changed
            SymbolsDataChangedBroadcast.send(jobService.getApplicationContext());

            database.setTransactionSuccessful();
        }
        catch (StockApiException stockApiException) {
            // If we cannot update prices, still proceed to update the views with old data.
            // Sometimes this task can be called from initialization, so views will be
            // completely empty otherwise.
        }
        finally {
            database.endTransaction();
        }

        // Don't start UI refresh, if cancellation requested
        if (isCancelled()) {
            return jobService;
        }

        // Update widgets views
        PortfolioItemWidgetManager.updateAllWidgets(jobService);

        return jobService;
    }

    /**
     * Finishes the parent job.
     * @param jobService Parent job service.
     */
    @Override
    protected void onPostExecute(JobService jobService) {
        if (jobService != null) {
            jobService.jobFinished(mJobParameters, false);
        }
    }

    /**
     * Finishes the parent job.
     * @param jobService Parent job service.
     */
    @Override
    protected void onCancelled(JobService jobService) {
        if (jobService != null) {
            jobService.jobFinished(mJobParameters, false);
        }
    }
}
