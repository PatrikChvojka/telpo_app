package com.telpo.tps550.api.demo.pos;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.common.apiutil.ResultCode;
import com.common.CommonConstants;
import com.common.apiutil.pos.RS232Reader;
import com.common.apiutil.pos.RS485Reader;
import com.common.callback.IRSReaderListener;
import com.common.demo.R;

import java.lang.reflect.Field;
import java.util.HashMap;

public class RSSerialActivity extends Activity {

    //UI
    private Spinner mSpRS232Type;
    private Spinner mSpRS485Type;
    private TextView mTvRS232Type;
    private TextView mTvRS485Type;
    private TextView mTvRS485drct;

    // RS232Type
    private ArrayAdapter<String> mRS232TypeAdapter;
    private final HashMap<String, String> mRS232TypeHashMap = new HashMap<>();
    private int mRS232Type = CommonConstants.RS232Type.RS232_1;

    // RS485Type
    private ArrayAdapter<String> mRS485TypeAdapter;
    private final HashMap<String, String> mRS485TypeHashMap = new HashMap<>();
    private int mRS485Type = CommonConstants.RS485Type.RS485_1;

    private RS232Reader mRS232Reader;
    private RS485Reader mRS485Reader;
    private boolean rs485Send = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_rsserial);

        initView();

        mRS232Reader = new RS232Reader(this);
        mRS485Reader = new RS485Reader(this);
        mRS232Reader.rsOpen(mRS232Type, 115200);
        mRS485Reader.rsOpen(mRS485Type, 115200);

        mRS232Reader.setRSReaderListener(new IRSReaderListener() {
            @Override
            public void onRecvData(final byte[] data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String tempString = toAsciiString(data);
                        mTvRS232Type.setText(tempString);
                    }
                });
            }
        });

        mRS485Reader.setRSReaderListener(new IRSReaderListener() {
            @Override
            public void onRecvData(final byte[] data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String tempString = toAsciiString(data);
                        mTvRS485Type.setText(tempString);
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRS232Reader.rsDestroy();
        mRS485Reader.rsDestroy();
    }

    private void initView() {
        mSpRS232Type = findViewById(R.id.spinner_rs232num);
        mSpRS232Type.setSelection(0);
        mRS232TypeAdapter = generateAdapterFromClass("com.common.CommonConstants$RS232Type", mRS232TypeHashMap);
        mSpRS232Type.setAdapter(mRS232TypeAdapter);
        mRS232TypeAdapter.notifyDataSetChanged();
        mSpRS232Type.setOnItemSelectedListener(spinnerRS232Listener);

        mSpRS485Type = findViewById(R.id.spinner_rs485num);
        mSpRS485Type.setSelection(0);
        mRS485TypeAdapter = generateAdapterFromClass("com.common.CommonConstants$RS485Type", mRS485TypeHashMap);
        mSpRS485Type.setAdapter(mRS485TypeAdapter);
        mRS485TypeAdapter.notifyDataSetChanged();
        mSpRS485Type.setOnItemSelectedListener(spinnerRS485Listener);

        mTvRS232Type = findViewById(R.id.tv_rs232recv);
        mTvRS485Type = findViewById(R.id.tv_rs485recv);
        mTvRS485drct = findViewById(R.id.tv_rs485drct);
    }

    private AdapterView.OnItemSelectedListener spinnerRS232Listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String name = mRS232TypeAdapter.getItem(position);
            String value = mRS232TypeHashMap.get(name);
            mRS232Type = Integer.parseInt(value == null ? String.valueOf(CommonConstants.RS232Type.RS232_1) : value);

            mRS232Reader.rsDestroy();
            mRS232Reader.rsOpen(mRS232Type, 115200);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private AdapterView.OnItemSelectedListener spinnerRS485Listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String name = mRS485TypeAdapter.getItem(position);
            String value = mRS485TypeHashMap.get(name);
            mRS485Type = Integer.parseInt(value == null ? String.valueOf(CommonConstants.RS485Type.RS485_1) : value);

            mRS485Reader.rsDestroy();
            mRS485Reader.rsOpen(mRS485Type, 115200);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_switchRS485:
                rs485Send = !rs485Send;
                if(rs485Send){
                    if(mRS485Reader.setMode(CommonConstants.RSMode.SEND_MODE) == ResultCode.SUCCESS)
                        mTvRS485drct.setText(getString(R.string.pos_sendmode));
                }else{
                    if(mRS485Reader.setMode(CommonConstants.RSMode.RECV_MODE) == ResultCode.SUCCESS)
                        mTvRS485drct.setText(getString(R.string.pos_recvmode));
                }
                break;
            case R.id.btn_sendRS485:
                mTvRS485Type.setText("");
                Toast.makeText(this, ""+ mRS485Reader.rsSend("123456789".getBytes()), Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_sendRS232:
                mTvRS232Type.setText("");
                Toast.makeText(this, ""+ mRS232Reader.rsSend("123456789".getBytes()), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private static String toAsciiString(byte[] data){

        if(data == null){
            return null;
        }
        StringBuffer  buffer = new StringBuffer();
        char[] tChar = new char[data.length];
        for(int i=0;i<data.length;i++){

            if(data[i] == 0x0A || data[i] == 0x0D){
                tChar[i] = ';';
            }else{
                tChar[i] = (char) data[i];
            }
        }
        buffer.append(tChar);
        return buffer.toString();
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
