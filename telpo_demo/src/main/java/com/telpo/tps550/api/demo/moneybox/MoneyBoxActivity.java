package com.telpo.tps550.api.demo.moneybox;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.common.apiutil.ErrorCode;
import com.common.apiutil.ResultCode;
import com.common.demo.BuildConfig;
import com.common.demo.R;
import com.common.apiutil.moneybox.MoneyBox;

public class MoneyBoxActivity extends Activity {

	private Button moneybox;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.money_box);

		moneybox = (Button) findViewById(R.id.open_moneybox);
		moneybox.setOnClickListener(new OnClickListener() {

			
			public void onClick(View arg0) {
				if (MoneyBox.open() == ResultCode.ERR_LOW_POWER)
				{
					Toast.makeText(MoneyBoxActivity.this, getString(R.string.moneybox_toast_text_low_battery), Toast.LENGTH_SHORT).show();
					return;
				}
				try {
					Thread.sleep(500);
					MoneyBox.close();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}

}
