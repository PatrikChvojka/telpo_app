package com.telpo.tps550.api.demo;

import static com.telpo.tps550.api.demo.timedata.globalFunctions.*;
import static com.telpo.tps550.api.demo.timedata.ovladanieSvetiel.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.common.apiutil.pos.CommonUtil;
import com.common.demo.R;
import com.telpo.tps550.api.demo.timedata.ticketHTTPClient;
import com.telpo.tps550.api.demo.timedata.timedataAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class scanningPageResult extends Activity  {

    private TextView showTime, zone1time, zone2time, zostavajucikredit, doplatok, cisloskrinky, nazovvstupu;
    // OkHttpClient client;
    ticketHTTPClient ticketClient;
    timedataAPI timedataAPI;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // hide status bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // custom home screen
        setContentView(R.layout.scanning_page_result);

        ////////////////////////////////////////////////////////////////////// show time in header
        showTime = (TextView) findViewById(R.id.showTime);
        Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                handler2.postDelayed(this, 1000);
                // Date
                Date now = new Date();
                TimeZone.setDefault(TimeZone.getTimeZone("Europe/Bratislava"));
                SimpleDateFormat formatMinutes = new SimpleDateFormat("mm");
                String getMinutes = formatMinutes.format(now);
                SimpleDateFormat formatHours = new SimpleDateFormat("HH");
                String getHours = formatHours.format(now);
                SimpleDateFormat formatSeconds = new SimpleDateFormat("ss");
                String getSeconds = formatSeconds.format(now);
                showTime.setText(getHours + ":" + getMinutes + ":" + getSeconds);
            }
        }, 1000);
        ////////////////////////////////////////////////////////////////////// show time in header




        ////////////////////////////////////////////////////////////////////// GET DATA

        ticketClient = new ticketHTTPClient();
        timedataAPI = new timedataAPI();
        final JSONObject[] jsonObject = {null};

        try {
            ticketClient.checkTicketValidity("12345"); // kod: 12345

            // Odosle k nam do systemu na webe log
            timedataAPI.sendLogData("Checked Ticket Code", "Ticket Code: 12345");

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Wait for response from server and in the meantime show loading screen
        // Wait for apiKey to be available
        final Handler handler = new Handler();
        final int delay = 1000; // 1000 milliseconds == 1 second

        handler.postDelayed(new Runnable() {
            public void run() {
                if (!ticketClient.ticketResponseReceived) {
                    // Show loading screen or something
                    System.out.println("Waiting for a response from ticket provider");
                    handler.postDelayed(this, delay);
                }
                else {

                    String _ticketValidity = "Lístok neplatný";

                    // Response available, show screen with data
                    if (ticketClient._cacheTicketData != "") {
                        _ticketValidity = "Lístok platný";
                    }

                    // resultTextView.setText(_ticketValidity);

                    try {
                        jsonObject[0] = new JSONObject(ticketClient._cacheTicketData);
                        Log.i("FirstName", jsonObject[0].getString("FirstName"));


                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }


                    // {"CustomerId":0,"Gender":0,"FirstName":"Test","LastName":"Infoterminál","FullName":"Test Infoterminál","Status":1,"CreditAccountAllowed":false,"CreditBalance":0.0,"AvailableTicketsNames":[],"OpennedTransaction":{"TransactionId":0,"EntryDate":"0001-01-01T00:00:00","TotalTime":"00:00:00","Zone1Time":0.0,"Zone2Time":0.0,"KeyNumber":null,"TicketName":null,"Surcharge":0.0,"IsValid":false,"IsBusy":false},"IsValid":false,"IsBusy":false}


                }
            }
        }, delay);

        ////////////////////////////////////////////////////////////////////// GET DATA






        //////////////////////////////////////////////////////////////////////  redirect after xy seconds
        /*new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // redirect
                        startActivity(new Intent(scanningPageResult.this, scanningPage.class));
                    }}, 5000);*/
        //////////////////////////////////////////////////////////////////////  redirect after xy seconds

    }
}
