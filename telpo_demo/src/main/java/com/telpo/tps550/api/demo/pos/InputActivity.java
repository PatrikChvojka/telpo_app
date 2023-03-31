package com.telpo.tps550.api.demo.pos;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.common.CommonConstants;
import com.common.apiutil.pos.CommonUtil;
import com.common.callback.IInputListener;
import com.common.apiutil.util.StringUtil;
import com.common.demo.R;

import java.lang.reflect.Field;
import java.util.HashMap;


public class InputActivity extends Activity {

    //UI
    private EditText edt_wiegandsend;
    private TextView tv_wiegandreceive;
    private TextView tv_doorreceive;
    private TextView tv_keyreceive;
    private TextView tv_magneticSensationreceive1, tv_magneticSensationreceive2;
    private Switch switch_input;
    private Spinner wiegandtypeSpr;


    // WiegandType
    private ArrayAdapter<String> mWiegandTypeAdapter;
    private final HashMap<String, String> mWiegandTypeHashMap = new HashMap<>();
    private int mWiegandType = CommonConstants.WiegandType.WG_26;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_input);

        initView();

        CommonUtil.registerInputBroadcast(this);
        CommonUtil.setInputListener(new IInputListener() {

            @Override
            public void wiegandInput(byte[] inputData) {

                Log.d("tagg", "wiegandInput receive[" + StringUtil.toHexString(inputData) + "]");
                tv_wiegandreceive.setText(StringUtil.toHexString(inputData));
            }

            @Override
            public void input(int sw, int status) {
                Log.d("tagg", "input receive[" + sw + " " + status + "]");
                //1，门磁（磁敏电路）；2，物理按键；3，磁感1；4，磁感2
                if(sw == 1) {
                    tv_doorreceive.setText((status == 1 ? "高电平" : "低电平"));
                }else if(sw == 2) {
                    tv_keyreceive.setText((status == 1 ? "高电平" : "低电平"));
                }else if(sw == 3) {
                    tv_magneticSensationreceive1.setText((status == 1 ? "高电平" : "低电平"));
                }else if(sw == 4) {
                    tv_magneticSensationreceive2.setText((status == 1 ? "高电平" : "低电平"));
                }
            }


        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtil.unRegisterInputBroadcast();
    }

    private void initView() {
        switch_input = (Switch) findViewById(R.id.switch_input);
        edt_wiegandsend = (EditText) findViewById(R.id.edt_wiegandsend);
        tv_wiegandreceive = (TextView) findViewById(R.id.tv_wiegandreceive);
        tv_doorreceive = (TextView) findViewById(R.id.tv_doorreceive);
        tv_keyreceive = (TextView)findViewById(R.id.tv_keyreceive);
        tv_magneticSensationreceive1 = findViewById(R.id.tv_magneticSensationreceive1);
        tv_magneticSensationreceive2 = findViewById(R.id.tv_magneticSensationreceive2);
        switch_input.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                int sw = -1;
                if(isChecked){
                    sw = CommonConstants.InputStatus.SWITCH_OPEN_INPUT2;
                }else{
                    sw = CommonConstants.InputStatus.SWITCH_OPEN_INPUT1;
                }
                Toast.makeText(InputActivity.this, "switch_input : " + CommonUtil.switchInput(CommonConstants.InputType.WIEGAND_INPUT, CommonConstants.InputType.DOOR_INPUT, sw), Toast.LENGTH_SHORT).show();


            }
        });

        wiegandtypeSpr = findViewById(R.id.wiegandtypeSpr);
        wiegandtypeSpr.setSelection(0);
        mWiegandTypeAdapter = generateAdapterFromClass("com.common.CommonConstants$WiegandType", mWiegandTypeHashMap);
        wiegandtypeSpr.setAdapter(mWiegandTypeAdapter);
        mWiegandTypeAdapter.notifyDataSetChanged();
        wiegandtypeSpr.setOnItemSelectedListener(spinnerWiegandTypeListener);
    }

    private AdapterView.OnItemSelectedListener spinnerWiegandTypeListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String name = mWiegandTypeAdapter.getItem(position);
            String value = mWiegandTypeHashMap.get(name);
            mWiegandType = Integer.parseInt(value == null ? String.valueOf(CommonConstants.WiegandType.WG_26) : value);

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_wiegandSend:
//                tv_wiegandreceive.setText("");

                String wiegandsend = edt_wiegandsend.getText().toString();
                if(!wiegandsend.isEmpty()){
                    Toast.makeText(this, "send wg ret : " + CommonUtil.wiegandSend(mWiegandType,StringUtil.hexStringToBytes(wiegandsend)), Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.wiegandInputBtn:
                tv_wiegandreceive.setText("");
                CommonUtil.setWiegandDirection(CommonConstants.WiegandDirection.INPUT);
                break;
            case R.id.wiegandOutputBtn:
                CommonUtil.setWiegandDirection(CommonConstants.WiegandDirection.OUTPUT);
        }
    }

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