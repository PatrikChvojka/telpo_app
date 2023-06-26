package com.telpo.tps550.api.demo.timedata;

import androidx.annotation.NonNull;

import java.io.IOException;

import android.os.Handler;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import okhttp3.RequestBody;
import okhttp3.FormBody;

public class ticketHTTPClient {

    // Init OkHttpClient
    OkHttpClient client;

    // iClub Connection Setup
    private String iClubAuthURL = "https://online.iclub.sk/api/oauth/token";
    private String iClubTicketURL = "https://online.iclub.sk/api/Person/GetCustomerInfo";

    private String client_user =  "demo";
    private String grant_type = "client_credentials";
    private String client_id = "demo_partner";
    private String client_secret = "5B8813DC-E167-49D4-AF1A-556B96C340F8";

    // Holds accessTokenData
    // iClub: access_token, token_type, expires_in (seconds)
    public String ticketServiceAccessTokenData = "";

    public Integer apiKeyExpiration = 0;

    // Cache helpers
    public String _cacheTicketCode = "";
    public Boolean _cacheTicketStatus;

    public String _cacheTicketData = "";

    public Boolean apiKeyAvailable;

    public Boolean ticketResponseReceived;

    // Get api key validation date, if invalid, request new api key from provider
    private void checkAPIkey() throws JSONException {

        // iClub login:
        // meno: demo@iclub.sk
        // heslo: demo

        apiKeyAvailable = false;

        // Get current system timestamp
        Long timestamp = System.currentTimeMillis() / 1000;

        if (ticketServiceAccessTokenData != "") {

            /*
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(ticketServiceAccessTokenData);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            */

            // if (Integer.parseInt(String.valueOf(timestamp)) + Integer.parseInt(jsonObject.getString("expires_in")) < apiKeyExpiration) {

            if (Integer.parseInt(String.valueOf(timestamp)) > apiKeyExpiration) {

                // Api key expired, request new api key
                requestNewApiKey();

            }
            else {

                // Api key available
                apiKeyAvailable = true;

            }

        }
        else {

            // Empty, request api key first
            requestNewApiKey();

        }

    }


    private void requestNewApiKey() {

        timedataAPI.sendLogData("requestNewApiKey()", "Requested new API key...");

        client = new OkHttpClient();

        RequestBody params = new FormBody.Builder()
                .add("client", client_user)
                .add("grant_type", grant_type)
                .add("client_id", client_id)
                .add("client_secret", client_secret)
                .build();

        Request request = new Request.Builder()
                .url(iClubAuthURL)
                .post(params)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {

                    apiKeyAvailable = true;

                    ticketServiceAccessTokenData = response.body().string();

                    Long timestamp = System.currentTimeMillis() / 1000;

                    apiKeyExpiration = Integer.parseInt(String.valueOf(timestamp)) + 3599;

                    timedataAPI.sendLogData("requestNewApiKey() response", ticketServiceAccessTokenData);

                    /*
                    Log.d("accessTokenData", String.valueOf(ticketServiceAccessTokenData));
                    Log.d("currentTimestamp", String.valueOf(timestamp));
                    Log.d("expirationTimestamp", String.valueOf(apiKeyExpiration));
                    */

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    // Validate ticket against ticket provider
    private void checkTicketStatus(String ticketCode) throws JSONException {

        client = new OkHttpClient();

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(ticketServiceAccessTokenData);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        Request request = new Request.Builder()
                .url(iClubTicketURL + "?client=" + client_user + "&cardCode=" + _cacheTicketCode)
                .addHeader("Authorization", "Bearer " + jsonObject.getString("access_token"))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {

                    ticketResponseReceived = true;

                    // _cacheTicketStatus = Boolean.parseBoolean(response.body().string());
                    _cacheTicketData = response.body().string();

                    Log.d("Ticket Data", _cacheTicketData);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void checkTicketValidity(String ticketCode) throws JSONException {

        // Reset values
        _cacheTicketCode = ticketCode;
        _cacheTicketStatus = false; // First set as False
        _cacheTicketData = "";
        ticketResponseReceived = false;

        // Init API key
        checkAPIkey();

        // Wait for apiKey to be available
        final Handler handler = new Handler();
        final int delay = 1000; // 1000 milliseconds == 1 second

        handler.postDelayed(new Runnable() {
            public void run() {
                if (!apiKeyAvailable) {
                    System.out.println("ApiKey not available - Requesting new ApiKey from provider");
                    handler.postDelayed(this, delay);
                }
                else {
                    // Api key is available
                    try {
                        checkTicketStatus(ticketCode);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }, delay);

    }

}
