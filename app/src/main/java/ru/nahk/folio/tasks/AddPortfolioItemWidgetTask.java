package ru.nahk.folio.tasks;

import android.content.Context;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;

import ru.nahk.folio.activities.PortfolioItemWidgetConfigureActivity;
import ru.nahk.folio.model.PortfolioItemWidgetEntity;
import ru.nahk.folio.model.WidgetDao;
import ru.nahk.folio.utils.Callback;
import ru.nahk.folio.utils.ProgressHandler;
import ru.nahk.folio.widgets.PortfolioItemWidgetManager;

/**
 * Async task to store new portfolio item widget information.
 */
public class AddPortfolioItemWidgetTask extends UiAsyncTask<Void> {
    /**
     * Current activity context.
     */
    private final WeakReference<Context> mContext;

    /**
     * Persisted storage for widgets data.
     */
    private final WidgetDao mWidgetDao;

    /**
     * Portfolio item widget information to add.
     */
    private final PortfolioItemWidgetEntity mWidget;

    /**
     * Callback to execute when background work is done.
     */
    private final Runnable mCompletionCallback;

    /**
     * Creates new instance of the {@link AddPortfolioItemWidgetTask} class
     * with the provided progress handler, widget entity and.
     * @param progressHandler Async task progress handler.
     * @param context Activity context.
     * @param widgetDao Persistent widgets data store.
     * @param widget Portfolio item widget configuration to store.
     * @param completionCallback Callback to execute when task completes.
     */
    public AddPortfolioItemWidgetTask(
        @NonNull ProgressHandler progressHandler,
        @NonNull Context context,
        @NonNull WidgetDao widgetDao,
        @NonNull PortfolioItemWidgetEntity widget,
        @NonNull Runnable completionCallback) {

        super(progressHandler);

        mContext = new WeakReference<>(context);
        mWidgetDao = widgetDao;
        mWidget = widget;
        mCompletionCallback = completionCallback;
    }

    /**
     * Stores widget configuration in persistent store and updates its view.
     * @return Nothing.
     */
    @Override
    protected Void doAsync() {
        mWidgetDao.insert(mWidget);

        Context context = mContext.get();
        if (context != null) {
            PortfolioItemWidgetManager.updateWidget(context, mWidget.id);
        }

        return null;
    }

    /**
     * Invokes completion callback.
     * @param aVoid Nothing.
     */
    @Override
    protected void doAfter(Void aVoid) {
        mCompletionCallback.run();
    }
}
