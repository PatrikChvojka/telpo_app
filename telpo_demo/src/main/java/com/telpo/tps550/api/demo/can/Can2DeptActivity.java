package com.telpo.tps550.api.demo.can;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.common.apiutil.CommonException;
import com.common.apiutil.can.CanUtil2Dept;
import com.common.demo.R;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class Can2DeptActivity extends Activity {

    private TextView tv;
    private EditText edt_id;
    private EditText edt_data;
    private CheckBox cb_mode_self;
    private Spinner spinner_frame_format;
    private Spinner spinner_frame_type;
    private boolean mTextColorRed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_can2_dept);

        initView();
    }

    private void initView() {

        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WindowManager.LayoutParams lp = this.getWindow().getAttributes();
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                this.getWindow().setAttributes(lp);
            }

            int fullScreenUiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
            getWindow().getDecorView().setSystemUiVisibility(fullScreenUiOptions);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        tv = findViewById(R.id.tv_can_log);
        edt_id = findViewById(R.id.edt_can_send_id);
        edt_data = findViewById(R.id.edt_can_send_data);
        cb_mode_self = findViewById(R.id.cb_can_mode_self);

        cb_mode_self.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast.makeText(Can2DeptActivity.this, R.string.can_set_mode_loopback_tips, Toast.LENGTH_SHORT).show();
                setLog(getString(R.string.can_set_mode_loopback_tips));
            }
        });

        {
            spinner_frame_format = findViewById(R.id.sp_can_frame_format);
            ArrayAdapter<String> mFormatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
            mFormatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mFormatAdapter.add(getString(R.string.can_standard_frame));
            mFormatAdapter.add(getString(R.string.can_extended_frame));
            spinner_frame_format.setAdapter(mFormatAdapter);

            mFormatAdapter.notifyDataSetChanged();
            spinner_frame_format.setSelection(0);
        }

        {
            spinner_frame_type = findViewById(R.id.sp_can_frame_type);
            ArrayAdapter<String> mTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
            mTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mTypeAdapter.add(getString(R.string.can_data_frame));
            mTypeAdapter.add(getString(R.string.can_remote_frame));
            spinner_frame_type.setAdapter(mTypeAdapter);

            mTypeAdapter.notifyDataSetChanged();
            spinner_frame_type.setSelection(0);
        }

        {
            final CanUtil2Dept can = new CanUtil2Dept(this);
            findViewById(R.id.btn_can_set_mode).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int mode = 0;
                    if (cb_mode_self.isChecked()) {
                        mode = 1;
                    }
                    try {
                        int ret = can.configMode(0, 0, 0, 36, mode);
                        String log = String.format(Locale.getDefault(), "%s: %d", getString(R.string.can_set_mode_result), ret);
                        setLog(log);
                    } catch (CommonException e) {
                        throw new RuntimeException(e);
                    }

                }
            });

            findViewById(R.id.btn_can_set_filter).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int ret = can.configFilter(0, 0, 1, 0, 0,
                                0, 0, 1);
                        String log = String.format(Locale.getDefault(), "%s: %d", getString(R.string.can_set_filter_result), ret);
                        setLog(log);
                    } catch (CommonException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            findViewById(R.id.btn_can_send_data).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String msg = edt_data.getText().toString();
                    if (TextUtils.isEmpty(msg)) {
                        Toast.makeText(Can2DeptActivity.this, R.string.can_send_parameter_error_tips, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String id = edt_id.getText().toString();
                    if (TextUtils.isEmpty(id)) {
                        Toast.makeText(Can2DeptActivity.this, R.string.can_send_parameter_null_tips, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int msgId = 0;
                    try {
                        msgId = Integer.parseInt(id);
                    } catch (NumberFormatException ignore) {
                    }

                    int format = 0;
                    if (spinner_frame_format.getSelectedItemPosition() == 1) {
                        format = 4;
                    }

                    int type = 0;
                    if (spinner_frame_type.getSelectedItemPosition() == 1) {
                        type = 2;
                    }

                    byte[] data = msg.getBytes(StandardCharsets.UTF_8);
                    try {
                        int ret = can.write(msgId, format, type, data, data.length);
                        String log = String.format(Locale.getDefault(), "%s: %d", getString(R.string.can_send_result), ret);
                        setLog(log);
                    } catch (CommonException e) {
                        throw new RuntimeException(e);
                    }

                }
            });

            findViewById(R.id.btn_can_receive_data).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        tv.setText("");
                        byte[] ret = can.read(20, 5);
                        if (ret.length < 20) {
                            return;
                        }

                        String log = "";
                        if (ret[8] == 0x00) {
                            int id = (((int) ret[3] & 0xff) << 24) +
                                    (((int) ret[2] & 0xff) << 16) +
                                    (((int) ret[1] & 0xff) << 8) +
                                    (((int) ret[0] & 0xff));
                            log += String.format(Locale.getDefault(), "ID: %d\r\n%s %s\r\n",
                                    id, getString(R.string.can_frame_format), getString(R.string.can_standard_frame));
                        } else if (ret[8] == 0x04) {
                            int id = (((int) ret[7] & 0xff) << 24) +
                                    (((int) ret[6] & 0xff) << 16) +
                                    (((int) ret[5] & 0xff) << 8) +
                                    (((int) ret[4] & 0xff));
                            log += String.format(Locale.getDefault(), "ID: %d\r\n%s %s\r\n",
                                    id, getString(R.string.can_frame_format), getString(R.string.can_extended_frame));
                        }
                        if (ret[9] == 0x00) {
                            log += String.format(Locale.getDefault(), "%s %s\r\n",
                                    getString(R.string.can_frame_type), getString(R.string.can_data_frame));
                        } else if (ret[9] == 0x02) {
                            log += String.format(Locale.getDefault(), "%s %s\r\n",
                                    getString(R.string.can_frame_type), getString(R.string.can_remote_frame));
                        }
                        String data = new String(ret, 11, 8, StandardCharsets.UTF_8);
                        log += String.format(Locale.getDefault(), "%s %d\r\n%s %s\r\n%s %d",
                                getString(R.string.can_data_len), ret[10],
                                getString(R.string.can_data), data,
                                "FMI:", ret[19]);

                        setLog(log);
                    } catch (CommonException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    private void setLog(String msg) {

        tv.setText("");
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        if (mTextColorRed) {
            tv.setTextColor(Color.RED);
        } else {
            tv.setTextColor(Color.BLUE);
        }
        mTextColorRed = !mTextColorRed;
        tv.setText(msg);
    }

}