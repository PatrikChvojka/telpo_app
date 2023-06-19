package com.telpo.tps550.api.demo;

import static com.telpo.tps550.api.demo.timedata.globalFunctions.*;
import static com.telpo.tps550.api.demo.timedata.ovladanieSvetiel.*;
import static com.telpo.tps550.api.demo.util.DataProcessUtils.getHexString;

import static java.nio.charset.StandardCharsets.US_ASCII;

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
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
			String cardData = null;
			// 5.2) and get the number of sectors this card has..and loop thru these sectors
			int secCount = mfc.getSectorCount();
			//Log.i("sectors count: ", String.valueOf(secCount));

			int bCount = 0;
			int bIndex = 0;
			for(int j = 0; j < secCount; j++){

				byte[] bytesKey = hex2Bytes("5FFFFFFFFFF9");

				auth = mfc.authenticateSectorWithKeyA(j,bytesKey );
				if(auth){
					Log.i("auth sector", String.valueOf(j) + " yes");
					bCount = mfc.getBlockCountInSector(j);
					bIndex = 0;
					for(int i = 0; i < bCount; i++){
						bIndex = mfc.sectorToBlock(j);
						data = mfc.readBlock(bIndex);


						cardData = new String(data);


						//String str = new String(data, StandardCharsets.UTF_8);

						Log.i("dataaaaa: ", bytesToHexString(data) );
						//Log.i("tag", getHexString(data));



						bIndex++;
					}
				}else{ // Authentication failed - Handle it
					//Log.i("auth sector", String.valueOf(j) + " not");
				}
			}
		}catch (IOException e) {
			Log.e("tag", e.getLocalizedMessage());
		}


/*
		// 2) Check if it was triggered by a tag discovered interruption.
		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
			//  3) Get an instance of the TAG from the NfcAdapter
			Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			// 4) Get an instance of the Mifare classic card from this TAG intent
			MifareClassic mfc = MifareClassic.get(tagFromIntent);
			byte[] data;

			try {       //  5.1) Connect to card
				mfc.connect();
				boolean auth = false;
				String cardData = null;
				// 5.2) and get the number of sectors this card has..and loop thru these sectors
				int secCount = mfc.getSectorCount();
				Log.i("sectors: ", String.valueOf(secCount));
				int bCount = 0;
				int bIndex = 0;
				for(int j = 0; j < secCount; j++){
					// 6.1) authenticate the sector

					byte[] bytesKey = hex2Bytes("5FFFFFFFFFF9");

					auth = mfc.authenticateSectorWithKeyA(j,bytesKey );
					if(auth){
						// 6.2) In each sector - get the block count
						bCount = mfc.getBlockCountInSector(j);
						bIndex = 0;
						for(int i = 0; i < bCount; i++){
							bIndex = mfc.sectorToBlock(j);
							// 6.3) Read the block
							data = mfc.readBlock(bIndex);
							// 7) Convert the data into a string from Hex format.
							Log.i("tag", getHexString(data));
							bIndex++;
						}
					}else{ // Authentication failed - Handle it

					}
				}
			}catch (IOException e) {
				Log.e("tag", e.getLocalizedMessage());
			}

		}*/
	}

	private static final char[] BYTE2HEX=(
			"000102030405060708090A0B0C0D0E0F"+
					"101112131415161718191A1B1C1D1E1F"+
					"202122232425262728292A2B2C2D2E2F"+
					"303132333435363738393A3B3C3D3E3F"+
					"404142434445464748494A4B4C4D4E4F"+
					"505152535455565758595A5B5C5D5E5F"+
					"606162636465666768696A6B6C6D6E6F"+
					"707172737475767778797A7B7C7D7E7F"+
					"808182838485868788898A8B8C8D8E8F"+
					"909192939495969798999A9B9C9D9E9F"+
					"A0A1A2A3A4A5A6A7A8A9AAABACADAEAF"+
					"B0B1B2B3B4B5B6B7B8B9BABBBCBDBEBF"+
					"C0C1C2C3C4C5C6C7C8C9CACBCCCDCECF"+
					"D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF"+
					"E0E1E2E3E4E5E6E7E8E9EAEBECEDEEEF"+
					"F0F1F2F3F4F5F6F7F8F9FAFBFCFDFEFF").toCharArray();
	;
	public static String getHexString(byte[] bytes) {
		final int len=bytes.length;
		final char[] chars=new char[len<<1];
		int hexIndex;
		int idx=0;
		int ofs=0;
		while (ofs<len) {
			hexIndex=(bytes[ofs++] & 0xFF)<<1;
			chars[idx++]=BYTE2HEX[hexIndex++];
			chars[idx++]=BYTE2HEX[hexIndex];
		}
		return new String(chars);
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

	public static String bytes2Hex(byte[] bytes) {
		StringBuilder ret = new StringBuilder();
		if (bytes != null) {
			for (Byte b : bytes) {
				ret.append(String.format("%02X", b.intValue() & 0xFF));
			}
		}
		return ret.toString();
	}


}



/*
* 	String time = "";
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
		//data = "UID: " +ID;
		data += "\nData format:";
		for (String tech : techList) {
			data += "\n" + tech;
		}


// key: 5FFFFFFFFFF9
		tv_show_nfc2.setText(data);
		*/

