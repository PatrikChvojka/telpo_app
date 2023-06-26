package com.telpo.tps550.api.demo;

import static androidx.core.content.ContextCompat.startActivity;
import static com.telpo.tps550.api.demo.timedata.globalFunctions.*;
import static com.telpo.tps550.api.demo.timedata.ovladanieSvetiel.*;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import com.common.apiutil.decode.DecodeReader;
import com.common.apiutil.nfc.PN512;
import com.common.apiutil.util.StringUtil;
import com.common.apiutil.util.SystemUtil;
import com.common.callback.IDecodeReaderListener;
import com.common.demo.R;
import com.telpo.tps550.api.demo.decode.KeyEventResolver;

import java.io.UnsupportedEncodingException;
import java.util.TimeZone;

public class scanningPage extends Activity implements KeyEventResolver.OnScanSuccessListener {


    private TextView textRestult, showTime;
    boolean isreading;
    private DecodeReader mDecodeReader;
    private KeyEventResolver mKeyEventResolver;

	// nfc
	private TextView tv_show_nfc2;
	private NfcAdapter mNfcAdapter;
	private PendingIntent mPendingIntent;
	String tempSuccessData;
	boolean firstData = false;
	int successCount = 0;
	boolean read = false;
	ImageView headBitmap;
	long validCardNum = 0;
	private Handler handler1 = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 2:
					new scanningPage.readNFC().execute();
					break;
				default:
					break;
			}
		};
	};


    /*
     * FUNKCIE:
     * greenLightOn(); 	- zapne zelene diodky
     * redLightOn();	- zapne cervene diodky
     * otvorTurniket(); - otvor turniket
     *
     * */

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // hide status bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // custom home screen
        setContentView(R.layout.scanning_page);

		textRestult = (TextView) findViewById(R.id.textRestult);
		textRestult.setInputType(0);
		textRestult.setSingleLine(false);
		textRestult.setHorizontallyScrolling(false);


		// TEST BUTTON
		// button
		Button buttonCheck = findViewById(R.id.buttonCheck);
		buttonCheck.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(scanningPage.this, scanningPageResult.class));
			}
		});


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


		////////////////// nfc
		tv_show_nfc2 = (TextView) findViewById(R.id.tv_show_nfc2);

		if(SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS573.ordinal() ){
			read = true;
			new scanningPage.readNFC().execute();
		}else{
			NfcManager mNfcManager = (NfcManager) getSystemService(Context.NFC_SERVICE);
			mNfcAdapter = mNfcManager.getDefaultAdapter();
			mPendingIntent =PendingIntent.getActivity(this, 0, new Intent(this,getClass()), 0);
			init_NFC();
		}
		////////////////// nfc

    }

	private class readNFC extends AsyncTask<Void, Integer, Long> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected Long doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return Long.parseLong(PN512.readnfc(), 16);
		}

		@Override
		protected void onPostExecute(Long result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(result==-1){
				tv_show_nfc2.setText("nieco");
			}else{
				validCardNum = result;
				tv_show_nfc2.setText("nieco2:"+result);
			}
			if(read){
				handler1.sendEmptyMessageDelayed(2, 100);
			}

		}

	}



	@Override
	public void onPause() {
		super.onPause();
		if (mNfcAdapter != null) {
			stopNFC_Listener();
		}
	}

	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (mDecodeReader != null) {
			mDecodeReader.close();
		}
		isreading = false;
		read = false;
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
							// api call
							int api_result = api_call(str);
							textRestult.setText(Integer.toString(api_result));

							if(api_result == 1){
								Log.w("app", "zapinam zelene svetlo");
								greenLightOn();
								// otvor turniket
								otvorTurniket();
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
		textRestult.setText("Tu bude výsledok z QR kódu");
		greenLightOff();
		redLightOff();
		int ret = mDecodeReader.open(115200);

		// nfc
		if (mNfcAdapter != null) {
			mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
			if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(this.getIntent().getAction())) {
				processIntent(this.getIntent());
			}
		}

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


	// nfc

	private String bytesToHexString(byte[] bytes) {
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			int hex = (0xff & bytes[i]);
			String tmp = Integer.toHexString(hex);
			tmp = (tmp.length() < 2) ? "0" + tmp : tmp; // if tmp=="9" => tmp=="09"
			hexString.append(tmp);
		}

		return hexString.toString().toUpperCase();
	}

	private void init_NFC() {
		IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
		tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
	}

	private void stopNFC_Listener() {
		mNfcAdapter.disableForegroundDispatch(this);
	}

	@Override
	public void onNewIntent(Intent intent) {
		processIntent(intent);
	}


	long getCardTime = 0;
	public void processIntent(Intent intent) {

		// 1) Parse the intent and get the action that triggered this intent
		String action = intent.getAction();
		Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		MifareClassic mfc = MifareClassic.get(tagFromIntent);

		byte[] data;

		try {       //  5.1) Connect to card
			mfc.connect();
			boolean auth = false;
			int secCount = mfc.getSectorCount();
			int bCount = 0;
			int bIndex = 0;
			for(int j = 0; j < secCount; j++){
				byte[] bytesKey = hex2Bytes("5FFFFFFFFFF9");

				auth = mfc.authenticateSectorWithKeyA(j,bytesKey );
				if(auth){
					bCount = mfc.getBlockCountInSector(j);
					bIndex = 0;
					for(int i = 0; i < bCount; i++){
						bIndex = mfc.sectorToBlock(j);
						data = mfc.readBlock(bIndex);


						// PRINT

						String temp = ByteArrayToHexString(data);
						Log.i("temp", temp);




						bIndex++;
					}
				}else{ // Authentication failed - Handle it
					//Log.i("auth sector", String.valueOf(j) + " not");
				}
			}
		}catch (IOException e) {
			Log.e("tag", e.getLocalizedMessage());
		}
	}



	public String convertHexToString(String hex) {

		StringBuilder sb = new StringBuilder();
		StringBuilder temp = new StringBuilder();

		//49204c6f7665204a617661 split into two characters 49, 20, 4c...
		for (int i = 0; i < hex.length() - 1; i += 2) {

			//grab the hex in pairs
			String output = hex.substring(i, (i + 2));
			//convert hex to decimal
			int decimal = Integer.parseInt(output, 16);
			//convert the decimal to character
			sb.append((char) decimal);

			temp.append(decimal);
		}
		System.out.println("Decimal : " + temp.toString());

		return sb.toString().trim();
	}

	private String ByteArrayToHexString(byte[] inarray) {
		int i, j, in;
		String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A",
				"B", "C", "D", "E", "F"};
		String out = "";

		for (j = 0; j < inarray.length; ++j) {
			in = inarray[j] & 0xff;
			i = (in >> 4) & 0x0f;
			out += hex[i];
			i = in & 0x0f;
			out += hex[i];
		}
		return out;
	}

	public static byte[] hex2Bytes(String hex) {
		if (!(hex != null && hex.length() % 2 == 0
				&& hex.matches("[0-9A-Fa-f]+"))) {
			return null;
		}
		int len = hex.length();
		byte[] data = new byte[len / 2];
		try {
			for (int i = 0; i < len; i += 2) {
				data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
						+ Character.digit(hex.charAt(i+1), 16));
			}
		} catch (Exception e) {
			Log.d("LOG_TAG", "Argument(s) for hexStringToByteArray(String s)"
					+ "was not a hex string");
		}
		return data;
	}

}
