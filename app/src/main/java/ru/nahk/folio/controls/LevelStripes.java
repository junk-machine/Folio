package ru.nahk.folio.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import ru.nahk.folio.R;
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
     * with the provided context and XML attributes.
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
            mPositionsListItem != null && mPositionsListItem.level > 0
                ? Math.max((float) w / mPositionsListItem.level, 0)
                : 0;

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
        float bottom = getPaddingTop() + mActualStripeHeight;

        // We draw stripes from inside-out
        float end = mIsRightToLeft ? getPaddingEnd() : getWidth() - getPaddingEnd();

        PositionsListItemViewModel currentItem = mPositionsListItem;

        while (currentItem != null && currentItem.level > 0) {
            int valueChangeDirection =
                currentItem.currentValue != null && currentItem.baseValue != null
                    ? currentItem.currentValue.compareTo(currentItem.baseValue)
                    : 0;

            canvas.drawRect(
                mIsRightToLeft ? end : end - mActualStripeWidth, top,
                mIsRightToLeft ? end + mActualStripeWidth : end, bottom,
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

            currentItem = currentItem.parent;
        }
    }
}
