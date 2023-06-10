package com.telpo.tps550.api.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.provider.Settings;
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
import com.common.apiutil.util.ShellUtils;
import com.common.apiutil.util.SystemUtil;
import com.common.callback.IDecodeReaderListener;
import com.common.demo.R;
import com.google.zxing.other.BeepManager;
import com.telpo.tps550.api.demo.can.Can2DeptActivity;
import com.telpo.tps550.api.demo.can.CanActivity;
import com.telpo.tps550.api.demo.decode.DecodeReaderActivity;
import com.telpo.tps550.api.demo.decode.KeyEventResolver;
import com.telpo.tps550.api.demo.encrypt.Encrypt_537Activity;
import com.telpo.tps550.api.demo.iccard.IccActivityNew;
import com.telpo.tps550.api.demo.iccard.PsamCardActivity;
import com.telpo.tps550.api.demo.ir.IrActivity;
import com.telpo.tps550.api.demo.ledscreen.LedScreenActivity;
import com.telpo.tps550.api.demo.megnetic.MegneticActivity;
import com.telpo.tps550.api.demo.moneybox.MoneyBoxActivity;
import com.telpo.tps550.api.demo.multireader.MultiReaderActivity;
import com.telpo.tps550.api.demo.nfc.NfcActivity;
import com.telpo.tps550.api.demo.nfc.NfcActivity_tps537;
import com.telpo.tps550.api.demo.nfc.NfcPN512ActivityMain;
import com.telpo.tps550.api.demo.pos.InterfaceActivityMain;
import com.telpo.tps550.api.demo.printer.PrinterActivity;
import com.telpo.tps550.api.demo.printer.PrinterActivitySY581;
import com.telpo.tps550.api.demo.printer.UsbPrinterActivity;
import com.telpo.tps550.api.demo.serial.SerialTestActivity;
import com.telpo.tps550.api.demo.system.SystemActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class MainActivityOLD extends Activity  implements KeyEventResolver.OnScanSuccessListener  {
	private static final String TAG = "TELPO_SDK";
	private int Oriental = -1;
	private Button BnPrint, BnQRCode, psambtn, magneticCardBtn, rfidBtn, pcscBtn, identifyBtn, 
	               moneybox, irbtn, ledbtn, decodebtn, nfcbtn, hardreaderbtn, tamperBtn, digitaltubeBtn,
			       otherApiBtn,systemApiBtn, canTestBtn,serialBtn;
	private TextView versionTv;
//	private Switch otgSwitchBtn;
	private BeepManager mBeepManager;
	private static int printerCheck = -1;
	private static final String FILE_NAME = "/sdcard/tpsdk/printerVersion.txt";
	ProgressDialog checkDialog;








	boolean isreading;

	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (mDecodeReader != null) {
			mDecodeReader.close();
		}
		isreading = false;
	}

	/////////////////////////////////////////////////////////////////// MOJE premenne
	private int mLedColor;
	private int mLedType;
	private Button nastavsvetlo, vypnisvetlo;
	private TextView showQrcode, circleCountShow;

	private DecodeReader mDecodeReader;
	private KeyEventResolver mKeyEventResolver;

	/////////////////////////////////////////////////////////////////// MOJE end


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// home screen
		//setContentView(R.layout.activity_home_main);

		// custom home screen
		setContentView(R.layout.activity_home_main);





		/////////////////////////////////////////////////////////////////// MOJE



		nastavsvetlo = findViewById(R.id.nastavsvetlo);
		vypnisvetlo = findViewById(R.id.vypnisvetlo);

		showQrcode = (TextView) findViewById(R.id.showQrcode);
		showQrcode.setInputType(0);
		showQrcode.setSingleLine(false);
		showQrcode.setHorizontallyScrolling(false);

		// zapni svetlo
		nastavsvetlo.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mLedType = Integer.parseInt(String.valueOf(CommonConstants.LedType.COLOR_LED_3));
				mLedColor = Integer.parseInt(String.valueOf(CommonConstants.LedColor.BLUE_LED));
				CommonUtil.setColorLed(mLedType, mLedColor, 100);
				showQrcode.setText("Pripravený skenovať?");
				Log.w("myApp", "Svetlo zapnute");

				int ret = mDecodeReader.open(115200);
				Log.w("myApp", "Qr zapnute");
			}
		});

		// vypni svetlo
		vypnisvetlo.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mLedType = Integer.parseInt(String.valueOf(CommonConstants.LedType.COLOR_LED_3));
				mLedColor = Integer.parseInt(String.valueOf(CommonConstants.LedColor.BLUE_LED));
				CommonUtil.setColorLed(mLedType, mLedColor, 0);
				showQrcode.setText("Už nič nenaskenuješ.");
				Log.w("myApp", "Svetlo vypnute");

				int ret = mDecodeReader.close();
				Log.w("myApp", "Qr vypnute");

			}
		});



		/////////////////////////////////////////////////////////////////// MOJE end






		BnPrint = (Button) findViewById(R.id.print_test);
		BnQRCode = (Button) findViewById(R.id.qrcode_verify);
		magneticCardBtn = (Button) findViewById(R.id.magnetic_card_btn);
		rfidBtn = (Button) findViewById(R.id.rfid_btn);
		pcscBtn = (Button) findViewById(R.id.pcsc_btn);
		identifyBtn = (Button) findViewById(R.id.identity_btn);
		moneybox = (Button) findViewById(R.id.moneybox_btn);
		irbtn = (Button) findViewById(R.id.ir_btn);
		ledbtn = (Button) findViewById(R.id.led_btn);
		psambtn = (Button) findViewById(R.id.psam);
		decodebtn = (Button) findViewById(R.id.decode_btn);
		nfcbtn = (Button) findViewById(R.id.nfc_btn);
		mBeepManager = new BeepManager(this, R.raw.beep);
		hardreaderbtn = (Button) findViewById(R.id.hardreader_btn);
		tamperBtn = (Button) findViewById(R.id.tamper_btn);
		digitaltubeBtn = (Button) findViewById(R.id.digitaltube_btn);
		otherApiBtn = (Button) findViewById(R.id.other_api_btn);
		systemApiBtn = (Button) findViewById(R.id.system_api_btn);
		canTestBtn = (Button) findViewById(R.id.can_test_btn);
		serialBtn = (Button) findViewById(R.id.serial_btn);

		versionTv = findViewById(R.id.tv_version);
		versionTv.setVisibility(View.VISIBLE);
		versionTv.setText("20230301");

		checkDialog = new ProgressDialog(MainActivityOLD.this);
		checkDialog.setTitle(getString(R.string.checkPrinterType));
		checkDialog.setMessage(getText(R.string.watting));
		checkDialog.setCancelable(false);

		setFunctions(MyApplication.getConfig());

		//C1Pro专业OTG
