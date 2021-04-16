package com.example.ptsupport;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.text.NumberFormat;
import java.util.Locale;

public class Uifragment extends Fragment implements SensorEventListener {

    int DEFAULT_GOAL = 10000;
    TextView titleView, stepcountView, kmcountView, kcalcountView, percentcountView, stopsignView;
    int todayOffset, since_boot, total_start, total_days, goal;
    public final static NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());

    public Uifragment() {

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().startService(new Intent(getActivity(), SensorListener.class));
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.uifragmentlayout, null);
        titleView = (TextView) v.findViewById(R.id.Title);
        stepcountView = (TextView) v.findViewById(R.id.Stepcount);
        kmcountView = (TextView) v.findViewById(R.id.Kmcount);
        kcalcountView = (TextView) v.findViewById(R.id.Kcalcount);
        percentcountView = (TextView) v.findViewById(R.id.percentcount);
        stopsignView = (TextView) v.findViewById(R.id.Stopsign);

        return v;
    }


    @Override
    public void onResume() {
        super.onResume();

        Database db = Database.getInstance(getActivity());
        todayOffset = db.getSteps(Util.getToday());

        SharedPreferences prefs =
                getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE);

        goal = prefs.getInt("goal", DEFAULT_GOAL);

        SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI, 0);

        since_boot = db.getCurrentSteps();

        total_start = db.getTotalWithoutToday();
        total_days = db.getDays();

        db.close();
    }

    @Override
    public void onPause() {
        super.onPause();

        Database db = Database.getInstance(getActivity());
        db.saveCurrentSteps(since_boot);
        db.close();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] > Integer.MAX_VALUE || event.values[0] == 0) {
            return;
        }
        if (todayOffset == Integer.MIN_VALUE) {
            // no values for today
            // we dont know when the reboot was, so set todays steps to 0 by
            // initializing them with -STEPS_SINCE_BOOT
            todayOffset = -(int) event.values[0];
            Database db = Database.getInstance(getActivity());
            db.insertNewDay(Util.getToday(), (int) event.values[0]);
            db.close();
        }
        since_boot = (int) event.values[0];
        updatestats();
    }

    private void updatestats() {
        int steps_today = Math.max(todayOffset + since_boot, 0);
        stepcountView.setText(formatter.format(steps_today));

        double percent = steps_today * 100 / goal;
        double kmcount = steps_today * 70 * 0.000001;
        double kcalcount = steps_today * 70 * 0.00001 * 40;
        percentcountView.setText(percent+"%");
        kmcountView.setText(String.format("%.2f",kmcount)+"km");
        kcalcountView.setText(String.format("%.2f",kcalcount)+"kcal");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
