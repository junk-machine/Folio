package ru.nahk.folio.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import ru.nahk.folio.utils.ProgressHandler;

/**
 * Async task that runs in the background.
 * @param <Result> Type of the task results.
 */
public abstract class UiAsyncTask<Result> extends AsyncTask<Void, Void, Result> {
    /**
     * Async task progress handler.
     */
    private ProgressHandler mProgressHandler;

    /**
     * Flag that indicates whether async task has failed.
     */
    private boolean hasFailed;

    /**
     * Creates new instance of the {@link UiAsyncTask} class
     * with the provided progress handler.
     * @param progressHandler Async task progress handler.
     */
    public UiAsyncTask(@NonNull ProgressHandler progressHandler) {
        mProgressHandler = progressHandler;
    }

    /**
     * Starts background work.
     * @param voids Nothing.
     * @return Result of the background work.
     */
    @Override
    protected final Result doInBackground(Void... voids) {
        try {
            return doAsync();
        }
        catch (Exception error) {
            hasFailed = true;
            mProgressHandler.progressError(error);
        }

        return null;
    }

    /**
     * Executes actual background work.
     * @return Result of the background work.
     * @throws Exception Thrown if anything goes wrong.
     */
    protected abstract Result doAsync() throws Exception;

    /**
     * Reports start of the task to progress handler.
     */
    @Override
    protected final void onPreExecute() {
        super.onPreExecute();
        mProgressHandler.progressStart();

        doBefore();
    }

    /**
     * Executes necessary work before background task starts.
     */
    protected void doBefore() { }

    /**
     * Reports completion of the task to progress handler.
     * @param result Async work result.
     */
    @Override
    protected final void onPostExecute(Result result) {
        super.onPostExecute(result);

        if (!hasFailed) {
            doAfter(result);
        }

        mProgressHandler.progressEnd();
    }

    /**
     * Handles result of the async work.
     * @param result Async work result.
     */
    protected void doAfter(Result result) { }
}
