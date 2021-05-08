package com.example.ptsupport;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import static com.example.ptsupport.HomeFragment.DEFAULT_GOAL;
import static com.example.ptsupport.HomeFragment.formatter;

public class ResultActivity extends AppCompatActivity {

    PieModel finalGoal, finalCurrent;
    PieChart pieChartR;
    TextView pieChartSteps, finalRkm, finalRpercent, finalRkcal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        //액션바 감추기
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //findViewById
        pieChartR = findViewById(R.id.piechart_result);
        pieChartSteps = findViewById(R.id.Result_chart_steps);
        finalRkm = findViewById(R.id.Result_km);
        finalRpercent = findViewById(R.id.Result_per);
        finalRkcal = findViewById(R.id.Result_kcal);

        int steps_today = 5000;

        //차트
        finalCurrent = new PieModel("", steps_today, Color.parseColor("#99CC99"));
        pieChartR.addPieSlice(finalCurrent);
        finalGoal = new PieModel("", DEFAULT_GOAL, Color.parseColor("#111111"));
        pieChartR.addPieSlice(finalGoal);

        pieChartR.startAnimation();

        //텍스트
        double percent = steps_today * 100 / DEFAULT_GOAL;
        double kmcount = steps_today * 70 * 0.000001;
        double kcalcount = steps_today * 70 * 0.00001 * 40;
        pieChartSteps.setText(formatter.format(steps_today));
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
    }
}
