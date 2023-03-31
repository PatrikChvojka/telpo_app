package com.telpo.tps550.api.demo;

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
	private Button nastavsvetlo, vypnisvetlo;


	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (mDecodeReader != null) {
			mDecodeReader.close();
		}
		isreading = false;
	}

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
	static byte[] trim(byte[] bytes)
	{
		int i = bytes.length - 1;
		while (i >= 0 && bytes[i] == 0){ --i; }
		return Arrays.copyOf(bytes, i + 1);
	}

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
							textRestult.setText(str);

							final String BASE_URL = "https://progresivneaplikacie.sk/project/telpo_java/index.php";

							final OkHttpClient client = new OkHttpClient();
							RequestBody formBody = new FormBody.Builder()
									.add("scanned", str)
									.build();
							Request request = new Request.Builder()
									.url(BASE_URL)
									.post(formBody)
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

										MainActivity.this.runOnUiThread(new Runnable(){
											@Override
											public void run () {
												Log.w("myApp", myResponse);
											}
										});
									}
								}
							});

						}
					});
					Log.w("myApp", "Oskenovane cez onResume");

				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		// ZAPNI SKENOVANIE PO ZAPNUTI
		textRestult.setText("Pripravený skenovať?");
		int ret = mDecodeReader.open(115200);

	}

	@Override
	public void onScanSuccess(String barcode) {
		textRestult.setText(barcode);
		Log.w("myApp", "Oskenovane cez onScanSuccess");
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

