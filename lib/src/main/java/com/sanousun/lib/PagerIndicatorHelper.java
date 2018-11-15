package com.sanousun.lib;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dashu
 * @date 2018/11/4
 * 仿照 {@link android.support.v7.widget.PagerSnapHelper} 写的滑动回调，
 * 对外提供接口回调，格式参照 {@link android.support.v4.view.ViewPager.OnPageChangeListener}
 */
public class PagerIndicatorHelper {

    private RecyclerView mRecyclerView;
    @Nullable
    private OrientationHelper mVerticalHelper;
    @Nullable
    private OrientationHelper mHorizontalHelper;
    private int mSelectedPage = RecyclerView.NO_POSITION;
    private List<OnPageChangeListener> mPageChangeListeners;

    private final RecyclerView.OnScrollListener mScrollListener =
            new RecyclerView.OnScrollListener() {

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    dispatchOnPageScrolled();
                }

                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    for (OnPageChangeListener listener : getPageListeners()) {
                        listener.onPageScrollStateChanged(newState);
                    }
                }
            };

    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) {
        if (mRecyclerView == recyclerView) {
            return; // nothing to do
        }
        if (mRecyclerView != null) {
            destroyCallbacks();
        }
        mRecyclerView = recyclerView;
        if (mRecyclerView != null) {
            setupCallbacks();
            dispatchOnPageScrolled();
        }
    }

    private void dispatchOnPageScrolled() {
        if (mRecyclerView == null) {
            return;
        }
        RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        if (layoutManager == null) {
            return;
        }
        View centerView = findSnapCenterView(layoutManager);
        if (centerView == null) {
            return;
        }
        int centerPosition = layoutManager.getPosition(centerView);
        if (mSelectedPage != centerPosition) {
            for (OnPageChangeListener listener : getPageListeners()) {
                listener.onPageSelected(centerPosition);
            }
            mSelectedPage = centerPosition;
        }
        View startView = findSnapStartView(layoutManager);
        if (startView == null) {
            return;
        }
        int startPosition = layoutManager.getPosition(startView);
        PositionOffset startOffset = calculateOffsetToSnap(layoutManager, startView);
        for (OnPageChangeListener listener : getPageListeners()) {
            listener.onPageScrolled(startPosition, startOffset.positionOffset, startOffset.positionOffsetPixels);
        }
    }

    private View findSnapCenterView(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager.canScrollVertically()) {
            return findCenterView(layoutManager, getVerticalHelper(layoutManager));
        } else if (layoutManager.canScrollHorizontally()) {
            return findCenterView(layoutManager, getHorizontalHelper(layoutManager));
        }
        return null;
    }

    private View findCenterView(RecyclerView.LayoutManager layoutManager,
                                OrientationHelper helper) {
        int childCount = layoutManager.getChildCount();
        if (childCount == 0) {
            return null;
        }

        View closestChild = null;
        final int center;
        if (layoutManager.getClipToPadding()) {
            center = helper.getStartAfterPadding() + helper.getTotalSpace() / 2;
        } else {
            center = helper.getEnd() / 2;
        }
        int absClosest = Integer.MAX_VALUE;

        for (int i = 0; i < childCount; i++) {
            final View child = layoutManager.getChildAt(i);
            int childCenter = helper.getDecoratedStart(child)
                    + (helper.getDecoratedMeasurement(child) / 2);
            int absDistance = Math.abs(childCenter - center);
            if (absDistance < absClosest) {
                absClosest = absDistance;
                closestChild = child;
            }
        }
        return closestChild;
    }

    private View findSnapStartView(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager.canScrollVertically()) {
            return findStartView(layoutManager, getVerticalHelper(layoutManager));
        } else if (layoutManager.canScrollHorizontally()) {
            return findStartView(layoutManager, getHorizontalHelper(layoutManager));
        }
        return null;
    }

    private View findStartView(RecyclerView.LayoutManager layoutManager,
                               OrientationHelper helper) {
        int childCount = layoutManager.getChildCount();
        if (childCount == 0) {
            return null;
        }

        View closestChild = null;
        int startest = Integer.MAX_VALUE;

        for (int i = 0; i < childCount; i++) {
            final View child = layoutManager.getChildAt(i);
            int childStart = helper.getDecoratedStart(child);

            if (childStart < startest) {
                startest = childStart;
                closestChild = child;
            }
        }
        return closestChild;
    }

    private PositionOffset calculateOffsetToSnap(@NonNull RecyclerView.LayoutManager layoutManager,
                                                 @NonNull View targetView) {
        if (layoutManager.canScrollHorizontally()) {
            return offsetToStart(layoutManager, targetView,
                    getHorizontalHelper(layoutManager));
        }

        if (layoutManager.canScrollVertically()) {
            return offsetToStart(layoutManager, targetView,
                    getVerticalHelper(layoutManager));
        }
        return new PositionOffset();
    }

    private PositionOffset offsetToStart(@NonNull RecyclerView.LayoutManager layoutManager,
                                         @NonNull View targetView, OrientationHelper helper) {
        final int childStart = helper.getDecoratedStart(targetView);
        final int containerStart, containerSpace;
        if (layoutManager.getClipToPadding()) {
            containerStart = helper.getStartAfterPadding();
            containerSpace = helper.getTotalSpace();
        } else {
            containerStart = 0;
            containerSpace = helper.getEnd();
        }
        PositionOffset positionOffset = new PositionOffset();
        positionOffset.positionOffset = (containerStart - childStart) * 1f / containerSpace;
        positionOffset.positionOffsetPixels = containerStart - childStart;
        return positionOffset;
    }

    @NonNull
    private OrientationHelper getVerticalHelper(@NonNull RecyclerView.LayoutManager layoutManager) {
        if (mVerticalHelper == null || mVerticalHelper.getLayoutManager() != layoutManager) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager);
        }
        return mVerticalHelper;
    }

    @NonNull
    private OrientationHelper getHorizontalHelper(
            @NonNull RecyclerView.LayoutManager layoutManager) {
        if (mHorizontalHelper == null || mHorizontalHelper.getLayoutManager() != layoutManager) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
        }
        return mHorizontalHelper;
    }

    private void setupCallbacks() {
        mRecyclerView.addOnScrollListener(mScrollListener);
    }

    private void destroyCallbacks() {
        mRecyclerView.removeOnScrollListener(mScrollListener);
    }

    public void addPageChangeListener(OnPageChangeListener listener) {
        getPageListeners().add(listener);
    }

    public void removePageChangeListener(OnPageChangeListener listener) {
        getPageListeners().remove(listener);
    }

    private List<OnPageChangeListener> getPageListeners() {
        if (mPageChangeListeners == null) {
            mPageChangeListeners = new ArrayList<>();
        }
        return mPageChangeListeners;
    }

    public interface OnPageChangeListener {
        void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

        void onPageSelected(int position);

        void onPageScrollStateChanged(int state);
    }

    private class PositionOffset {
        float positionOffset;
        int positionOffsetPixels;
    }
}
