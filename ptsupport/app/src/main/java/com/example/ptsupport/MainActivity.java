package com.example.ptsupport;

import androidx.annotation.IdRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

public class MainActivity extends FragmentActivity {

    private HomeFragment homeFragment;
    private ChartFragment chartFragment;
    private SettingsFragment settingsFragment;

    private static final int PHYISCAL_ACTIVITY = 1;

    private AdView bannerAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        homeFragment = new HomeFragment();
        chartFragment = new ChartFragment();
        settingsFragment = new SettingsFragment();

        initFragment();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        bannerAdView = findViewById(R.id.bannerad);
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdView.loadAd(adRequest);
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-9372890258143372/9476296850");

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, PHYISCAL_ACTIVITY);
        }

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

    public void initFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.contentContainer, homeFragment);
        //transaction.addToBackStack(null);
        transaction.commit();
    }

}