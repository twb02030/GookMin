package com.example.ptsupport;

import androidx.annotation.IdRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

public class MainActivity extends FragmentActivity {

    FrameLayout popup;
    FrameLayout f_main;
    TextView skipText;

    private Fragment homeFragment, chartFragment, settingsFragment;
    private ViewPager2 sliderViewPager; //recyclerView를 기반으로 만들어진 위젯
    private LinearLayout layoutIndicator; //이미지가 변경될 때 위치 보여주는 indicator

    //이미지가 들어갈 배열
    private int[] images = new int[]{
            R.drawable.ac_pop_img00, R.drawable.ac_pop_img01, R.drawable.ac_pop__img02, R.drawable.ac_pop_img03,
            R.drawable.ac_pop_img04, R.drawable.ac_pop_img05, R.drawable.ac_pop_img06, R.drawable.ac_pop_img07,
            R.drawable.ac_pop_img08, R.drawable.ac_pop__img09, R.drawable.ac_pop_img10, R.drawable.ac_pop_img11,
            R.drawable.ac_pop_img12
    };

    private static final int PHYISCAL_ACTIVITY = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //팝업창과 메인창(상단바)
        popup = (FrameLayout) findViewById(R.id.frame_popup);
        popup.setVisibility(View.VISIBLE);
        f_main = (FrameLayout) findViewById(R.id.contentContainer);
        f_main.setVisibility(View.INVISIBLE);

        //슬라이더될 이미지가 담길 공간, 하단 indicator 레이아웃
        sliderViewPager = findViewById(R.id.sliderViewPager);
        layoutIndicator = findViewById(R.id.layoutIndicators);

        sliderViewPager.setOffscreenPageLimit(1);
        sliderViewPager.setAdapter(new ImageSliderAdapter(this, images));

        sliderViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentIndicator(position);
            }
        });

        setupIndicators(images.length);


        //건너뛰기 글자 누를 시
        skipText = (TextView) findViewById(R.id.skipText) ;
        skipText.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                f_main.setVisibility(View.VISIBLE);
                popup.setVisibility(View.INVISIBLE);
                skipText.setVisibility(View.INVISIBLE);
            }

        }) ;

        
        //각 프래그먼트
        homeFragment = new HomeFragment();
        chartFragment = new ChartFragment();
        settingsFragment = new SettingsFragment();

        initFragment();

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, PHYISCAL_ACTIVITY);
        }

        //상단바 누르면 이동
        BottomBar topBar = (BottomBar) findViewById(R.id.topBar);
        topBar.setOnTabSelectListener(new OnTabSelectListener(){
            @Override

            public void onTabSelected(@IdRes int tabId){
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                if(tabId == R.id.tab_run){
                    transaction.replace(R.id.contentContainer, homeFragment).commit();
                }

                else if (tabId == R.id.tab_chart){
                    transaction.replace(R.id.contentContainer, chartFragment).commit();
                }

                else if (tabId == R.id.tab_setting){
                    transaction.replace(R.id.contentContainer, settingsFragment).commit();
                }
            }
        });
    }


    //이미지의 수만큼 imageView 생성, LinearLayout에 addView를 통해 추가
    private void setupIndicators(int count) {
        ImageView[] indicators = new ImageView[count];
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        params.setMargins(16, 8, 16, 8);

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(this);
            indicators[i].setImageDrawable(ContextCompat.getDrawable(this,
                    R.drawable.bg_indicator_inactive));
            indicators[i].setLayoutParams(params);
            layoutIndicator.addView(indicators[i]);
        }
        setCurrentIndicator(0);
    }


    //함수 해당 위치를 전달해 indicator가 변경될 수 있게
    private void setCurrentIndicator(int position) {
        int childCount = layoutIndicator.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) layoutIndicator.getChildAt(i);
            if (i == position) {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        this,
                        R.drawable.bg_indicator_active
                ));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        this,
                        R.drawable.bg_indicator_inactive
                ));
            }
        }
    }

    public void initFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.contentContainer, homeFragment);
        //transaction.addToBackStack(null);
        transaction.commit();
    }

}