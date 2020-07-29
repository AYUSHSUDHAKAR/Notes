package com.example.firstapp;


import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    private Button start,stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(MainActivity.this,ScreenshotService.class));
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this,ScreenshotService.class));
            }
        });
    }

//    @Override
//    public void onScreenCaptured(String path) {
//        Toast.makeText(this, "Path"+path, Toast.LENGTH_LONG).show();
//    }
//
//    @Override
//    public void onScreenCapturedWithDeniedPermission() {
//        Toast.makeText(this, "Please grant read external storage permission for screenshot detection", Toast.LENGTH_LONG).show();
//    }
}