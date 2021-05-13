package com.example.ptsupport;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ChartFragment extends Fragment {
    public ChartFragment() {

    }

    public void onResume() {
        super.onResume();
        updateBars();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        //BarChart barChart = (BarChart) getView().findViewById(R.id.chart_bargraph);

        return inflater.inflate(R.layout.fragment_chart,container, false);
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


}