//		otgSwitchBtn = findViewById(R.id.otgSwitchBtn);
//		ShellUtils.execCommand("echo host > /sys/class/extcon/extcon3/role", false);
//		otgSwitchBtn.setChecked(true);
//
//		otgSwitchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView,
//										 boolean isChecked) {
//				// TODO Auto-generated method stub
//				String error = null;
//				if(isChecked){
//					error = ShellUtils.execCommand("echo host > /sys/class/extcon/extcon3/role", false).errorMsg;
//				}else{
//					error = ShellUtils.execCommand("echo devices > /sys/class/extcon/extcon3/role", false).errorMsg;
//				}
//				Log.d("tagg","otg switch :" + error);
//			}
//		});

		//串口测试
		serialBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivityOLD.this, SerialTestActivity.class));
			}
		});

		//can总线测试
		canTestBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!SystemUtil.checkPackage("com.common.service")) {
					startActivity(new Intent(MainActivityOLD.this, Can2DeptActivity.class));
				} else {
					startActivity(new Intent(MainActivityOLD.this, CanActivity.class));
				}
			}
		});

		//其他接口测试
		otherApiBtn.setOnClickListener(new OnClickListener() {
		   @Override
		   public void onClick(View view) {
			   startActivity(new Intent(MainActivityOLD.this, InterfaceActivityMain.class));
		   }
	   	});

		systemApiBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivityOLD.this, SystemActivity.class));
			}
		});

		digitaltubeBtn.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View arg0) {
				startActivity(new Intent(MainActivityOLD.this, LedScreenActivity.class));
			}
		});
		
		//MoneyBox
		moneybox.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View arg0) {
				startActivity(new Intent(MainActivityOLD.this, MoneyBoxActivity.class));
			}
		});

		//Barcode And Qrcode
		BnQRCode.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {

				if (!SystemUtil.checkPackage("com.telpo.tps550.api")) {
					// 软解码APP不存在，直接跳转硬解码界面
					startActivity(new Intent(MainActivityOLD.this, DecodeReaderActivity.class));
				} else {
					// 软解码APP存在，让用户选择解码类型
					AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivityOLD.this);
					dialog.setTitle(getString(R.string.qrcode_dialog_title));
					dialog.setMessage(getString(R.string.qrcode_dialog_msg));
					dialog.setNegativeButton(getString(R.string.qrcode_dialog_soft_decoding), new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialogInterface, int i) {
							// 软解码
							Intent intent = new Intent();
							intent.setClassName("com.telpo.tps550.api", "com.telpo.tps550.api.barcode.Capture");
							try {
								startActivityForResult(intent, 0x124);
							} catch (ActivityNotFoundException e) {
								Toast.makeText(MainActivityOLD.this, getResources().getString(R.string.identify_fail), Toast.LENGTH_LONG).show();
							}
						}
					});
					dialog.setPositiveButton(getString(R.string.qrcode_dialog_hard_decoding), new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialogInterface, int i) {
							// 硬解码
							startActivity(new Intent(MainActivityOLD.this, DecodeReaderActivity.class));
						}
					});
					dialog.show();
				}
			}
		});

		//Print
		BnPrint.setOnClickListener(new OnClickListener() {
			
			public void onClick(View view) {
				checkDialog.show();
				new Thread(new Runnable() {
					@Override
					public void run() {
						if(!SystemUtil.checkPackage("com.common.service")){
							startActivity(new Intent(MainActivityOLD.this, UsbPrinterActivity.class));
							runOnUiThread(new Runnable() {
								public void run() {
									checkDialog.dismiss();
								}
							});
						}else{
							int type = SystemUtil.checkPrinter581(MainActivityOLD.this);
							Log.d("tagg","printer type = " + type);
							runOnUiThread(new Runnable() {
								public void run() {
									checkDialog.dismiss();
								}
							});

							if(type == SystemUtil.PRINTER_80MM_USB_COMMON || type == SystemUtil.PRINTER_SY581){
								if(type == SystemUtil.PRINTER_80MM_USB_COMMON){
									SystemUtil.setProperty("persist.printer.interface", "usb");
								}else{
									SystemUtil.setProperty("persist.printer.interface", "serial");
								}
								startActivity(new Intent(MainActivityOLD.this, PrinterActivitySY581.class));
							} else if(type == SystemUtil.PRINTER_PRT_COMMON){
								startActivity(new Intent(MainActivityOLD.this, UsbPrinterActivity.class));
							} else {
								startActivity(new Intent(MainActivityOLD.this, PrinterActivity.class));
							}
						}
					}
				}).start();

//				AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
//				dialog.setTitle(getString(R.string.printer_type_select));
//				dialog.setNegativeButton(getString(R.string.printer_type_80mm), new DialogInterface.OnClickListener() {
//
//					public void onClick(DialogInterface dialogInterface, int i) {
//						if(printerCheck == 9){
//							showSelectPrinterInterfaceDialog();
//						}else{
//							if (deviceType == StringUtil.DeviceModelEnum.TPS650.ordinal()
//									&& !(Build.MODEL.equals("MTDP-618A") || Build.MODEL.equals("TPS650M"))) {
//								if(getFileContent(FILE_NAME) != null && getFileContent(FILE_NAME).contains("SY581")) {
//									startActivity(new Intent(MainActivity.this, PrinterActivitySY581.class));
//								}else {
//									startActivity(new Intent(MainActivity.this, PrinterActivity.class));
//								}
//							}else if((deviceType == StringUtil.DeviceModelEnum.TPS650T.ordinal() && SystemUtil.tps650t_is_sy581) ||
//									deviceType == StringUtil.DeviceModelEnum.TPS680.ordinal() ||
//									deviceType == StringUtil.DeviceModelEnum.C1B.ordinal() ||
//									deviceType == StringUtil.DeviceModelEnum.TPS650P.ordinal() ||
//									"C1".equals(SystemUtil.getInternalModel()) || "C1P".equals(SystemUtil.getInternalModel())){
//								if(deviceType == StringUtil.DeviceModelEnum.C1B.ordinal()){
//									checkDialog.show();
//									new Thread(new Runnable() {
//										@Override
//										public void run() {
//											// TODO Auto-generated method stub
//											SystemUtil.checkPrinter581(MainActivity.this);
//											runOnUiThread(new Runnable() {
//												@Override
//												public void run() {
//													// TODO Auto-generated method stub
//													checkDialog.dismiss();
//													startActivity(new Intent(MainActivity.this, PrinterActivitySY581.class));
//												}
//											});
//										}
//									}).start();
//								}else{
//									startActivity(new Intent(MainActivity.this, PrinterActivitySY581.class));
//								}
//							}else{
//								startActivity(new Intent(MainActivity.this, PrinterActivity.class));
//							}
//						}
//					}
//				});
//				dialog.setPositiveButton(getString(R.string.printer_type_58mm), new DialogInterface.OnClickListener() {
//
//					public void onClick(DialogInterface dialogInterface, int i) {
//						startActivity(new Intent(MainActivity.this, UsbPrinterActivity.class));
////						startActivity(new Intent(MainActivity.this, UsbPrintTest.class));
//
//					}
//				});
//				dialog.show();
			}
		});
		
		//Magnetic Card
		magneticCardBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View view) {
				startActivity(new Intent(MainActivityOLD.this, MegneticActivity.class));
			}
		});
		
