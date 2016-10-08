
package com.kandy.starter;


import android.app.Activity;
import android.os.Bundle;

public class ChatSettingsActivity extends Activity {

	public static final String PREF_KEY_PATH = "download_path_preference";
	public static final String PREF_KEY_CUSTOM_PATH = "download_custom_path_preferences";
	public static final String PREF_KEY_MAX_SIZE = "media_size_picker_preference";
	public static final String PREF_KEY_POLICY = "download_policy_preference";
	public static final String PREF_KEY_THUMB_SIZE = "auto_download_thumbnail_size_preference";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction().replace(android.R.id.content,
                new ChatPrefsFragment()).commit();
			
	}	
}
