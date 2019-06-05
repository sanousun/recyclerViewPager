package com.mhc.libx

import android.view.View
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import com.mhc.libx.PagerIndicatorHelper.OnPageChangeListener
import java.util.*

/**
 * @author dashu
 * @date 2018/11/4
 * 仿照 [androidx.recyclerview.widget.PagerSnapHelper] 写的滑动回调，
 * 对外提供接口回调 [OnPageChangeListener]
 */
class PagerIndicatorHelper {

    private var mRecyclerView: RecyclerView? = null
    private var mVerticalHelper: OrientationHelper? = null
    private var mHorizontalHelper: OrientationHelper? = null
    private var mSelectedPage = RecyclerView.NO_POSITION
    private val mPageChangeListeners = ArrayList<OnPageChangeListener>()

    private val mScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            dispatchOnPageScrolled()
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            for (listener in mPageChangeListeners) {
                listener.onPageScrollStateChanged(newState)
            }
        }
    }

    fun attachToRecyclerView(recyclerView: RecyclerView) {
        if (mRecyclerView === recyclerView) {
            return
        }
        if (mRecyclerView != null) {
            destroyCallbacks()
        }
        mRecyclerView = recyclerView
        if (mRecyclerView != null) {
            setupCallbacks()
            dispatchOnPageScrolled()
        }
    }

    private fun dispatchOnPageScrolled() {
        if (mRecyclerView == null) {
            return
        }
        val layoutManager = mRecyclerView!!.layoutManager ?: return
        val centerView = findSnapCenterView(layoutManager) ?: return
        val centerPosition = layoutManager.getPosition(centerView)
        if (mSelectedPage != centerPosition) {
            for (listener in mPageChangeListeners) {
                listener.onPageSelected(centerPosition)
            }
            mSelectedPage = centerPosition
        }
        val startView = findSnapStartView(layoutManager) ?: return
        val startPosition = layoutManager.getPosition(startView)
        val startOffset = calculateOffsetToSnap(layoutManager, startView)
        for (listener in mPageChangeListeners) {
            listener.onPageScrolled(startPosition, startOffset.positionOffset, startOffset.positionOffsetPixels)
        }
    }

    private fun findSnapCenterView(layoutManager: RecyclerView.LayoutManager): View? {
        if (layoutManager.canScrollVertically()) {
            return findCenterView(layoutManager, getVerticalHelper(layoutManager))
        } else if (layoutManager.canScrollHorizontally()) {
            return findCenterView(layoutManager, getHorizontalHelper(layoutManager))
        }
        return null
    }

    private fun findCenterView(layoutManager: RecyclerView.LayoutManager,
                               helper: OrientationHelper): View? {
        val childCount = layoutManager.childCount
        if (childCount == 0) {
            return null
        }

        var closestChild: View? = null
        val center = if (layoutManager.clipToPadding) {
            helper.startAfterPadding + helper.totalSpace / 2
        } else {
            helper.end / 2
        }
        var absClosest = Integer.MAX_VALUE

        for (i in 0 until childCount) {
            val child = layoutManager.getChildAt(i)
            val childCenter = helper.getDecoratedStart(child) + helper.getDecoratedMeasurement(child) / 2
            val absDistance = Math.abs(childCenter - center)
            if (absDistance < absClosest) {
                absClosest = absDistance
                closestChild = child
            }
        }
        return closestChild
    }

    private fun findSnapStartView(layoutManager: RecyclerView.LayoutManager): View? {
        if (layoutManager.canScrollVertically()) {
            return findStartView(layoutManager, getVerticalHelper(layoutManager))
        } else if (layoutManager.canScrollHorizontally()) {
            return findStartView(layoutManager, getHorizontalHelper(layoutManager))
        }
        return null
    }

    private fun findStartView(layoutManager: RecyclerView.LayoutManager,
                              helper: OrientationHelper): View? {
        val childCount = layoutManager.childCount
        if (childCount == 0) {
            return null
        }

        var closestChild: View? = null
        var starTest = Integer.MAX_VALUE

        for (i in 0 until childCount) {
            val child = layoutManager.getChildAt(i)
            val childStart = helper.getDecoratedStart(child)

            if (childStart < starTest) {
                starTest = childStart
                closestChild = child
            }
        }
        return closestChild
    }

    private fun calculateOffsetToSnap(layoutManager: RecyclerView.LayoutManager,
                                      targetView: View): PositionOffset {
        if (layoutManager.canScrollHorizontally()) {
            return offsetToStart(layoutManager, targetView,
                    getHorizontalHelper(layoutManager))
        }

        return if (layoutManager.canScrollVertically()) {
            offsetToStart(layoutManager, targetView,
                    getVerticalHelper(layoutManager))
        } else PositionOffset()
    }

    private fun offsetToStart(layoutManager: RecyclerView.LayoutManager,
                              targetView: View, helper: OrientationHelper): PositionOffset {
        val childStart = helper.getDecoratedStart(targetView)
        val containerStart: Int
        val containerSpace: Int
        if (layoutManager.clipToPadding) {
            containerStart = helper.startAfterPadding
            containerSpace = helper.totalSpace
        } else {
            containerStart = 0
            containerSpace = helper.end
        }
        val positionOffset = PositionOffset()
        positionOffset.positionOffset = (containerStart - childStart) * 1f / containerSpace
        positionOffset.positionOffsetPixels = containerStart - childStart
        return positionOffset
    }

    private fun getVerticalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (mVerticalHelper == null || mVerticalHelper!!.layoutManager !== layoutManager) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager)
        }
        return mVerticalHelper!!
    }

    private fun getHorizontalHelper(
            layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (mHorizontalHelper == null || mHorizontalHelper!!.layoutManager !== layoutManager) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
        }
        return mHorizontalHelper!!
    }

    private fun setupCallbacks() {
        mRecyclerView?.addOnScrollListener(mScrollListener)
    }

    private fun destroyCallbacks() {
        mRecyclerView?.removeOnScrollListener(mScrollListener)
    }

    fun addPageChangeListener(listener: OnPageChangeListener) {
        mPageChangeListeners.add(listener)
    }

    fun removePageChangeListener(listener: OnPageChangeListener) {
        mPageChangeListeners.remove(listener)
    }

    interface OnPageChangeListener {
        fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int)

        fun onPageSelected(position: Int)

        fun onPageScrollStateChanged(state: Int)
    }

    private inner class PositionOffset {
        internal var positionOffset: Float = 0.toFloat()
        internal var positionOffsetPixels: Int = 0
    }
}
