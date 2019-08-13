package ru.nahk.folio.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import ru.nahk.folio.R;
import ru.nahk.folio.model.GroupViewModel;
import ru.nahk.folio.model.PositionsListItemViewModel;

/**
 * Control that renders colored vertical stripes for multiple nesting levels.
 */
public class LevelStripes extends View {
    /**
     * Configured width of the stripe.
     */
    private int mStripeWidth;

    /**
     * Configured margin between stripes.
     */
    private int mStripeMargin;

    /**
     * Paint for negative value change stripe.
     */
    private Paint mNegativeChangePaint;

    /**
     * Paint for no value change stripe.
     */
    private Paint mNoChangePaint;

    /**
     * Paint for positive change stripe.
     */
    private Paint mPositiveChangePaint;

    /**
     * Flag that indicates if stripes should be rendered for right-to-left layout.
     */
    private boolean mIsRightToLeft;

    /**
     * Actual computed stripe height.
     */
    private float mActualStripeHeight;

    /**
     * Actual computed stripe width.
     */
    private float mActualStripeWidth;

    /**
     * Positions list item to display level stripes for.
     */
    private PositionsListItemViewModel mPositionsListItem;

    /**
     * Group list item to display level stripes for, if it is a group, otherwise {@code null}.
     */
    private GroupViewModel mGroupItem;

    /**
     * Pre-allocated {@link Path} for stripe rectangle rendering.
     */
    private Path mRectangle;

    /**
     * Pre-allocated array of corner radius values for rendering.
     */
    private float[] mRectangleCorners;

    /**
     * Corner radius for stripe rectangle.
     */
    private float mCornerRadius;

    /**
     * Creates new instance of the {@link LevelStripes} class
     * with the provided context.
     * @param context Parent activity context.
     */
    public LevelStripes(Context context) {
        super(context);
        init();
    }

