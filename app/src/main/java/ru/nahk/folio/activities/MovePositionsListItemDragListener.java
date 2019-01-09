package ru.nahk.folio.activities;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.DragEvent;
import android.view.View;

import ru.nahk.folio.R;
import ru.nahk.folio.model.GroupViewModel;
import ru.nahk.folio.model.PositionsListItemViewModel;

/**
 * Listener for drag-and-drop events on recycler view that allows to move items
 * from one group to another.
 * At the time it scrolls the {@link RecyclerView} when drag reaches specified
 * threshold at top or bottom of the list.
 */
class MovePositionsListItemDragListener implements View.OnDragListener {
    /**
     * Scroll listener that starts new scrolling when previous completes.
     */
    private class ContinuousScrollListener extends RecyclerView.OnScrollListener {
        /**
         * Current scrolling speed.
         */
        private int mScrollSpeed;

        /**
         * Starts new smooth scrolling.
         * @param recyclerView {@link RecyclerView} to scroll.
         * @param dx X axis delta of completed scroll.
         * @param dy Y axis delta of completed scroll.
         */
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            if (recyclerView.canScrollVertically(mScrollSpeed)) {
                recyclerView.smoothScrollBy(0, mScrollSpeed);
            } else {
                // Stop scrolling, as there is nowhere to scroll anymore
                setScrollSpeed(recyclerView, 0);
            }
        }

