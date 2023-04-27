package com.telpo.tps550.api.demo;


import static com.telpo.tps550.api.demo.timedata.globalFunctions.contains;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends Activity  {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// hide status bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.login);

		// get login status
		String loginStatus = String.valueOf(appLoginStatus());
		if( contains( loginStatus, "Not logged in" ) ) {
			// not logged in
		}else{
			// logged in -> redirect to scanning page
			startActivity(new Intent(MainActivity.this, scanningPage.class));
		}

		// login button & textfield
		EditText loginInputNumber = findViewById(R.id.loginInputNumber);
		Button loginButton = findViewById(R.id.loginButton);

		// click on loggin button
		loginButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// userId from INPUT
				String userId = loginInputNumber.getText().toString();

				// try to login
				login(userId);
			}
		});

	}

	// login
	public void login(String userId) {

		if (userId == null || userId.length() < 6) {

			AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
			alertDialog.setTitle("Chyba");
			alertDialog.setMessage("Id musí obsahovať 6 znakov");
			alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			alertDialog.show();

		} else {

		// make HTTP request
		final OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
				.url("https://timedata.grafdev.sk/api/user/code/" + userId)
				.build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				e.printStackTrace();
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (response.isSuccessful()) {
					// response
					final String myResponse = response.body().string();
					try {

						JSONObject jObject = new JSONObject(myResponse);
						String aJsonString = jObject.getString("data");

						if (contains(aJsonString, "No user with this Code")) {
							// error dialog

							MainActivity.this.runOnUiThread(new Runnable() {
								public void run() {

									AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
									alertDialog.setTitle("Chyba");
									alertDialog.setMessage("Uživateľ s týmto ID neexistuje");
									alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
											new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog, int which) {
													dialog.dismiss();
												}
											});
									alertDialog.show();
								}
							});

						} else {
							// success login
							Log.w("myApp result ", "success");
							JSONArray jsonArray = jObject.getJSONArray("data");
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject userDetail = jsonArray.getJSONObject(i);
								String company = userDetail.getString("company");

								// save to shared preferences
								SharedPreferences.Editor editor = getSharedPreferences("MyPrefsFile", MODE_PRIVATE).edit();
								editor.putString("company", company);
								editor.apply();

								// redirect
								startActivity(new Intent(MainActivity.this, scanningPage.class));
							}
						}

					} catch (JSONException e) {
						// error
						Log.w("myApp result ", "error");
					}
				}
			}
		});

	}
	}

	// logout
	public void logout() {
		Log.w("myApp", "logout");
	}

	// login status 0/userId
	public String appLoginStatus(){
		SharedPreferences preferences = getSharedPreferences("MyPrefsFile", MODE_PRIVATE);
		String userId = preferences.getString("company", "Not logged in");
		return userId;
	}
}

