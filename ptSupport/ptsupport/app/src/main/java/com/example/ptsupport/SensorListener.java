package com.example.ptsupport;

import android.Manifest;
import android.app.Service;
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

public class SensorListener extends Service implements SensorEventListener {

    static long mstime = 6000000;
    static int steps;
    static int laststeps;

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        steps = (int) event.values[0];
        savesteps();
    }

    private boolean savesteps() {
        Database db = Database.getInstance(this);
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
