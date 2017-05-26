package dev.arounda.chesnock;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;

import Adapter.CustomPagerAdapter;
import me.relex.circleindicator.CircleIndicator;

public class ImageViewActivity extends AppCompatActivity {
CustomPagerAdapter mCustomPagerAdapter;
    CircleIndicator indicator;


    ArrayList<String> imgList = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        indicator =(CircleIndicator)findViewById(R.id.indicator);
        imgList =  getIntent().getExtras().getStringArrayList("urls");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar menu = getSupportActionBar();

        menu.setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mCustomPagerAdapter = new CustomPagerAdapter(ImageViewActivity.this, imgList);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mCustomPagerAdapter);
        indicator.setViewPager(mViewPager);

    }


}
