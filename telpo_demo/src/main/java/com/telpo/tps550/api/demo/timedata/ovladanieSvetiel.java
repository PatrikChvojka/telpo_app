package com.telpo.tps550.api.demo.timedata;

import com.common.CommonConstants;
import com.common.apiutil.pos.CommonUtil;

public class ovladanieSvetiel {

    private static int mLedColor;
    private static int mLedType;

    public static void greenLightOn(){
        redLightOff();
        CommonUtil.setColorLed(Integer.parseInt(String.valueOf(CommonConstants.LedType.COLOR_LED_1)), Integer.parseInt(String.valueOf(CommonConstants.LedColor.GREEN_LED)), 100);
        CommonUtil.setColorLed(Integer.parseInt(String.valueOf(CommonConstants.LedType.COLOR_LED_2)), Integer.parseInt(String.valueOf(CommonConstants.LedColor.GREEN_LED)), 100);
        CommonUtil.setColorLed(Integer.parseInt(String.valueOf(CommonConstants.LedType.COLOR_LED_3)), Integer.parseInt(String.valueOf(CommonConstants.LedColor.GREEN_LED)), 100);
        CommonUtil.setColorLed(Integer.parseInt(String.valueOf(CommonConstants.LedType.COLOR_LED_4)), Integer.parseInt(String.valueOf(CommonConstants.LedColor.GREEN_LED)), 100);
    }
    public static void greenLightOff(){
        CommonUtil.setColorLed(Integer.parseInt(String.valueOf(CommonConstants.LedType.COLOR_LED_1)), Integer.parseInt(String.valueOf(CommonConstants.LedColor.GREEN_LED)), 0);
        CommonUtil.setColorLed(Integer.parseInt(String.valueOf(CommonConstants.LedType.COLOR_LED_2)), Integer.parseInt(String.valueOf(CommonConstants.LedColor.GREEN_LED)), 0);
        CommonUtil.setColorLed(Integer.parseInt(String.valueOf(CommonConstants.LedType.COLOR_LED_3)), Integer.parseInt(String.valueOf(CommonConstants.LedColor.GREEN_LED)), 0);
        CommonUtil.setColorLed(Integer.parseInt(String.valueOf(CommonConstants.LedType.COLOR_LED_4)), Integer.parseInt(String.valueOf(CommonConstants.LedColor.GREEN_LED)), 0);
    }
    public static void redLightOn(){
        greenLightOff();
        CommonUtil.setColorLed(Integer.parseInt(String.valueOf(CommonConstants.LedType.COLOR_LED_1)), Integer.parseInt(String.valueOf(CommonConstants.LedColor.RED_LED)), 100);
        CommonUtil.setColorLed(Integer.parseInt(String.valueOf(CommonConstants.LedType.COLOR_LED_2)), Integer.parseInt(String.valueOf(CommonConstants.LedColor.RED_LED)), 100);
        CommonUtil.setColorLed(Integer.parseInt(String.valueOf(CommonConstants.LedType.COLOR_LED_3)), Integer.parseInt(String.valueOf(CommonConstants.LedColor.RED_LED)), 100);
        CommonUtil.setColorLed(Integer.parseInt(String.valueOf(CommonConstants.LedType.COLOR_LED_4)), Integer.parseInt(String.valueOf(CommonConstants.LedColor.RED_LED)), 100);
    }
    public static void redLightOff(){
        CommonUtil.setColorLed(Integer.parseInt(String.valueOf(CommonConstants.LedType.COLOR_LED_1)), Integer.parseInt(String.valueOf(CommonConstants.LedColor.RED_LED)), 0);
        CommonUtil.setColorLed(Integer.parseInt(String.valueOf(CommonConstants.LedType.COLOR_LED_2)), Integer.parseInt(String.valueOf(CommonConstants.LedColor.RED_LED)), 0);
        CommonUtil.setColorLed(Integer.parseInt(String.valueOf(CommonConstants.LedType.COLOR_LED_3)), Integer.parseInt(String.valueOf(CommonConstants.LedColor.RED_LED)), 0);
        CommonUtil.setColorLed(Integer.parseInt(String.valueOf(CommonConstants.LedType.COLOR_LED_4)), Integer.parseInt(String.valueOf(CommonConstants.LedColor.RED_LED)), 0);
    }

}
