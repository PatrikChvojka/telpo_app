package com.telpo.tps550.api.demo.timedata;
import static androidx.core.content.ContextCompat.startActivity;
import static com.telpo.tps550.api.demo.timedata.ovladanieSvetiel.greenLightOn;
import static com.telpo.tps550.api.demo.timedata.ovladanieSvetiel.redLightOn;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.common.apiutil.pos.CommonUtil;
import com.telpo.tps550.api.demo.MainActivity;
import com.telpo.tps550.api.demo.scanningPage;
import com.telpo.tps550.api.demo.scanningPageResult;

import java.util.Arrays;

public class globalFunctions {

    public static int api_call (String $result){
        int randomNumber = (int) ((Math.random() * (2 - 0)) + 0);
        return randomNumber;
    }

    public static boolean contains( String haystack, String needle ) {
        haystack = haystack == null ? "" : haystack;
        needle = needle == null ? "" : needle;

        // Works, but is not the best.
        //return haystack.toLowerCase().indexOf( needle.toLowerCase() ) > -1

        return haystack.toLowerCase().contains( needle.toLowerCase() );
    }

    public static byte[] trim(byte[] bytes)
    {
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] == 0){ --i; }
        return Arrays.copyOf(bytes, i + 1);
    }

    // časove zapinanie relatka

    public static void otvorTurniket() {

        // otvor
        Log.w("turniket ", "otvaram");
        CommonUtil.setRelayPower(1,1);
        greenLightOn();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // zatvor
                        Log.i("turniket","zatvaram");
                        CommonUtil.setRelayPower(1,0);
                        redLightOn();
                    }
                }, 5000);
    }

    public static double roundAvoid(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }
}
