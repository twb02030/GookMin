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
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import java.io.Console;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.TimerTask;

import static android.media.MediaPlayer.*;

public class HomeFragment extends Fragment implements SensorEventListener {

    static int DEFAULT_GOAL = 10000;
    int todayOffset, since_boot, total_start, total_days, goal, steps_today, modenum;

    TextView stepcountView, kmcountView, kcalcountView, percentcountView, stopsignView, testText, randomText,
            modeNum;
    ImageView walkcircle0, walkcircle10, walkcircle20, walkcircle50, walkcircle80, walkcircle100;


    MediaPlayer ready, start_sound, middle_sound, finish_sound, cheerup_sound, ww_sound, d_sound;
    CountDownTimer timer;
    Button startstopButton;

    String MODE1 = "EASY";
    String MODE2 = "DIET";

    private int walkways[];
    private int starts[];
    private int middles[];
    private int finishs[];
    private int cheerups[];

    private static boolean checknowSteps;
    private static boolean modechecktf;


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
        modeNum = (TextView) v.findViewById(R.id.modeNum);

        startstopButton = (Button) v.findViewById(R.id.startstopbutton);


        //시작 및 정지 버튼, 터치시마다 상태 변경
        startstopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startstopButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checknowSteps = !checknowSteps;
                        onPressedStartStop();
                    }
                });
            }
        });

        ready = create(getActivity(), R.raw.ready);

        return v;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().startService(new Intent(getActivity(), SensorListener.class));

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

        checknowSteps = prefs.getBoolean("check", true); //check 키의 값을 불러옴, 해당하는 값이 없으면 기본값인 true로 설정

        SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI, 0);

        since_boot = db.getCurrentSteps();

        total_start = db.getTotalWithoutToday();
        total_days = db.getDays();

        db.close();
        updatestats();
        onPressedStartStop();
    }


    public void onPressedStartStop() {

        SharedPreferences pref = getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();

        if(checknowSteps == true) {
            edit.putBoolean("check", true); //check 키에 true 값을 넣는다
            edit.commit();
            startstopButton.setText("START");
//            ready.start();
            //startstopButton.setBackgroundColor(Color.RED);
            stopsignView.setVisibility(View.GONE);
            Toast.makeText(getActivity(), "만보기 기록 중입니다.", Toast.LENGTH_SHORT).show();
//            if(steps_today < 10000)
//            {
//                Intent intent = new Intent(getActivity(), ResultActivity.class);
//                startActivity(intent);
//            }
        } else {
            edit.putBoolean("check", false); //check 키에 false 값을 넣는다
            edit.commit();
            startstopButton.setText("STOP");
            //startstopButton.setBackgroundColor(Color.BLUE);
            stopsignView.setVisibility(View.VISIBLE);
            Toast.makeText(getActivity(), "만보기 기록이 일시정지되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }


    //앱을 나갔을 때 실행, 이때 걸음수 저장해야 함
    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences prefs = getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        checknowSteps = prefs.getBoolean("check", true); //check 키의 값을 불러옴, 해당하는 값이 없으면 기본값인 true로 설정

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


        //
        SharedPreferences sp = getActivity().getSharedPreferences("SharedPrefFile", Context.MODE_PRIVATE);
        modechecktf = sp.getBoolean("mode", true);
        if(modechecktf == true) {
            testText.setText(MODE1);
            modenum = 10;
            modeNum.setText(String.valueOf(modenum));
        }

        else {
            testText.setText(MODE2);
            modenum = 20;
            modeNum.setText(String.valueOf(modenum));
        }



        //이지 모드일 때
        if(modenum == 10){

            /*************************************사운드*******************************************/
            //걷기 자세 코칭
            walkways = new int[4];
            walkways[0] = R.raw.walkway_1;
            walkways[1] = R.raw.walkway_2;
            walkways[2] = R.raw.walkway_3;
            walkways[3] = R.raw.walkway_4;

            //시작
            starts = new int[3];
            starts[0] = R.raw.start_1;
            starts[1] = R.raw.start_2;
            starts[2] = R.raw.start_3;

            //중간
            middles = new int[4];
            middles[0] = R.raw.middle_1;
            middles[1] = R.raw.middle_2;
            middles[2] = R.raw.middle_3;

            //끝
            finishs = new int[3];
            finishs[0] = R.raw.finish_1;
            finishs[1] = R.raw.finish_2;
            finishs[2] = R.raw.finish_3;

            //격려
            cheerups = new int[2];
            cheerups[0] = R.raw.cheerup_1;
            cheerups[1] = R.raw.cheerup_2;

            // 0-1 중에서
            int ma = 1;
            int mi = 0;
            Random random0 = new Random();
            int randomNum0 = random0.nextInt(ma-mi+1)+mi;
            randomText.setText(String.valueOf(randomNum0));

            // 0-2까지
            int max = 2;
            int min = 0;
            Random random1 = new Random();
            int randomNum1 = random1.nextInt(max - min + 1)+min;
            randomText.setText(String.valueOf(randomNum1));

            // 0-3까지
            int max_num_value = 3;
            int min_num_value = 0;
            Random random = new Random();
            int randomNum2 = random.nextInt(max_num_value - min_num_value + 1)+min_num_value;
            randomText.setText(String.valueOf(randomNum2));

            start_sound = MediaPlayer.create(getActivity(), starts[randomNum1]);
            middle_sound = MediaPlayer.create(getActivity(), middles[randomNum1]);
            finish_sound = MediaPlayer.create(getActivity(), finishs[randomNum1]);
            cheerup_sound = MediaPlayer.create(getActivity(), cheerups[randomNum0]);
            ww_sound = MediaPlayer.create(getActivity(), walkways[randomNum2]);


            if(steps_today == 0) {
                ready.start();
                ready.setLooping(false);
            }

            if(steps_today == 1000){
                start_sound.start();
                start_sound.setLooping(false);
            }
            if(steps_today == 2000){
                cheerup_sound.start();
                cheerup_sound.setLooping(false);
            }
            if(steps_today == 3000){
                ww_sound.start();
                ww_sound.setLooping(false);
            }
            if(steps_today == 4000){
                ww_sound.start();
                ww_sound.setLooping(false);
            }
            if(steps_today == 5000){
               middle_sound.start();
               middle_sound.setLooping(false);
            }
            if(steps_today == 6000){
                cheerup_sound.start();
                cheerup_sound.setLooping(false);
            }
            if(steps_today == 7000){
                ww_sound.start();
                ww_sound.setLooping(false);
            }
            if(steps_today == 8000){
                cheerup_sound.start();
                cheerup_sound.setLooping(false);
            }
            if(steps_today == 10000){
                finish_sound.start();
                finish_sound.setLooping(false);
            }


            /*************************************이미지*******************************************/
            if (steps_today < 10) {
                walkcircle0.setVisibility(View.VISIBLE);

                walkcircle10.setVisibility(View.INVISIBLE);
                walkcircle20.setVisibility(View.INVISIBLE);
                walkcircle50.setVisibility(View.INVISIBLE);
                walkcircle80.setVisibility(View.INVISIBLE);
                walkcircle100.setVisibility(View.INVISIBLE);
            }
            else if ((10 <= steps_today)&&(steps_today < 20)) {
                walkcircle10.setVisibility(View.VISIBLE);

                walkcircle0.setVisibility(View.INVISIBLE);
                walkcircle20.setVisibility(View.INVISIBLE);
                walkcircle50.setVisibility(View.INVISIBLE);
                walkcircle80.setVisibility(View.INVISIBLE);
                walkcircle100.setVisibility(View.INVISIBLE);
            }
            else if((20 <= steps_today)&&(steps_today < 50)){
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

        //다이어트 모드일 때
        else if(modenum == 20){

//            //사운드
//            d_sound = create(getActivity(), R.raw.diet_sound);
//
//            timer = new CountDownTimer(7200000, 5000) {
//                    @Override
//                    public void onTick(long millisUntilFinished) {
//                        d_sound.start();
//                    }
//                    @Override
//                    public void onFinish() {
//                    }
//            }; timer.start();


            //이미지
            if (steps_today < 10) {
                walkcircle0.setVisibility(View.VISIBLE);

                walkcircle10.setVisibility(View.INVISIBLE);
                walkcircle20.setVisibility(View.INVISIBLE);
                walkcircle50.setVisibility(View.INVISIBLE);
                walkcircle80.setVisibility(View.INVISIBLE);
                walkcircle100.setVisibility(View.INVISIBLE);
            }
            else if ((10 <= steps_today)&&(steps_today < 20)) {
                walkcircle10.setVisibility(View.VISIBLE);

                walkcircle0.setVisibility(View.INVISIBLE);
                walkcircle20.setVisibility(View.INVISIBLE);
                walkcircle50.setVisibility(View.INVISIBLE);
                walkcircle80.setVisibility(View.INVISIBLE);
                walkcircle100.setVisibility(View.INVISIBLE);
            }
            else if((20 <= steps_today)&&(steps_today < 50)){
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
