package com.example.firstapp;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import static com.example.firstapp.App.CHANNEL_ID_1;
import static com.example.firstapp.App.CHANNEL_ID_2;

public class ScreenshotService extends Service implements ScreenshotDetectionDelegate.ScreenshotDetectionListener {

    private AlertDialog.Builder builder;
    Uri filePath;
    private StorageReference storageReference;
    private FirebaseStorage storage;
    private ScreenshotDetectionDelegate screenshotDetectionDelegate = new ScreenshotDetectionDelegate(this, this);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("ScreenshotService","wrong");
        startForeground();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
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
                CHANNEL_ID_1) // don't forget create a notification channel first
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
    public void onScreenCaptured(final String path) {
        Toast.makeText(this, "Path"+path, Toast.LENGTH_LONG).show();
        Intent activityIntent=new Intent(this,DialogActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent= PendingIntent.getActivity(this,0,activityIntent,PendingIntent.FLAG_UPDATE_CURRENT);

//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_2);
//        builder.setContentIntent(pendingIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("My notification")
                .setContentText("Much longer text that cannot fit one line...")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Much longer text that cannot fit one line..."))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1002, builder.build());

        //        uploadImage();

//        builder = new AlertDialog.Builder(this);
////        filePath = Uri.fromFile(new File(path));
//
//        builder.setMessage("Do you want to save Screenshot?")
//                .setCancelable(false)
//                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        //finish();
//                        Toast.makeText(getApplicationContext(),"you choose yes action for alertbox",
//                                Toast.LENGTH_SHORT).show();
//
////                        uploadImage();
//
//                    }
//                })
//                .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        Toast.makeText(getApplicationContext(),"you choose no action for alertbox",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//        AlertDialog alert = builder.create();
//        alert.setTitle("Confirmation");
//        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//        alert.show();
    }

    @Override
    public void onScreenCapturedWithDeniedPermission() {
        Toast.makeText(this, "Please grant read external storage permission for screenshot detection", Toast.LENGTH_LONG).show();
    }

    private void uploadImage()
    {
        if (filePath != null) {


//            final ProgressDialog progressDialog= new ProgressDialog(this);
//            progressDialog.setTitle("Uploading Image......");
//            progressDialog.show();

            StorageReference ref = storageReference.child("images/"+filePath.getLastPathSegment());

            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            //Uri downloadUrl = taskSnapshot.getDownloadUrl();
//                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"sucess",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
//                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"fail",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>(){
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            double processPercent =(100.00 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
//                            progressDialog.setMessage("Percentage: "+ (int) processPercent + "%");

                        }
                    });

        }
        else
        {
            Toast.makeText(this,"error",Toast.LENGTH_SHORT).show();
        }
    }
}