        /**
         * Sets the current scrolling speed.
         * @param recyclerView {@link RecyclerView} to scroll.
         * @param scrollSpeed New scrolling speed.
         */
        void setScrollSpeed(RecyclerView recyclerView, int scrollSpeed) {
            if (mScrollSpeed == 0 && scrollSpeed != 0) {
                // Start scrolling
                mScrollSpeed = scrollSpeed;
                recyclerView.addOnScrollListener(this);
                recyclerView.smoothScrollBy(0, mScrollSpeed);
            } else if (mScrollSpeed != 0 && scrollSpeed == 0) {
                // Stop scrolling
                mScrollSpeed = scrollSpeed;
                recyclerView.removeOnScrollListener(this);
                recyclerView.stopScroll();
            } else {
                mScrollSpeed = scrollSpeed;
            }
        }
    }

    /**
     * Defines listener for move events.
     */
    public interface OnPositionsListItemMovedListener {
        /**
         * Moves item to new parent group.
         * @param item Positions list item that is being moved.
         * @param targetGroup New parent group.
         */
        void onItemMoved(PositionsListItemViewModel item, GroupViewModel targetGroup);
    }

    /**
     * Scroll acceleration.
     * How many pixels will be scrolled for each pixel into trigger area.
     */
    private final static float SCROLL_ACCELERATION = 5f;

    /**
     * Scroll listener that implements continuous scrolling.
     */
    private final ContinuousScrollListener mScroller;

    /**
     * Size of the area in pixels that triggers the scrolling.
     */
    private final int mScrollTriggerArea;

    /**
     * Duration in milliseconds for all animations.
     */
    private final int mAnimationDuration;

    /**
     * Current listener for move events.
     */
    private OnPositionsListItemMovedListener mItemMovedListener;

    /**
     * Item view that was highlighted previously.
     */
    private View mLastCoveredView;

    /**
     * Drag tint layer of the item view that was highlighted previously.
     */
    private View mLastCoveredDragTint;

    /**
     * Creates new instance of the {@link MovePositionsListItemDragListener} class
     * with the provided trigger area and animation duration.
     * @param scrollTriggerArea Size of the area in pixels that triggers the scrolling.
     * @param animationDuration Duration of all animations.
     */
    MovePositionsListItemDragListener(int scrollTriggerArea, int animationDuration) {
        mScroller = new ContinuousScrollListener();
        mScrollTriggerArea = scrollTriggerArea;
        mAnimationDuration = animationDuration;
    }

    /**
     * Sets the listener for item move events.
     * @param listener New listener for item move events.
     */
    void setOnItemMovedListener(OnPositionsListItemMovedListener listener) {
        mItemMovedListener = listener;
    }

    /**
     * Handles drag events for the {@link RecyclerView}.
     * @param v View that accepts drag events.
     * @param event Type of the drag event.
     * @return True if event was handled, otherwise false.
     */
    @Override
    public boolean onDrag(final View v, DragEvent event) {
        if (!(v instanceof RecyclerView)) {
            return false;
        }

        RecyclerView recyclerView = (RecyclerView) v;
        Object item = event.getLocalState();

        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                return true;
            case DragEvent.ACTION_DRAG_LOCATION:
                float currentY = event.getY();

                if (currentY < recyclerView.getTop() + mScrollTriggerArea) {
                    // Scroll up
                    mScroller.setScrollSpeed(
                        recyclerView,
                        Math.round(SCROLL_ACCELERATION * (currentY - recyclerView.getTop() - mScrollTriggerArea)));
                } else if (currentY > recyclerView.getBottom() - mScrollTriggerArea) {
                    // Scroll down
                    mScroller.setScrollSpeed(
                        recyclerView,
                        Math.round(SCROLL_ACCELERATION * (currentY - recyclerView.getBottom() + mScrollTriggerArea)));
                } else {
                    // Stop scrolling
                    mScroller.setScrollSpeed(recyclerView, 0);
                }

                if (item != null) {
                    // Highlight target item
                    View coveredView = recyclerView.findChildViewUnder(event.getX(), event.getY());

                    if (mLastCoveredView != coveredView) {
                        resetItemTint(mLastCoveredDragTint);

                        GroupViewModel targetGroup =
                            tryGetTargetGroup(recyclerView, coveredView);

                        // Drag is not allowed if target is not a group or
                        // target group is nested under the drag item
                        mLastCoveredDragTint =
                            targetGroup == null || isParentOfTarget(event.getLocalState(), targetGroup)
                                ? null
                                : highlightItemView(coveredView);

                        mLastCoveredView = coveredView;
                    }
                }

                return true;
            case DragEvent.ACTION_DROP:
                GroupViewModel targetGroup =
                    tryGetTargetGroup(
                        recyclerView,
                        recyclerView.findChildViewUnder(event.getX(), event.getY()));

                if (targetGroup == null || isParentOfTarget(item, targetGroup) ||
                        mItemMovedListener == null || !(item instanceof PositionsListItemViewModel)) {
                    return false;
                }

                mItemMovedListener.onItemMoved((PositionsListItemViewModel) item, targetGroup);
                return true;
            case DragEvent.ACTION_DRAG_EXITED:
                // Assuming ListView will take entire screen - do not stop scroll when
                // drag moves to notification bar or navigation controls.
                // This allows to just drop the finger down to scroll all the way to the end.
                resetItemTint(mLastCoveredDragTint);
                return true;
            case DragEvent.ACTION_DRAG_ENDED:
                mScroller.setScrollSpeed(recyclerView, 0);
                resetItemTint(mLastCoveredDragTint);
                return true;
            default:
                return false;
        }
    }

    /**
     * Tries to extract an associated group view-model for the given item view.
     * @param recyclerView Recycler view.
     * @param itemView Item view.
     * @return Associated group view-model or NULL.
     */
    private GroupViewModel tryGetTargetGroup(RecyclerView recyclerView, View itemView) {
        GroupViewModel viewModel = null;

        if (itemView != null) {
            RecyclerView.ViewHolder itemViewHolder =
                recyclerView.findContainingViewHolder(itemView);

            if (itemViewHolder instanceof PositionsListAdapter.GroupViewHolder) {
                viewModel =
                    ((PositionsListAdapter.GroupViewHolder) itemViewHolder)
                        .getBoundData();
            }
        }

        return viewModel;
    }

    /**
     * Checks if target group is nested under given item.
     * @param item Item that is being moved.
     * @param targetGroup Target group.
     * @return True if target group is nested under given item, otherwise false.
     */
    private boolean isParentOfTarget(Object item, GroupViewModel targetGroup) {
        while (targetGroup != null) {
            if (targetGroup == item) {
                return true;
            }

            targetGroup = targetGroup.parent;
        }

        return false;
    }

    /**
     * Highlights item view, if drop is allowed by it.
     * @param itemView Item view.
     * @return Drag tint view, if drop is allowed, otherwise false.
     */
    private View highlightItemView(View itemView) {
        View dragTint = null;

        if (itemView != null) {
            dragTint = itemView.findViewById(R.id.drag_target_tint);
            if (dragTint != null) {
                dragTint
                    .animate()
                    .alpha(0.15f)
                    .setDuration(mAnimationDuration)
                    .start();
            }
        }

        return dragTint;
    }

    /**
     * Hides item drag tint.
     * @param dragTint Drag tint to hide.
     */
    private void resetItemTint(View dragTint) {
        if (dragTint != null) {
            dragTint
                .animate()
                .alpha(0)
                .setDuration(mAnimationDuration)
                .start();
        }
    }
}