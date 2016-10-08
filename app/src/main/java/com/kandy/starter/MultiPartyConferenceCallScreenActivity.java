package com.kandy.starter;

import java.util.ArrayList;
import java.util.List;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.audio.IKandyAudioDevice;
import com.genband.kandy.api.services.audio.KandyAudioDeviceType;
import com.genband.kandy.api.services.audio.KandyAudioServiceNotificationListener;
import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.KandyCallState;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.calls.KandyView;
import com.genband.kandy.api.services.common.IKandyDomain;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.genband.kandy.api.services.mpv.IKandyMultiPartyConferenceNotificationListener;
import com.genband.kandy.api.services.mpv.IKandyMultiPartyConferenceParticipant;
import com.genband.kandy.api.services.mpv.IKandyMultiPartyConferenceRoomDetails;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceActionType;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceEvent;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceFailedInvitees;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceInvite;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceInviteListener;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceInvitees;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceParticipantActionParmas;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceParticipantFailedActionParmas;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceParticipantHold;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceParticipantMute;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceParticipantUnHold;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceParticipantUnMute;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceParticipantJoined;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceParticipantLeft;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceParticipantNameChanged;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceParticipantRemoved;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceParticipantVideoDisabled;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceParticipantVideoEnabled;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceRoomDetailsListener;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceRoomRemoved;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceSuccessfullInvitees;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceUpdateParticipantActionsListener;
import com.genband.kandy.api.utils.KandyError;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import com.kandy.starter.utils.UIUtils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.text.TextUtils;
import android.util.Log;

