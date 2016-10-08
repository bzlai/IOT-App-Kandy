
package com.kandy.starter;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.location.IKandyAreaCode;
import com.genband.kandy.api.services.location.KandyCountryInfoResponseListener;
import com.kandy.starter.utils.UIUtils;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class LocationServiceActivity extends Activity {

	private static final String TAG = LocationServiceActivity.class.getSimpleName();
	
	private Button uiGetLocationCodeButton;
	
	private TextView uiLocationCodeTextView;
	private TextView uiLocationLongNameTextView;
	private TextView uiLocationNumCodeTextView;
	
	private OnClickListener onGetLocationCodeButtonClicked = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			getLocationCode();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);
		
		initViews();
	}
	
	private void initViews() {
		uiLocationCodeTextView = (TextView)findViewById(R.id.activity_location_two_lewtter_code_text);
		uiLocationLongNameTextView = (TextView)findViewById(R.id.activity_location_long_name_label);
		uiLocationNumCodeTextView = (TextView)findViewById(R.id.activity_location_code_text);
		uiGetLocationCodeButton = (Button)findViewById(R.id.activity_location_get_location_button);
		uiGetLocationCodeButton.setOnClickListener(onGetLocationCodeButtonClicked);
	}
	
	private void setTextInTextViewOnUIThread(final TextView pTxtView, final String pText) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				pTxtView.setText(pText);	
			}
		});
	}
	
	/**
	 * Get Two letter country code
	 */
	private void getLocationCode() {
		
		Kandy.getServices().getLocationService().getCountryInfo(new KandyCountryInfoResponseListener() {
			
			@Override
			public void onRequestFailed(int responseCode, String err) {
				Log.i(TAG, "Kandy.getServices().getLocationService().getCountryInfo: " + err + ".Response code: " + responseCode);
				UIUtils.handleResultOnUiThread(LocationServiceActivity.this, true, err);
			}
			
			@Override
			public void onRequestSuccess(IKandyAreaCode response) {
				UIUtils.handleResultOnUiThread(LocationServiceActivity.this, false, getString(R.string.activity_loaction_got_location_toast_text));
				setTextInTextViewOnUIThread(uiLocationCodeTextView, response.getCountryNameShort());
				setTextInTextViewOnUIThread(uiLocationLongNameTextView, response.getCountryNameLong());
				setTextInTextViewOnUIThread(uiLocationNumCodeTextView, response.getCountryCode());
			}
		});
	}
}