package com.example.assignment2;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteException;

import com.arthenica.mobileffmpeg.BuildConfig;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    final static int APP_STORAGE_ACCESS_REQUEST_CODE = 501;

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyStoragePermissions(this);

        EditText userNameText=(EditText)findViewById(R.id.editTextTextUserName);
        EditText passwordText=(EditText)findViewById(R.id.editTextTextPassword);

        Button loginBtn=(Button) findViewById(R.id.button);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(userNameText.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter user name first",Toast.LENGTH_LONG).show();
                }else if(passwordText.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter password first",Toast.LENGTH_LONG).show();
                }else{
                    Intent intent=new Intent(MainActivity.this, MainActivity2.class);
                    intent.putExtra(CommonUtilities.USER_NAME, userNameText.getText().toString());
                    intent.putExtra(CommonUtilities.PASSWORD, passwordText.getText().toString());
                    startActivity(intent);
                }
            }
        });
    }

}