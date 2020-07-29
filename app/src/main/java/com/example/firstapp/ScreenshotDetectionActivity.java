package com.example.firstapp;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import static com.example.firstapp.App.CHANNEL_ID;

public abstract class ScreenshotDetectionActivity extends AppCompatActivity implements ScreenshotDetectionDelegate.ScreenshotDetectionListener {
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION = 3009;
//    private ScreenshotDetectionDelegate screenshotDetectionDelegate = new ScreenshotDetectionDelegate(this, this);
//    private static Boolean start = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkReadExternalStoragePermission();
//        if(start){
//            startScreenshot();
//        }else {
//            stopScreenshot();
//        }
    }

//    protected void startScreenshot() {
//        screenshotDetectionDelegate.startScreenshotDetection();
//    }
//
//    protected void stopScreenshot() {
//        screenshotDetectionDelegate.stopScreenshotDetection();
//    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        screenshotDetectionDelegate.startScreenshotDetection();
//    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        screenshotDetectionDelegate.stopScreenshotDetection();
//    }

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

    @Override
    public void onScreenCaptured(String path) {
        // Do something when screen was captured
    }

    @Override
    public void onScreenCapturedWithDeniedPermission() {
        // Do something when screen was captured but read external storage permission has denied
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

//    public class ScreenshotService extends Service{
//
////        private static final int NOTIF_ID = 1;
////        private static final String NOTIF_CHANNEL_ID = "Channel_Id";
//
//
//        @Override
//        public int onStartCommand(Intent intent, int flags, int startId) {
//            startScreenshot();
//            startForeground();
//            return START_STICKY;
//        }
//
//        @Override
//        public void onDestroy() {
//            super.onDestroy();
//            stopScreenshot();
//        }
//
//        private void startForeground() {
//            Intent notificationIntent = new Intent(this, MainActivity.class);
//
//            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
//                    notificationIntent, 0);
//
//            startForeground(1,new NotificationCompat.Builder(this,
//                    CHANNEL_ID) // don't forget create a notification channel first
//                    .setOngoing(true)
//                    .setSmallIcon(R.drawable.ic_notification)
//                    .setContentTitle(getString(R.string.app_name))
//                    .setContentText("Service is running background")
//                    .setContentIntent(pendingIntent)
//                    .build());
//        }
//
//        @Nullable
//        @Override
//        public IBinder onBind(Intent intent) {
//            return null;
//        }
//    }
}
