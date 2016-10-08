
package com.kandy.starter;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.provisioning.IKandyValidationResponse;
import com.genband.kandy.api.provisioning.KandyValidationMethoud;
import com.genband.kandy.api.provisioning.KandyValidationResponseListener;
import com.genband.kandy.api.services.calls.GSMCallPhoneStateListener;
import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.KandyGSMCallServiceNotificationListener;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.genband.kandy.api.services.location.IKandyLocationService;
import com.kandy.starter.utils.UIUtils;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ProvisioningActivity extends Activity
{
	private static final String TAG = ProvisioningActivity.class.getSimpleName();

	/**
	 * To retrieve the two letter country code use {@link IKandyLocationService}
	 * from {@link Kandy#getServices()}
	 */
	private static final String twoLetterISOCountryCode = "IL";

	private TextView uiSignedUserText;
	private EditText uiPhoneNumberEdit;
	private EditText uiOTPCodeEdit;

	private String mUserId;
	private String mActivationCode;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);

		initViews();
	}

	/**
	 * Initializes the all views in activity and binds them to the callbacks
	 */
	public void initViews()
	{
		TextView uiApiKeyText = (TextView) findViewById(R.id.activity_signup_api_key_text);
		uiApiKeyText.setText(KandySampleApplication.API_KEY);
		
		TextView uiApiSecretText = (TextView) findViewById(R.id.activity_signup_api_secret_text);
		uiApiSecretText.setText(KandySampleApplication.API_SECRET);
		
		uiSignedUserText = (TextView) findViewById(R.id.activity_signup_signed_as_text);

		uiPhoneNumberEdit = (EditText) findViewById(R.id.activity_signup_phone_edit);
		uiOTPCodeEdit = (EditText) findViewById(R.id.activity_signup_otp_edit);

		Button uideactivateButton = (Button) findViewById(R.id.activity_signup_signoff_button);
		uideactivateButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				deactivate();
			}
		});

		Button uiSignupButton = (Button) findViewById(R.id.activity_signup_signup_button);
		uiSignupButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				startSignupProcess();
			}
		});
		
		Button uiIVRSignupButton = (Button) findViewById(R.id.activity_signup_ivr_signup_button);
		uiIVRSignupButton.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				startIVRSignupProcess();
			}
		});

		Button uiValidateButton = (Button) findViewById(R.id.activity_signup_otp_button);
		uiValidateButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				startValidationProcess();
			}
		});
	}

	/**
	 * Init and start the signup process
	 */
	public void startSignupProcess()
	{

		mUserId = getPhoneNumber();
		if(mUserId == null || mUserId.isEmpty()) {
			UIUtils.showToastWithMessage(ProvisioningActivity.this, getString(R.string.activity_signup_enter_phone_label));
			return;
		}

		UIUtils.showProgressDialogWithMessage(ProvisioningActivity.this, getString(R.string.registration_signinup_msg));

		requestCode(mUserId);
	}
	
	/**
	 * Init and start the signup process by IVR
	 * will get incoming call with activation code would be extracted from the incoming phone number, 
	 * 4 last digits of incoming phone number
	 */
	public void startIVRSignupProcess()
	{
		
		mUserId = getPhoneNumber();
		if(mUserId == null || mUserId.isEmpty()) {
			UIUtils.showToastWithMessage(ProvisioningActivity.this, getString(R.string.activity_signup_enter_phone_label));
			return;
		}
		
		UIUtils.showProgressDialogWithMessage(ProvisioningActivity.this, getString(R.string.registration_signinup_msg));
		
		requestCodeByIVR(mUserId);
	}

	/**
	 * Request code for verification and registration
	 * 
	 * @param pUserId
	 *            userid - user's phone number
	 */
	public void requestCode(String phoneNumber)
	{
		Kandy.getProvisioning().requestCode(KandyValidationMethoud.SMS, phoneNumber, twoLetterISOCountryCode, new KandyResponseListener()
		{

			@Override
			public void onRequestFailed(int responseCode, String err)
			{
				Log.i(TAG, "sendSignupRequest: onRequestFailed: " + err + " response code: " + responseCode);
				UIUtils.handleResultOnUiThread(ProvisioningActivity.this, true, err);
			}

			@Override
			public void onRequestSucceded()
			{
				Log.i(TAG, "sendSignupRequest: onRequestSucceded");
				UIUtils.handleResultOnUiThread(ProvisioningActivity.this, false,
						getString(R.string.activity_signup_signup_succeed));
			}
		});
	}

	/**
	 * Request code for verification and registration by get incoming call from Kandy.
	 * The validation cod eis included into the incoming phone number,
	 * and it is the 4 last digits of the incoming number
	 * 
	 * @param pUserId
	 *            userid - user's phone number
	 */
	public void requestCodeByIVR(String phoneNumber) {
		
		/*
		 * Defining from which prefix will start the incoming phone number
		 * prefix could contain only digits and must be not longer than 6 digits,
		 * in case of the prefix length less than 6 digits will be auto-completed with zeros
		 * If won't be defined would be used default Kandy's incoming phone number
		 */
		final String customPrefix = "123456";
		
		//validation code contains from 4 digits
		final int activationCodeLength = 4;
		
		/*
		 * Will start to listen for incoming call to get the validation code
		 */
		final GSMCallPhoneStateListener gsmCallStateListener = new GSMCallPhoneStateListener(this, new KandyGSMCallServiceNotificationListener() {
			
			@Override
			public void onGSMCallIncoming(IKandyCall call, String incomingNumber) {
				incomingNumber = incomingNumber.replace("+", "");
				//will check if number starts from defined custom prefix
				if(incomingNumber.startsWith(customPrefix)) {
					//will extract the validation/activation code from the incoming phone number
					mActivationCode = incomingNumber.substring(incomingNumber.length() - activationCodeLength);
				}
			}
			
			@Override
			public void onGSMCallDisconnected(IKandyCall call, String incomingNumber) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onGSMCallConnected(IKandyCall call, String incomingNumber) {
				// TODO Auto-generated method stub
				
			}
		});		
		
		gsmCallStateListener.startToListenState(null);
		
		/*
		 * In case of Call validation the onRequestFailed/onRequestSucceed would be invoked only after incoming 
		 * call estimated: ignored(no answer up to 10 seconds), answered and ended, rejected(call was canceled without answer)
		 */
		Kandy.getProvisioning().requestCode(KandyValidationMethoud.CALL, phoneNumber, twoLetterISOCountryCode, customPrefix, new KandyResponseListener() {
			
			@Override
			public void onRequestFailed(int responseCode, String err) {
				gsmCallStateListener.stopToListenState();
				Log.i(TAG, "sendSignupRequest: onRequestFailed: " + err + " response code: " + responseCode);
				UIUtils.handleResultOnUiThread(ProvisioningActivity.this, true, err);
				
			}
			
			@Override
			public void onRequestSucceded() {
				gsmCallStateListener.stopToListenState();
				UIUtils.showDialogOnUiThread(ProvisioningActivity.this, getString(R.string.activity_signup_ivr_signup_dialog_title),
						getString(R.string.activity_signup_ivr_signup_succeed) + mActivationCode);
			}
		});
	}

	/**
	 * Init and start validation process
	 */
	private void startValidationProcess()
	{
		String otp = getOTPCode();

		if (otp == null || otp.isEmpty())
		{
			Toast.makeText(ProvisioningActivity.this, getString(R.string.activity_signup_enter_otp_label),
					Toast.LENGTH_LONG).show();
			return;
		}

		UIUtils.showProgressDialogWithMessage(ProvisioningActivity.this,
				getString(R.string.registration_validation_msg));
		validate(otp);
	}

	/**
	 * Validation of the signed up phone number send received code to the server
	 */
	public void validate(String pOtp)
	{
		Kandy.getProvisioning().validateAndProvision(mUserId, pOtp, twoLetterISOCountryCode, new KandyValidationResponseListener()
		{

			@Override
			public void onRequestFailed(int responseCode, String err)
			{
				Log.e(TAG, "sendValidationCode: onRequestFailed: " + err + " response code: " + responseCode);
				UIUtils.handleResultOnUiThread(ProvisioningActivity.this, true, err);
			}

			@Override
			public void onRequestSuccess(IKandyValidationResponse response)
			{
				mUserId = response.getUserId();
				setSignedUser(mUserId);
				
				String message = "You are now signup to Kandy!\n\nDomain:%s\nUser:%s\nPassword:%s\n\nPlease Login to start using the SDK"; 
				message = String.format(message, response.getDomainName(), response.getUser(), response.getUserPassword());
				UIUtils.showDialogOnUiThread(ProvisioningActivity.this,
						getString(R.string.activity_signup_validation_succeed_title),
						 message);
			}
		});
	}

	/**
	 * Signing off the registered account(phone number) from a Kandy
	 */
	public void deactivate()
	{
		UIUtils.showProgressDialogWithMessage(ProvisioningActivity.this, getString(R.string.registration_signoff_msg));

		Kandy.getProvisioning().deactivate(new KandyResponseListener()
		{

			@Override
			public void onRequestFailed(int responseCode, String err)
			{
				Log.i(TAG, "send deactivateRequest: onRequestFailed: " + err + " response code: " + responseCode);
				UIUtils.handleResultOnUiThread(ProvisioningActivity.this, true, err);
			}

			@Override
			public void onRequestSucceded()
			{
				Log.i(TAG, "send deactivateRequest: onRequestSucceded");
				UIUtils.handleResultOnUiThread(ProvisioningActivity.this, false,
						getString(R.string.activity_signup_signoff_succeed_label));
				setSignedUser("");
			}
		});
	}

	public void setSignedUser(final String user)
	{
		runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				uiSignedUserText.setText(user);
			}
		});
	}

	public String getPhoneNumber()
	{
		return uiPhoneNumberEdit.getText().toString();
	}

	public String getOTPCode()
	{
		return uiOTPCodeEdit.getText().toString();
	}
}