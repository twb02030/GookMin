package com.example.ptsupport;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.SensorEvent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.example.ptsupport.HomeFragment.DEFAULT_GOAL;
import static com.example.ptsupport.HomeFragment.formatter;

public class ChartFragment extends Fragment {

    TextView CFstepCountView, avgCountView, totalCountView;
    int CFsteps_today, CFtodayOffset, CFSince_boot, total_start, total_days;

    private PieChart CFpieChart;


    public ChartFragment() {

    }

    public void onResume() {
        super.onResume();

        Database cfdb = Database.getInstance(getActivity());
        CFtodayOffset = cfdb.getSteps(Util.getToday());
        CFSince_boot = cfdb.getCurrentSteps();

        total_start = cfdb.getTotalWithoutToday();
        total_days = cfdb.getDays();
        cfdb.close();
        updateCFstats();
        updateBars();

        CFloadPieChartData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View cfv = inflater.inflate(R.layout.fragment_chart, null);

        CFstepCountView = (TextView) cfv.findViewById(R.id.piechartCFsteps);
        avgCountView = (TextView) cfv.findViewById(R.id.avg_count);
        totalCountView = (TextView) cfv.findViewById(R.id.total_count);

        CFpieChart = (PieChart) cfv.findViewById(R.id.piechartCF);

        CFsetupPieChart();
        CFloadPieChartData();

        return cfv;
    }


    private void updateCFstats() {
        CFsteps_today = Math.max(CFtodayOffset + CFSince_boot, 0);
        CFstepCountView.setText(formatter.format(CFsteps_today));
        avgCountView.setText(formatter.format((total_start + CFsteps_today) / total_days));
        totalCountView.setText(formatter.format(total_start + CFsteps_today));


    }

    private void updateBars() {
        SimpleDateFormat df = new SimpleDateFormat("E", Locale.getDefault());
        BarChart barChart = (BarChart) getView().findViewById(R.id.chart_bargraph);
        if (barChart.getData().size() > 0) barChart.clearChart();
        int chartSteps;

        //barChart.setShowDecimal(true); // show decimal in distance view only
        BarModel bm;
        Database db = Database.getInstance(getActivity());
        List<Pair<Long, Integer>> last = db.getLastEntries(4);
        db.close();
        for (int i = last.size() - 1; i > 0; i--) {
            Pair<Long, Integer> current = last.get(i);
            chartSteps = current.second;
            if (chartSteps > 0) {
                bm = new BarModel(df.format(new Date(current.first)), 0,
                        chartSteps > HomeFragment.DEFAULT_GOAL ? Color.parseColor("#99CC00") : Color.parseColor("#0099cc"));
                bm.setValue(chartSteps);
                barChart.addBar(bm);
            }
        }
        if (barChart.getData().size() > 0) {
            barChart.startAnimation();
        } else {
            barChart.setVisibility(View.GONE);
        }
    }

    private void CFsetupPieChart() {

        CFpieChart.setDrawEntryLabels(false);

        Legend cfl = CFpieChart.getLegend();
        cfl.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        cfl.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        cfl.setOrientation(Legend.LegendOrientation.VERTICAL);
        cfl.setDrawInside(false);
        cfl.setEnabled(false);

        CFpieChart.setCenterText("Spending by Category");
        CFpieChart.setCenterTextSize(18);
        CFpieChart.setDrawCenterText(false);
        CFpieChart.setHoleColor(R.color.Transparent);
        CFpieChart.setHoleRadius(85f);

        CFpieChart.getDescription().setEnabled(false);


    }

    private void CFloadPieChartData() {

        int CFpieCS = Math.max(CFtodayOffset + CFSince_boot, 0);
        int CFpieLeftGoal = 10000 - CFpieCS;

        ArrayList<PieEntry> CFentries = new ArrayList<>();
        CFentries.add(new PieEntry(Math.max(CFtodayOffset + CFSince_boot, 0), "CFCurrent"));
        CFentries.add(new PieEntry(CFpieLeftGoal, "CFGoal"));

        PieDataSet CFdataSet = new PieDataSet(CFentries, "CFExpense Category");
        CFdataSet.setColors(new int[] {Color.GREEN, Color.WHITE, Color.GRAY, Color.BLACK, Color.BLUE});

        PieData CFdata = new PieData(CFdataSet);
        CFdata.setDrawValues(false);
        CFdata.setValueFormatter(new PercentFormatter(CFpieChart));
        CFdata.setValueTextSize(12f);
        CFdata.setValueTextColor(Color.BLACK);

        CFpieChart.setData(CFdata);
        CFpieChart.notifyDataSetChanged();
        CFpieChart.invalidate();

        //pieChart.animateY(1400, Easing.EaseInOutQuad);
    }

}