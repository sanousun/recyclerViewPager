package com.mhc.libx

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

/**
 * @author dashu
 * @date 2018/11/4
 * 轮播图指示器
 */
class PagerIndicator @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), OnPageScrollListener {

    private var mIndicatorSize: Int = 0
    private var mIndicatorSpace: Int = 0
    private var mIndicatorColor: Int = 0

    private var mIndicatorCount = 0
    private var mIndicatorSelected = 0
    private var mIndicatorOffset = 0f

    private val mOvalPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mOvalRectF: RectF = RectF()
    private var mTempRect: Rect = Rect()

    init {
        context.obtainStyledAttributes(attrs, R.styleable.PagerIndicator).apply {
            mIndicatorSize = getDimensionPixelSize(R.styleable.PagerIndicator_pageIndicatorSize, dpToPixel(6f))
            val multiplier = getFloat(R.styleable.PagerIndicator_pageIndicatorSpacingMultiplier, 0.8f)
            mIndicatorSpace = (mIndicatorSize * multiplier + 0.5f).toInt()
            mIndicatorColor = getColor(R.styleable.PagerIndicator_pageIndicatorColor, Color.WHITE)
            recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        if (widthMode != MeasureSpec.EXACTLY) {
            widthSize = mIndicatorSize * mIndicatorCount + mIndicatorSpace * (mIndicatorCount - 1)
            widthSize = if (widthSize > 0) widthSize else 0
        }
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        if (heightMode != MeasureSpec.EXACTLY) {
            heightSize = mIndicatorSize
        }
        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mOvalPaint.color = mIndicatorColor
        mOvalPaint.alpha = 0x88
        val top = height / 2 - mIndicatorSize / 2
        val bottom = height / 2 + mIndicatorSize / 2
        for (i in 0 until mIndicatorCount) {
            val left = (mIndicatorSize + mIndicatorSpace) * i
            mTempRect.set(left, top, left + mIndicatorSize, bottom)
            mOvalRectF.set(mTempRect)
            canvas.drawOval(mOvalRectF, mOvalPaint)
        }
        mOvalPaint.alpha = 0xff
        val selectedLeft = if (mIndicatorSelected >= mIndicatorCount - 1) {
            if (mIndicatorOffset > 0.5f) {
                0
            } else {
                (mIndicatorSize + mIndicatorSpace) * (mIndicatorCount - 1)
            }
        } else {
            (mIndicatorSize + mIndicatorSpace) * (mIndicatorSelected + mIndicatorOffset).toInt()
        }
        mTempRect.set(selectedLeft, top, selectedLeft + mIndicatorSize, bottom)
        mOvalRectF.set(mTempRect)
        canvas.drawOval(mOvalRectF, mOvalPaint)
    }

    private fun dpToPixel(value: Float): Int {
        return (TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics) + 0.5f).toInt()
    }

    override fun onPageScrolled(totalItem: Int, position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        mIndicatorSelected = position
        mIndicatorOffset = positionOffset
        if (mIndicatorCount != totalItem) {
            mIndicatorCount = totalItem
            requestLayout()
        } else {
            invalidate()
        }
    }
}
