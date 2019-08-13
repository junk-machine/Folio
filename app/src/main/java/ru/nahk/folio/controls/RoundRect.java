package ru.nahk.folio.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * View with rounded corners.
 */
public class RoundRect extends View {
    /**
     * Width of the view.
     */
    private int mWidth;

    /**
     * Height of the view.
     */
    private int mHeight;

    /**
     * Corner radius for the rounded rectangle.
     */
    private float mCornerRadius;

    /**
     * Background paint.
     */
    private Paint mBackgroundPaint;

    /**
     * Creates new instance of the {@link RoundRect} class
     * with the provided context.
     * @param context Parent activity context.
     */
    public RoundRect(Context context) {
        super(context);
        init();
    }

    /**
     * Creates new instance of the {@link RoundRect} class
     * with the provided context and XML attributes.
     * @param context Parent activity context.
     * @param attrs XML attributes.
     */
    public RoundRect(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Creates new instance of the {@link RoundRect} class
     * with the provided context, XML attributes and default style.
     * @param context Parent activity context.
     * @param attrs XML attributes.
     * @param defStyleAttr Default style attributes.
     */
    public RoundRect(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Initializes all fields with default values.
     */
    private void init() {
        mBackgroundPaint = new Paint();
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

        mWidth = Math.max(0, w - getPaddingLeft() - getPaddingRight());
        mHeight = Math.max(0, h - getPaddingTop() - getPaddingBottom());
        mCornerRadius = mWidth / 2f;
    }

    /**
     * Sets background color for the view.
     * @param color New background color.
     */
    @Override
    public void setBackgroundColor(@ColorInt int color) {
        mBackgroundPaint.setColor(color);
        // Do not set it in base class.
        // Background is rendered in draw(..), rather than onDraw(..).
        // In order to prevent it from rendering, we want to keep its color as transparent,
        // then our custom shape will be rendered properly.
    }

    /**
     * Resets background color to transparent and performs basic rendering.
     * @param canvas Target canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        // Make sure there no background will be rendered by the base class
        super.setBackgroundColor(Color.TRANSPARENT);
        super.draw(canvas);
    }

    /**
     * Renders rectangle with rounded corners.
     * @param canvas Target canvas.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRoundRect(
            getPaddingLeft(),
            getPaddingTop(),
            mWidth,
            mHeight,
            mCornerRadius,
            mCornerRadius,
            mBackgroundPaint);
    }
}
