package com.telpo.tps550.api.demo;

import static com.telpo.tps550.api.demo.timedata.globalFunctions.*;
import static com.telpo.tps550.api.demo.timedata.ovladanieSvetiel.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TableLayout;
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

        nazovvstupu = (TextView) findViewById(R.id.nazovvstupu);
        cisloskrinky = (TextView) findViewById(R.id.cisloskrinky);
        doplatok = (TextView) findViewById(R.id.doplatok);
        zostavajucikredit = (TextView) findViewById(R.id.zostavajucikredit);
        zone1time = (TextView) findViewById(R.id.zone1time);
        zone2time = (TextView) findViewById(R.id.zone2time);

        ProgressBar pgsBar = (ProgressBar)findViewById(R.id.pBar);
        TableLayout tableLayout2 = (TableLayout)findViewById(R.id.tableLayout2);

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

                        // get openet transaction data
                        JSONObject OpennedTransaction = new JSONObject(jsonObject[0].getString("OpennedTransaction"));

                        // print data
                        if(OpennedTransaction.has("TicketName")) {      nazovvstupu.setText(OpennedTransaction.getString("TicketName")); }
                        if(OpennedTransaction.has("KeyNumber")) {       cisloskrinky.setText(OpennedTransaction.getString("KeyNumber")); }
                        if(OpennedTransaction.has("Surcharge")) {
                            doplatok.setText(String.format("%s EUR", OpennedTransaction.getString("Surcharge")));
                        } else {
                            zostavajucikredit.setText("0.00 EUR");
                        }
                        if(OpennedTransaction.has("CreditBalance")) {
                            zostavajucikredit.setText(String.format("%s EUR", OpennedTransaction.getString("CreditBalance")));
                        } else {
                            zostavajucikredit.setText("0.00 EUR");
                        }

                        if(OpennedTransaction.has("Zone1Time")) {
                            Double minutes2 = Double.parseDouble(OpennedTransaction.getString("Zone1Time"));
                            zone1time.setText(String.format("%s MINÚT", Math.round(minutes2)));
                        } else {
                            zone1time.setText("0 MINÚT");
                        }

                        if(OpennedTransaction.has("Zone2Time")) {
                            Double minutes2 = Double.parseDouble(OpennedTransaction.getString("Zone2Time"));
                            zone2time.setText(String.format("%s MINÚT", Math.round(minutes2)));
                        } else {
                            zone2time.setText("0 MINÚT");
                        }

                        // hide loader
                        pgsBar.setVisibility(View.GONE);
                        tableLayout2.setVisibility(View.VISIBLE);



                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
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
