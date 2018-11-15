package com.sanousun.lib;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

/**
 * @author dashu
 * @date 2018/11/4
 * 轮播适配器，为了实现轮播效果以及最小程度的改造影响
 */
public class PagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements PagerIndicatorHelper.OnPageChangeListener {

    private static final int MSG_CAROUSEL = 1;
    private static final int DEFAULT_CAROUSEL_DURATION = 5000;

    /**
     * 宿主RecyclerView，轮播时需要
     */
    private RecyclerView mOwnerRecyclerView;
    /**
     * 滑动状态，轮播时需要，处于不滑动时，自动轮播才会生效
     */
    private int mRecyclerState = RecyclerView.SCROLL_STATE_IDLE;
    /**
     * 当前选择的位置，轮播时需要，定位下一个位置需要
     */
    private int mSelectPosition = RecyclerView.NO_POSITION;
    /**
     * 帮助实现ViewPager的效果
     */
    private PagerSnapHelper mPagerSnapHelper;
    /**
     * 仿造 PagerSnapHelper 实现对 Page 位置的监听
     */
    private PagerIndicatorHelper mPagerIndicatorHelper;
    private OnPageScrolledListener mOnPageScrolledListener;
    /**
     * 是否轮播
     */
    private boolean mCarousel = false;
    private int mCarouselDuration = DEFAULT_CAROUSEL_DURATION;
    private Handler mCarouselHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_CAROUSEL:
                    if (mOwnerRecyclerView == null) {
                        return;
                    }
                    if (mRecyclerState == RecyclerView.SCROLL_STATE_IDLE) {
                        int targetPosition = mSelectPosition + 1;
                        int totalPosition = getItemCount();
                        if (targetPosition == totalPosition) {
                            mOwnerRecyclerView.scrollToPosition(0);
                        } else {
                            mOwnerRecyclerView.smoothScrollToPosition(targetPosition);
                        }
                    }
                    mCarouselHandler.sendEmptyMessageDelayed(MSG_CAROUSEL, mCarouselDuration);
                    break;
                default:
            }
        }
    };
    /**
     * 是否循环
     */
    private boolean mCircle = false;
    private RecyclerView.Adapter mDelegateAdapter;
    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            notifyItemMoved(fromPosition, toPosition);
        }
    };

    public PagerAdapter(RecyclerView.Adapter adapter) {
        mDelegateAdapter = adapter;
        mPagerIndicatorHelper = new PagerIndicatorHelper();
        mPagerIndicatorHelper.addPageChangeListener(this);
        mPagerSnapHelper = new PagerSnapHelper();
    }

    /**
     * 设置自动轮播效果
     *
     * @param carousel 是否自动轮播
     */
    public void setCarousel(boolean carousel) {
        mCarousel = carousel;
        if (mCarousel) {
            if (mOwnerRecyclerView != null) {
                mCarouselHandler.sendEmptyMessageDelayed(MSG_CAROUSEL, mCarouselDuration);
            }
        } else {
            mCarouselHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * 设置页面循环效果
     *
     * @param circle 是否循环展示
     */
    public void setCircle(boolean circle) {
        mCircle = circle;
        notifyDataSetChanged();
    }

    /**
     * 设置滚动监听，可用于 PageIndicator 的展示
     * 参数和{@link android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrolled(int, float, int)} 一致，
     * 方便改造现有基于 ViewPager 的轮播
     *
     * @param onPageScrolledListener 滚动监听接口
     */
    public void setOnPageScrolledListener(OnPageScrolledListener onPageScrolledListener) {
        mOnPageScrolledListener = onPageScrolledListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return mDelegateAdapter.createViewHolder(parent, viewType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        mDelegateAdapter.onBindViewHolder(holder, getRealPosition(position));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        mDelegateAdapter.onBindViewHolder(holder, getRealPosition(position), payloads);
    }

    @Override
    public int getItemViewType(int position) {
        return mDelegateAdapter.getItemViewType(getRealPosition(position));
    }

    @Override
    public long getItemId(int position) {
        return mDelegateAdapter.getItemId(getRealPosition(position));
    }

    @Override
    public int getItemCount() {
        return mCircle ? Integer.MAX_VALUE : mDelegateAdapter.getItemCount();
    }

    /**
     * 当循环滚动时，itemCount 返回的并不是真实的 itemCount
     *
     * @return 真实的 page 数量
     */
    public int getRealItemCount() {
        return mDelegateAdapter.getItemCount();
    }

    /**
     * 当循环滚动时，position 并不是真实的 position
     *
     * @param position 基于此 Adapter 的位置
     * @return 真实的 page 位置
     */
    private int getRealPosition(int position) {
        return mCircle ? position % getRealItemCount() : position;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mPagerIndicatorHelper.attachToRecyclerView(recyclerView);
        mPagerSnapHelper.attachToRecyclerView(recyclerView);
        // 循环需要设置一个大整数，可以无缝循环
        if (mCircle) {
            recyclerView.scrollToPosition(Integer.MAX_VALUE / getRealItemCount() / 2 * getRealItemCount());
        }
        // 这里需要注册观察者来接受代理 adapter 的事件
        mDelegateAdapter.registerAdapterDataObserver(mDataObserver);
        // 用于轮播
        mOwnerRecyclerView = recyclerView;
        if (mCarousel) {
            mCarouselHandler.sendEmptyMessageDelayed(MSG_CAROUSEL, mCarouselDuration);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (mCarousel) {
            mCarouselHandler.removeCallbacksAndMessages(null);
        }
        mOwnerRecyclerView = null;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mOnPageScrolledListener != null) {
            mOnPageScrolledListener.onPageScrolled(getRealPosition(position), positionOffset, positionOffsetPixels);
        }
    }

    @Override
    public void onPageSelected(int position) {
        mSelectPosition = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        mRecyclerState = state;
    }

    public interface OnPageScrolledListener {
        /**
         * item 滑动回调方法，参数等同于viewPager的回调函数
         *
         * @param position             左侧展示的item position
         * @param positionOffset       滑动距离的百分比
         * @param positionOffsetPixels 滑动距离
         */
        void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);
    }
}
