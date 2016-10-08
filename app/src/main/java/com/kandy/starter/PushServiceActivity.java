
package com.kandy.starter;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.google.android.gcm.GCMRegistrar;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class PushServiceActivity extends Activity {

	private static final String TAG = PushServiceActivity.class.getSimpleName();
	
	private TextView uiPushServiceStateTextView;
	
	private Button uiPushEnableButton;
	private Button uiPushDisableButton;
	
	private OnClickListener onPushEnableButtonCLicked = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			Context context = PushServiceActivity.this;
			GCMRegistrar.checkDevice(context);
			GCMRegistrar.checkManifest(context);
			String registrationId = GCMRegistrar.getRegistrationId(context);

			if (TextUtils.isEmpty(registrationId))
			{
				Log.d(TAG, "onPushEnableButtonCLicked GCM Push registration sent");
				GCMRegistrar.register(context, KandySampleApplication.GCM_PROJECT_ID);
			}

			Log.d(TAG, "onPushEnableButtonCLicked GCM Push registered update Kandy servers with registrationId: " + registrationId);
			
			Kandy.getServices().getPushService().enablePushNotification(registrationId, new KandyResponseListener() {
				
				@Override
				public void onRequestFailed(int responseCode, String err) {
					Log.i(TAG, "Kandy.getPushService().enablePushNotification:onRequestFailed with error: " + err);
				}
				
				@Override
				public void onRequestSucceded() {
					Log.i(TAG, "Kandy.getPushService().enablePushNotification:onRequestSucceded");
					setPushServiceStateOnUIThread(getString(R.string.activity_push_state_enabled_label));
				}
			});
		}
	};
	
	private OnClickListener onPushDisableButtonCLicked = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Kandy.getServices().getPushService().disablePushNotification(new KandyResponseListener() {
				
				@Override
				public void onRequestFailed(int responseCode, String err) {
					Log.i(TAG, "Kandy.getPushService().disablePushNotification:onRequestFailed with error: " + err);
				}
				
				@Override
				public void onRequestSucceded() {
					Log.i(TAG, "Kandy.getPushService().disablePushNotification:onRequestSucceded");
					setPushServiceStateOnUIThread(getString(R.string.activity_push_state_disabled_label));
				}
			});
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_push);
		
		initViews();
	}
	
	private void initViews() {
		uiPushServiceStateTextView = (TextView)findViewById(R.id.activity_push_state_label);
		
		uiPushEnableButton = (Button)findViewById(R.id.activity_push_enable_button);
		uiPushEnableButton.setOnClickListener(onPushEnableButtonCLicked);
		
		uiPushDisableButton = (Button)findViewById(R.id.activity_push_disable_button);
		uiPushDisableButton.setOnClickListener(onPushDisableButtonCLicked);
	}
	
	public void setPushServiceState(String pState) {
		uiPushServiceStateTextView.setText(pState);
	}
	
	public void setPushServiceStateOnUIThread(final String pState) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				setPushServiceState(pState);
			}
		});
	}
}