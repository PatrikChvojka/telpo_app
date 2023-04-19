package com.telpo.tps550.api.demo;

import static com.telpo.tps550.api.demo.timedata.globalFunctions.httpRequest;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import com.common.demo.R;

import android.widget.EditText;

public class MainActivity extends Activity  {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// hide status bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.login);

		EditText loginInputNumber = findViewById(R.id.loginInputNumber);
		Button loginButton = findViewById(R.id.loginButton);
		loginButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				// userId from INPUT
				String userId = loginInputNumber.getText().toString();

				// login
				login(userId);

				//String loginResult = String.valueOf(appLoginStatus());
				// Log.w("myApp", loginResult);

			}
		});

	}

	// login
	public void login(String userId) {

		// make HTTP request
		String result = httpRequest("https://timedata.grafdev.sk/api/user/code/" + userId);

		// save to shared preferences
		/*SharedPreferences.Editor editor = getSharedPreferences("MyPrefsFile", MODE_PRIVATE).edit();
		editor.putString("name", "TimeData User Name");
		editor.apply();*/

		Log.w("myApp result", result);

		// redirect
	}

	// logout
	public void logout() {
		Log.w("myApp", "logout");
	}

	// login status 0/1
	public int appLoginStatus(){
		SharedPreferences preferences = getSharedPreferences("MyPrefsFile", MODE_PRIVATE);
		int userId = preferences.getInt("userId", 0);
		return userId;
	}
}

