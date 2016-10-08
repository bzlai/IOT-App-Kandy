
package com.kandy.starter;


import org.json.JSONObject;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.access.KandyConnectServiceNotificationListener;
import com.genband.kandy.api.access.KandyConnectionState;
import com.genband.kandy.api.access.KandyLoginResponseListener;
import com.genband.kandy.api.access.KandyLogoutResponseListener;
import com.genband.kandy.api.access.KandyRegistrationState;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import com.kandy.starter.utils.UIUtils;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AccessActivity extends Activity implements KandyConnectServiceNotificationListener {

	private static final String TAG = AccessActivity.class.getSimpleName();

	private String mUserAccessToken = ""; // put your USER_ACCESS_TOKEN here to force login with it instead of user & password
	
	private EditText uiUsernameEditText;
	private EditText uiPasswordEditText;

	private Button uiLoginButton;
	private Button uiLogoutButton;

	private TextView uiRegistrationStateTextView;
	private TextView uiConnectionStateTextView;

	private OnClickListener onLoginButtonCLicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			startLoginProcess();
		}
	};

	private OnClickListener onLogoutButtonClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			startLogoutProcess();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		initViews();	
	}

	@Override
	protected void onResume() {
		super.onResume();
		initRegistrationStateListeners();
		setConnectionState(Kandy.getAccess().getConnectionState());
		setRegistrationState(Kandy.getAccess().getRegistrationState());
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterRegistrationStateListeners();
	}

	/**
	 * Set the connection state from the NON UI thread on a UI thread
	 * 
	 * @param pState
	 *            connection state {@link KandyConnectionState}
	 */
	private void setConnectionStateOnUIThread(final KandyConnectionState pState) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				setConnectionState(pState);
			}
		});
	}

	/**
	 * Set the registration state from the NON UI thread on a UI thread
	 * 
	 * @param pState
	 *            registration state {@link KandyRegistrationState}
	 */
	private void setRegistrationStateOnUIThread(final KandyRegistrationState pState) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				setRegistrationState(pState);
			}
		});
	}

	/**
	 * Set the registration state
	 * 
	 * @param pState
	 *            registration state {@link KandyRegistrationState}
	 */
	private void setRegistrationState(KandyRegistrationState pState) {
		uiRegistrationStateTextView.setText("Login state: " + pState.name());
	}
	
	/**
	 * Set the connection state
	 * 
	 * @param pState
	 *            connection state {@link KandyConnectionState}
	 */
	private void setConnectionState(KandyConnectionState pState) {
		uiConnectionStateTextView.setText("Websocket state: " + pState.name());
	}

	private void initViews() {
		uiLoginButton = (Button) findViewById(R.id.activity_login_login_button);
		uiLoginButton.setOnClickListener(onLoginButtonCLicked);
		uiLogoutButton = (Button) findViewById(R.id.activity_login_logout_button);
		uiLogoutButton.setOnClickListener(onLogoutButtonClicked);
		uiRegistrationStateTextView = (TextView) findViewById(R.id.activity_login_registration_state);
		uiConnectionStateTextView = (TextView) findViewById(R.id.activity_login_connection_state);
		
		uiUsernameEditText = (EditText)findViewById(R.id.activity_login_username_edit);
		uiPasswordEditText = (EditText)findViewById(R.id.activity_login_password_edit);
	}

	private String getUsername() {
		return uiUsernameEditText.getText().toString();
	}

	private String getPassword() {
		return uiPasswordEditText.getText().toString();
	}

	private void startLoginProcess() {
		UIUtils.showProgressDialogWithMessage(AccessActivity.this, getString(R.string.activity_login_login_process));
		
		// if user access token is define we login without user and password
		if (!TextUtils.isEmpty(mUserAccessToken)) {
			login(mUserAccessToken);
		} else {
			login(getUsername(), getPassword());
		}
	}
	
	/**
	 * Register/login the user on the server with credentials received from admin
	 * @param pUsername username
	 * @param pDomain user's domain
	 * @param pPassword password
	 */
	private void login(final String pUsername, final String pPassword) {

		KandyRecord kandyUser ;

		try {
			kandyUser = new KandyRecord(pUsername);

		} catch (KandyIllegalArgumentException ex) {
			UIUtils.showDialogWithErrorMessage(this, getString(R.string.activity_login_empty_username_text));
			return;
		}

		if(pPassword == null || pPassword.isEmpty()) {
			UIUtils.showDialogWithErrorMessage(this, getString(R.string.activity_login_empty_password_text));
			return ;
		}
		
		Kandy.getAccess().login(kandyUser, pPassword, new KandyLoginResponseListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {
				Log.i(TAG, "Kandy.getAccess().login:onRequestFailed error: " + err + ". Response code: " + responseCode);
				UIUtils.handleResultOnUiThread(AccessActivity.this, true, err);
			}

			@Override
			public void onLoginSucceeded() {
				Log.i(TAG, "Kandy.getAccess().login:onLoginSucceeded");
				UIUtils.handleResultOnUiThread(AccessActivity.this, false, getString(R.string.activity_login_login_success));
				finish();
			}
		});
	}
	
	/**
	 * Register/login the user on the server with userAccessToken 
	 * @param userAccessToken The use access token string
	 */
	private void login(String userAccessToken) {
		Kandy.getAccess().login(userAccessToken, new KandyLoginResponseListener()
		{

			@Override
			public void onRequestFailed(int responseCode, String err) {
				Log.i(TAG, "Kandy.getAccess().login:onRequestFailed error: " + err + ". Response code: " + responseCode);
				UIUtils.handleResultOnUiThread(AccessActivity.this, true, err);
			}

			@Override
			public void onLoginSucceeded() {
				Log.i(TAG, "Kandy.getAccess().login:onLoginSucceeded");
				UIUtils.handleResultOnUiThread(AccessActivity.this, false, getString(R.string.activity_login_login_success));
				finish();
			}
		});
	}

	private void startLogoutProcess() {
		UIUtils.showProgressDialogWithMessage(AccessActivity.this, getString(R.string.activity_login_logout_process));
		logout();
	}

	/**
	 * This method unregisters user from the server
	 */
	private void logout() {
		
		Kandy.getAccess().logout(new KandyLogoutResponseListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {
				Log.i(TAG, "Kandy.getAccess().logout:onRequestFailed error: " + err + ". Response code: " + responseCode);
				UIUtils.handleResultOnUiThread(AccessActivity.this, true, err);
			}

			@Override
			public void onLogoutSucceeded() {
				UIUtils.handleResultOnUiThread(AccessActivity.this, false, getString(R.string.activity_login_logout_success));
			}
		});
	}

	/**
	 * Check if device already logged in or logged out
	 * @return logged or not
	 */
	public static boolean isLoggedIn() {
		return (KandyConnectionState.CONNECTED.equals(Kandy.getAccess().getConnectionState()));
	}

	/**
	 * Register the {@link KandyRegistrationNotificationListener} to reeceive
	 * calls related events like onRegistrationStateChenged
	 */
	private void initRegistrationStateListeners() {
		Log.d(TAG, "initRegistrationStateListeners()");
		Kandy.getAccess().registerNotificationListener(this);
	}
	
	private void unregisterRegistrationStateListeners() {
		Log.d(TAG, "unregisterRegistrationStateListeners()");
		Kandy.getAccess().unregisterNotificationListener(this);
	}
	
	@Override
	public void onConnectionStateChanged(KandyConnectionState state) {
		Log.i(TAG,"onConnectionStateChanged() fired with state: " + state.name());
		setConnectionStateOnUIThread(state);
	}

	@Override
	public void onRegistrationStateChanged(KandyRegistrationState state) {
		Log.i(TAG,"onRegistrationStateChanged() fired with state: " + state.name());
		setRegistrationStateOnUIThread(state);
	}
	
	@Override
	public void onInvalidUser(String error) {
		Log.i(TAG,"onInvalidUser() fired with error: " + error);
		UIUtils.handleResultOnUiThread(AccessActivity.this, true, error);
	}

	@Override
	public void onSessionExpired(String error) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSDKNotSupported(String error) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCertificateError(String error)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onServerConfigurationReceived(JSONObject serverConfiguration)
	{
		// TODO Auto-generated method stub
		
	}
}