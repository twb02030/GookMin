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
import android.os.Bundle;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

public class MainActivity extends FragmentActivity {

    private HomeFragment homeFragment;
    private ChartFragment chartFragment;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        homeFragment = new HomeFragment();
        chartFragment = new ChartFragment();
        settingsFragment = new SettingsFragment();

        initFragment();

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