    /**
     * Creates new instance of the {@link LevelStripes} class
     * with the provided context and XML attributes.
     * @param context Parent activity context.
     * @param attrs XML attributes.
     */
    public LevelStripes(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * Creates new instance of the {@link LevelStripes} class
     * with the provided context, XML attributes and default style.
     * @param context Parent activity context.
     * @param attrs XML attributes.
     * @param defStyleAttr Default style attributes.
     */
    public LevelStripes(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * Sets the positions list item to display level stripes for.
     * @param positionsListItem Positions list item.
     */
    public void setPositionsListItem(PositionsListItemViewModel positionsListItem) {
        if (positionsListItem != this.mPositionsListItem) {
            this.mPositionsListItem = positionsListItem;
            this.mGroupItem =
                positionsListItem instanceof GroupViewModel
                    ? (GroupViewModel)positionsListItem
                    : null;

            invalidate();
            requestLayout();
        }
    }

    /**
     * Initializes all fields with default values.
     */
    private void init() {
        mStripeWidth = 2;
        mNegativeChangePaint = new Paint();
        mNoChangePaint = new Paint();
        mPositiveChangePaint = new Paint();
        mIsRightToLeft =
            ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
        mRectangle = new Path();
        mRectangleCorners = new float[8];
    }

    /**
     * Initializes all fields from XML attributes.
     * @param context Parent activity context.
     * @param attrs XML attributes.
     */
    private void init(@NonNull Context context, AttributeSet attrs) {
        init();

        TypedArray typedAttrs = null;

        try {
            typedAttrs =
                context.obtainStyledAttributes(attrs, R.styleable.LevelStripes);

            for (int attrIndex = 0; attrIndex < typedAttrs.getIndexCount(); ++attrIndex) {
                int attrId = typedAttrs.getIndex(attrIndex);
                switch (attrId) {
                    case R.styleable.LevelStripes_stripeWidth:
                        mStripeWidth =
                            typedAttrs.getDimensionPixelSize(attrId, mStripeWidth);
                        break;

                    case R.styleable.LevelStripes_stripeMargin:
                        mStripeMargin =
                            typedAttrs.getDimensionPixelSize(attrId, mStripeMargin);
                        break;

                    case R.styleable.LevelStripes_negativeChangeColor:
                        mNegativeChangePaint.setColor(
                            typedAttrs.getColor(attrId, Color.RED));
                        break;

                    case R.styleable.LevelStripes_noChangeColor:
                        mNoChangePaint.setColor(
                            typedAttrs.getColor(attrId, Color.GRAY));
                        break;

                    case R.styleable.LevelStripes_positiveChangeColor:
                        mPositiveChangePaint.setColor(
                            typedAttrs.getColor(attrId, Color.GREEN));
                        break;
                }
            }
        }
        finally {
            if (typedAttrs != null) {
                typedAttrs.recycle();
            }
        }
    }

    /**
     * Handles size change events.
     * @param w New width.
     * @param h New height.
     * @param oldw Old width.
     * @param oldh Old height.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mActualStripeHeight =
            Math.max(h - getPaddingTop() - getPaddingBottom(), 0);

        // Subtract horizontal paddings
        w -= getPaddingStart() + getPaddingEnd();

        // Subtract margin between stripes
        w -= Math.max(mPositionsListItem.level - 1, 0) * mStripeMargin;

        mActualStripeWidth =
            mPositionsListItem.level > 0
                ? Math.max((float) w / mPositionsListItem.level, 0)
                : 0;

        mCornerRadius = mActualStripeWidth / 2;

        mIsRightToLeft =
            ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    /**
     * Handles layout measurement.
     * @param widthMeasureSpec Width measurement specification.
     * @param heightMeasureSpec Height measurement specification.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredHeight = getPaddingTop() + getPaddingBottom() + getSuggestedMinimumHeight();
        int desiredWidth = getPaddingStart() + getPaddingEnd() + getSuggestedMinimumWidth();

        setMeasuredDimension(
            resolveSizeAndState(desiredWidth, widthMeasureSpec, 1),
            resolveSizeAndState(desiredHeight, heightMeasureSpec, 0));
    }

    /**
     * Computes desired minimum width.
     * @return Desired minimum width.
     */
    @Override
    protected int getSuggestedMinimumWidth() {
        return mPositionsListItem == null
            ? super.getSuggestedMinimumWidth()
            : Math.max(
                mPositionsListItem.level * mStripeWidth + Math.max(mPositionsListItem.level - 1, 0) * mStripeMargin,
                super.getSuggestedMinimumWidth());
    }

    /**
     * Draws stripes that designate hierarchy levels.
     * @param canvas Target canvas.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float top = getPaddingTop();
        float bottomPadded = getPaddingTop() + mActualStripeHeight;

        // Bottom of the stripe, if need to render a full height stripe
        float bottomFull = bottomPadded + getPaddingBottom();

        // We draw stripes from inside-out
        float end = mIsRightToLeft ? getPaddingEnd() : getWidth() - getPaddingEnd();

        PositionsListItemViewModel currentItem = mPositionsListItem;

        // If rendering first stripe
        boolean isSelf = true;

        // Determine if stripe for current item needs bottom padding
        boolean needsBottomPadding =
            mGroupItem == null // when it is not a group
            || !mGroupItem.isExpanded // or collapsed group
            || mGroupItem.children.isEmpty(); // or group with no children

        // Round top corners for the first stripe (self)
        mRectangleCorners[0] = mRectangleCorners[1] =
            mRectangleCorners[2] = mRectangleCorners[3] =
                mCornerRadius;

        while (currentItem != null && currentItem.level > 0) {
            int valueChangeDirection =
                currentItem.currentValue != null && currentItem.baseValue != null
                    ? currentItem.currentValue.compareTo(currentItem.baseValue)
                    : 0;

            if (needsBottomPadding) {
                // Round bottom corners, if adding padding
                mRectangleCorners[4] = mRectangleCorners[5]=
                    mRectangleCorners[6] = mRectangleCorners[7] =
                        mCornerRadius;
            }

            mRectangle.reset();
            mRectangle.addRoundRect(
                mIsRightToLeft ? end : end - mActualStripeWidth,
                isSelf ? top : 0, // Do not need top padding when rendering parents
                mIsRightToLeft ? end + mActualStripeWidth : end,
                needsBottomPadding ? bottomPadded : bottomFull,
                mRectangleCorners,
                Path.Direction.CW);

            canvas.drawPath(
                mRectangle,
                valueChangeDirection < 0
                    ? mNegativeChangePaint
                    : (valueChangeDirection > 0
                        ? mPositiveChangePaint
                        : mNoChangePaint));

            if (mIsRightToLeft) {
                end += mActualStripeWidth + mStripeMargin;
            } else {
                end -= mActualStripeWidth + mStripeMargin;
            }

            needsBottomPadding &= currentItem.lastInParent;
            currentItem = currentItem.parent;
            isSelf = false;

            // Reset rounded corners
            mRectangleCorners[0] = mRectangleCorners[1] = mRectangleCorners[2] = mRectangleCorners[3] =
                mRectangleCorners[4] = mRectangleCorners[5] = mRectangleCorners[6] = mRectangleCorners[7] = 0f;
        }
    }
}
