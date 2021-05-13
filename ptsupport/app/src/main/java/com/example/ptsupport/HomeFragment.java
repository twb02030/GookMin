package com.example.ptsupport;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.text.NumberFormat;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment implements SensorEventListener {

    static int DEFAULT_GOAL = 10000;
    TextView titleView, stepcountView, kmcountView, kcalcountView, percentcountView, stopsignView;
    int todayOffset, since_boot, total_start, total_days, goal, steps_today;
    ImageView ic_km, ic_per, ic_kcal;
    private boolean checknowSteps = true; //시작 혹은 정지 상태. START = true,   STOP = false
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
        // titleView = (TextView) v.findViewById(R.id.Title);
        stepcountView = (TextView) v.findViewById(R.id.Stepcount);
        kmcountView = (TextView) v.findViewById(R.id.Kmcount);
        kcalcountView = (TextView) v.findViewById(R.id.Kcalcount);
        percentcountView = (TextView) v.findViewById(R.id.percentcount);
        stopsignView = (TextView) v.findViewById(R.id.Stopsign);

        ic_km = (ImageView) v.findViewById(R.id.ic_km);
        ic_kcal = (ImageView) v.findViewById(R.id.ic_kcal);
        ic_per = (ImageView) v.findViewById(R.id.ic_per);

        Button startstopButton = (Button) v.findViewById(R.id.startstopbutton);

        //시작및정지 버튼, 터치시마다 상태 변경
        startstopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checknowSteps == true)
                {
                    checknowSteps = false;
                    startstopButton.setText("START");
                    //startstopButton.setBackgroundColor(Color.BLUE);
                    stopsignView.setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity(), "만보기 기록이 일시 정지되었습니다.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    checknowSteps = true;
                    startstopButton.setText("STOP");
                    //startstopButton.setBackgroundColor(Color.RED);
                    stopsignView.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), "만보기 기록이 기록됩니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return v;
    }


    //앱을 다시 열었을 때, 액티비티/프래그먼트 전환 시 실행
    //이때 걸음수 볼러오고 updatestats() 함수를 계속 호출하여 현재 정보를 표시해야 함
    @Override
    public void onResume() {
        super.onResume();

        Database db = Database.getInstance(getActivity());
        todayOffset = db.getSteps(Util.getToday());

        SharedPreferences prefs =
                getActivity().getSharedPreferences("pedometer", MODE_PRIVATE);

        goal = prefs.getInt("goal", DEFAULT_GOAL);

        SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI, 0);

        since_boot = db.getCurrentSteps();

        db.close();
        updatestats();
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

    //미구현, 테스트용 코드, 터치 시 최종결과창 클래스 불러옴
    public void confirmbutton() {
        if (steps_today >= DEFAULT_GOAL)
        {
            Intent intent = new Intent(getActivity(), ResultActivity.class);
            startActivity(intent);
        }
    }

    public void totalSound(){

    }


}