//		//RFID
//		rfidBtn.setOnClickListener(new View.OnClickListener() {
//
//			public void onClick(View view) {
//				startActivity(new Intent(MainActivity.this, RfidActivity.class));
//			}
//		});

		//IC Card
		pcscBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				startActivity(new Intent(MainActivityOLD.this, IccActivityNew.class));
			}
		});

		//IR
		irbtn.setOnClickListener(new OnClickListener() {

			
			public void onClick(View arg0) {
				startActivity(new Intent(MainActivityOLD.this, IrActivity.class));
			}
		});
		
		//Led
//		ledbtn.setOnClickListener(new OnClickListener() {
//
//
//			public void onClick(View arg0) {
//				startActivity(new Intent(MainActivity.this, LedActivity.class));
//			}
//		});

		//ID Card
		identifyBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View view) {
//				AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
//				dialog.setTitle(getString(R.string.idcard_xzgn));
//				dialog.setMessage(getString(R.string.idcard_xzsfsbfs));
//
//				dialog.setNegativeButton(getString(R.string.idcard_sxtsb), new DialogInterface.OnClickListener() {
//
//					public void onClick(DialogInterface dialogInterface, int i) {
//						//use camera
////						startActivity(new Intent(MainActivity.this, OcrIdCardActivity.class));
//					}
//				});
//				dialog.setPositiveButton(getString(R.string.idcard_dkqsb), new DialogInterface.OnClickListener() {
//
//					public void onClick(DialogInterface dialogInterface, int i) {
//						//use ID Card reader
//						//startActivity(new Intent(MainActivity.this, IdCardActivity.class));
//						AlertDialog.Builder idcard_dialog = new AlertDialog.Builder(MainActivity.this);
//						idcard_dialog.setTitle(getString(R.string.idcard_xzgn));
//						idcard_dialog.setMessage(getString(R.string.idcard_xzsfsbfs));
//						idcard_dialog.setNegativeButton(getString(R.string.idcard_hqsfzxx), new DialogInterface.OnClickListener() {
//
//
//							public void onClick(DialogInterface dialog, int which) {
//								// TODO Auto-generated method stub
//								startActivity(new Intent(MainActivity.this, /*IdCardActivity*/TwoInOneReaderActivity.class));
//							}
//						});
//						idcard_dialog.setPositiveButton(getString(R.string.idcard_bt_hqsfzxx), new DialogInterface.OnClickListener() {
//
//
//							public void onClick(DialogInterface dialog, int which) {
//								// TODO Auto-generated method stub
////								startActivity(new Intent(MainActivity.this, BluetoothIdCardActivity.class));
//							}
//						});
//						idcard_dialog.show();
//					}
//				});
//				dialog.show();

				startActivity(new Intent(MainActivityOLD.this, MultiReaderActivity.class));

			}

		});
		
		//PSAM Card
		psambtn.setOnClickListener(new OnClickListener() {

			
			public void onClick(View arg0) {
				startActivity(new Intent(MainActivityOLD.this, PsamCardActivity.class));
			}
		});		

		//laser qrcode
		decodebtn.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View v) {
//				startActivity(new Intent(MainActivity.this, DecodeActivity.class));
			}
		});

		//NFC
		nfcbtn.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View v) {

				File device = new File("/dev/pn544");
				File device2 = new File("/dev/nq-nci");
				File device3 = new File("dev/nxpnfc");
				if (device.exists() || device2.exists() || device3.exists()) {
					Log.i("turniket","1");
					startActivity(new Intent(MainActivityOLD.this, NfcActivity.class));
				} else if(!SystemUtil.checkPackage("com.common.service") || SystemUtil.isPN512NFC()){
					Log.i("turniket","2");
					startActivity(new Intent(MainActivityOLD.this, NfcPN512ActivityMain.class));
				} else {
					Log.i("turniket","3");
					startActivity(new Intent(MainActivityOLD.this, NfcActivity_tps537.class));
				}

//				AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
//				dialog.setTitle(getString(R.string.nfc_xzgn));
//				dialog.setMessage(getString(R.string.nfc_xznfcsbfs));

//				dialog.setNegativeButton(getString(R.string.nfc_ys), new DialogInterface.OnClickListener() {
//
//					public void onClick(DialogInterface dialogInterface, int i) {
//						//use camera
//						startActivity(new Intent(MainActivity.this, NfcActivity.class));
//					}
//				});
//				dialog.setPositiveButton(getString(R.string.nfc_telpo), new DialogInterface.OnClickListener() {
//
//					public void onClick(DialogInterface dialogInterface, int i) {
//						if(!SystemUtil.checkPackage("com.common.service") || SystemUtil.isPN512NFC()){
//							startActivity(new Intent(MainActivity.this, NfcActivity_tps900.class));
//						}else {
//							startActivity(new Intent(MainActivity.this, NfcActivity_tps537.class));
//						}
//					}
//				});
//				dialog.show();


//			    if(deviceType == StringUtil.DeviceModelEnum.TPS900.ordinal() &&
//			    		!"TPS360S".equals(SystemUtil.getInternalModel())  &&
//			    		!"S10A".equals(SystemUtil.getInternalModel())){
//			        startActivity(new Intent(MainActivity.this, NfcActivity_tps900.class));
//			    }else if("D2".equals(SystemUtil.getInternalModel()) || "C20".equals(SystemUtil.getInternalModel()) || "T20".equals(SystemUtil.getInternalModel())){
//			        startActivity(new Intent(MainActivity.this, NfcActivity_tps537.class));
//			    }else{
//			        startActivity(new Intent(MainActivity.this, NfcActivity.class));
//			    }
			}
		});
		
		//硬读头 hardreade Scan
		hardreaderbtn.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View v) {
				startActivity(new Intent(MainActivityOLD.this, DecodeReaderActivity.class));
			}
		});
		
		//防拆传感器
		tamperBtn.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View v) {
				startActivity(new Intent(MainActivityOLD.this, Encrypt_537Activity.class));
			}
		});

