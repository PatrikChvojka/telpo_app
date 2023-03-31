package com.telpo.tps550.api.demo;

import android.app.Application;
import android.util.Log;

import com.common.apiutil.can.CanUtil;
import com.common.apiutil.decode.Decode;
import com.common.apiutil.decode.QrcodePower;
import com.common.apiutil.ledscreen.LedScreenUtil;
import com.common.apiutil.nfc.SelectCardReturn;
import com.common.apiutil.pos.CommonUtil;
import com.common.apiutil.printer.CheckC1Bprinter;
import com.common.apiutil.printer.ThermalPrinterSY581;
import com.common.apiutil.pos.PosUtil;
import com.common.apiutil.fingerprint.FingerPrint;
import com.common.apiutil.magnetic.MagneticCard;
import com.common.apiutil.nfc.PN512;
import com.common.apiutil.printer.ThermalPrinter;
import com.common.apiutil.moneybox.MoneyBox;
import com.common.apiutil.util.PowerUtil;
import com.common.apiutil.util.SDKUtil;
import com.common.apiutil.util.SystemUtil;


public class MyApplication extends Application {
//    private static RefWatcher refWatcher;
//    public static SystemUtil systemUtil;
//    public static ThermalPrinter thermalPrinter;
//    public static MoneyBox moneyBox;
//    public static PowerUtil powerUtil;
//    public static FingerPrint fingerPrint;
//    public static MagneticCard magneticCard;
//    public static ReaderMonitor readerMonitor;
//    public IdCard idCard;
//    public static PosUtil posUtil;
//    public static PN512 pn512;
    private static int[] config = null;
    private String internal_Model;

    @Override
    public void onCreate() {
        super.onCreate();

        SDKUtil.getInstance(this).initSDK();

        if (!SystemUtil.checkPackage("com.common.service")) {
            Log.d("tagg", "API 调用 >> 系统反射");
        }else {
            Log.d("tagg", "API 调用 >> 服务APK");
        }

//        SystemUtil.getProperty("ro.serial.port.hardreader", "")
        internal_Model = SystemUtil.getInternalModel();
        Log.d("tagMM", internal_Model);
        initConfig();
//        moneyBox = MoneyBox.getInstance(this);
//        powerUtil = PowerUtil.getInstance(this);
//        fingerPrint = FingerPrint.getInstance(this);
//        magneticCard = MagneticCard.getInstance(this);
//        readerMonitor = ReaderMonitor.getInstance(this);
//        pn512 = PN512.getInstance(this);

//        refWatcher = LeakCanary.install(this);
//        SystemClock.sleep(3000);
    }
//    //提供给外部调用的方法
//    public static RefWatcher getRefWatcher() {
//
//        return refWatcher;
//    }

    public static void setConfig(int[] config) {
        MyApplication.config = config;
    }

    public static int[] getConfig() {
        return config;
    }

    private void initConfig() {
        /*if(internal_Model.equals("S1")){
            setConfig(ConfigureUtil.S1);
        }else */
        if (internal_Model.equals("T20")) {
            setConfig(ConfigureUtil.T20);
        } else if (internal_Model.equals("T20P")) {
            setConfig(ConfigureUtil.T20P);
        } else if (internal_Model.equals("C1B")) {
            setConfig(ConfigureUtil.C1B);
        } else if (internal_Model.equals("C11")) {
            setConfig(ConfigureUtil.C11);
        } else if ("T20B".equals(internal_Model)) {
            setConfig(ConfigureUtil.T20B);
        } else if ("C1P".equals(internal_Model)) {
            setConfig(ConfigureUtil.C1P);
        }  else if ("TPS967M".equals(internal_Model)) {
            setConfig(ConfigureUtil.TPS967M);
        }  else if ("F10B".equals(internal_Model)) {
            setConfig(ConfigureUtil.F10B);
        }  else if ("TPS980B".equals(internal_Model)) {
            setConfig(ConfigureUtil.TPS980B);
        } else if ("C1Pro".equals(internal_Model)) {
            setConfig(ConfigureUtil.C1Pro);
        } else if ("TPS900".equals(internal_Model)) {
            setConfig(ConfigureUtil.TPS900);
        } else if ("TPS530Q".equals(internal_Model)) {
            setConfig(ConfigureUtil.TPS530);
        } else {
            setConfig(ConfigureUtil.COMMON);
        }
    }
}
