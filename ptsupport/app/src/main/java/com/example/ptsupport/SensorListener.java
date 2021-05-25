package com.example.ptsupport;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import static com.example.ptsupport.HomeFragment.checknowSteps;

public class SensorListener extends Service implements SensorEventListener {

    static long mstime = 6000000;
    static int steps;
    static int laststeps;

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //센서 흔들림 감지 시 걸음수 저장, savesteps() 함수 호출하여 데이터베이스 작업
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(checknowSteps) {
            steps = (int) event.values[0];
            savesteps();
        }
    }

    //데이터베이스에 값 저장, 최근 걸음 수 저장
    private boolean savesteps() {
        Database db = Database.getInstance(this);

        //변경 20210524
        if (db.getSteps(Util.getToday()) == Integer.MIN_VALUE) {
            int pauseDifference = steps -
                    getSharedPreferences("pedometer", Context.MODE_PRIVATE)
                            .getInt("pauseCount", steps);
            db.insertNewDay(Util.getToday(), steps - pauseDifference);
            if (pauseDifference > 0) {
                // update pauseCount for the new day
                getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit()
                        .putInt("pauseCount", steps).commit();
            }
        }

        db.saveCurrentSteps(steps);
        db.close();
        laststeps = steps;
        return true; //
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        reRegistersensor();

        return START_STICKY; //
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void reRegistersensor() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                SensorManager.SENSOR_DELAY_NORMAL, (int) (5 * mstime));
        //sensorManager.registerListener(this, stepCountSensor, SensorManager.SENSOR_DELAY_GAME);
    }
}
