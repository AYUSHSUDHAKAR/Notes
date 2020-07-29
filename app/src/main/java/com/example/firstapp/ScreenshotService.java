package com.example.firstapp;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import static com.example.firstapp.App.CHANNEL_ID;

public class ScreenshotService extends Service implements ScreenshotDetectionDelegate.ScreenshotDetectionListener {


private ScreenshotDetectionDelegate screenshotDetectionDelegate = new ScreenshotDetectionDelegate(this, this);


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("ScreenshotService","wrong");
        startForeground();
        screenshotDetectionDelegate.startScreenshotDetection();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        screenshotDetectionDelegate.stopScreenshotDetection();
    }

    private void startForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        startForeground(1,new NotificationCompat.Builder(this,
                CHANNEL_ID) // don't forget create a notification channel first
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Service is running background")
                .setContentIntent(pendingIntent)
                .build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onScreenCaptured(String path) {
        Toast.makeText(this, "Path"+path, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onScreenCapturedWithDeniedPermission() {
        Toast.makeText(this, "Please grant read external storage permission for screenshot detection", Toast.LENGTH_LONG).show();
    }
}