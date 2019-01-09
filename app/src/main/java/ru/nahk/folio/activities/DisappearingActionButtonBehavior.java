package ru.nahk.folio.activities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;

/**
 * Floating action button behavior that makes it disappear when nested control is scrolling.
 */
public class DisappearingActionButtonBehavior extends FloatingActionButton.Behavior {
    /**
     * Creates new instance of the {@link DisappearingActionButtonBehavior} class.
     */
    public DisappearingActionButtonBehavior() {
        super();
    }

    /**
     * Creates new instance of the {@link DisappearingActionButtonBehavior} class
     * with the given context and attributes.
     * This constructor allows behavior to be used in layout XML.
     */
    public DisappearingActionButtonBehavior(Context context, AttributeSet attrs) {
        super();
    }

    /**
     * Hides associated floating action button.
     * @param coordinatorLayout Parent coordinator layout.
     * @param child Associated floating action button.
     * @param directTargetChild Child view that either is or contains the scroll target.
     * @param target Scroll target.
     * @param axes Scroll axis.
     * @param type Type of input which cause this scroll event.
     * @return Always true.
     */
    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        if (child.isOrWillBeShown()) {
            child.hide();
        }

        return true;
    }

    /**
     * Displays associated floating action button.
     * @param coordinatorLayout Parent coordinator layout.
     * @param child Associated floating action button.
     * @param target Scroll target.
     * @param type Type of input which cause this scroll event.
     */
    @Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View target, int type) {
        if (child.isOrWillBeHidden()) {
            child.show();
        }
    }
}