//		int deviceType = SystemUtil.getDeviceType();
//		if(deviceType == StringUtil.DeviceModelEnum.TPS650.ordinal() ||
//				deviceType == StringUtil.DeviceModelEnum.TPS650T.ordinal() ||
//				"C1".equals(SystemUtil.getInternalModel()) || "C1P".equals(SystemUtil.getInternalModel()) ||
//				"C2".equals(SystemUtil.getInternalModel()) ||
//				deviceType == StringUtil.DeviceModelEnum.C1B.ordinal() ||
//				deviceType == StringUtil.DeviceModelEnum.TPS680.ordinal()) {
//			checkDialog.show();
//
//			new Thread(new Runnable() {
//
//				public void run() {
//					// TODO Auto-generated method stub
//					printerCheck = SystemUtil.checkPrinter581(MainActivity.this);
//					Log.d(TAG, "check read:"+getFileContent(FILE_NAME));
//					runOnUiThread(new Runnable() {
//						public void run() {
//							checkDialog.dismiss();
//						}
//					});
//				}
//			}).start();
//		}

		ShellUtils.CommandResult result = ShellUtils.execCommand("echo on >/sys/class/gpio-ctrl/prt_pwr/ctrl", false);
		Log.d("tagg", "/sys/class/gpio-ctrl/prt_pwr/ctrl result = " + result.result + " [" + result.successMsg + "] [" + result.errorMsg + "]");
		try {
			UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
			HashMap<String, UsbDevice> deviceHashMap = mUsbManager.getDeviceList();
			Iterator<UsbDevice> iterator = deviceHashMap.values().iterator();
			while (iterator.hasNext()) {
				UsbDevice usbDevice = iterator.next();
				int pid = usbDevice.getProductId();
				int vid = usbDevice.getVendorId();

				if (pid == 0x028d && vid == 0x28e9){
					Log.d("tagg", "pid="+pid+" vid="+vid);
					if (mUsbManager.hasPermission(usbDevice)) {
						// Do something with the device
					} else {
						PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent("com.android.usb.USB_PERMISSION"), 0);
						mUsbManager.requestPermission(usbDevice, mPermissionIntent);
					}
					break;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

    private void showSelectPrinterInterfaceDialog(){
    	AlertDialog.Builder selectInterfaceDialog = new AlertDialog.Builder(MainActivityOLD.this);
    	selectInterfaceDialog.setTitle("请选择使用USB方式或串口方式");
    	selectInterfaceDialog.setNegativeButton("USB", new DialogInterface.OnClickListener() {

    		public void onClick(DialogInterface dialogInterface, int i) {
    			SystemUtil.setProperty("persist.printer.interface", "usb");
    			startActivity(new Intent(MainActivityOLD.this, PrinterActivitySY581.class));
    		}
    	});
    	selectInterfaceDialog.setPositiveButton("串口", new DialogInterface.OnClickListener() {

    		public void onClick(DialogInterface dialogInterface, int i) {
    			SystemUtil.setProperty("persist.printer.interface", "serial");
            	startActivity(new Intent(MainActivityOLD.this, PrinterActivitySY581.class));
            }
       });
       selectInterfaceDialog.show();
    }

	private boolean checkPackage(String packageName) {
		PackageManager manager = this.getPackageManager();
		Intent intent = new Intent().setPackage(packageName);
		List<ResolveInfo> infos = manager.queryIntentActivities(intent, PackageManager.GET_INTENT_FILTERS);
		if (infos == null || infos.size() < 1) {
			return false;
		}
		return true;
	}

	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0x124) {
			if (resultCode == 0) {
				if (data != null) {
					try {
						mBeepManager.playBeepSoundAndVibrate();
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
					String qrcode = data.getStringExtra("qrCode");
					//change(qrcode);
					Toast.makeText(MainActivityOLD.this, "Scan result:" + qrcode, Toast.LENGTH_LONG).show();
					return;
				}
			} else {
				Toast.makeText(MainActivityOLD.this, "Scan Failed", Toast.LENGTH_LONG).show();
			}
			
		}
	}

	
	protected void onResume() {
		super.onResume();
		setRequestedOrientation(Oriental);

		if (mDecodeReader == null) {
			mDecodeReader = new DecodeReader(this);//初始化
		}
		mKeyEventResolver = new KeyEventResolver(this);

		mDecodeReader.setDecodeReaderListener(new IDecodeReaderListener() {

			@Override
			public void onRecvData(byte[] data) {
				// TODO Auto-generated method stub

				try {
					final String str = new String(data, "UTF-8");
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							showQrcode.setText(str);
						}
					});
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
	}
	
	private static void openAirplaneModeOn(Context context,boolean enabling) {  
	    
		Settings.Global.putInt(context.getContentResolver(),  
		                     Settings.Global.AIRPLANE_MODE_ON,enabling ? 1 : 0);  
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);  
		intent.putExtra("state", enabling);  
		context.sendBroadcast(intent);

	}

	
	protected void onDestroy() {
		super.onDestroy();
		//KeyEventResolver.onDestroy();
		mBeepManager.close();
		mBeepManager = null;
		System.exit(0);
	}
	
	private static String getFileContent(String file_name) {    
        String filePath = file_name;
        String fileContent = null;
        try {    
            File file = new File(filePath);    
            if (file.isFile() && file.exists()) {
                InputStreamReader isr = new InputStreamReader(new FileInputStream(file));    
                BufferedReader br = new BufferedReader(isr);    
                String lineTxt = null;    
                while ((lineTxt = br.readLine()) != null) {  
                	fileContent = lineTxt; 
                }
                isr.close();    
                br.close();    
            }else {
                Log.e(TAG, "can not find file");
                file.createNewFile();
            }    
        } catch (Exception e) {    
            e.printStackTrace();    
        }    
        return fileContent;    
    }
	
	public static Bitmap getLoacalBitmap(String url) {
        if (url != null) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(url);
                return BitmapFactory.decodeStream(fis);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            } finally {
                if(fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    fis = null;
                }
            }
        } else {
            return null;
        }
    }
	
	String[] portNum = new String[20];
	String[] productNum = new String[20];
	String[] readerNum = new String[4];

	public void setFunctions(int[] configure){
		BnPrint.setVisibility(View.GONE);
		BnQRCode.setVisibility(View.GONE);
		magneticCardBtn.setVisibility(View.GONE);
		rfidBtn.setVisibility(View.GONE);
		pcscBtn.setVisibility(View.GONE);
		identifyBtn.setVisibility(View.GONE);
		moneybox.setVisibility(View.GONE);
		irbtn.setVisibility(View.GONE);
		ledbtn.setVisibility(View.GONE);
		psambtn.setVisibility(View.GONE);
		decodebtn.setVisibility(View.GONE);
		nfcbtn.setVisibility(View.GONE);
		hardreaderbtn.setVisibility(View.GONE);
		tamperBtn.setVisibility(View.GONE);
		digitaltubeBtn.setVisibility(View.GONE);
		otherApiBtn.setVisibility(View.GONE);
		systemApiBtn.setVisibility(View.GONE);
		canTestBtn.setVisibility(View.GONE);
		serialBtn.setVisibility(View.GONE);

		for(int i=0; i<configure.length;i++){
			switch (configure[i]){
				case 1:
					BnPrint.setVisibility(View.VISIBLE);
					break;
				case 2:
					BnQRCode.setVisibility(View.VISIBLE);
					break;
				case 3:
					magneticCardBtn.setVisibility(View.VISIBLE);
					break;
				case 4:
					moneybox.setVisibility(View.VISIBLE);
					break;
				case 5:
					identifyBtn.setVisibility(View.VISIBLE);
				case 6:
					pcscBtn.setVisibility(View.VISIBLE);
					break;
				case 7:
					nfcbtn.setVisibility(View.VISIBLE);
					break;
				case 8:
					psambtn.setVisibility(View.VISIBLE);
					break;
				case 9:
					tamperBtn.setVisibility(View.VISIBLE);
					break;
				case 10:
					otherApiBtn.setVisibility(View.VISIBLE);
					break;
				case 11:
					otherApiBtn.setVisibility(View.VISIBLE);
					break;
				case 12:
					otherApiBtn.setVisibility(View.VISIBLE);
					break;
				case 13:
					otherApiBtn.setVisibility(View.VISIBLE);
					break;
				case 14:
					digitaltubeBtn.setVisibility(View.VISIBLE);
					break;
				case 15:
					systemApiBtn.setVisibility(View.VISIBLE);
					break;
				case 16:
					canTestBtn.setVisibility(View.VISIBLE);
					break;
				case 17:
					otherApiBtn.setVisibility(View.VISIBLE);
					break;
				case 18:
					serialBtn.setVisibility(View.VISIBLE);
					break;
			}

		}
	}

	/// moje
	@Override
	public void onScanSuccess(String barcode) {
		showQrcode.setText(barcode);
	}

	@Override
	public boolean dispatchKeyEvent(android.view.KeyEvent event) {
		//要是重虚拟键盘输入怎不拦截
		if ("Virtual".equals(event.getDevice().getName())) {
			return super.dispatchKeyEvent(event);
		}
		mKeyEventResolver.analysisKeyEvent(event);
		return true;
	}

}
