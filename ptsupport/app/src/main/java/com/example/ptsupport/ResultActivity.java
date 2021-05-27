package com.example.ptsupport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;

import static com.example.ptsupport.HomeFragment.DEFAULT_GOAL;
import static com.example.ptsupport.HomeFragment.formatter;

public class ResultActivity extends AppCompatActivity {

    TextView finalSteps, finalRkm, finalRpercent, finalRkcal;
    int Rsteps_today, RtodayOffset, RSince_boot;

    private Button retryButton;
    private PieChart RpieChart;

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

        RpieChart = (PieChart) findViewById(R.id.activity_main_piechart);

        Database rdb = Database.getInstance(this);
        RtodayOffset = rdb.getSteps(Util.getToday());
        RSince_boot = rdb.getCurrentSteps();
        rdb.close();
        Rsteps_today = Math.max(RtodayOffset + RSince_boot, 0);

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

        RsetupPieChart();
        RloadPieChartData();
    }

    private void RsetupPieChart() {

        RpieChart.setDrawEntryLabels(false);

        Legend rl = RpieChart.getLegend();
        rl.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        rl.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        rl.setOrientation(Legend.LegendOrientation.VERTICAL);
        rl.setDrawInside(false);
        rl.setEnabled(false);

        RpieChart.setCenterText("Spending by Category");
        RpieChart.setCenterTextSize(18);
        RpieChart.setDrawCenterText(false);
        RpieChart.setHoleColor(R.color.Transparent);
        RpieChart.setHoleRadius(85f);

        RpieChart.getDescription().setEnabled(false);


    }

    private void RloadPieChartData() {

        int RpieCS = Math.max(RtodayOffset + RSince_boot, 0);
        int RpieLeftGoal = DEFAULT_GOAL - RpieCS;

        ArrayList<PieEntry> Rentries = new ArrayList<>();
        Rentries.add(new PieEntry(Math.max(RtodayOffset + RSince_boot, 0), "Current"));
        Rentries.add(new PieEntry(RpieLeftGoal, "Goal"));

        PieDataSet RdataSet = new PieDataSet(Rentries, "Expense Category");
        RdataSet.setColors(new int[] {Color.GREEN, Color.WHITE, Color.GRAY, Color.BLACK, Color.BLUE});

        PieData Rdata = new PieData(RdataSet);
        Rdata.setDrawValues(false);
        Rdata.setValueFormatter(new PercentFormatter(RpieChart));
        Rdata.setValueTextSize(12f);
        Rdata.setValueTextColor(Color.BLACK);

        RpieChart.setData(Rdata);
        RpieChart.notifyDataSetChanged();
        RpieChart.invalidate();

    }

}