public class MultiPartyConferenceCallScreenActivity extends CallServiceActivity implements KandyAudioServiceNotificationListener, 
IKandyMultiPartyConferenceNotificationListener, OnClickListener
{

	private static final String TAG = MultiPartyConferenceCallScreenActivity.class.getSimpleName();

	public static final String CONFERENCE_ID = "conference_id";
	public static final String ROOM_NUMBER = "room_number";
	public static final String ROOM_PSTN_NUMBER = "room_pstn_number";
	public static final String ROOM_PIN_CODE = "room_pin_code";
	public static final String PARTICIPANT_NICK_NAME = "participant_nick_name";
	public static final String START_CONFERENCE_CALL_WITH_VIDEO ="start_conference_call_with_video";
	public static final String START_CONFERENCE_CALL_WITH_SPEAKER ="start_conference_call_with_speaker";


	private Button muteBtn;
	private Button unmuteBtn;
	private Button holdBtn;
	private Button unholdBtn;
	private Button enableVideoBtn;
	private Button disableVideoBtn;
	private EditText changeNameEditText;
	private Button changeNameBtn;
	private EditText inviteParticipantEditText;
	private Button inviteParticipantBtn;
	private Button removeParticipentBtn;
	private Button infoConferenceBtn;


	private String conferenceId;
	private String roomNumber;
	private String pstnNumber;
	private String pinCode;

	private String nickName;
	private Boolean startConferenceCallWithSpeaker;
	private Boolean isUserJoined = false;

	private KandyCallState mPrevState;

	protected OnCheckedChangeListener onSpeakerTButtonClicked = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(isChecked){
				Kandy.getServices().getAudioService().setAudioDevice(KandyAudioDeviceType.SPEAKER);
			}
			else{
				Kandy.getServices().getAudioService().setAudioDevice(KandyAudioDeviceType.EARPIECE);
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multi_party_converrence_calls);
		initViews();

		Intent intent = getIntent();
		conferenceId = intent.getStringExtra(CONFERENCE_ID);
		roomNumber = intent.getStringExtra(ROOM_NUMBER);
		pstnNumber = intent.getStringExtra(ROOM_PSTN_NUMBER);
		pinCode = intent.getStringExtra(ROOM_PIN_CODE);
		nickName = intent.getStringExtra(PARTICIPANT_NICK_NAME);
		mIsCreateVideoCall = intent.getBooleanExtra(START_CONFERENCE_CALL_WITH_VIDEO, false);
		startConferenceCallWithSpeaker = intent.getBooleanExtra(START_CONFERENCE_CALL_WITH_SPEAKER, false);

		//start conference call
		callRoom(roomNumber);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerMultiPartyConferenceListener();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterMultiPartyConferenceListener();
	}

	
	@Override
	public void onBackPressed() {
		if(mCurrentCall != null) {
			doHangup(mCurrentCall);
		}
		super.onBackPressed();
	}

	private void initViews()
	{
		uiLocalVideoView = (KandyView)findViewById( R.id.activity_calls_local_video_view );
		uiRemoteVideoView = (KandyView)findViewById( R.id.activity_calls_video_view );
		muteBtn = (Button)findViewById( R.id.activity_multi_party_conference_calls_action_mute_btn );
		unmuteBtn = (Button)findViewById( R.id.activity_multi_party_conference_calls_action_unmute_btn );
		holdBtn = (Button)findViewById( R.id.activity_multi_party_conference_calls_action_hold_btn );
		unholdBtn = (Button)findViewById( R.id.activity_multi_party_conference_calls_action_unhold_btn );
		enableVideoBtn = (Button)findViewById( R.id.activity_multi_party_conference_calls_action_enable_video_btn );
		disableVideoBtn = (Button)findViewById( R.id.activity_multi_party_conference_calls_action_disable_video_btn );
		changeNameEditText = (EditText)findViewById( R.id.activity_multi_party_conference_calls_action_change_name_edit );
		changeNameBtn = (Button)findViewById( R.id.activity_multi_party_conference_calls_action_change_name_btn );
		inviteParticipantEditText = (EditText)findViewById( R.id.activity_multi_party_conference_calls_action_invite_participant_edit );
		inviteParticipantBtn = (Button)findViewById( R.id.activity_multi_party_conference_calls_action_invite_participant_btn );
		removeParticipentBtn = (Button)findViewById( R.id.activity_multi_party_conference_calls_action_remove_participent_btn );
		infoConferenceBtn = (Button)findViewById( R.id.activity_multi_party_conference_calls_action_info_conference_btn );
		uiAudioStateTextView = (TextView)findViewById( R.id.activity_calls_state_audio_text );
		uiVideoStateTextView = (TextView)findViewById( R.id.activity_calls_state_video_text );
		uiCallsStateTextView = (TextView)findViewById( R.id.activity_calls_state_call_text );
		uiHoldTButton = (ToggleButton)findViewById( R.id.activity_calls_hold_tbutton );
		uiMuteTButton = (ToggleButton)findViewById( R.id.activity_calls_mute_tbutton );
		uiVideoTButton = (ToggleButton)findViewById( R.id.activity_calls_video_tbutton );
		uiSwitchCameraTButton = (ToggleButton)findViewById( R.id.activity_calls_switch_camera_tbutton );

		uiHangupButton = (ImageView)findViewById(R.id.activity_calls_hangup_button);
		uiHangupButton.setOnClickListener(onHangupButtonClicked);

		uiHoldTButton = (ToggleButton)findViewById(R.id.activity_calls_hold_tbutton);
		uiHoldTButton.setOnCheckedChangeListener(onHoldTButtonClicked);

		uiMuteTButton = (ToggleButton)findViewById(R.id.activity_calls_mute_tbutton);
		uiMuteTButton.setOnCheckedChangeListener(onMuteTButtonClicked);

		uiVideoTButton = (ToggleButton)findViewById(R.id.activity_calls_video_tbutton);
		uiVideoTButton.setOnCheckedChangeListener(onVideoTButtonClicked);

		uiSwitchCameraTButton = (ToggleButton )findViewById(R.id.activity_calls_switch_camera_tbutton);
		uiSwitchCameraTButton.setOnCheckedChangeListener(onSwitchCameraTButtonClicked);

		uiSpeakerButton = (ToggleButton)findViewById(R.id.activity_calls_call_route_speaker);
		uiSpeakerButton.setOnCheckedChangeListener(onSpeakerTButtonClicked);


		muteBtn.setOnClickListener( this );
		unmuteBtn.setOnClickListener( this );
		holdBtn.setOnClickListener( this );
		unholdBtn.setOnClickListener( this );
		enableVideoBtn.setOnClickListener( this );
		disableVideoBtn.setOnClickListener( this );
		changeNameBtn.setOnClickListener( this );
		inviteParticipantBtn.setOnClickListener( this );
		removeParticipentBtn.setOnClickListener( this );
		infoConferenceBtn.setOnClickListener( this );

		setAudioState(mIsMute);
		setVideoState(false, false);
		setCallState(KandyCallState.INITIAL.name());


		KandyCallState callState = mCurrentCall != null ? mCurrentCall.getCallState() : KandyCallState.INITIAL;
		setCallSettingsOnUIThread(callState);
		hideKeyboard();
	}

	@Override
	public void onClick(View v) 
	{
		if ( v == muteBtn )
		{
			muteParticipant();
		} 
		else if ( v == unmuteBtn ) 
		{
			unMuteParticipant();
		}
		else if ( v == holdBtn ) 
		{
			holdParticipant();
		}
		else if ( v == unholdBtn ) 
		{
			unholdParticipant();
		} 
		else if ( v == enableVideoBtn ) 
		{
			enableVideoParticipant();
		}
		else if ( v == disableVideoBtn ) 
		{
			disableVideoParticipant();
		}
		else if ( v == changeNameBtn ) 
		{
			changeParticipantName();
		}
		else if ( v == inviteParticipantBtn ) 
		{
			inviteParticipant();
		}
		else if ( v == removeParticipentBtn ) 
		{
			removeParticipant();
		}
		else if ( v == infoConferenceBtn ) 
		{
			getRoomDeatiles();
		}
	}

	

	private void registerMultiPartyConferenceListener() {
		Log.d(TAG, "registerMultiPartyConferenceListener()");
		Kandy.getServices().getMultiPartyConferenceService().registerNotificationListener(MultiPartyConferenceCallScreenActivity.this);
	}

	private void unregisterMultiPartyConferenceListener() {
		Log.d(TAG, "unregisterMultiPartyConferenceListener()");
		Kandy.getServices().getMultiPartyConferenceService().unregisterNotificationListener(MultiPartyConferenceCallScreenActivity.this);
	}

	public void callRoom(String roomNumber)
	{
		KandyRecord roomRecord = createKandyRecordWithDomain(roomNumber);
		if(roomRecord != null)
		{
			doCall(roomRecord.getUri());
		}
	}


	private KandyRecord createKandyRecordWithDomain(String number) {
		IKandyDomain kandyDomain = Kandy.getSession().getKandyDomain();
		if(kandyDomain == null)
		{
			Log.e(TAG,"createKandyRecordWithDomain:  missing domain" );
			return null;
		}
		String kandyDomainName = kandyDomain.getName();

		KandyRecord record = null;
		try
		{
			record = new KandyRecord(number,kandyDomainName);
		}
		catch (KandyIllegalArgumentException e)
		{
			Log.e(TAG,"createKandyRecordWithDomain: "  + e.getLocalizedMessage());
		}
		return record;
	}

	



	@Override
	public void onAudioRouteChanged(IKandyAudioDevice activeAudioDevice, KandyError error) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAvailableAudioRoutesChanged(ArrayList<IKandyAudioDevice> availableAudioDevices) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVideoStateChanged(IKandyCall call, boolean isReceivingVideo, boolean isSendingVideo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCallStateChanged(KandyCallState state, IKandyCall call) {
		// TODO Auto-generated method stub
		super.onCallStateChanged(state, call);

		switch (state)
		{
		case INITIAL:

			break;
		case TALKING:
			if(mPrevState != KandyCallState.TALKING)
			{
				if(startConferenceCallWithSpeaker && uiSpeakerButton.isChecked() == false)
				{
					//turn on speaker 
					uiSpeakerButton.performClick();
				}
				joinRoom();
			}
			break;
		case TERMINATED:
			if(mPrevState != KandyCallState.TERMINATED)
			{
				removeRoomBeforeLeavingRoom();
			}
		default:
			break;
		}
		mPrevState = state;
	}

	private void closeMultiPartyConferenceScreen()
	{
		Log.d(TAG, "closeMultiPartyConferenceScreen: " + " ");
		finish();
	}
	
	@Override
	protected void setCallSettingsOnUIThread(KandyCallState state) {
		super.setCallSettingsOnUIThread(state);
		
		refreshUI();
	}
	
	private void hideKeyboard() {
		 getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

	}
	

	private void refreshUI() {
		final boolean isInCall;
	
		KandyCallState state = KandyCallState.TERMINATED;
		if(mCurrentCall != null){
			state = mCurrentCall.getCallState();
		}
		
		switch (state) {
		case TERMINATED:
			isInCall = false;
			break;
		case TALKING:
			isInCall = true;
			break;
			
		case INITIAL:
			isInCall = false;
			break;
		case ON_HOLD:
		case ON_DOUBLE_HOLD:
			isInCall = false;
			break;
		default:
			isInCall = false;
			break;
		}

		final boolean isControlActionEnable = isInCall && isUserJoined;

		
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				
				if (muteBtn != null) {
					muteBtn.setEnabled(isControlActionEnable);
				}
				if (unmuteBtn != null) {
					unmuteBtn.setEnabled(isControlActionEnable);
				}
				if (holdBtn != null) {
					holdBtn.setEnabled(isControlActionEnable);
				}
				if (unholdBtn != null) {
					unholdBtn.setEnabled(isControlActionEnable);
				}
				if (enableVideoBtn != null) {
					enableVideoBtn.setEnabled(isControlActionEnable);
				}
				if (disableVideoBtn != null) {
					disableVideoBtn.setEnabled(isControlActionEnable);
				}
				if (changeNameEditText != null) {
					changeNameEditText.setEnabled(isControlActionEnable);
				}
				if (changeNameBtn != null) {
					changeNameBtn.setEnabled(isControlActionEnable);
				}
				if (inviteParticipantEditText != null) {
					inviteParticipantEditText.setEnabled(isControlActionEnable);
				}
				if (inviteParticipantBtn != null) {
					inviteParticipantBtn.setEnabled(isControlActionEnable);
				}
				if (removeParticipentBtn != null) {
					removeParticipentBtn.setEnabled(isControlActionEnable);
				}
				if (infoConferenceBtn != null) {
					infoConferenceBtn.setEnabled(isControlActionEnable);
				}
			}
		});
	}

	
	private void leaveRoom() 
	{
		UIUtils.showProgressDialogOnUiThread(MultiPartyConferenceCallScreenActivity.this, "Leaving conference room");

		Kandy.getServices().getMultiPartyConferenceService().leave(conferenceId, new KandyResponseListener()
		{

			@Override
			public void onRequestFailed(int responseCode, String err)
			{
				String message = String.format(getString(R.string.multi_party_conference_calls_request_failed), "joinRoom", responseCode, err);
				Log.e(TAG, message);
				
				UIUtils.dismissProgressDialog();
			}

			@Override
			public void onRequestSucceded()
			{
				String message = String.format(getString(R.string.multi_party_conference_calls_request_suceeded), "leaveRoom");
				Log.d(TAG, message);
				
				UIUtils.dismissProgressDialog();
				closeMultiPartyConferenceScreen();
			}
		});
	}


	private void joinRoom() 
	{
		Kandy.getServices().getMultiPartyConferenceService().join(conferenceId, nickName, new KandyResponseListener()
		{

			@Override
			public void onRequestFailed(int responseCode, String err)
			{
				String message = String.format(getString(R.string.multi_party_conference_calls_request_failed), "joinRoom", responseCode, err);
				Log.e(TAG, message);
			}

			@Override
			public void onRequestSucceded()
			{
				String message = String.format(getString(R.string.multi_party_conference_calls_request_suceeded), "joinRoom");
				Log.d(TAG, message);
				
				isUserJoined = true;
				refreshUI();
			}
		});
	}
	
	private void removeRoomBeforeLeavingRoom() 
	{
		UIUtils.showProgressDialogOnUiThread(MultiPartyConferenceCallScreenActivity.this, getString(R.string.multi_party_conference_calls_hangup_call));
		
		Kandy.getServices().getMultiPartyConferenceService().getRoomDetails(conferenceId, new KandyMultiPartyConferenceRoomDetailsListener() {
			
			@Override
			public void onRequestFailed(int responseCode, String err) 
			{
				String message = String.format(getString(R.string.multi_party_conference_calls_request_failed), "getRoomDetails", responseCode, err);
				Log.e(TAG, message);

				UIUtils.dismissProgressDialog();
				leaveRoom();
			}
			
			@Override
			public void onRequestSuceeded( IKandyMultiPartyConferenceRoomDetails roomDetails) 
			{
				String message = String.format(getString(R.string.multi_party_conference_calls_request_suceeded), "getRoomDetails");
				Log.d(TAG, message);
				
				UIUtils.dismissProgressDialog();
				if(roomDetails.getParticipants().size() == 0)
				{
					destroyRoom();
				}
				else
				{
					leaveRoom();
				}
			}
		});	
	}

	public void destroyRoom()
	{
		UIUtils.showProgressDialogOnUiThread(MultiPartyConferenceCallScreenActivity.this, getString(R.string.multi_party_conference_calls_destroy_room));
		
		Kandy.getServices().getMultiPartyConferenceService().destroyRoom(conferenceId, new KandyResponseListener() {
			
			@Override
			public void onRequestFailed(int responseCode, String err) {
				String message = String.format(getString(R.string.multi_party_conference_calls_request_failed), "destroyRoom", responseCode, err);
				Log.e(TAG, message);

				UIUtils.dismissProgressDialog();
				leaveRoom();
			}
			
			@Override
			public void onRequestSucceded() {
				String message = String.format(getString(R.string.multi_party_conference_calls_request_suceeded), "destroyRoom");
				Log.d(TAG, message);
				
				UIUtils.dismissProgressDialog();
				closeMultiPartyConferenceScreen();
			}
		});	
	}
	
	
	public void getRoomDeatiles()
	{

		UIUtils.showProgressDialogOnUiThread(MultiPartyConferenceCallScreenActivity.this, getString(R.string.multi_party_conference_calls_get_info_conference));

		Kandy.getServices().getMultiPartyConferenceService().getRoomDetails(conferenceId, new KandyMultiPartyConferenceRoomDetailsListener()
		{

			@Override
			public void onRequestFailed(int responseCode, String err)
			{
				String message = String.format(getString(R.string.multi_party_conference_calls_request_failed), "getRoomDeatiles", responseCode, err);
				Log.e(TAG, message);
				UIUtils.dismissProgressDialog();
				UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, true, message);
			}

			@Override
			public void onRequestSuceeded(final IKandyMultiPartyConferenceRoomDetails roomDetails)
			{
				final String message = String.format(getString(R.string.multi_party_conference_calls_request_suceeded_with_params), 
						"getRoomDeatiles", "roomDetails = "+roomDetails);
				Log.d(TAG, message);
				
				UIUtils.dismissProgressDialog();
				
				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						String info = "conference room = "+roomDetails.getConferenceRoom()+"\n\n"
									+"call duration = "+roomDetails.getCallDuration()+"\n\n"
									+"participants = "+roomDetails.getParticipants()+"\n\n"
									+"administrators = "+roomDetails.getAdministrators();
						
						AlertDialog.Builder builder = new AlertDialog.Builder(MultiPartyConferenceCallScreenActivity.this);
						builder.setTitle(getString(R.string.multi_party_conference_calls_info_conference_title));
						builder.setMessage(info);
						builder.show();
					}
				});

				
			}
		});
	}

	private void inviteParticipant() 
	{
		String inviteeUser = inviteParticipantEditText.getText().toString();
		
		if (TextUtils.isEmpty(inviteeUser)) {
			UIUtils.showDialogWithErrorMessage(MultiPartyConferenceCallScreenActivity.this, getString(R.string.multi_party_conference_invalid_invitee));
			return;
		}
		
		ArrayList<KandyRecord> inviteesByChat = new ArrayList<KandyRecord>();
		try {
			KandyRecord inviteKandyRecord = new KandyRecord(inviteeUser);
			inviteesByChat.add(inviteKandyRecord);
		} 
		catch (KandyIllegalArgumentException e) 
		{
			Log.e(TAG, "addInviteesButton: onClick: " + e.getLocalizedMessage());
			UIUtils.showDialogWithErrorMessage(MultiPartyConferenceCallScreenActivity.this, getString(R.string.activity_chat_phone_number_verification_text));
			return;
		}
		
		KandyMultiPartyConferenceInvitees kandyInvitees = new KandyMultiPartyConferenceInvitees();
		kandyInvitees.setInviteByChat(inviteesByChat);
		kandyInvitees.setInviteBySMS(new ArrayList<String>());
		kandyInvitees.setInviteByMail(new ArrayList<String>());
		
		UIUtils.showProgressDialogOnUiThread(MultiPartyConferenceCallScreenActivity.this, getString(R.string.multi_party_conference_calls_send_invite_participants));

		Kandy.getServices().getMultiPartyConferenceService().invite(conferenceId, kandyInvitees, new KandyMultiPartyConferenceInviteListener() {
			
			@Override
			public void onRequestFailed(int responseCode, String err) 
			{
				String message = String.format(getString(R.string.multi_party_conference_calls_request_failed), "invite", responseCode, err);
				Log.e(TAG, message);

				UIUtils.dismissProgressDialog();
				UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, true, message);

			}
			
			@Override
			public void onRequestSuceeded( KandyMultiPartyConferenceSuccessfullInvitees inviteesSeccess,
					KandyMultiPartyConferenceFailedInvitees failedInvite) 
			{
				String params = inviteesSeccess+" "+failedInvite;
				String message = String.format(getString(R.string.multi_party_conference_calls_request_suceeded_with_params), "invite", params);
				Log.d(TAG, message);

				String messageWithoutParams = String.format(getString(R.string.multi_party_conference_calls_request_suceeded), "invite");

				UIUtils.dismissProgressDialog();
				UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, messageWithoutParams);

			}
		});
	}
	
	
	private void changeParticipantName() 
	{
		String newNickName = changeNameEditText.getText().toString();
		
		if (TextUtils.isEmpty(newNickName)) {
			UIUtils.showDialogWithErrorMessage(MultiPartyConferenceCallScreenActivity.this, getString(R.string.multi_party_conference_calls_invalid_new_nick_name));
			return;
		}
		
		performUpdateNewNickNameOnParticipant(newNickName);
	}

	
	
	public void muteParticipant()
	{
		performControlActionOnParticipant(KandyMultiPartyConferenceActionType.MUTE);
	}
	
	public void unMuteParticipant()
	{
		performControlActionOnParticipant(KandyMultiPartyConferenceActionType.UNMUTE);
	}
	
	public void holdParticipant()
	{
		performControlActionOnParticipant(KandyMultiPartyConferenceActionType.HOLD);
	}
	
	public void unholdParticipant()
	{
		performControlActionOnParticipant(KandyMultiPartyConferenceActionType.UNHOLD);
	}
	
	public void enableVideoParticipant()
	{
		performControlActionOnParticipant(KandyMultiPartyConferenceActionType.ENABLE_VIDEO);
	}
	
	public void disableVideoParticipant()
	{
		performControlActionOnParticipant(KandyMultiPartyConferenceActionType.DISABLE_VIDEO);
	}
	
	public void removeParticipant()
	{
		performControlActionOnParticipant(KandyMultiPartyConferenceActionType.REMOVE);
	}
	
	/** 
	 * This method call to updateParticipantActions API with the action to perform on chosen participant.
	 * @param {@link KandyMultiPartyConferenceActionType} actionType - KandyMultiPartyConferenceActionType the conference action to perform on the chosen participant 
	 */
	private void performControlActionOnParticipant(final KandyMultiPartyConferenceActionType actionType)
	{
		
		performControlActionOnParticipant(actionType.getName(), new CallSelectionListener() {
			
			@Override
			public void execute(String participantId) {

				KandyMultiPartyConferenceParticipantActionParmas actionParam = new KandyMultiPartyConferenceParticipantActionParmas(participantId, actionType); 
				List<KandyMultiPartyConferenceParticipantActionParmas> participantActionParams = new ArrayList<KandyMultiPartyConferenceParticipantActionParmas>();
				participantActionParams.add(actionParam);
				
				String message = getString(R.string.multi_party_conference_calls_preforn_action_on_participant, actionType.getName(), participantId);
				UIUtils.showProgressDialogOnUiThread(MultiPartyConferenceCallScreenActivity.this, message);
				
				Kandy.getServices().getMultiPartyConferenceService().updateParticipantActions(conferenceId, participantActionParams, new KandyMultiPartyConferenceUpdateParticipantActionsListener() {
					
					@Override
					public void onRequestFailed(int responseCode, String err) {
						String message = String.format(getString(R.string.multi_party_conference_calls_request_failed), "updateParticipantActions", responseCode, err);
						Log.e(TAG, message);
						
						UIUtils.dismissProgressDialog();
						UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, true, message);
					}
					
					@Override
					public void onRequestSuceeded(
							List<KandyMultiPartyConferenceParticipantActionParmas> suceededActionParams,
							List<KandyMultiPartyConferenceParticipantFailedActionParmas> failedActionParams) 
					{
						String params = "suceeded = "+suceededActionParams+" failed = "+failedActionParams;
						String message = String.format(getString(R.string.multi_party_conference_calls_request_suceeded_with_params), 
								"updateParticipantActions", params);
						Log.d(TAG, message);
						
						UIUtils.dismissProgressDialog();

						if(failedActionParams.size() > 0 ){
							UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, true, message);
						}
						else{
							UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
						}
					}
				});
			}
		});
	}
	
	/**
	 * This method call to updateParticipantName API with the action to perform on chosen participant.
	 * @param newNickName - the new nick name for the chosen participant
	 */
	private void performUpdateNewNickNameOnParticipant(final String newNickName)
	{

		performControlActionOnParticipant(getString(R.string.multi_party_conference_calls_new_nick_name_title), new CallSelectionListener(){

			@Override
			public void execute(String participantId) {

				UIUtils.showProgressDialogOnUiThread(MultiPartyConferenceCallScreenActivity.this, getString(R.string.multi_party_conference_calls_update_new_nick_name_participants));

				Kandy.getServices().getMultiPartyConferenceService().updateParticipantName(conferenceId, participantId, newNickName, new KandyResponseListener() {

					@Override
					public void onRequestFailed(int responseCode, String err) {
						String message = String.format(getString(R.string.multi_party_conference_calls_request_failed), "updateParticipantActions", responseCode, err);
						Log.e(TAG, message);
						UIUtils.dismissProgressDialog();
						UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, true, message);
					}

					@Override
					public void onRequestSucceded() {
						String message = String.format(getString(R.string.multi_party_conference_calls_request_suceeded_with_params), 
								"updateParticipantName", "newNickName = "+newNickName);
						Log.d(TAG, message);

						UIUtils.dismissProgressDialog();
						UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
					}
				});
			}
		});
	}
	
	
	/**
	 * This method call to getRoomDetails API to get all the participants in the conference.
	 * when the request is succeeded it call to show popup with list of participants with the action that will performed on the chosen participant
	 * @param title - popup title
	 * @param action - the action to perform on the chosen participant
	 */
	private void performControlActionOnParticipant(final String title, final CallSelectionListener action)
	{
		UIUtils.showProgressDialogOnUiThread(MultiPartyConferenceCallScreenActivity.this, getString(R.string.multi_party_conference_calls_search_for_participants));
		
		Kandy.getServices().getMultiPartyConferenceService().getRoomDetails(conferenceId, new KandyMultiPartyConferenceRoomDetailsListener()
		{

			@Override
			public void onRequestFailed(int responseCode, String err)
			{
				String message = String.format(getString(R.string.multi_party_conference_calls_request_failed), "performControlActionOnParticipant:getRoomDetails", responseCode, err);
				Log.e(TAG, message);

				UIUtils.dismissProgressDialog();
				UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, true, message);
			}

			@Override
			public void onRequestSuceeded(IKandyMultiPartyConferenceRoomDetails roomDetails)
			{
				String message = String.format(getString(R.string.multi_party_conference_calls_request_suceeded_with_params), 
						"performControlActionOnParticipant:getRoomDetails", "roomDetails = "+roomDetails);
				Log.d(TAG, message);

				UIUtils.dismissProgressDialog();
				List<IKandyMultiPartyConferenceParticipant> participants = roomDetails.getParticipants();
				showSelectionDialogOnUIThread(title, participants, action);
			}
		});
	}
	
	/**
	 * show dialog with all the participants, when user choose a participant this function will call to execute the action on this participant 
	 * @param title - popup title
	 * @param participants - all the participants to show in popup
	 * @param action - the action to perform on this participant
	 */
	public void showSelectionDialogOnUIThread(final String title, final List<IKandyMultiPartyConferenceParticipant> participants, final CallSelectionListener action)
	{
		
		runOnUiThread(new Runnable()
		{
			
			@Override
			public void run()
			{
				
				if (participants == null || participants.size() == 0)
				{
					UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, true, getString(R.string.multi_party_conference_calls_request_no_participant_to_action));
				}
				else
				{
					CharSequence selections[] = new CharSequence[participants.size()];
					for (int i = 0; i < participants.size(); i++)
					{
						IKandyMultiPartyConferenceParticipant participant = participants.get(i);
						selections[i] = participant.getParticipantId(); 
					}
				
					AlertDialog.Builder builder = new AlertDialog.Builder(MultiPartyConferenceCallScreenActivity.this);
					builder.setTitle(title);
					builder.setItems(selections, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							action.execute(participants.get(which).getParticipantId());
						}
					});
					builder.show();
				}
				
			}
		});
	}
	
	
	private interface CallSelectionListener
	{
		public void execute(String participantId);
	}
	
	

	
	
	
	
	
	
	
	/**
	 * IKandyMultiPartyConferenceNotificationListener
	 */

	@Override
	public void onInviteRecieved(KandyMultiPartyConferenceInvite multiPartyConferenceInvite) 
	{
		Log.d(TAG, "onInviteRecieved = "+multiPartyConferenceInvite);
		markAsRecived(multiPartyConferenceInvite);
		String message = String.format(getString(R.string.multi_party_conference_calls_invite_event), multiPartyConferenceInvite.getRoom().getConferenceId());
		UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
	}

	@Override
	public void onParticipantMute(KandyMultiPartyConferenceParticipantMute participantMute) 
	{
		Log.d(TAG, "onParticipantMute = "+participantMute);
		markAsRecived(participantMute);
		String message = String.format(getString(R.string.multi_party_conference_calls_participant_action_event), 
				participantMute.getParticipantId(), getString(R.string.multi_party_conference_calls_action_mute_btn));

		UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
	}

	@Override
	public void onParticipantUnMute(KandyMultiPartyConferenceParticipantUnMute participantUnMute) 
	{
		Log.d(TAG, "onParticipantUnMute = "+participantUnMute);
		markAsRecived(participantUnMute);
		String message = String.format(getString(R.string.multi_party_conference_calls_participant_action_event), 
				participantUnMute.getParticipantId(), getString(R.string.multi_party_conference_calls_action_unmute_btn));

		UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
	}

	@Override
	public void onParticipantJoinedRoom(KandyMultiPartyConferenceParticipantJoined participantJoinedRoom) 
	{
		Log.d(TAG, "onParticipantJoinedRoom = "+participantJoinedRoom);
		markAsRecived(participantJoinedRoom);
		String message = String.format(getString(R.string.multi_party_conference_calls_participant_action_event), 
				participantJoinedRoom.getParticipantId(), getString(R.string.multi_party_conference_calls_participant_join_room_event));

		UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
	}

	@Override
	public void onParticipantLeftRoom(KandyMultiPartyConferenceParticipantLeft participantLeftRoom)
	{
		Log.d(TAG, "onParticipantLeftRoom = "+participantLeftRoom);
		markAsRecived(participantLeftRoom);
		String message = String.format(getString(R.string.multi_party_conference_calls_participant_action_event), 
				participantLeftRoom.getParticipantId(), getString(R.string.multi_party_conference_calls_participant_left_room_event));

		UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
	}

	@Override
	public void onParticipantNameChanged(KandyMultiPartyConferenceParticipantNameChanged participantNameChanged) 
	{
		Log.d(TAG, "onParticipantNameChanged = "+participantNameChanged);
		markAsRecived(participantNameChanged);
		String message = String.format(getString(R.string.multi_party_conference_calls_participant_name_changed_event), 
				participantNameChanged.getParticipantId(), participantNameChanged.getNewName());
		UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
	}

	@Override
	public void onParticipantVideoEnabled(KandyMultiPartyConferenceParticipantVideoEnabled participantVideoEnable) 
	{
		Log.d(TAG, "onParticipantVideoEnabled = "+participantVideoEnable);
		markAsRecived(participantVideoEnable);
		String message = String.format(getString(R.string.multi_party_conference_calls_participant_action_event), 
				participantVideoEnable.getParticipantId(), getString(R.string.multi_party_conference_calls_action_enable_video_btn));
		UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
	}

	@Override
	public void onParticipantVideoDisabled(KandyMultiPartyConferenceParticipantVideoDisabled participantVideoDisabled) 
	{
		Log.d(TAG, "onParticipantVideoDisabled = "+participantVideoDisabled);
		markAsRecived(participantVideoDisabled);
		String message = String.format(getString(R.string.multi_party_conference_calls_participant_action_event), 
				participantVideoDisabled.getParticipantId(), getString(R.string.multi_party_conference_calls_action_disable_video_btn));
		UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
	}

	@Override
	public void onParticipantHold(KandyMultiPartyConferenceParticipantHold participantHold) 
	{
		Log.d(TAG, "onParticipantHold = "+participantHold);
		markAsRecived(participantHold);
		String message = String.format(getString(R.string.multi_party_conference_calls_participant_action_event), 
				participantHold.getParticipantId(), getString(R.string.multi_party_conference_calls_action_hold_btn));
		UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
	}

	@Override
	public void onParticipantUnHold(KandyMultiPartyConferenceParticipantUnHold participantUnHold) 
	{
		Log.d(TAG, "onParticipantUnHold = "+participantUnHold);
		markAsRecived(participantUnHold);
		String message = String.format(getString(R.string.multi_party_conference_calls_participant_action_event), 
				participantUnHold.getParticipantId(), getString(R.string.multi_party_conference_calls_action_unhold_btn));
		UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
	}

	@Override
	public void onParticipantRemoved(KandyMultiPartyConferenceParticipantRemoved participantRemoved) 
	{
		Log.d(TAG, "onParticipantRemoved = "+participantRemoved);
		markAsRecived(participantRemoved);
		String message = String.format(getString(R.string.multi_party_conference_calls_participant_action_event), 
				participantRemoved.getParticipantId(), getString(R.string.multi_party_conference_calls_action_remove_participent_btn));
		UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
	}

	@Override
	public void onConferenceRoomRemoved(KandyMultiPartyConferenceRoomRemoved conferenceRoomRemoved) 
	{
		Log.d(TAG, "onConferenceRoomRemoved = "+conferenceRoomRemoved);
		markAsRecived(conferenceRoomRemoved);
		String message = String.format(getString(R.string.multi_party_conference_calls_room_removed_event), conferenceRoomRemoved.getConferenceId());
		UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
	}
	
	private void markAsRecived(final KandyMultiPartyConferenceEvent event)
	{
		event.markAsReceived(new KandyResponseListener()
		{
			
			@Override
			public void onRequestFailed(int responseCode, String err)
			{
				Log.e(TAG, event.getEventType()+":markAsReceived:onRequestFailed: "
						+ " responseCode = "+responseCode+" err = "+err);
			}
			
			@Override
			public void onRequestSucceded()
			{
				Log.d(TAG, event.getEventType()+":markAsReceived::onRequestSucceded: "+ " ");
			}
		});
	}


}
