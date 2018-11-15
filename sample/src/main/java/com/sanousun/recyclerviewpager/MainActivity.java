package com.sanousun.recyclerviewpager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.sanousun.lib.PagerAdapter;
import com.sanousun.lib.PagerIndicator;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    PagerAdapter pagerAdapter;
    PagerIndicator pagerIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycler);
        pagerIndicator = findViewById(R.id.page_indicator);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            data.add("" + i);
        }
        DemoAdapter adapter = new DemoAdapter(this, data);
        pagerAdapter = new PagerAdapter(adapter);
        pagerAdapter.setCircle(true);
        pagerAdapter.setCarousel(true);
        pagerIndicator.setIndicatorCount(pagerAdapter.getRealItemCount());
        pagerAdapter.setOnPageScrolledListener(new PagerAdapter.OnPageScrolledListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                pagerIndicator.setIndicatorScrolled(position, positionOffset);
            }
        });
        recyclerView.setAdapter(pagerAdapter);
        CheckBox circle = findViewById(R.id.circle);
        CheckBox carousel = findViewById(R.id.carousel);
        circle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pagerAdapter.setCircle(isChecked);
            }
        });
        carousel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pagerAdapter.setCarousel(isChecked);
            }
        });
    }
}
