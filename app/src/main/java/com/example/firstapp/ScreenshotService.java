package com.example.firstapp;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.format.Time;
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
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.example.firstapp.App.CHANNEL_ID_1;
import static com.example.firstapp.App.CHANNEL_ID_2;

public class ScreenshotService extends Service implements ScreenshotDetectionDelegate.ScreenshotDetectionListener {

    private AlertDialog.Builder builder;
    Uri filePath;
    private StorageReference storageReference;
    private FirebaseStorage storage;
    private ScreenshotDetectionDelegate screenshotDetectionDelegate = new ScreenshotDetectionDelegate(this, this);
    private InputImage image;
    TextRecognizer recognizer = TextRecognition.getClient();
    private String text;


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
    public void onScreenCaptured(final String path) throws IOException {
        Toast.makeText(this, "Path"+path, Toast.LENGTH_LONG).show();

        String fileName;
            Time t= new Time();
            t.setToNow();
            int timeFileMinute= t.minute;
            int timeFileDate= t.yearDay;
            int timeFileYear= t.year;

            long timestamp=System.currentTimeMillis();

            //creating file name
            fileName= "notes-" +timeFileMinute + timeFileDate + timeFileYear + android.os.Build.SERIAL;

        final Uri imageuri = Uri.fromFile(new File(path));
        try {
            image = InputImage.fromFilePath(this, imageuri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        recognizer.process(image)
                .addOnSuccessListener(
                        new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text texts) {
                                text= texts.getText();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                e.printStackTrace();
                            }
                        });

        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageuri);

        saveToInternalStorage(bitmap,fileName);

        Toast.makeText(this, "text:"+text, Toast.LENGTH_LONG).show();

        Intent activityIntent=new Intent(this,DialogActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activityIntent.putExtra("sspath",path);

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
    }

    @Override
    public void onScreenCapturedWithDeniedPermission() {
        Toast.makeText(this, "Please grant read external storage permission for screenshot detection", Toast.LENGTH_LONG).show();
    }

    private void saveToInternalStorage(Bitmap bitmapImage,String name){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,name);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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