package com.mhc.libx

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.ViewGroup
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.*

/**
 * @author dashu
 * @date 2018/11/4
 * 轮播适配器，为了实现轮播效果以及最小程度的改造影响
 */
class PagerAdapter<VH : RecyclerView.ViewHolder>(
        private val mDelegateAdapter: RecyclerView.Adapter<VH>
) : RecyclerView.Adapter<VH>(), PagerIndicatorHelper.OnPageChangeListener {

    /**
     * 宿主RecyclerView，轮播时需要
     */
    private var mOwnerRecyclerView: RecyclerView? = null
    /**
     * 滑动状态，轮播时需要，处于不滑动时，自动轮播才会生效
     */
    private var mRecyclerState = RecyclerView.SCROLL_STATE_IDLE
    /**
     * 当前选择的位置，轮播时需要，定位下一个位置需要
     */
    private var mSelectPosition = RecyclerView.NO_POSITION
    /**
     * 帮助实现ViewPager的效果
     */
    private val mPagerSnapHelper: PagerSnapHelper
    /**
     * 仿造 PagerSnapHelper 实现对 Page 位置的监听
     */
    private val mPagerIndicatorHelper = PagerIndicatorHelper()
    private var mOnPageScrollListeners = ArrayList<OnPageScrollListener>()

    /**
     * 是否轮播
     */
    private var mCarousel = false
    private val mCarouselDuration = DEFAULT_CAROUSEL_DURATION
    private val mCarouselHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_CAROUSEL -> {
                    if (mOwnerRecyclerView == null) {
                        return
                    }
                    if (mRecyclerState == RecyclerView.SCROLL_STATE_IDLE) {
                        val targetPosition = mSelectPosition + 1
                        val totalPosition = itemCount
                        if (targetPosition == totalPosition) {
                            mOwnerRecyclerView!!.scrollToPosition(0)
                        } else {
                            mOwnerRecyclerView!!.smoothScrollToPosition(targetPosition)
                        }
                    }
                    this.sendEmptyMessageDelayed(MSG_CAROUSEL, mCarouselDuration.toLong())
                }
            }
        }
    }
    /**
     * 是否循环
     */
    private var mCircle = false
    private val mDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            notifyDataSetChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            notifyItemRangeChanged(positionStart, itemCount)
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            notifyItemRangeChanged(positionStart, itemCount, payload)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            notifyItemRangeInserted(positionStart, itemCount)
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            notifyItemRangeRemoved(positionStart, itemCount)
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            notifyItemMoved(fromPosition, toPosition)
        }
    }

    /**
     * 当循环滚动时，itemCount 返回的并不是真实的 itemCount
     *
     * @return 真实的 page 数量
     */
    val realItemCount: Int
        get() = mDelegateAdapter.itemCount

    /**
     * 设置滚动监听，可用于 PageIndicator 的展示
     * 方便改造现有基于 ViewPager 的轮播
     *
     * @param listener 滚动监听接口
     */
    fun addOnPageScrollListener(listener: OnPageScrollListener) {
        mOnPageScrollListeners.add(listener)
    }

    fun removeOnScrollListener(listener: OnPageScrollListener) {
        mOnPageScrollListeners.remove(listener)
    }

    fun clearOnScrollListeners() {
        mOnPageScrollListeners.clear()
    }

    init {
        mPagerIndicatorHelper.addPageChangeListener(this)
        mPagerSnapHelper = PagerSnapHelper()
    }

    /**
     * 设置自动轮播效果
     *
     * @param carousel 是否自动轮播
     */
    fun setCarousel(carousel: Boolean) {
        mCarousel = carousel
        if (mCarousel) {
            if (mOwnerRecyclerView != null) {
                mCarouselHandler.sendEmptyMessageDelayed(MSG_CAROUSEL, mCarouselDuration.toLong())
            }
        } else {
            mCarouselHandler.removeCallbacksAndMessages(null)
        }
    }

    /**
     * 设置页面循环效果
     *
     * @param circle 是否循环展示
     */
    fun setCircle(circle: Boolean) {
        mCircle = circle
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return mDelegateAdapter.createViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        mDelegateAdapter.onBindViewHolder(holder, getRealPosition(position))
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: List<Any>) {
        mDelegateAdapter.onBindViewHolder(holder, getRealPosition(position), payloads)
    }

    override fun getItemViewType(position: Int): Int {
        return mDelegateAdapter.getItemViewType(getRealPosition(position))
    }

    override fun getItemId(position: Int): Long {
        return mDelegateAdapter.getItemId(getRealPosition(position))
    }

    override fun getItemCount(): Int {
        return if (mCircle) Integer.MAX_VALUE else mDelegateAdapter.itemCount
    }

    /**
     * 当循环滚动时，position 并不是真实的 position
     *
     * @param position 基于此 Adapter 的位置
     * @return 真实的 page 位置
     */
    private fun getRealPosition(position: Int): Int {
        return if (mCircle) position % realItemCount else position
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mPagerIndicatorHelper.attachToRecyclerView(recyclerView)
        mPagerSnapHelper.attachToRecyclerView(recyclerView)
        // 循环需要设置一个大整数，可以无缝循环
        if (mCircle) {
            recyclerView.scrollToPosition(Integer.MAX_VALUE / realItemCount / 2 * realItemCount)
        }
        // 这里需要注册观察者来接受代理 adapter 的事件
        mDelegateAdapter.registerAdapterDataObserver(mDataObserver)
        // 用于轮播
        mOwnerRecyclerView = recyclerView
        if (mCarousel) {
            mCarouselHandler.sendEmptyMessageDelayed(MSG_CAROUSEL, mCarouselDuration.toLong())
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        if (mCarousel) {
            mCarouselHandler.removeCallbacksAndMessages(null)
        }
        mOwnerRecyclerView = null
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        for (listener in mOnPageScrollListeners) {
            listener.onPageScrolled(
                    realItemCount,
                    getRealPosition(position),
                    positionOffset,
                    positionOffsetPixels)
        }
    }

    override fun onPageSelected(position: Int) {
        mSelectPosition = position
    }

    override fun onPageScrollStateChanged(state: Int) {
        mRecyclerState = state
    }

    companion object {

        private const val MSG_CAROUSEL = 1
        private const val DEFAULT_CAROUSEL_DURATION = 5000
    }
}
