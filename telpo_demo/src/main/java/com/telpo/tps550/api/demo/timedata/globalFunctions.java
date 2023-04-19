package com.telpo.tps550.api.demo.timedata;

import android.util.Log;

import com.telpo.tps550.api.demo.MainActivity;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class globalFunctions {


    public static String httpRequest (String $url){

        final OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url($url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure (Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse (Call call, Response response) throws IOException {
                if( response.isSuccessful() ){
                    final String myResponse = response.body().string();
                    Log.w("myApp", myResponse);
                }
            }
        });

        return $url;
    }

    public static int api_call (String $result){
        int randomNumber = (int) ((Math.random() * (2 - 0)) + 0);
        return randomNumber;
    }

    public static byte[] trim(byte[] bytes)
    {
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] == 0){ --i; }
        return Arrays.copyOf(bytes, i + 1);
    }
}
