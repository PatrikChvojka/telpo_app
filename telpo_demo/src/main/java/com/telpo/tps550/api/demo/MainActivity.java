package com.telpo.tps550.api.demo;

import static com.telpo.tps550.api.demo.timedata.globalFunctions.*;
import static com.telpo.tps550.api.demo.timedata.ovladanieSvetiel.*;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.common.CommonConstants;
import com.common.apiutil.decode.DecodeReader;
import com.common.apiutil.pos.CommonUtil;
import com.common.callback.IDecodeReaderListener;
import com.common.demo.R;
import com.telpo.tps550.api.demo.decode.KeyEventResolver;

import android.widget.EditText;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends Activity implements KeyEventResolver.OnScanSuccessListener {

	private TextView textRestult;
	boolean isreading;
	private DecodeReader mDecodeReader;
	private KeyEventResolver mKeyEventResolver;

	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (mDecodeReader != null) {
			mDecodeReader.close();
		}
		isreading = false;
	}

	/*
	* FUNKCIE:
	* greenLightOn(); 	- zapne zelene diodky
	* redLightOn();	- zapne cervene diodky
	*
	* */

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// hide status bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// custom home screen
		setContentView(R.layout.front);

		textRestult = (TextView) findViewById(R.id.textRestult);
		textRestult.setInputType(0);
		textRestult.setSingleLine(false);
		textRestult.setHorizontallyScrolling(false);

	}

	// vyčisti byte array


	protected void onResume()  {
		super.onResume();
		if (mDecodeReader == null) {
			mDecodeReader = new DecodeReader(this);//初始化
		}
		mKeyEventResolver = new KeyEventResolver(this);
		mDecodeReader.setDecodeReaderListener(new IDecodeReaderListener() {

			@Override
			public void onRecvData(final byte[] data) {

				try {
					// SKENOVANIE
					final String str = new String(trim(data), "UTF-8");
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// api call
							int api_result = api_call(str);
							textRestult.setText(Integer.toString(api_result));

							if(api_result == 1){
								Log.w("app", "zapinam zelene svetlo");
								greenLightOn();
							}else{
								Log.w("app", "zapinam červene svetlo");
								redLightOn();
							}
						}
					});

				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		});

		// ZAPNI SKENOVANIE PO ZAPNUTI A NASTAV DIODKY
		textRestult.setText("Pripravený skenovať?");
		greenLightOff();
		redLightOff();
		int ret = mDecodeReader.open(115200);

	}

	@Override
	public void onScanSuccess(String barcode) {
		textRestult.setText(barcode);
	}

	@Override
	public boolean dispatchKeyEvent(android.view.KeyEvent event) {
		if ("Virtual".equals(event.getDevice().getName())) {
			return super.dispatchKeyEvent(event);
		}
		mKeyEventResolver.analysisKeyEvent(event);
		return true;
	}

}

