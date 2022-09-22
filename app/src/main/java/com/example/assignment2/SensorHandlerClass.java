package com.example.assignment2;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class SensorHandlerClass extends Service implements SensorEventListener {

    private SensorManager accelManage;
    private Sensor senseAccel;
    float accelValuesX[] = new float[128];
    float accelValuesY[] = new float[128];
    ArrayList<Float> accelValuesZ=new ArrayList<Float>();

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//            accelValuesX[index] = sensorEvent.values[0];
//            accelValuesY[index] = sensorEvent.values[1];
            accelValuesZ.add(sensorEvent.values[2]);
            System.out.println("Z: " + sensorEvent.values[2]);

        }
    }

    public float measureRespRate(){
        ArrayList<Float> zValuesList=new ArrayList<Float>();

        double sum=0;
        double max=-Double.MAX_VALUE;
        double min=Double.MAX_VALUE;
        for(float val:accelValuesZ){
            if(val>=0){
                zValuesList.add(val);
                sum+=val;
                double dblVal=val;
                max=Math.max(max, dblVal);
                min=Math.min(min, dblVal);
            }
        }

        double baseline=sum/zValuesList.size();
        double dynamicRangeUp=max-baseline;
        double dynamicRangeDown=baseline-min;
        double thresholdUp = 0.002*dynamicRangeUp;
        double thresholdR = 0.5*dynamicRangeUp;
        double thresholdDown = 0.000002*dynamicRangeDown;
        double thresholdQ = 0.1*dynamicRangeDown;

        int up = 1;
        double previousPeak = zValuesList.get(0);
        int k = -1;
        double maximum = -1000;
        double minimum = 1000;
        int possiblePeak = 0;

        int rPeak = 0;
        ArrayList<Integer> rPeakIndex = new ArrayList<>();

        int qPeak = 0;
        ArrayList<Integer> qPeakIndex = new ArrayList<>();

        int sPeak = 0;
        ArrayList<Integer> sPeakIndex = new ArrayList<>();

        int peakType = 0;

        ArrayList<Integer> peakIndex = new ArrayList<>();


        int i=0;
        while(i<zValuesList.size()){
            if(zValuesList.get(i) > maximum)
                maximum = zValuesList.get(i);

            if(zValuesList.get(i) < minimum)
                minimum = zValuesList.get(i);

            if(up==1){
                if(zValuesList.get(i) < maximum){
                    if(possiblePeak==0)
                        possiblePeak=i;
                    if(zValuesList.get(i) < maximum - thresholdUp){
                        k++;
                        peakIndex.add(possiblePeak-1);
                        minimum=zValuesList.get(i);
                        up=0;
                        possiblePeak=0;
                        if(peakType==0){
                            if(zValuesList.get(peakIndex.get(k)) > baseline+thresholdR){
                                rPeak++;
                                rPeakIndex.add(peakIndex.get(k));
                                previousPeak=zValuesList.get(peakIndex.get(k));
                            }
                        }else{
                            if((Math.abs((zValuesList.get(peakIndex.get(k)) - previousPeak)/previousPeak) > 1.5) && (zValuesList.get(peakIndex.get(k))>baseline+thresholdR)){
                                rPeak++;
                                rPeakIndex.add(peakIndex.get(k));
                                previousPeak=zValuesList.get(peakIndex.get(k));
                                peakType=2;
                            }
                        }
                    }
                }
            }else{
                if(zValuesList.get(i) > minimum){
                    if(possiblePeak==0)
                        possiblePeak=i;
                    if(zValuesList.get(i) > minimum + thresholdDown) {
                        k++;
                        peakIndex.add(possiblePeak-1);
                        maximum=zValuesList.get(i);
                        up=1;
                        possiblePeak=0;
                    }
                }
            }
            i++;
        }

        float ans = peakIndex.size()/50;

        Intent send = new Intent();
        send.setAction("GET_RESP_RATE");
        send.putExtra( "BREATHING_RATE",Float.toString(ans));
        sendBroadcast(send);

        return ans;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onCreate(){
        Toast.makeText(this, "Service Started, please wait 45s for respiratory rate value to update", Toast.LENGTH_LONG).show();
        accelManage = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senseAccel = accelManage.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelManage.registerListener(this, senseAccel, SensorManager.SENSOR_DELAY_NORMAL);
        SensorEventListener listener = this;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                accelManage.unregisterListener(listener);
                measureRespRate();
                stopSelf();
            }
        }, 45000);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
