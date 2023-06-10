package com.telpo.tps550.api.demo;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.common.apiutil.decode.DecodeReader;
import com.common.apiutil.nfc.PN512;
import com.common.apiutil.util.StringUtil;
import com.common.apiutil.util.SystemUtil;
import com.common.callback.IDecodeReaderListener;
import com.common.demo.R;
import com.telpo.tps550.api.demo.decode.KeyEventResolver;
import com.telpo.tps550.api.demo.nfc.NfcActivity;

import java.io.UnsupportedEncodingException;
import java.util.Formatter;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

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

		////////////////////////////////////////////////////////////////////// show time in header
		showTime = (TextView) findViewById(R.id.showTime);
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				handler.postDelayed(this, 1000);

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
								// otvor turniket
								otvorTurniket();
							}else{
								Log.w("app", "zapinam červene svetlo");

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

	private String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("0x");
		if (src == null || src.length <= 0) {
			return null;
		}
		char[] buffer = new char[2];
		for (int i = 0; i < src.length; i++) {
			buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
			buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
			stringBuilder.append(buffer);
		}
		return stringBuilder.toString();
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

		String time = "";
		if(getCardTime == 0){
		}else{
			time = "" + (System.currentTimeMillis() - getCardTime);
		}
		getCardTime = System.currentTimeMillis();

		String data = "";
		Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

		String[] techList = tag.getTechList();
		byte[] ID = new byte[20];
		//data = tag.toString();
		ID =  tag.getId();
		//data += "\n\nUID:\n" +bytesToHexString(ID);
		data = "UID: " +bytesToHexString(ID);
		/*data += "\nData format:";
		for (String tech : techList) {
			data += "\n" + tech;
		}*/
		tv_show_nfc2.setText(data);

	}

}

