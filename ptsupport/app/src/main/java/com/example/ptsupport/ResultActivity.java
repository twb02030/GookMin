package com.example.ptsupport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import static com.example.ptsupport.HomeFragment.DEFAULT_GOAL;
import static com.example.ptsupport.HomeFragment.formatter;

public class ResultActivity extends AppCompatActivity {

    TextView finalSteps, finalRkm, finalRpercent, finalRkcal;
    int Rsteps_today, RtodayOffset, RSince_boot;


    private Button retryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        //액션바 감추기
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //findViewById

        finalSteps = findViewById(R.id.Result_chart_steps);
        finalRkm = findViewById(R.id.Result_km);
        finalRpercent = findViewById(R.id.Result_per);
        finalRkcal = findViewById(R.id.Result_kcal);

        Database rdb = Database.getInstance(this);
        RtodayOffset = rdb.getSteps(Util.getToday());
        RSince_boot = rdb.getCurrentSteps();
        rdb.close();
        Rsteps_today = Math.max(RtodayOffset + RSince_boot, 0);
        //Rsteps_today = 1000;

        //텍스트
        double percent = Rsteps_today * 100 / DEFAULT_GOAL;
        double kmcount = Rsteps_today * 70 * 0.000001;
        double kcalcount = Rsteps_today * 70 * 0.00001 * 40;
        finalSteps.setText(formatter.format(Rsteps_today));
        finalRkm.setText(String.format("%.2f",kmcount)+"km");
        finalRpercent.setText(percent+"%");
        finalRkcal.setText(String.format("%.2f",kcalcount)+"kcal");

        //메인 화면으로 돌아가기
        Button returnbutton = (Button) findViewById(R.id.returnbutton);
        returnbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        Button retryButton = (Button) findViewById(R.id.retry_button);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

}
