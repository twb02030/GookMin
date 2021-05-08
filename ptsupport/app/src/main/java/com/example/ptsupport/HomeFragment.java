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
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.text.NumberFormat;
import java.util.Locale;

public class HomeFragment extends Fragment implements SensorEventListener {

    static int DEFAULT_GOAL = 10000;
    TextView titleView, stepcountView, kmcountView, kcalcountView, percentcountView, stopsignView;
    int todayOffset, since_boot, total_start, total_days, goal, steps_today;
    private boolean checknowSteps = false;

    public final static NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());

    public HomeFragment() {

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().startService(new Intent(getActivity(), SensorListener.class));

    }


    //v.findViewById
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, null);
        titleView = (TextView) v.findViewById(R.id.Title);
        stepcountView = (TextView) v.findViewById(R.id.Stepcount);
        kmcountView = (TextView) v.findViewById(R.id.Kmcount);
        kcalcountView = (TextView) v.findViewById(R.id.Kcalcount);
        percentcountView = (TextView) v.findViewById(R.id.percentcount);
        stopsignView = (TextView) v.findViewById(R.id.Stopsign);
        Button startstopButton = (Button) v.findViewById(R.id.startstopbutton);

        //시작및정지 버튼
        startstopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checknowSteps = !checknowSteps;
                confirmbutton();
            }
        });

        return v;
    }


    //앱을 다시 열었을 떄 실행, 이때 걸음수 불러와야 함
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

    //앱을 나갔을 때 실행, 이때 걸음수 저장해야 함
    @Override
    public void onPause() {
        super.onPause();

        Database db = Database.getInstance(getActivity());
        db.saveCurrentSteps(since_boot);
        db.close();
    }

    //센서 흔들림 감지 시 걸음수 저장, updatestats() 함수 호출
    //시작 버튼을 눌렀을 떄만 작동
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (checknowSteps == true) {
            if (event.values[0] > Integer.MAX_VALUE || event.values[0] == 0)
            {
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
    }

    //그래프, 수치 값 업데이트
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

    //코드에 아무것도 들어가지 않음, 그러나 지우면 안됨
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //목표 달성 여부에 따라 최종 값 저장하고 결과창 액티비티 호출
    public void confirmbutton() {
        if (steps_today >= DEFAULT_GOAL)
        {
            Intent intent = new Intent(getActivity(), ResultActivity.class);
            startActivity(intent);
        }
    }


}
