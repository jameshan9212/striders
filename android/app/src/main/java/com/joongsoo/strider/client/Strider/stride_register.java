package com.joongsoo.strider.client.Strider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.joongsoo.strider.client.Main;
import com.joongsoo.strider.client.R;

/**
 * Created by on 2016-10-21.
 */
public class stride_register extends Activity implements SensorEventListener {

    private static final String TAG = "STRIDER_REGISTER";

    private SensorManager sensorManager;
    int SENSOR_ACC = Sensor.TYPE_ACCELEROMETER;
    int SENSOR_DELAY_ACC = SensorManager.SENSOR_DELAY_FASTEST;

    boolean btn_record = false;
    /*  0 is INIT
        1 is RUNNING
        2 is PAUSE      */
    int btn_timer = 0;
    TextView tv_recording;
    TextView tv_elapsed;
    ImageButton ib_thumb;

    long mPauseTime= 0, mBaseTime = 0;

    final double alpha = 0.8;
    double gravity_x = 0;
    double gravity_y = 0;
    double gravity_z = 0;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)

     public void onCreate(Bundle savedInstaceState) {
        super.onCreate(savedInstaceState);

        setContentView(R.layout.strider_register);

        tv_recording = (TextView)findViewById(R.id.tv_recoding);
        tv_elapsed = (TextView)findViewById(R.id.tv_elapsed);
        ib_thumb = (ImageButton)findViewById(R.id.btn_thumb);

        ib_thumb.setOnTouchListener(mOnTouchListener);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    protected void onResume() {
        super.onResume();

        Sensor sensor = sensorManager.getDefaultSensor(SENSOR_ACC);
        sensorManager.registerListener(this, sensor, SENSOR_DELAY_ACC);
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }


    ImageButton.OnTouchListener mOnTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            // show interest in events resulting from ACTION_DOWN
            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                // INIT -> RUNNING
                if(btn_timer == 0) {
                    tv_recording.setText("RECORDING...");
                    mBaseTime = SystemClock.elapsedRealtime();
                    mTimer.sendEmptyMessage(0);
                    btn_timer = 1;

                    ib_thumb.setImageResource(R.drawable.thumb_var);
                    btn_record = true;
                    return true;
                }

                // PAUSE -> RUNNING
                else if(btn_timer == 2) {
                    tv_recording.setText("RECORDING...");
                    long now = SystemClock.elapsedRealtime();
                    mBaseTime += (now - mPauseTime);
                    mTimer.sendEmptyMessage(0);
                    btn_timer = 1;

                    ib_thumb.setImageResource(R.drawable.thumb_var);
                    btn_record = true;
                    return true;
                }

                return true;
            }

            // don't handle event unless its ACTION_UP so "doSomething()" only runs once.
            if (event.getAction() == MotionEvent.ACTION_UP) {

                // RUNNING -> PAUSE
                mTimer.removeMessages(0);
                mPauseTime = SystemClock.elapsedRealtime();
                btn_timer = 2;

                tv_recording.setText("STOPPED...");
                btn_record = false;
                ib_thumb.setImageResource(R.drawable.thumb_use);

                return false;
            }

            return true;
        }
    };

    Handler mTimer = new Handler(){

        public void handleMessage(android.os.Message msg) {
            tv_elapsed.setText(getElapsed());
            mTimer.sendEmptyMessage(0);
        }
    };

    String getElapsed() {
        long now = SystemClock.elapsedRealtime();
        long ell = now - mBaseTime;

        String sEll = String.format("%02d:%02d:%02d", ell / 1000 / 60, (ell/1000) % 60, (ell % 1000)/10);
        return sEll;
    }

    public void onAccuracyChanged(Sensor sensor, int accracy) {}

    public void onSensorChanged(SensorEvent event) {
        // when it is not motion mode

        if(!btn_record)
            return;

        Main main = Main.getInstance();

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity_x = alpha * gravity_x + (1 - alpha) * event.values[0];
            gravity_y = alpha * gravity_y + (1 - alpha) * event.values[1];
            gravity_z = alpha * gravity_z + (1 - alpha) * event.values[2];

            //gravity_x=0;            gravity_y=0;            gravity_z=0;
            String temp_acc_x = String.format("%.0f", event.values[0]- gravity_x);
            String temp_acc_y = String.format("%.0f", event.values[1]- gravity_y);
            String temp_acc_z = String.format("%.0f", event.values[2]- gravity_z);

            // send acc_x acc_y acc_z
            main.sendMessage(castAccer(temp_acc_x, temp_acc_y, temp_acc_z));

        }
    }

    private String castAccer(String accX, String accY, String accZ) {
        return "0000000000000000000000000X" + accX + "D" + accY + "D" + accZ + "F00000000000000000000000000000";
    }
}