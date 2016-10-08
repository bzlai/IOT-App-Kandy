
package com.kandy.starter;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.KandyCallSettings;
import com.genband.kandy.api.services.common.KandyCameraInfo;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import com.genband.kandy.api.utils.KandyLog;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class CallSettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	private static final String TAG = CallSettingsFragment.class.getSimpleName();
	
	private KandyCallSettings mSettings;

	private ListPreference mCameraFacingPrefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preference_call_settings);
		bindDefaultSettings();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

	}

	@Override
	public void onPause() {
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		if (CallSettingsActivity.PREF_KEY_CAMERA_FACING.equals(key)) {

			Preference preference = findPreference(key);
			ListPreference listPref = (ListPreference) preference;
			listPref.getValue();
			mCameraFacingPrefs.setSummary(listPref.getEntry());
			KandyCameraInfo cameraInfo = KandyCameraInfo.valueOf(listPref.getValue());
			applyCameraFacingMode(cameraInfo);
		}
	}

	private void applyCameraFacingMode(KandyCameraInfo cameraInfo) {
		try
		{
			mSettings.setCameraMode(cameraInfo);
		}
		catch (KandyIllegalArgumentException e)
		{
			KandyLog.e(TAG, "applyCameraFacingMode", e);
		}
	}

	/**
	 * Binding the preferences and setting the default values on first run
	 */
	private void bindDefaultSettings() {
		mSettings = Kandy.getServices().getCallService().getSettings();

		mCameraFacingPrefs = (ListPreference) findPreference(CallSettingsActivity.PREF_KEY_CAMERA_FACING);
		int index = getValueIndex(mSettings.getCameraMode().name());
		mCameraFacingPrefs.setValueIndex(index);
		mCameraFacingPrefs.setSummary(mCameraFacingPrefs.getEntry());
	}

	/**
	 * get Value of Preference List by index of the value
	 * 
	 * @param value
	 * @return
	 */
	private int getValueIndex(String value) {

		CharSequence values[] = mCameraFacingPrefs.getEntryValues();
		for (int i = 0; i < values.length; i++) {

			if (values[i].toString().equals(value)) {
				return i;
			}
		}

		return 0;
	}
}
