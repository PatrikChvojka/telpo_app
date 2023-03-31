package com.telpo.tps550.api.demo.pos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.common.demo.R;
import com.telpo.tps550.api.demo.MyApplication;

public class InterfaceActivityMain extends Activity {

	//UI
	private Button led_btn;
	private Button relay_btn;
	private Button rsserial_btn;
	private Button input_btn;
	private Button gpio_btn;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_other_main);

		initView();

		setFunctions(MyApplication.getConfig());
	}

	private void initView() {
		led_btn = findViewById(R.id.led_btn);
		relay_btn = findViewById(R.id.relay_btn);
		rsserial_btn = findViewById(R.id.rsserial_btn);
		input_btn = findViewById(R.id.input_btn);
		gpio_btn = findViewById(R.id.gpio_btn);

	}

	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.led_btn:
				Intent intentLed = new Intent(InterfaceActivityMain.this, LedActivity.class);
				startActivity(intentLed);
				break;

			case R.id.relay_btn:
				Intent intentRelay = new Intent(InterfaceActivityMain.this, RelayActivity.class);
				startActivity(intentRelay);
				break;

			case R.id.rsserial_btn:
				Intent intentRs = new Intent(InterfaceActivityMain.this, RSSerialActivity.class);
				startActivity(intentRs);
				break;

			case R.id.input_btn:
				Intent intentWiegand = new Intent(InterfaceActivityMain.this, InputActivity.class);
				startActivity(intentWiegand);
				break;
			case R.id.gpio_btn:
				Intent intentGpio = new Intent(InterfaceActivityMain.this, GpioActivity.class);
				startActivity(intentGpio);
				break;

		}
	}

	public void setFunctions(int[] configure) {
		led_btn.setVisibility(View.GONE);
		relay_btn.setVisibility(View.GONE);
		rsserial_btn.setVisibility(View.GONE);
		input_btn.setVisibility(View.GONE);
		gpio_btn.setVisibility(View.GONE);

		for (int i = 0; i < configure.length; i++) {
			switch (configure[i]) {
				case 10:
					led_btn.setVisibility(View.VISIBLE);
					break;
				case 11:
					relay_btn.setVisibility(View.VISIBLE);
					break;
				case 12:
					rsserial_btn.setVisibility(View.VISIBLE);
					break;
				case 13:
					input_btn.setVisibility(View.VISIBLE);
					break;
				case 17:
					gpio_btn.setVisibility(View.VISIBLE);
			}
		}
	}
}
