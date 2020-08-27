package com.example.firstapp;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class ScreenshotDetectionDelegate {
    private WeakReference<Service> serviceWeakReference;
//    private WeakReference<Activity> activityWeakReference;
    private ScreenshotDetectionListener listener;

    public ScreenshotDetectionDelegate(Service serviceWeakReference, ScreenshotDetectionListener listener) {
        this.serviceWeakReference = new WeakReference<>(serviceWeakReference);
        this.listener = listener;
    }

//    public ScreenshotDetectionDelegate(Activity activityWeakReference, ScreenshotDetectionListener listener) {
//        this.activityWeakReference = new WeakReference<>(activityWeakReference);
//        this.listener = listener;
//    }

    public void startScreenshotDetection() {
        serviceWeakReference.get()
                .getContentResolver()
                .registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, contentObserver);
    }

    public void stopScreenshotDetection() {
        serviceWeakReference.get().getContentResolver().unregisterContentObserver(contentObserver);
    }

    private ContentObserver contentObserver = new ContentObserver(new Handler()) {
        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (isReadExternalStoragePermissionGranted()) {
                String path = getFilePathFromContentResolver(serviceWeakReference.get(), uri);
                if (isScreenshotPath(path)) {
                    try {
                        onScreenCaptured(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                onScreenCapturedWithDeniedPermission();
            }
        }
    };

    private void onScreenCaptured(String path) throws IOException {
        if (listener != null) {
            listener.onScreenCaptured(path);
        }
    }

    private void onScreenCapturedWithDeniedPermission() {
        if (listener != null) {
            listener.onScreenCapturedWithDeniedPermission();
        }
    }

    private boolean isScreenshotPath(String path) {
        return path != null && path.toLowerCase().contains("screenshots");
    }

    private String getFilePathFromContentResolver(Context context, Uri uri) {
        try {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATA
            }, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                cursor.close();
                return path;
            }
        } catch (IllegalStateException ignored) {
        }
        return null;
    }

    private boolean isReadExternalStoragePermissionGranted() {
        return ContextCompat.checkSelfPermission(serviceWeakReference.get(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public interface ScreenshotDetectionListener {
        void onScreenCaptured(String path) throws IOException;

        void onScreenCapturedWithDeniedPermission();
    }
}