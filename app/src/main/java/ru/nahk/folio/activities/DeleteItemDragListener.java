package ru.nahk.folio.activities;

import android.support.annotation.NonNull;
import android.view.DragEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Listener for drag-and-drop events on delete button for list items.
 * @param <TItem> Type of supported list items.
 */
public class DeleteItemDragListener<TItem> implements View.OnDragListener {
    /**
     * Defines listener for delete events.
     * @param <TItem> Type of the list item to be deleted.
     */
    public interface OnItemDeletedListener<TItem> {
        void onItemDeleted(TItem item);
    }

    /**
     * Current delete list item event listener.
     */
    private final OnItemDeletedListener<TItem> mItemDeletedListener;

    /**
     * Duration in milliseconds for all animations.
     */
    private final int mAnimationDuration;

    /**
     * Creates new instance of the {@link DeleteItemDragListener} class
     * with the provided delete event listener and animation duration.
     * @param itemDeletedListener Listener for delete item events.
     * @param animationDuration Duration of all animations.
     */
    DeleteItemDragListener(
            @NonNull OnItemDeletedListener<TItem> itemDeletedListener,
            int animationDuration) {
        mItemDeletedListener = itemDeletedListener;
        mAnimationDuration = animationDuration;
    }

    /**
     * Handles drag events.
     * @param v View that accepts drag events.
     * @param event Type of the drag event.
     * @return True if event was handled, otherwise false.
     */
    @Override
    public boolean onDrag(final View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                v.animate()
                    .setInterpolator(new OvershootInterpolator())
                    .scaleX(1)
                    .scaleY(1)
                    .setDuration(mAnimationDuration)
                    .start();
                return true;
            case DragEvent.ACTION_DRAG_ENTERED:
                v.animate()
                    .setInterpolator(new AccelerateInterpolator())
                    .scaleX(1.15f)
                    .scaleY(1.15f)
                    .setDuration(mAnimationDuration)
                    .start();
                return true;
            case DragEvent.ACTION_DROP:
                try {
                    @SuppressWarnings("unchecked")
                    TItem item = (TItem) event.getLocalState();
                    mItemDeletedListener.onItemDeleted(item);
                    return true;
                }
                catch (ClassCastException castException) {
                    return false;
                }
            case DragEvent.ACTION_DRAG_EXITED:
                v.animate()
                    .setInterpolator(new AccelerateInterpolator())
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(mAnimationDuration)
                    .start();
                return true;
            case DragEvent.ACTION_DRAG_ENDED:
                v.animate()
                    .setInterpolator(new AccelerateInterpolator())
                    .scaleX(0)
                    .scaleY(0)
                    .setDuration(mAnimationDuration)
                    .start();
                return true;
            default:
                return false;
        }
    }
}
