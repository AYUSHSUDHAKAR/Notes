package com.example.firstapp;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class RequestQueueSigleton {
    private static RequestQueueSigleton requestQueueSigleton;
    private RequestQueue requestQueue;
    private static Context context;

    private RequestQueueSigleton(Context ctx){
        context=ctx;
        requestQueue = getRequestQueue();
    }

    public static synchronized RequestQueueSigleton getInstance(Context context){
        if (requestQueueSigleton == null){
            requestQueueSigleton = new RequestQueueSigleton(context);
        }
        return requestQueueSigleton;
    }

    public RequestQueue getRequestQueue(){
        if(requestQueue==null){
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }
}
