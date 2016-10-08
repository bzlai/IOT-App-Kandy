
package com.kandy.starter;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.genband.kandy.api.IKandyGlobalSettings;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.chats.KandyChatSettings;
import com.genband.kandy.api.services.chats.KandyThumbnailSize;
import com.genband.kandy.api.services.common.ConnectionType;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import com.genband.kandy.api.utils.KandyLog.Level;
import com.kandy.starter.utils.FileUtils;
import com.kandy.starter.utils.KandyLogger;

import java.io.File;

public class KandySampleApplication extends Application {


	public static final String TAG = KandySampleApplication.class.getSimpleName();

	public static final String API_KEY = "";
	public static final String API_SECRET = "";
	public static final String GCM_PROJECT_ID = "876416103603";

	
	@Override
	public void onCreate() {
		super.onCreate();

		// set log level
		Kandy.getKandyLog().setLogLevel(Level.VERBOSE);

		//Init Kandy SDK
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Kandy.initialize(this, prefs.getString(DashboardActivity.API_KEY_PREFS_KEY, KandySampleApplication.API_KEY),
				prefs.getString(DashboardActivity.API_SECRET_PREFS_KEY, KandySampleApplication.API_SECRET));
		
		// set custom logger instead of using default one
		Kandy.getKandyLog().setLogger(new KandyLogger());
		
		// set chat settings
		applyKandyChatSettings();
		
		// set host
		IKandyGlobalSettings settings = Kandy.getGlobalSettings();
		settings.setKandyHostURL(prefs.getString(DashboardActivity.KANDY_HOST_PREFS_KEY, settings.getKandyHostURL()));
		
		// load tutorials
		prepareLocalStorage();
		
	}


	private void prepareLocalStorage()
	{
		File localStorageDirectory = FileUtils.getFilesDirectory(KandyCloudStorageServiceActivity.LOCAL_STORAGE);
		FileUtils.clearDirectory(localStorageDirectory);
		FileUtils.copyAssets(getApplicationContext(), localStorageDirectory);
	}


	/**
	 * Applies the {@link KandyChatSettings} with user defined settings or default if not set by developer
	 */
	public void applyKandyChatSettings() {

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		KandyChatSettings settings = Kandy.getServices().getChatService().getSettings();

		String value = sp.getString(ChatSettingsActivity.PREF_KEY_POLICY, null);
		if(value != null) { //otherwise will be used default setting from SDK
			ConnectionType downloadPolicy = ConnectionType.valueOf(value);
			settings.setAutoDownloadMediaConnectionType(downloadPolicy);
		}

		int uploadSize = sp.getInt(ChatSettingsActivity.PREF_KEY_MAX_SIZE, -1);
		
		if(uploadSize != -1) { //otherwise will be used default setting from SDK
			try {
				settings.setMediaMaxSize(uploadSize);
			} catch (KandyIllegalArgumentException e) {
				Log.d(TAG, "applyKandyChatSettings: " + e.getMessage());
			}
		}

		value = sp.getString(ChatSettingsActivity.PREF_KEY_PATH, null);
		if(value != null) { //otherwise will be used default setting from SDK
			File downloadPath = new File(value);
			settings.setDownloadMediaPath(downloadPath);
		}
		
		value = sp.getString(ChatSettingsActivity.PREF_KEY_THUMB_SIZE, null);
		if(value != null) { //otherwise will be used default setting from SDK
			KandyThumbnailSize thumbnailSize = KandyThumbnailSize.valueOf(value);
			settings.setAutoDownloadThumbnailSize(thumbnailSize);
		}
	}


}