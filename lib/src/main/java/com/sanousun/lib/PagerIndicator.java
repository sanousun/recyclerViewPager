package com.sanousun.lib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * @author dashu
 * @date 2018/11/4
 * 轮播图指示器
 */
public class PagerIndicator extends View {

    private int mIndicatorSize;
    private int mIndicatorSpace;
    private int mIndicatorColor;

    private int mIndicatorCount = 0;
    private int mIndicatorSelected = 0;
    private float mIndicatorOffset = 0f;

    private Paint mOvalPaint;
    private RectF mOvalRectF;

    public PagerIndicator(Context context) {
        this(context, null);
    }

    public PagerIndicator(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagerIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PagerIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PagerIndicator);
        mIndicatorSize = typedArray.getDimensionPixelSize(R.styleable.PagerIndicator_pageIndicatorSize, dpToPixel(6f));
        float multiplier = typedArray.getFloat(R.styleable.PagerIndicator_pageIndicatorSpacingMultiplier, 0.8f);
        mIndicatorSpace = (int) (mIndicatorSize * multiplier + 0.5f);
        mIndicatorColor = typedArray.getColor(R.styleable.PagerIndicator_pageIndicatorColor, Color.WHITE);
        typedArray.recycle();
        mOvalPaint = new Paint();
        mOvalPaint.setAntiAlias(true);
        mOvalRectF = new RectF();
    }

    public void setIndicatorCount(int indicatorCount) {
        mIndicatorCount = indicatorCount;
        requestLayout();
    }

    public void setIndicatorScrolled(int indicatorSelected, float indicatorOffset) {
        mIndicatorSelected = indicatorSelected;
        mIndicatorOffset = indicatorOffset;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            widthSize = mIndicatorSize * mIndicatorCount + mIndicatorSpace * (mIndicatorCount - 1);
            widthSize = widthSize > 0 ? widthSize : 0;
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
            heightSize = mIndicatorSize;
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mOvalPaint.setColor(mIndicatorColor);
        mOvalPaint.setAlpha(125);
        int top = getHeight() / 2 - mIndicatorSize / 2;
        int bottom = getHeight() / 2 + mIndicatorSize / 2;
        for (int i = 0; i < mIndicatorCount; i++) {
            int left = (mIndicatorSize + mIndicatorSpace) * i;
            mOvalRectF.set(left, top, left + mIndicatorSize, bottom);
            canvas.drawOval(mOvalRectF, mOvalPaint);
        }
        mOvalPaint.setAlpha(255);
        float selectedLeft;
        if (mIndicatorSelected >= mIndicatorCount - 1) {
            if (mIndicatorOffset > 0.5f) {
                selectedLeft = 0;
            } else {
                selectedLeft = (mIndicatorSize + mIndicatorSpace) * (mIndicatorCount - 1);
            }
        } else {
            selectedLeft = (mIndicatorSize + mIndicatorSpace) * (mIndicatorSelected + mIndicatorOffset);
        }
        mOvalRectF.set(selectedLeft, top, selectedLeft + mIndicatorSize, bottom);
        canvas.drawOval(mOvalRectF, mOvalPaint);
    }

    private int dpToPixel(float value) {
        return (int) (TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()) + 0.5f);
    }
}
