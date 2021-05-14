package com.example.ptsupport;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import java.io.Console;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import static android.media.MediaPlayer.*;

public class HomeFragment extends Fragment implements SensorEventListener {

    static int DEFAULT_GOAL = 10000;
    int todayOffset, since_boot, total_start, total_days, goal, steps_today;

    TextView stepcountView, kmcountView, kcalcountView, percentcountView, stopsignView, testText, randomText;
    ImageView ic_km, ic_per, ic_kcal,
            walkcircle0, walkcircle10, walkcircle20, walkcircle50, walkcircle80, walkcircle100;

    private int MODE_NAME = 1;
    private String MODE1;
    private String MODE2;

    MediaPlayer ready, start1, start2, start3, middle1, middle2, middle3, finish1, finish2, finish3,
    walkway1, walkway2, walkway3, walkway4, cheerup1, cheerup2;

    private boolean checknowSteps = false;
    private SharedViewModel sharedViewModel; //ViewModel을 사용하여 fragment 간 데이터 전달


    public final static NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());


    public HomeFragment() {

    }


    //v.findViewById
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, null);
        // titleView = (TextView) v.findViewById(R.id.Title);
        stepcountView = (TextView) v.findViewById(R.id.Stepcount);
        kmcountView = (TextView) v.findViewById(R.id.Kmcount);
        kcalcountView = (TextView) v.findViewById(R.id.Kcalcount);
        percentcountView = (TextView) v.findViewById(R.id.percentcount);
        stopsignView = (TextView) v.findViewById(R.id.Stopsign);

        //ICON
        ic_km = (ImageView) v.findViewById(R.id.ic_km);
        ic_kcal = (ImageView) v.findViewById(R.id.ic_kcal);
        ic_per = (ImageView) v.findViewById(R.id.ic_per);

        //STEP_BACKGROUND_IMAGES
        walkcircle0 = (ImageView) v.findViewById(R.id.walk_circle0);
        walkcircle10 = (ImageView) v.findViewById(R.id.walk_circle10);
        walkcircle20 = (ImageView) v.findViewById(R.id.walk_circle20);
        walkcircle50 = (ImageView) v.findViewById(R.id.walk_circle50);
        walkcircle80 = (ImageView) v.findViewById(R.id.walk_circle80);
        walkcircle100 = (ImageView) v.findViewById(R.id.walk_circle100);

        //test
        testText = (TextView) v.findViewById(R.id.testtext);
        randomText = (TextView) v.findViewById(R.id.random);

        Button startstopButton = (Button) v.findViewById(R.id.startstopbutton);

        //시작 및 정지 버튼, 터치시마다 상태 변경
        startstopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checknowSteps == true) {
                    checknowSteps = false;
                    startstopButton.setText("STOP");
                    //startstopButton.setBackgroundColor(Color.RED);
                    stopsignView.setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity(), "만보기 기록이 일시정지되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    checknowSteps = true;
                    startstopButton.setText("START");
                    //startstopButton.setBackgroundColor(Color.BLUE);
                    stopsignView.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), "만보기 기록이 기록됩니다.", Toast.LENGTH_SHORT).show();
                    ready.start();
                }
            }
        });

        ready = create(getActivity(), R.raw.ready);

        return v;
    }





    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().startService(new Intent(getActivity(), SensorListener.class));

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getLiveData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                //settingsFragment에서 받아온 모드 이름이 같은지 확인
                testText.setText(s);

                //settingsFragment에서 받아온 데이터와 같은지 비교하기 위함
                MODE1 = "EASY";
                MODE2 = "DIET";

                if (s.equals(MODE1))
                {
                    MODE_NAME = 1;
                }

                if (s.equals(MODE2))
                {
                    MODE_NAME = 2;

                }
            }
        });
    }



    //앱을 다시 열었을 때, 액티비티/프래그먼트 전환 시 실행
    //이때 걸음수 볼러오고 updatestats() 함수를 계속 호출하여 현재 정보를 표시해야 함
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
            if (event.values[0] > Integer.MAX_VALUE || event.values[0] == 0) {
                return;
            }
            if (todayOffset == Integer.MIN_VALUE) {
                // no values for today
                // we dont know when the reboot was, so set todays steps to 0 by
                // initializing them with -STEPS_SINCE_BOOT
                todayOffset = (int) event.values[0];
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

        percentcountView.setText(percent + "%");
        kmcountView.setText(String.format("%.2f", kmcount) + "km");
        kcalcountView.setText(String.format("%.2f", kcalcount) + "kcal");

        if(MODE_NAME == 1){
            //사운드
            start1 = MediaPlayer.create(getActivity(), R.raw.start_1);
            middle1 = MediaPlayer.create(getActivity(), R.raw.middle_1);
            finish1 = MediaPlayer.create(getActivity(), R.raw.finish_1);
            cheerup1 = MediaPlayer.create(getActivity(), R.raw.cheerup_1);
            cheerup2 = MediaPlayer.create(getActivity(), R.raw.cheerup_2);
            walkway1 = MediaPlayer.create(getActivity(), R.raw.walkway_1);
            walkway2 = MediaPlayer.create(getActivity(), R.raw.walkway_2);


            if(steps_today == 10){
                start1.start();
            }
            if(steps_today == 2000){
                walkway1.start();
            }
            if(steps_today == 3000){
                cheerup1.start();
            }
            if(steps_today == 4000){
                walkway2.start();
            }
            if(steps_today == 5000){
                middle1.start();
            }
            if(steps_today == 6000){
                cheerup2.start();
            }
            if(steps_today == 10000){
                finish1.start();
            }

            //이미지
            if (steps_today < 10) {
                walkcircle0.setVisibility(View.VISIBLE);

                walkcircle10.setVisibility(View.INVISIBLE);
                walkcircle20.setVisibility(View.INVISIBLE);
                walkcircle50.setVisibility(View.INVISIBLE);
                walkcircle80.setVisibility(View.INVISIBLE);
                walkcircle100.setVisibility(View.INVISIBLE);
            }
            else if ((1000 <= steps_today)&&(steps_today < 2000)) {
                walkcircle10.setVisibility(View.VISIBLE);

                walkcircle0.setVisibility(View.INVISIBLE);
                walkcircle20.setVisibility(View.INVISIBLE);
                walkcircle50.setVisibility(View.INVISIBLE);
                walkcircle80.setVisibility(View.INVISIBLE);
                walkcircle100.setVisibility(View.INVISIBLE);
            }
            else if((2000<=steps_today)&&(steps_today<5000)){
                walkcircle20.setVisibility(View.VISIBLE);

                walkcircle0.setVisibility(View.INVISIBLE);
                walkcircle10.setVisibility(View.INVISIBLE);
                walkcircle50.setVisibility(View.INVISIBLE);
                walkcircle80.setVisibility(View.INVISIBLE);
                walkcircle100.setVisibility(View.INVISIBLE);
            }

            else if((5000<=steps_today)&&(steps_today<8000)){
                walkcircle50.setVisibility(View.VISIBLE);

                walkcircle0.setVisibility(View.INVISIBLE);
                walkcircle10.setVisibility(View.INVISIBLE);
                walkcircle20.setVisibility(View.INVISIBLE);
                walkcircle80.setVisibility(View.INVISIBLE);
                walkcircle100.setVisibility(View.INVISIBLE);
            }
            else if((8000<=steps_today)&&(steps_today<10000)){
                walkcircle80.setVisibility(View.VISIBLE);

                walkcircle0.setVisibility(View.INVISIBLE);
                walkcircle10.setVisibility(View.INVISIBLE);
                walkcircle20.setVisibility(View.INVISIBLE);
                walkcircle50.setVisibility(View.INVISIBLE);
                walkcircle100.setVisibility(View.INVISIBLE);
            }
            else if(10000<=steps_today){
                walkcircle100.setVisibility(View.VISIBLE);

                walkcircle0.setVisibility(View.INVISIBLE);
                walkcircle10.setVisibility(View.INVISIBLE);
                walkcircle20.setVisibility(View.INVISIBLE);
                walkcircle50.setVisibility(View.INVISIBLE);
                walkcircle80.setVisibility(View.INVISIBLE);
            }
        }

        else if(MODE_NAME == 2){
            //이미지
            if (steps_today < 10) {
                walkcircle0.setVisibility(View.VISIBLE);

                walkcircle10.setVisibility(View.INVISIBLE);
                walkcircle20.setVisibility(View.INVISIBLE);
                walkcircle50.setVisibility(View.INVISIBLE);
                walkcircle80.setVisibility(View.INVISIBLE);
                walkcircle100.setVisibility(View.INVISIBLE);
            }
            else if ((1000 <= steps_today)&&(steps_today < 2000)) {
                walkcircle10.setVisibility(View.VISIBLE);

                walkcircle0.setVisibility(View.INVISIBLE);
                walkcircle20.setVisibility(View.INVISIBLE);
                walkcircle50.setVisibility(View.INVISIBLE);
                walkcircle80.setVisibility(View.INVISIBLE);
                walkcircle100.setVisibility(View.INVISIBLE);
            }
            else if((2000<=steps_today)&&(steps_today<5000)){
                walkcircle20.setVisibility(View.VISIBLE);

                walkcircle0.setVisibility(View.INVISIBLE);
                walkcircle10.setVisibility(View.INVISIBLE);
                walkcircle50.setVisibility(View.INVISIBLE);
                walkcircle80.setVisibility(View.INVISIBLE);
                walkcircle100.setVisibility(View.INVISIBLE);
            }

            else if((5000<=steps_today)&&(steps_today<8000)){
                walkcircle50.setVisibility(View.VISIBLE);

                walkcircle0.setVisibility(View.INVISIBLE);
                walkcircle10.setVisibility(View.INVISIBLE);
                walkcircle20.setVisibility(View.INVISIBLE);
                walkcircle80.setVisibility(View.INVISIBLE);
                walkcircle100.setVisibility(View.INVISIBLE);
            }
            else if((8000<=steps_today)&&(steps_today<10000)){
                walkcircle80.setVisibility(View.VISIBLE);

                walkcircle0.setVisibility(View.INVISIBLE);
                walkcircle10.setVisibility(View.INVISIBLE);
                walkcircle20.setVisibility(View.INVISIBLE);
                walkcircle50.setVisibility(View.INVISIBLE);
                walkcircle100.setVisibility(View.INVISIBLE);
            }
            else if(10000<=steps_today){
                walkcircle100.setVisibility(View.VISIBLE);

                walkcircle0.setVisibility(View.INVISIBLE);
                walkcircle10.setVisibility(View.INVISIBLE);
                walkcircle20.setVisibility(View.INVISIBLE);
                walkcircle50.setVisibility(View.INVISIBLE);
                walkcircle80.setVisibility(View.INVISIBLE);
            }
        }
    }


    //코드에 아무것도 들어가지 않음, 그러나 지우면 안됨
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    //미구현, 테스트용 코드, 터치 시 최종결과창 클래스 불러옴
    public void confirmbutton() {
        if (steps_today >= DEFAULT_GOAL) {
            Intent intent = new Intent(getActivity(), ResultActivity.class);
            startActivity(intent);
        }
    }


}