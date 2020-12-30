package com.example.firstapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import com.example.firstapp.UtilsService.UtilService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private Button loginBtn, registerBtn;
    private EditText nameText,emailText,passwordText;
    ProgressBar progressBar;

    private String name,email,password;

    UtilService utilService;
    SharedPreferenceClass sharedPreferenceClass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        loginBtn = findViewById(R.id.activity_loginbtn);
        registerBtn = findViewById(R.id.register_btn);
        nameText = findViewById(R.id.register_name);
        emailText = findViewById(R.id.register_email);
        passwordText = findViewById(R.id.register_password);

        utilService = new UtilService();
        sharedPreferenceClass = new SharedPreferenceClass(this);
//        progressBar = findViewById(R.id.progressbar);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                utilService.hideKeyboard(v,RegisterActivity.this);

                name = nameText.getText().toString();
                email = emailText.getText().toString();
                password = passwordText.getText().toString();
                
                if(validate(v)){
                    registerUser(v);
                }
            }
        });
    }

    private void registerUser(View view) {
//        progressBar.setVisibility(View.VISIBLE);
        final HashMap<String, String> params = new HashMap<>();
        params.put("username", name);
        params.put("email", email);
        params.put("password", password);

        String apiKey = "https://notesandroid.herokuapp.com/api/notes/auth/register";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, apiKey, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")){
                        String token = response.getString("token");
                        sharedPreferenceClass.setValue_string("token",token);
                        Toast.makeText(RegisterActivity.this,token,Toast.LENGTH_LONG).show();
                        startActivity(new Intent(RegisterActivity.this,MainActivity.class));
                    }
//                    progressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
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
                        Toast.makeText(RegisterActivity.this, object.getString("msg"),Toast.LENGTH_LONG).show();

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
                return params;
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

    public boolean validate(View view){
        boolean isValid;

        if(!TextUtils.isEmpty(name)){
            if(!TextUtils.isEmpty(email)){
                if(!TextUtils.isEmpty(password)){
                    isValid = true;
                }else {
                    utilService.showSnackBar(view,"please enter password...");
                    isValid = false;
                }
            }else {
                utilService.showSnackBar(view,"please enter email...");
                isValid = false;
            }
        }else {
            utilService.showSnackBar(view,"please enter name...");
            isValid = false;
        }
        return isValid;
    }

    protected void onStart(){
        super.onStart();
        SharedPreferences notes_pref = getSharedPreferences("user_notes",MODE_PRIVATE);

        if(notes_pref.contains("token")){
            startActivity(new Intent(RegisterActivity.this,MainActivity.class));
        }
    }
}