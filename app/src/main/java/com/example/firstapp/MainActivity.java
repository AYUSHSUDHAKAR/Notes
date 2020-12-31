package com.example.firstapp;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity {
//    private Button start,stop,logout;
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION = 3009;

    SharedPreferenceClass sharedPreferenceClass;
    SearchView searchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();

        if(id== R.id.search){
            Toast.makeText(getApplicationContext(),"search clicked",Toast.LENGTH_SHORT).show();
            if(searchView.getVisibility()==View.VISIBLE){
                searchView.setVisibility(View.GONE);
            }else{
                searchView.setVisibility(View.VISIBLE);
            }
        }else if(id== R.id.stop){
            Toast.makeText(getApplicationContext(),"stop clicked",Toast.LENGTH_SHORT).show();
            stopService(new Intent(MainActivity.this, ScreenshotService.class));
        }if(id== R.id.start){
            Toast.makeText(getApplicationContext(),"start clicked",Toast.LENGTH_SHORT).show();
            startService(new Intent(MainActivity.this, ScreenshotService.class));
        }if(id== R.id.logout){
            Toast.makeText(getApplicationContext(),"logout clicked",Toast.LENGTH_SHORT).show();
            sharedPreferenceClass.clear();
            stopService(new Intent(MainActivity.this, ScreenshotService.class));
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//
        checkReadExternalStoragePermission();

        searchView=findViewById(R.id.search);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedPreferenceClass = new SharedPreferenceClass(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    showReadExternalStoragePermissionDeniedMessage();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }



    private void checkReadExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestReadExternalStoragePermission();
        }
    }

    private void requestReadExternalStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION);
    }

    private void showReadExternalStoragePermissionDeniedMessage() {
        Toast.makeText(this, "Read external storage permission has denied", Toast.LENGTH_SHORT).show();
    }

}