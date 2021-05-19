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

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

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
    private ImageView mImageView;
    private String text;
//    Uri imageuri;
    String token;
    SharedPreferenceClass sharedPreferenceClass;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("ScreenshotService","wrong");
//        sharedPreferenceClass=new SharedPreferenceClass(this);
//        token = sharedPreferenceClass.getValue_string("token");
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
//        Toast.makeText(this, "Path::::"+path, Toast.LENGTH_LONG).show();
        //  file ka unique name k liye

//        String fileName;
//            Time t= new Time();
//            t.setToNow();
//            int timeFileMinute= t.minute;
//            int timeFileDate= t.yearDay;
//            int timeFileYear= t.year;
//
//            long timestamp=System.currentTimeMillis();
//
//            //creating file name
//            fileName= "notes-" +timeFileMinute + timeFileDate + timeFileYear + android.os.Build.SERIAL;



//        final Uri imageuri = Uri.fromFile(new File(path));
////        Toast.makeText(this, "Path==="+imageuri, Toast.LENGTH_LONG).show();
//
//        try {
//            image = InputImage.fromFilePath(this, imageuri);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//
//        /*--------------------------------*/
//        //ML wala code
//        /*--------------------------------*/
//
//        recognizer.process(image)
//                .addOnSuccessListener(
//                        new OnSuccessListener<Text>() {
//                            @Override
//                            public void onSuccess(Text texts) {
//                                text= texts.getText();
//                            }
//                        })
//                .addOnFailureListener(
//                        new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                // Task failed with an exception
//                                e.printStackTrace();
//                            }
//                        });
//
//        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageuri);
////        saveToInternalStorage(bitmap,fileName);
//
//        final HashMap<String, String> params = new HashMap<>();
//        params.put("text", text);
//        params.put("path", imageuri.toString());
//
//        String apiKey = "https://notesandroid.herokuapp.com/api/notes";
//
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, apiKey, new JSONObject(params), new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                try {
//                    if(response.getBoolean("success")){
//                        Toast.makeText(ScreenshotService.this,"Text saved successfully",Toast.LENGTH_LONG).show();
//                    }
////                    progressBar.setVisibility(View.GONE);
//                } catch (JSONException e) {
//                    e.printStackTrace();
////                    progressBar.setVisibility(View.GONE);
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//
//                NetworkResponse response = error.networkResponse;
//                if(error instanceof ServerError && response !=null){
//                    try {
//                        String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers,"utf-8"));
//                        JSONObject object= new JSONObject(res);
//                        //Toast.makeText(ScreenshotService.this, object.getString("msg"),Toast.LENGTH_LONG).show();
//
////                        progressBar.setVisibility(View.GONE);
//                    }catch (JSONException| UnsupportedEncodingException je){
//                        je.printStackTrace();
////                        progressBar.setVisibility(View.GONE);
//                    }
//                }
//
//            }
//        }) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                HashMap<String, String> headers = new HashMap<>();
//                headers.put("Content-Type", "application/json");
//                headers.put("Authorization", token);
//                return headers;
//            }
//        };
//        //set retry policy
//        final int socketTime = 3000;
//        RetryPolicy policy = new DefaultRetryPolicy(
//                socketTime, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
//        jsonObjectRequest.setRetryPolicy(policy);
//
//        //request add
//        RequestQueue requestQueue = Volley.newRequestQueue(this);
//        requestQueue.add(jsonObjectRequest);

        //Toast.makeText(this, "text:"+text, Toast.LENGTH_LONG).show();

        Intent activityIntent=new Intent(this,DialogActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activityIntent.putExtra("sspath",path);

        PendingIntent pendingIntent= PendingIntent.getActivity(this,0,activityIntent,PendingIntent.FLAG_UPDATE_CURRENT);



        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Editor")
                .setContentText("Select required texts")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Select required texts"))
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

}