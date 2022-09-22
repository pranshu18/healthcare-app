package com.example.assignment2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity2 extends AppCompatActivity {

    private static final int VIDEO_CAPTURE = 101;
    private static final int RESP_RATE_MEASURE = 102;

    private Uri fileUri;
    Uri videoLocationUri;
    RespRateReceiver receiver;
    ProgressDialog progressDialog;
    PeriodicWorkRequest saveRequest=null;
    WorkManager wm=null;

    AlarmManager alarmManager=null;
    PendingIntent pendingIntent=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        Intent intent = getIntent();
        String username=intent.getStringExtra(CommonUtilities.USER_NAME);
        String password=intent.getStringExtra(CommonUtilities.PASSWORD);

        Button locationServiceBtn = (Button)findViewById(R.id.button6);
        Button stopLocationServiceBtn = (Button)findViewById(R.id.button7);

        locationServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Data myData = new Data.Builder()
//                        .putString(CommonUtilities.USER_NAME, username)
//                        .putString(CommonUtilities.PASSWORD, password)
//                        .build();
//
//                saveRequest =
//                        new PeriodicWorkRequest.Builder(LocationWorker.class, 15, TimeUnit.MINUTES)
//                                .setInputData(myData)
//                                .build();
//
//                wm= WorkManager.getInstance(MainActivity2.this);
//                wm.enqueueUniquePeriodicWork(
//                        "sendDb",
//                        ExistingPeriodicWorkPolicy.REPLACE,
//                        saveRequest);


                alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(MainActivity2.this, AlarmReceiver.class);
                intent.putExtra(CommonUtilities.USER_NAME,username);
                intent.putExtra(CommonUtilities.PASSWORD,password);
                pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, pendingIntent);
            }
        });

        stopLocationServiceBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
