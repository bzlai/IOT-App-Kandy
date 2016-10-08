
package com.kandy.starter;

import android.app.Activity;
import android.os.Bundle;

public class CallSettingsActivity extends Activity {

	public static final String PREF_KEY_CAMERA_FACING = "camera_facing_preference";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction().replace(android.R.id.content,
                new CallSettingsFragment()).commit();
			
	}
}
