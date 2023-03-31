package com.telpo.tps550.api.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.common.demo.R;

public class LoginFront extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // hide status bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // custom home screen
        setContentView(R.layout.loginfront);

    }

}
