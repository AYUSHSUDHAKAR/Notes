package com.example.firstapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.Time;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

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
//import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropOverlayView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class DialogActivity extends AppCompatActivity {
//    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ImageButton btBrowse, btReset, btDone;
    ImageView imageView;
    Uri uri;
    private FirebaseStorage storage;
    private InputImage image;
    TextRecognizer recognizer = TextRecognition.getClient();
    private String text;
    private Uri imageuri;
    String token;
    SharedPreferences sharedPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent=getIntent();
        String sspath = intent.getStringExtra("sspath");
        imageuri = Uri.fromFile(new File(sspath));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        storage = FirebaseStorage.getInstance();
        btBrowse= findViewById(R.id.bt_edit);
        btReset=findViewById(R.id.bt_reset);
        btDone=findViewById(R.id.bt_done);
        imageView=findViewById(R.id.image_view);
        imageView.setImageURI(imageuri);

        sharedPreference = getSharedPreferences("user_notes",MODE_PRIVATE);
        token=sharedPreference.getString("token","");
        btBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCrop(imageuri);
//                if (CropImage.isReadExternalStoragePermissionsRequired(this, imageuri)) {
//                    uri = imageuri;
//                    requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},0);
//                }
//                else{
//                    startCrop(imageuri);
//                }
            }
        });
        btReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageBitmap(null);
            }
        });
        btDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadText();
                finishAffinity();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result= CropImage.getActivityResult(data);
            imageView.setImageURI(result.getUri());
            Toast.makeText(this, "Image update Successfully!!!",
                    Toast.LENGTH_SHORT).show();

            try {
                image = InputImage.fromFilePath(getBaseContext(), result.getUri());
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


            Toast.makeText(getBaseContext(), "text:"+text, Toast.LENGTH_LONG).show();

        }
    }

    private void uploadText(){
        Toast.makeText(getBaseContext(),"upload called"+token,Toast.LENGTH_LONG).show();
        final HashMap<String, String> params = new HashMap<>();
        params.put("text", text);
        params.put("path", imageuri.toString());

        String apiKey = "https://notesandroid.herokuapp.com/api/notes";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, apiKey, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")){
                        Toast.makeText(DialogActivity.this,"Text saved successfully",Toast.LENGTH_LONG).show();
                    }
//                    progressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    Toast.makeText(DialogActivity.this,"Text saved failed",Toast.LENGTH_LONG).show();
                    e.printStackTrace();
//                    progressBar.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                NetworkResponse response = error.networkResponse;
                if(error instanceof ServerError && response !=null){
                    try {
                        String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers,"utf-8"));
                        JSONObject object= new JSONObject(res);
                        Toast.makeText(DialogActivity.this, object.getString("msg"),Toast.LENGTH_LONG).show();

//                        progressBar.setVisibility(View.GONE);
                    }catch (JSONException| UnsupportedEncodingException je){
                        je.printStackTrace();
//                        progressBar.setVisibility(View.GONE);
                    }
                }

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                return headers;
            }
        };
        //set retry policy
        final int socketTime = 3000;
        RetryPolicy policy = new DefaultRetryPolicy(
                socketTime, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);

        //request add
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    private void startCrop(Uri imageuri) {
        CropImage.activity(imageuri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(this);
    }
}