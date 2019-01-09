package ru.nahk.folio.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;

import ru.nahk.folio.utils.ProgressHandler;
import ru.nahk.folio.widgets.PortfolioItemWidgetManager;

/**
 * Async task that refreshes portfolio item widgets after background work is done.
 * @param <Result> Type of the task results.
 */
public abstract class RefreshPortfolioItemWidgetsTask<Result> extends UiAsyncTask<Result> {
    /**
     * Associated activity context.
     */
    private final WeakReference<Context> mContext;

    /**
     * Creates new instance of the {@link RefreshPortfolioItemWidgetsTask} class
     * with the provided progress handler.
     * @param progressHandler Async task progress handler.
     * @param context Activity context.
     */
    RefreshPortfolioItemWidgetsTask(
        @NonNull ProgressHandler progressHandler,
        @NonNull Context context) {

        super(progressHandler);

        mContext = new WeakReference<>(context);
    }

    /**
     * Kicks off background task to update all portfolio item widgets.
     * @param result Async work result.
     */
    @Override
    protected void doAfter(Result result) {
        super.doAfter(result);

        final Context context = mContext.get();
        if (context != null) {
            // Update all portfolio item widgets
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    PortfolioItemWidgetManager.updateAllWidgets(context);
                }
            });
        }
    }
}
