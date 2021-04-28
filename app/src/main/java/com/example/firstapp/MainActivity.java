package com.example.firstapp;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
//    private Button start,stop,logout;
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION = 3009;

    SharedPreferenceClass sharedPreferenceClass;
    SearchView searchView;
    RequestQueue requestQueue;
    ImageView imageView;

    List<String> paths = new ArrayList<>();

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

        imageView=findViewById(R.id.imageview);
        searchView=findViewById(R.id.search);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedPreferenceClass = new SharedPreferenceClass(this);

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {


                final HashMap<String, String> params = new HashMap<>();
                params.put("text", query);

                final JSONObject parameters = new JSONObject(params);



                String apiKey = "https://notesandroid.herokuapp.com/api/notes/search";
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, apiKey, new JSONObject(params), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(MainActivity.this,"Result  ++"+response.toString(),Toast.LENGTH_LONG).show();
                        try{
                            JSONArray jsonArray = response.getJSONArray("result");
                            int length = jsonArray.length();
                            for(int i = 0; i < jsonArray.length(); i++){
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String path = jsonObject.getString("path");

                                paths.add(path);
                            }
//                            Uri imageUri = Uri.parse(paths.get(0));
//                            imageView.setImageURI(imageUri);
                            Toast.makeText(MainActivity.this,"Result  "+paths,Toast.LENGTH_LONG).show();
                        }catch (JSONException e){
                            Toast.makeText(MainActivity.this,"No Text Found"+e,Toast.LENGTH_LONG).show();
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
                                Toast.makeText(MainActivity.this, object.getString("msg"),Toast.LENGTH_LONG).show();

//                        progressBar.setVisibility(View.GONE);
                            }catch (JSONException| UnsupportedEncodingException je){
                                je.printStackTrace();
//                        progressBar.setVisibility(View.GONE);
                            }
                        }

                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
//                        headers.put("text", query);
                        return headers;
                    }
                };

//                final int socketTime = 9000;
//                RetryPolicy policy = new DefaultRetryPolicy(
//                        socketTime, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
//                jsonObjectRequest.setRetryPolicy(policy);

                //request add

                requestQueue.add(jsonObjectRequest);



                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

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