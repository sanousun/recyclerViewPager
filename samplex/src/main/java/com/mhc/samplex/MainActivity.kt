package com.mhc.samplex

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mhc.libx.PagerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private var pagerAdapter: PagerAdapter<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val data = ArrayList<String>()
        for (i in 0..9) {
            data.add("" + i)
        }
        val adapter = DemoAdapter(this, data)
        pagerAdapter = PagerAdapter(adapter)
        pagerAdapter?.setCircle(true)
        pagerAdapter?.setCarousel(true)
        pagerAdapter?.addOnPageScrollListener(page_indicator)
        recycler.adapter = pagerAdapter
        circle.setOnCheckedChangeListener { _, isChecked -> pagerAdapter?.setCircle(isChecked) }
        carousel.setOnCheckedChangeListener { _, isChecked -> pagerAdapter?.setCarousel(isChecked) }
    }
}
