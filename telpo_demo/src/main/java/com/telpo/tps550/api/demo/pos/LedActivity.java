package com.telpo.tps550.api.demo.pos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.common.CommonConstants;
import com.common.apiutil.pos.CommonUtil;
import com.common.demo.R;
import com.telpo.tps550.api.demo.MainActivity;
import com.telpo.tps550.api.demo.serial.SerialTestActivity;

import java.lang.reflect.Field;
import java.util.HashMap;

public class LedActivity extends Activity {

    // ledColor
    private ArrayAdapter<String> mColorAdapter;
    private final HashMap<String, String> mColorHashMap = new HashMap<>();
    private int mLedColor;

    // ledType
    private ArrayAdapter<String> mTypeAdapter;
    private final HashMap<String, String> mTypeHashMap = new HashMap<>();
    private int mLedType;

    //UI
    private TextView mTvBrightness;
    private SeekBar mSeekbarColor;
    private Spinner mSpinnerColor;
    private Spinner mSpLedType;

    // moj button
    private Button nastavsvetlo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_leds);

        initView();

        nastavsvetlo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

               // String name = mTypeAdapter.getItem(position);
                String value = mTypeHashMap.get("COLOR_LED_3");
                mLedType = Integer.parseInt(value == null ? String.valueOf(CommonConstants.LedType.COLOR_LED_1) : value);

                // String namemLedColor = mColorAdapter.getItem(position);
                String valuemLedColor = mColorHashMap.get("BLUE_LED");
                mLedColor = Integer.parseInt(value == null ? String.valueOf(CommonConstants.LedColor.WHITE_LED) : valuemLedColor);

                CommonUtil.setColorLed(mLedType, mLedColor, 100);

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {

        // vyber svetielka
        mSpLedType = findViewById(R.id.spinner_led_type);

        // vyber farby
        mSpinnerColor = findViewById(R.id.spinner_led);

        // posuvnik
        mSeekbarColor = findViewById(R.id.seekbar_led);
        mSeekbarColor.setOnSeekBarChangeListener(onSeekBarChangeListener);

        // moj button
        nastavsvetlo = findViewById(R.id.nastavsvetlo);


        mTvBrightness = findViewById(R.id.seekbar_led_num);
        mColorAdapter = generateAdapterFromClass("com.common.CommonConstants$LedColor", mColorHashMap);
        mColorAdapter.notifyDataSetChanged();
        mTypeAdapter = generateAdapterFromClass("com.common.CommonConstants$LedType", mTypeHashMap);
        mTypeAdapter.notifyDataSetChanged();

        mSpLedType.setSelection(0);
        mSpLedType.setAdapter(mTypeAdapter);
        mSpLedType.setOnItemSelectedListener(mIndexSelectListener);
        mSpinnerColor.setAdapter(mColorAdapter);
        mSpinnerColor.setSelection(0);
        mSpinnerColor.setOnItemSelectedListener(spinnerSelectedListener);

    }



    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            switch (seekBar.getId()) {
                case R.id.seekbar_led:
                    CommonUtil.setColorLed(mLedType, mLedColor, progress);
                    mTvBrightness.setText(progress + "");
                    break;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private AdapterView.OnItemSelectedListener spinnerSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String name = mColorAdapter.getItem(position);
            String value = mColorHashMap.get(name);
            mLedColor = Integer.parseInt(value == null ? String.valueOf(CommonConstants.LedColor.WHITE_LED) : value);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private AdapterView.OnItemSelectedListener mIndexSelectListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String name = mTypeAdapter.getItem(position);
            String value = mTypeHashMap.get(name);
            mLedType = Integer.parseInt(value == null ? String.valueOf(CommonConstants.LedType.COLOR_LED_1) : value);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    /**
     * 根据包名获取常量名字并装包
     *
     * @param packageName 需要装包的常量类包名
     * @param map         以<常量名，常量值>存储的HashMap
     * @return 返回装包后的数组适配器
     */
    private ArrayAdapter<String> generateAdapterFromClass(String packageName, HashMap<String, String> map) {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        try {
            Class<?> c = Class.forName(packageName);
            Field[] f = c.getDeclaredFields();
            for (Field field : f) {
                String name = field.getName();
                String value = String.valueOf(field.get(name));
                adapter.add(name);
                map.put(name, value);
            }
        } catch (ClassNotFoundException ignore) {
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return adapter;
    }
}
