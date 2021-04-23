package com.example.ptsupport;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final int PHYISCAL_ACTIVITY = 1;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        //시작시 바로 센서리스너 불러옴, 메인 프래그먼트 띄움
        startService(new Intent(this, SensorListener.class));
        if (b == null) {
            Uifragment uifragment = new Uifragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.uiframe, uifragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, PHYISCAL_ACTIVITY);
        }

        setContentView(R.layout.activity_main);

        //액션바 감춤
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    public void settingsbutton(View view){
        Intent intent = new Intent(getApplicationContext(), result.class);
        startActivity(intent);
    }
}