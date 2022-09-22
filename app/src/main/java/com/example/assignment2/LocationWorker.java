package com.example.assignment2;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.work.Data;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocationWorker extends Worker {

    public Context context;
    SQLiteDatabase db;

    public LocationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context=context;
    }

    @Override
    public Result doWork() {
        String username= getInputData().getString(CommonUtilities.USER_NAME);
        String password= getInputData().getString(CommonUtilities.PASSWORD);

        try {
            createOrSetDb(username, password);
        } catch (SQLException e) {
        }

        setLocation();

        return Result.success();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private long updateDb() {
        ContentValues values = new ContentValues();
        values.put("HeartRate", Person.getHeartRate());
        values.put("RespRate", Person.getRespRate());
        values.put("Nausea", Person.getNausea());
        values.put("Headache", Person.getHeadache());
        values.put("Diarrhea", Person.getDiarrhea());
        values.put("SoreThroat", Person.getSoreThroat());
        values.put("Fever", Person.getFever());
        values.put("MuscleAche", Person.getMuscleAche());
        values.put("LossOfSmellOrTaste", Person.getLossOfSmellOrTaste());
        values.put("Cough", Person.getCough());
        values.put("ShortnessOfBreath", Person.getShortnessOfBreath());
        values.put("FeelingTired", Person.getFeelingTired());
        values.put("XCoordinate", Person.getLatitude());
        values.put("YCoordinate", Person.getLongitude());
        String dateTime=LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy:HH:mm"));
        values.put("Time", dateTime);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert("symptoms", null, values);

        Person.setHeartRate(0);
        Person.setRespRate(0);
        Person.setNausea(0);
        Person.setHeadache(0);
        Person.setDiarrhea(0);
        Person.setSoreThroat(0);
        Person.setFever(0);
        Person.setMuscleAche(0);
        Person.setLossOfSmellOrTaste(0);
        Person.setCough(0);
        Person.setShortnessOfBreath(0);
        Person.setFeelingTired(0);
        Person.setLatitude(0);
        Person.setLongitude(0);

        return newRowId;

    }

    private void createOrSetDb(String username, String password) {
        File folder=new File(context.getExternalFilesDir(Environment.DIRECTORY_DCIM) + File.separator + "databaseFolder");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        db = SQLiteDatabase.openOrCreateDatabase(folder.getPath() + File.separator + username, null);
        db.beginTransaction();
        try {
            db.execSQL("create table if not exists symptoms ("
                    + " recID integer PRIMARY KEY autoincrement, "
                    + "HeartRate float default 0.0, "
                    + "RespRate float default 0.0,"
                    + "Nausea float default 0.0,"
                    + "Headache float default 0.0,"
                    + "Diarrhea float default 0.0,"
                    + "SoreThroat float default 0.0,"
                    + "Fever float default 0.0,"
                    + "MuscleAche float default 0.0,"
                    + "LossOfSmellOrTaste float default 0.0,"
                    + "Cough float default 0.0,"
                    + "ShortnessOfBreath float default 0.0,"
                    + "FeelingTired float default 0.0,"
                    + "XCoordinate float default 0.0,"
                    + "YCoordinate float default 0.0,"
                    + "Time text default 0.0"
                    + " ); " );
            db.setTransactionSuccessful();
        }
        catch (SQLiteException e) {
        }
        finally {
            db.endTransaction();
        }
    }


    private void setLocation() {

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Task<Location> task = fusedLocationClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Person.setLatitude(location.getLatitude());
                    Person.setLongitude(location.getLongitude());
                    updateDb();
                    new UploadFileAsync().execute("");
                }
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });

    }

    private class UploadFileAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                File dbFile=new File(db.getPath());
                String asuID="123445";
                String date= LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
                if(!dbFile.exists()){
                    System.out.println("wtf");
                }
                long fileSizeBytes=dbFile.length();
                String sourceFileUri = dbFile.getAbsolutePath();

                HttpURLConnection conn = null;
                DataOutputStream dos = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                File sourceFile = new File(sourceFileUri);

                if (sourceFile.isFile()) {

                    try {
                        String upLoadServerUri = "http://192.168.0.240:8080/Process.php";

                        // open a URL connection to the Servlet
                        FileInputStream fileInputStream = new FileInputStream(
                                sourceFile);
                        URL url = new URL(upLoadServerUri);

                        // Open a HTTP connection to the URL
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true); // Allow Inputs
                        conn.setDoOutput(true); // Allow Outputs
                        conn.setUseCaches(false); // Don't use a Cached Copy
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("ENCTYPE",
                                "multipart/form-data");
                        conn.setRequestProperty("Content-Type",
                                "multipart/form-data;boundary=" + boundary);
                        conn.setRequestProperty("bill", sourceFileUri);

                        dos = new DataOutputStream(conn.getOutputStream());

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""
                                + sourceFileUri + "\"" + lineEnd);

                        dos.writeBytes(lineEnd);

                        // create a buffer of maximum size
                        bytesAvailable = fileInputStream.available();

                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];

                        // read file and write it into form...
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        while (bytesRead > 0) {

                            dos.write(buffer, 0, bufferSize);
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math
                                    .min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0,
                                    bufferSize);

                        }

                        // send multipart form data necesssary after file
                        // data...
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens
                                + lineEnd);

                        dos.writeBytes("Content-Disposition: form-data; name=\"id\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(asuID);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens
                                + lineEnd);

                        dos.writeBytes("Content-Disposition: form-data; name=\"day\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(date);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens
                                + lineEnd);

                        // Responses from the server (code and message)
                        LocalDateTime dt1=LocalDateTime.now();
                        int serverResponseCode = conn.getResponseCode();
                        String serverResponseMessage = conn
                                .getResponseMessage();

                        if (serverResponseCode == 200) {

                            System.out.println("done");
                            LocalDateTime dt2=LocalDateTime.now();
                            Duration duration = Duration.between(dt1, dt2);
                            long timeTakenSeconds = duration.toMillis();
                            long speed=fileSizeBytes/timeTakenSeconds;
                            double speedMb=speed/Math.pow(10, 3);
                            File root = new File(context.getExternalFilesDir(Environment.DIRECTORY_DCIM) + File.separator + "logFolder");
                            if (!root.exists()) {
                                root.mkdirs();
                            }
                            File logFile = new File(root, "Log.txt");
                            StringBuilder text;
                            if(logFile.exists()){
                                text=readFile(logFile);
                            }else{
                                text=new StringBuilder();
                            }
                            text.append('\n');
                            String line=Double.toString(speedMb)+" Mbps at "+dt2.format(DateTimeFormatter.ofPattern("dd/MM/yyyyHH:mm"));
                            text.append(line);
                            FileWriter writer = new FileWriter(logFile);
                            writer.append(text.toString());
                            writer.flush();
                            writer.close();
                        }

                        // close the streams //
                        fileInputStream.close();
                        dos.flush();
                        dos.close();

                    } catch (Exception e) {

                        // dialog.dismiss();
                        e.printStackTrace();

                    }
                    // dialog.dismiss();

                } // End else block


            } catch (Exception ex) {
                // dialog.dismiss();

                ex.printStackTrace();
            }
            return "Executed";
        }

        private StringBuilder readFile(File file) {
            StringBuilder text = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine())!= null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
            } catch (IOException e) { }
            return text;
        }
        @Override
        protected void onPostExecute(String result) {

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }


}
