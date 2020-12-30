package com.example.firstapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
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

import java.io.File;
import java.io.IOException;

public class DialogActivity extends AppCompatActivity {
//    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ImageButton btBrowse, btReset, btDone;
    ImageView imageView;
    Uri uri;
    private FirebaseStorage storage;
    private InputImage image;
    TextRecognizer recognizer = TextRecognition.getClient();
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent=getIntent();
        String sspath = intent.getStringExtra("sspath");
        final Uri imageuri = Uri.fromFile(new File(sspath));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        storage = FirebaseStorage.getInstance();
        btBrowse= findViewById(R.id.bt_edit);
        btReset=findViewById(R.id.bt_reset);
        btDone=findViewById(R.id.bt_done);
        imageView=findViewById(R.id.image_view);
        imageView.setImageURI(imageuri);

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

    private void startCrop(Uri imageuri) {
        CropImage.activity(imageuri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(this);
    }
}