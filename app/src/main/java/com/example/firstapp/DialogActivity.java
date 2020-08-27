package com.example.firstapp;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.theartofdev.edmodo.cropper.CropOverlayView;

import java.io.File;

public class DialogActivity extends AppCompatActivity {
    ImageButton btBrowse, btReset;
    ImageView imageView;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent=getIntent();
        String sspath = intent.getStringExtra("sspath");
        final Uri imageuri = Uri.fromFile(new File(sspath));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        btBrowse= findViewById(R.id.bt_edit);
        btReset=findViewById(R.id.bt_reset);
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
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result= CropImage.getActivityResult(data);
                imageView.setImageURI(result.getUri());
                Toast.makeText(this, "Image update Successfully!!!",
                        Toast.LENGTH_SHORT).show();
        }
    }

    private void startCrop(Uri imageuri) {
        CropImage.activity(imageuri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(this);
    }
}