//                if(saveRequest!=null){
//                    wm.cancelWorkById(saveRequest.getId());
//                }
                if(pendingIntent!=null){
                    alarmManager.cancel(pendingIntent);
                }
            }
        });

        OpenCVLoader.initDebug();

        receiver = new RespRateReceiver();
        registerReceiver(receiver, new IntentFilter("GET_RESP_RATE"));

        Button symptomsBtn = (Button)findViewById(R.id.button2);
        progressDialog = new ProgressDialog(this);

        symptomsBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity2.this, MainActivity3.class);
                startActivity(intent);
            }
        });

        Button measureHeartRateBtn = (Button)findViewById(R.id.button3);
        if(!hasCamera()){
            measureHeartRateBtn.setEnabled(false);
        }
        measureHeartRateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressBar();
                startRecording();
            }
        });

        Button measureRespRateBtn = (Button)findViewById(R.id.button4);

        measureRespRateBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent startSenseService = new Intent(MainActivity2.this, SensorHandlerClass.class);
                startService(startSenseService);
                showProgressBar();
            }
        });


    }


    public void getHeartRateFromVideo()
    {
        File folder = new
                File(getExternalFilesDir(Environment.DIRECTORY_DCIM) + File.separator + "videoFolder");
        File mediaFile = new File(folder.getPath()+File.separator+"myVideo.mp4");

        File opFile=new File(folder.getPath()+File.separator+"myVideo.avi");
        if(opFile.exists())
            opFile.delete();

        int rc = FFmpeg.execute("-i "+mediaFile.getAbsolutePath()+" -vcodec mjpeg -an "+folder.getAbsolutePath()+ File.separator+"myVideo.avi");

        if (rc == Config.RETURN_CODE_SUCCESS) {
            System.out.println("Command execution completed successfully.");
            String rate= calculateHeartRate();
            Person.setHeartRate(Float.parseFloat(rate));
        } else if (rc == Config.RETURN_CODE_CANCEL) {
            System.out.println("Command execution cancelled by user.");
        } else {
            System.out.println("Command execution failed.");
            Config.printLastCommandOutput(Log.INFO);
        }
    }

    public String calculateHeartRate() {
        File folder = new
                File(getExternalFilesDir(Environment.DIRECTORY_DCIM) + File.separator + "videoFolder");
        File mediaFile = new File(folder.getPath()+File.separator+"myVideo.avi");

        VideoCapture videoCapture = new VideoCapture();

        if(mediaFile.exists()){
            videoCapture.open(folder.getPath()+File.separator+"/myVideo.avi");
            if(videoCapture.isOpened()){

                int numFrames = (int) videoCapture.get(Videoio.CAP_PROP_FRAME_COUNT);

                Mat curr = new Mat();
                Mat next = new Mat();
                Mat diffFr = new Mat();

                List<Double> averageMov = new ArrayList<Double>();

                videoCapture.read(curr);
                int k =0;
                while( k < numFrames - 1){
                    videoCapture.read(next);
                    Core.subtract(next, curr, diffFr);
                    next.copyTo(curr);
                    averageMov.add(Core.mean(diffFr).val[0] + Core.mean(diffFr).val[1] + Core.mean(diffFr).val[2]);
                    k++;
                }

                int windowSize=5;

                ArrayList<Double> movAverage=new ArrayList<Double>();
                for(int j=0;j<averageMov.size()-windowSize;j=j+windowSize){
                    double movMean=0;
                    for(k=j;k<j+windowSize;k++){
                        movMean+=averageMov.get(k);
                    }
                    movMean=movMean/((double) windowSize);
                    movAverage.add(movMean);
                }

                ArrayList<Double> movAverage2=new ArrayList<Double>();

                windowSize=50;
                double movMean=0;

                for(int j=0;j<movAverage.size();j++){
                    if(j<windowSize)
                        movMean+=averageMov.get(j);
                    else{
                        double avg=movMean/((double) windowSize);
                        movAverage2.add(avg);
                        movMean = movMean-movAverage.get(j-windowSize)+movAverage.get(j);
                    }
                }
                double avg=movMean/((double) windowSize);
                movAverage2.add(avg);

                int index=1;
                int peakCount=0;
                double prevSlope=0;
                double prev=movAverage2.get(0);
                while(index < movAverage2.size()){
                    double slope=movAverage2.get(index)-prev;
                    if(prevSlope*slope<0)
                        peakCount++;
                    prevSlope=slope;
                    prev=movAverage2.get(index);
                    index++;
                }


                int fps=(int) videoCapture.get(Videoio.CAP_PROP_FPS);
                double timeW=numFrames/fps;
                double heartRate = (peakCount/2)*(60)/timeW;

                return Double.toString(heartRate);
            }
            else{
                return "0";
            }
        }
        else{
            return "0";
        }
    }

    public void startRecording()
    {
        File folder = new
                File(getExternalFilesDir(Environment.DIRECTORY_DCIM) + File.separator + "videoFolder");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File mediaFile = new File(folder.getPath()+"/myVideo.mp4");
        fileUri = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                BuildConfig.APPLICATION_ID + ".provider", mediaFile);

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,45);
        startActivityForResult(intent, VIDEO_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Video has been saved to:\n" +
                        data.getData(), Toast.LENGTH_LONG).show();
                videoLocationUri = data.getData();
                getHeartRateFromVideo();
                hideProgressBar();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Video recording cancelled.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to record video",
                        Toast.LENGTH_LONG).show();
            }
        }else if(requestCode == RESP_RATE_MEASURE){

        }
    }


    class RespRateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals("GET_RESP_RATE"))
            {
                hideProgressBar();
                String rate = intent.getStringExtra("BREATHING_RATE");
                Person.setRespRate(Float.parseFloat(rate));
            }
        }

    }

    private void hideProgressBar() {
        progressDialog.hide();
    }

    private boolean hasCamera() {
        if (getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_ANY)){
            return true;
        } else {
            return false;
        }
    }

    private void showProgressBar(){
        progressDialog.setMessage("Calculating...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

}