# RecyclerViewPager

通过 RecyclerView 的方式以更少的用户感知去实现基于 ViewPager 轮播图

## 注意
supportVersion > 25.1.0，这个版本之后才有的 PagerSnapHelper
minSdkVersion > 18，小于 18 感觉也没啥意义了

## 引入

```
repositories {
    maven { url 'https://dl.bintray.com/shenhui1876/maven' }
}

dependencies {
    ...
    implementation 'com.sanousun:recycler-view-pager:1.0.0@aar'
}
```

AndroidX 版本

```
repositories {
    maven { url 'https://dl.bintray.com/shenhui1876/maven' }
}

dependencies {
    ...
    implementation 'com.sanousun:recycler-view-pager-x:1.0.0@aar'
}
```

## 实现原理

PagerSnapHelper 可以让 RecyclerView 实现 ViewPager 的效果，通过参考 PagerSnapHelper 创建的 PagerIndicatorHelper 则可以实现 ViewPager.OnPageChangeListener 回调，有了这两个就可以完全模拟简单 ViewPager 的效果了

在实现 ViewPager 经常使用的轮播控件场景中，为了减少使用者的工作量，通过 PagerAdapter 去代理实现绝大部分的逻辑，使用方法只需如下

```java
DemoAdapter adapter = new DemoAdapter(this, data);
PagerAdapter pagerAdapter = new PagerAdapter(adapter);
// 循环
pagerAdapter.setCircle(true);
// 轮播
pagerAdapter.setCarousel(true);
// 所有轮播相关操作都由 PagerAdapter 实现
recyclerView.setAdapter(pagerAdapter);
```

同时 PagerAdapter 暴露了如下接口

```
public interface OnPageScrolledListener {
    void onPageScrolled(int totalItem, int position, float positionOffset, int positionOffsetPixels);
}
```
参数与 ViewPager 中 OnPageChangeListener 的 onPageScrolled 方法完全一致，可以方便实现对轮播指示器的实现和改造，lib 中也提供了一个简单的 PagerIndicator 实现，具体可以参考 sample/MainActivity


