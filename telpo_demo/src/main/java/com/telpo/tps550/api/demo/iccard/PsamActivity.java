package com.telpo.tps550.api.demo.iccard;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.common.apiutil.usbreader.PsamCard;
import com.common.demo.R;
import com.common.apiutil.reader.SmartCardReader;
import com.common.apiutil.util.PowerUtil;
import com.common.apiutil.util.StringUtil;
import com.common.apiutil.util.SystemUtil;

public class PsamActivity extends Activity {
	private static final String TAG = "TELPO_SDK";
	private SmartCardReader reader;
	private Button readButton, protocolbtn, buttonAPDU;
	private Button getFDButton;
	private Button poweronButton;
	private Button poweroffButton;
	private Button openButton;
	private Button closeButton;
	private EditText mEditTextApdu;
	private TextView textReader;

	private Button psam1;
	private Button psam2;
	private Button psam3;
	private Button psam4;
	
	Spinner pasmNum;
	Spinner psamMode;
	Spinner psamAuto;
	Spinner psamVol;
	ArrayAdapter<String> psamNum_mAdapter;
	ArrayAdapter<String> psamMode_mAdapter;
	ArrayAdapter<String> psamAuto_mAdapter;
	ArrayAdapter<String> psamVol_mAdapter;
	int usingSlot = 0;
	int usingMode = 0;
	int usingAuto = 0;
	int usingVol = 0;
	
	int ICC_MODE_EMV = 0x00;
	int ICC_MODE_ISO = (byte) 0x80;
	int ICC_AUTO_PPS = 0x00;
	int ICC_DISABLE_PPS = 0X40;
	int ICC_VOL_5V = 0x00;
	int ICC_VOL_3V = 0x20;
	int ICC_VOL_1V8 = 0x10;
	
	String internalModel;
	
	int tps537psamSelect = 1;
	
