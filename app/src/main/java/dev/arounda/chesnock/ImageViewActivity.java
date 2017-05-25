package dev.arounda.chesnock;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;

import Adapter.CustomPagerAdapter;

public class ImageViewActivity extends FragmentActivity {
CustomPagerAdapter mCustomPagerAdapter;


    ArrayList<String> imgList = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        imgList =  getIntent().getExtras().getStringArrayList("urls");

        mCustomPagerAdapter = new CustomPagerAdapter(ImageViewActivity.this, imgList);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mCustomPagerAdapter);

    }

}
