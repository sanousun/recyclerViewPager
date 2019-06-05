package com.mhc.libx

/**
 * @author dashu
 * @date 2019-06-05
 * 页面滑动监听
 */
interface OnPageScrollListener {

    /**
     * item 滑动回调方法，参数等同于viewPager的回调函数
     *
     * @param totalItem            总个数
     * @param position             左侧展示的item position
     * @param positionOffset       滑动距离的百分比
     * @param positionOffsetPixels 滑动距离
     */
    fun onPageScrolled(totalItem: Int, position: Int, positionOffset: Float, positionOffsetPixels: Int)
}