	private PsamCard psamCard;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.smartcard);
		
		psamCard = new PsamCard(this);
		pasmNum = (Spinner) findViewById(R.id.psamNum);
		psamMode = (Spinner) findViewById(R.id.psamMode);
		psamAuto = (Spinner) findViewById(R.id.psamAuto);
		psamVol = (Spinner) findViewById(R.id.psamVol);
		internalModel = SystemUtil.getInternalModel();
		if(("TPS530".contains(internalModel) && !"S5".equals(internalModel)) || 
				"TPS950".contains(internalModel)){
			pasmNum.setVisibility(View.VISIBLE);
			psamMode.setVisibility(View.VISIBLE);
			psamAuto.setVisibility(View .VISIBLE);
			psamVol.setVisibility(View.VISIBLE);
		}else if("T20".equals(internalModel)){
			pasmNum.setVisibility(View.VISIBLE);
			LinearLayout select_psam = (LinearLayout) findViewById(R.id.select_psam);
			select_psam.setVisibility(View.GONE);
		}
		/*if("TPS980P".equals(internalModel)){
			PosUtil.setUsbPsamPower(1);
		}*/
		psamNum_mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		psamNum_mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		psamNum_mAdapter.add("psam1");
		psamNum_mAdapter.add("psam2");
		psamNum_mAdapter.add("psam3");
		psamNum_mAdapter.add("psam4");
		psamNum_mAdapter.add("psam5");
		psamNum_mAdapter.add("psam6");
		psamNum_mAdapter.add("psam7");
		psamNum_mAdapter.add("psam8");
		pasmNum.setAdapter(psamNum_mAdapter);
		pasmNum.setOnItemSelectedListener(new OnItemSelectedListener() {

			
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				usingSlot = position;
				if("T20".equals(internalModel)){
					usingSlot = position+1;
				}
				Log.d("tagg", "using slot:"+usingSlot);
				closeButton.setEnabled(false);
				openButton.setEnabled(true);
				poweroffButton.setEnabled(false);
				poweronButton.setEnabled(false);
				readButton.setEnabled(false);
				protocolbtn.setEnabled(false);
				getFDButton.setEnabled(false);
				buttonAPDU.setEnabled(false);
				textReader.setText("");
			}

			
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		
		psamMode_mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		psamMode_mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		psamMode_mAdapter.add("ICC_MODE_EMV");
		psamMode_mAdapter.add("ICC_MODE_ISO");
		psamMode.setAdapter(psamMode_mAdapter);
		psamMode.setOnItemSelectedListener(new OnItemSelectedListener() {

			
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if(psamMode_mAdapter.getItem(position).equals("ICC_MODE_EMV")){
					usingMode = ICC_MODE_EMV;
				}else if(psamMode_mAdapter.getItem(position).equals("ICC_MODE_ISO")){
					usingMode = ICC_MODE_ISO;
				}
				Log.d("tagg", "using mode:"+usingMode);
				closeButton.setEnabled(false);
				openButton.setEnabled(true);
				poweroffButton.setEnabled(false);
				poweronButton.setEnabled(false);
				readButton.setEnabled(false);
				protocolbtn.setEnabled(false);
				getFDButton.setEnabled(false);
				buttonAPDU.setEnabled(false);
				textReader.setText("");
			}

			
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		
		psamAuto_mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		psamAuto_mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		psamAuto_mAdapter.add("ICC_AUTO_PPS");
		psamAuto_mAdapter.add("ICC_DISABLE_PPS");
		psamAuto.setAdapter(psamAuto_mAdapter);
		psamAuto.setOnItemSelectedListener(new OnItemSelectedListener() {

			
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if(psamAuto_mAdapter.getItem(position).equals("ICC_AUTO_PPS")){
					usingAuto = ICC_AUTO_PPS;
				}else if(psamAuto_mAdapter.getItem(position).equals("ICC_DISABLE_PPS")){
					usingAuto = ICC_DISABLE_PPS;
				}
				Log.d("tagg", "using auto:"+usingAuto);
				closeButton.setEnabled(false);
				openButton.setEnabled(true);
				poweroffButton.setEnabled(false);
				poweronButton.setEnabled(false);
				readButton.setEnabled(false);
				protocolbtn.setEnabled(false);
				getFDButton.setEnabled(false);
				buttonAPDU.setEnabled(false);
				textReader.setText("");
			}

			
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		
		psamVol_mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		psamVol_mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		psamVol_mAdapter.add("ICC_VOL_5V");
		psamVol_mAdapter.add("ICC_VOL_3V");
		psamVol_mAdapter.add("ICC_VOL_1V8");
		psamVol.setAdapter(psamVol_mAdapter);
		psamVol.setOnItemSelectedListener(new OnItemSelectedListener() {

			
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if(psamVol_mAdapter.getItem(position).equals("ICC_VOL_5V")){
					usingVol = ICC_VOL_5V;
				}else if(psamVol_mAdapter.getItem(position).equals("ICC_VOL_3V")){
					usingVol = ICC_VOL_3V;
				}else if(psamVol_mAdapter.getItem(position).equals("ICC_VOL_1V8")){
					usingVol = ICC_VOL_1V8;
				}
				Log.d("tagg", "using vol:"+usingVol);
				closeButton.setEnabled(false);
				openButton.setEnabled(true);
				poweroffButton.setEnabled(false);
				poweronButton.setEnabled(false);
				readButton.setEnabled(false);
				protocolbtn.setEnabled(false);
				getFDButton.setEnabled(false);
				buttonAPDU.setEnabled(false);
				textReader.setText("");
			}

			
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		
		mEditTextApdu = (EditText) findViewById(R.id.editTextAPDU);
		textReader = (TextView) findViewById(R.id.textReader);
		psam1 = (Button) findViewById(R.id.psam1);
		psam2 = (Button) findViewById(R.id.psam2);
		psam3 = (Button) findViewById(R.id.psam3);
		psam4 = (Button) findViewById(R.id.psam4);
		
		
		readButton = (Button)findViewById(R.id.read_btn);
		readButton.setOnClickListener(listener);
		readButton.setEnabled(false);
		poweronButton = (Button)findViewById(R.id.poweron_btn);
		poweronButton.setOnClickListener(listener);
		poweronButton.setEnabled(false);
		poweroffButton = (Button)findViewById(R.id.poweroff_btn);
		poweroffButton.setOnClickListener(listener);
		poweroffButton.setEnabled(false);
		openButton = (Button)findViewById(R.id.open_btn);
		openButton.setOnClickListener(listener);
		closeButton = (Button)findViewById(R.id.close_btn);
		closeButton.setOnClickListener(listener);
		closeButton.setEnabled(false);
		protocolbtn = (Button) findViewById(R.id.protocol_btn);
		protocolbtn.setOnClickListener(listener);
		protocolbtn.setEnabled(false);
		getFDButton = (Button) findViewById(R.id.get_fd__btn);
		getFDButton.setOnClickListener(listener);
		getFDButton.setEnabled(false);
		buttonAPDU = (Button) findViewById(R.id.buttonAPDU);
		buttonAPDU.setEnabled(false);
		int deviceType = SystemUtil.getDeviceType();
		if(deviceType == StringUtil.DeviceModelEnum.TPS510.ordinal() ||
				deviceType == StringUtil.DeviceModelEnum.TPS510A.ordinal() ||
				deviceType == StringUtil.DeviceModelEnum.TPS510A_NHW.ordinal() ||
				deviceType == StringUtil.DeviceModelEnum.TPS510D.ordinal() ||
				"TPS537".equals(SystemUtil.getInternalModel()) ||
				"T20".equals(SystemUtil.getInternalModel()) ||
				"T20B".equals(SystemUtil.getInternalModel())){
			psam3.setVisibility(View.VISIBLE);
			if("TPS537".equals(SystemUtil.getInternalModel()) ||
					"T20".equals(SystemUtil.getInternalModel()) ||
					"T20B".equals(SystemUtil.getInternalModel())){
				psam4.setVisibility(View.VISIBLE);
			}
		}
		
		reader = new SmartCardReader(PsamActivity.this, SmartCardReader.SLOT_PSAM1);
		
	}
	
	//rk3399pro 客户psam住建部卡片需要先设置频率才能对卡片上电
	public void zhujianbuBaudrate(View view){
		if(reader != null){
			reader.escape4057();
		}
	}
	
	//rk3399pro psam读卡器复位(设置过住建部卡片频率,其他卡片不能使用,需要对读卡器芯片复位才行)
	public void resetReader(View view){
		if("TPS537".equals(internalModel)){
			PowerUtil.TPS537psamReset(tps537psamSelect);
		}else{
			PowerUtil.rk3399proResetReader();
		}
		
	}
	
	OnClickListener listener = new OnClickListener() {

		
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.open_btn:
				if (("TPS530".contains(SystemUtil.getInternalModel()) && !"S5".equals(SystemUtil.getInternalModel())) && reader.close()) {
					int temp = usingSlot | usingMode | usingAuto | usingVol;
					reader = new SmartCardReader(PsamActivity.this, usingSlot, temp);
				}else if("T20".equals(internalModel)){
					reader = new SmartCardReader(PsamActivity.this, usingSlot);
				}
				new openTask().execute();
				break;

			case R.id.close_btn:
				reader.close();
				closeButton.setEnabled(false);
				openButton.setEnabled(true);
				poweroffButton.setEnabled(false);
				poweronButton.setEnabled(false);
				readButton.setEnabled(false);
				protocolbtn.setEnabled(false);
				getFDButton.setEnabled(false);
				buttonAPDU.setEnabled(false);
				textReader.setText("");
				break;

			case R.id.read_btn:
				String atrString = reader.getATRString();
				textReader.setText("ATR:" + (TextUtils.isEmpty(atrString.trim()) ? "null" : atrString));
				Toast.makeText(PsamActivity.this, getString(R.string.get_data_success), Toast.LENGTH_SHORT).show();
				break;

			case R.id.poweron_btn:
				if (reader.iccPowerOn()) {
					poweronButton.setEnabled(false);
					poweroffButton.setEnabled(true);
					readButton.setEnabled(true);
					protocolbtn.setEnabled(true);
					getFDButton.setEnabled(true);
					buttonAPDU.setEnabled(true);
				} else {
					Toast.makeText(PsamActivity.this, "ICC power on failed", Toast.LENGTH_SHORT).show();
				}
				
				break;

			case R.id.poweroff_btn:
				reader.iccPowerOff();
				poweroffButton.setEnabled(false);
				poweronButton.setEnabled(true);
				readButton.setEnabled(false);
				protocolbtn.setEnabled(false);
				getFDButton.setEnabled(false);
				buttonAPDU.setEnabled(false);
				break;
			case R.id.protocol_btn:
				int proto = reader.getProtocol();
				if (proto == SmartCardReader.PROTOCOL_T0) {
					textReader.setText("protocol: T0");
				} else if (proto == SmartCardReader.PROTOCOL_T1) {
					textReader.setText("protocol: T1");
				} else {
					textReader.setText("protocol: NA");
				}
				Toast.makeText(PsamActivity.this, getString(R.string.get_protocol_success), Toast.LENGTH_SHORT).show();
				break;
			case R.id.get_fd__btn:
				int fd = reader.getFD();
				textReader.setText(getString(R.string.psam_read_rate) + StringUtil.toHexString(new byte[]{(byte)fd}));
				break;
			default:
				break;
			}
		}
	};

	public void changePsam(View view) {
		switch (view.getId()) {
		case R.id.psam1:
			tps537psamSelect = 1;
			psam1.setEnabled(false);
			psam2.setEnabled(true);
			psam3.setEnabled(true);
			psam4.setEnabled(true);
			if (reader.close()) {
				reader = new SmartCardReader(PsamActivity.this, SmartCardReader.SLOT_PSAM1);
				closeButton.setEnabled(false);
				openButton.setEnabled(true);
				poweroffButton.setEnabled(false);
				poweronButton.setEnabled(false);
				readButton.setEnabled(false);
				protocolbtn.setEnabled(false);
				getFDButton.setEnabled(false);
				buttonAPDU.setEnabled(false);
				textReader.setText("");
			}

			break;

		case R.id.psam2:
			tps537psamSelect = 2;
			psam1.setEnabled(true);
			psam2.setEnabled(false);
			psam3.setEnabled(true);
			psam4.setEnabled(true);
			if (reader.close()) {
				reader = new SmartCardReader(PsamActivity.this, SmartCardReader.SLOT_PSAM2);
				closeButton.setEnabled(false);
				openButton.setEnabled(true);
				poweroffButton.setEnabled(false);
				poweronButton.setEnabled(false);
				readButton.setEnabled(false);
				protocolbtn.setEnabled(false);
				getFDButton.setEnabled(false);
				buttonAPDU.setEnabled(false);
				textReader.setText("");		
			}
			
			break;
		case R.id.psam3:
			tps537psamSelect = 3;
			psam1.setEnabled(true);
			psam2.setEnabled(true);
			psam3.setEnabled(false);
			psam4.setEnabled(true);
			if (reader.close()) {
				reader = new SmartCardReader(PsamActivity.this, SmartCardReader.SLOT_PSAM3);
				closeButton.setEnabled(false);
				openButton.setEnabled(true);
				poweroffButton.setEnabled(false);
				poweronButton.setEnabled(false);
				readButton.setEnabled(false);
				protocolbtn.setEnabled(false);
				getFDButton.setEnabled(false);
				buttonAPDU.setEnabled(false);
				textReader.setText("");
			}

			break;
			
		case R.id.psam4:
			tps537psamSelect = 4;
			psam1.setEnabled(true);
			psam2.setEnabled(true);
			psam3.setEnabled(true);
			psam4.setEnabled(false);
			if (reader.close()) {
				reader = new SmartCardReader(PsamActivity.this, SmartCardReader.SLOT_PSAM4);
				closeButton.setEnabled(false);
				openButton.setEnabled(true);
				poweroffButton.setEnabled(false);
				poweronButton.setEnabled(false);
				readButton.setEnabled(false);
				protocolbtn.setEnabled(false);
				getFDButton.setEnabled(false);
				buttonAPDU.setEnabled(false);
				textReader.setText("");
			}

			break;
		}
	}
	
	
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS360IC.ordinal())
			psam2.setVisibility(View.GONE);
	}

	
	protected void onDestroy()
	{
		super.onDestroy();
		/*if("TPS980P".equals(internalModel)){
			PosUtil.setUsbPsamPower(0);
		}*/
	}

	ProgressDialog dialog;
	private class openTask extends AsyncTask<Void, Integer, Boolean>
	{

		
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			if(SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS360U.ordinal()) {
				dialog = new ProgressDialog(PsamActivity.this);
				dialog.setMessage("please wait ...");
				dialog.setCancelable(false);
				dialog.show();
			}
		}
		
		
		protected Boolean doInBackground(Void... params)
		{
			return reader.open();
		}
		
		
		protected void onPostExecute(Boolean result)
		{
			if(SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS360U.ordinal()) {
				dialog.dismiss();
			}
			
			if (result)
			{
				openButton.setEnabled(false);
				closeButton.setEnabled(true);
				poweronButton.setEnabled(true);
			}
			else 
			{
				Toast.makeText(PsamActivity.this, "Open reader failed", Toast.LENGTH_SHORT).show();
			}
		}
	}
	

	public void sendAPDUkOnClick(View view) {
		byte[] pSendAPDU;
		byte[] result;
		int[] pRevAPDULen = new int[1];
		String apduStr;
		pRevAPDULen[0] = 300;
		apduStr = mEditTextApdu.getText().toString();
		pSendAPDU = toByteArray(apduStr);
		result = reader.transmit(pSendAPDU);
		textReader.setText(TextUtils.isEmpty(StringUtil.toHexString(result)) ? getString(R.string.send_APDU_fail) : getString(R.string.send_APDU_success) + StringUtil.toHexString(result));
		if (!TextUtils.isEmpty(StringUtil.toHexString(result))) {
			Toast.makeText(PsamActivity.this, getString(R.string.send_comm_success), Toast.LENGTH_SHORT).show();
		}
		
	}
	
	private String BCD2Str(byte[] data)
	{
		String string;
		StringBuilder stringBuilder = new StringBuilder();
		
		for (int i = 0; i < data.length; i++)
		{
			string = Integer.toHexString(data[i] & 0xFF);
			if (string.length() == 1)
			{
				stringBuilder.append("0");
			}
			
			stringBuilder.append(string.toUpperCase());
			stringBuilder.append(" ");
		}
		
		return stringBuilder.toString();
	}
	
	private byte[] str2BCD(String string)
	{
		int len;
		String str;
		String hexStr = "0123456789ABCDEF";
		
		String s = string.toUpperCase();
		
		len = s.length();
		if ((len % 2) == 1)
		{
			str = s + "0";
			len = (len + 1) >> 1;
		}
		else 
		{
			str = s;
			len >>= 1;
		}
		
		byte[] bytes = new byte[len];
		byte high;
		byte low;
		
		for (int i = 0, j = 0; i < len; i++, j += 2)
		{
			high = (byte)(hexStr.indexOf(str.charAt(j)) << 4);
			low = (byte)hexStr.indexOf(str.charAt(j + 1));
			bytes[i] = (byte)(high | low);
		}
		
		return bytes;
	}

	public static byte[] toByteArray(String hexString) {

		int hexStringLength = hexString.length();
		byte[] byteArray = null;
		int count = 0;
		char c;
		int i;

		// Count number of hex characters
		for (i = 0; i < hexStringLength; i++) {

			c = hexString.charAt(i);
			if (c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f') {
				count++;
			}
		}

		byteArray = new byte[(count + 1) / 2];
		boolean first = true;
		int len = 0;
		int value;
		for (i = 0; i < hexStringLength; i++) {

			c = hexString.charAt(i);
			if (c >= '0' && c <= '9') {
				value = c - '0';
			} else if (c >= 'A' && c <= 'F') {
				value = c - 'A' + 10;
			} else if (c >= 'a' && c <= 'f') {
				value = c - 'a' + 10;
			} else {
				value = -1;
			}

			if (value >= 0) {

				if (first) {

					byteArray[len] = (byte) (value << 4);

				} else {

					byteArray[len] |= value;
					len++;
				}

				first = !first;
			}
		}

		return byteArray;
	}
	
}
