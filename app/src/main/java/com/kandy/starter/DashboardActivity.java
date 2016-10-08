
package com.kandy.starter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class DashboardActivity extends Activity {

	public static final String API_KEY_CHANGED_PREFS_KEY = "api_key_changed";
	public static final String API_SECRET_CHANGED_PREFS_KEY = "api_secret_changed";
	public static final String API_KEY_PREFS_KEY = "api_key";
	public static final String API_SECRET_PREFS_KEY = "api_secret";
	public static final String KANDY_HOST_PREFS_KEY = "kandy_host";
	

	enum KandyServices {
		PROVISIONING, ACCESS, CALLS, CHATS, PRESENCE, LOCATION, PUSH, ADDRESSBOOK, GROUPS, CLOUD_STORAGE, MULTI_PARTY_CONFERENCE
	}

	private ListView uiSDKListView;
	private ListAdapter mListAdapter;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.api_settings_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			startActivity(new Intent(this, APISettingsActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private OnItemClickListener onListItemClicked = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			String name = (String)arg0.getAdapter().getItem(arg2);
			startSampleActivity(KandyServices.valueOf(name.replace(" ", "_")));
		}
	};

	private static List<String> createKandyServicesList() {
		KandyServices[] services = KandyServices.values();
		List<String> list = new ArrayList<String>(KandyServices.values().length);
		for(KandyServices service:services) {
			list.add(service.name().replace("_", " "));
		}

		return list;
	}

	private void initViews() {
		List<String> services = createKandyServicesList();
		mListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, services);
		uiSDKListView = (ListView)findViewById(R.id.activity_main_functionality_list);
		uiSDKListView.setAdapter(mListAdapter);
		uiSDKListView.setOnItemClickListener(onListItemClicked);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initViews();
	}

	/**
	 * Starts the activity with sample by KandyService enum
	 * @param service type of Kandy SDK sample in functionality list
	 */
	private void startSampleActivity(KandyServices service) {

		if(!AccessActivity.isLoggedIn() && !KandyServices.PROVISIONING.equals(service)) {
			startActivity(new Intent(this, AccessActivity.class));
			return;
		}

		switch(service) {
		case PROVISIONING:
			startActivity(new Intent(this, ProvisioningActivity.class));
			break;

		case ACCESS:
			startActivity(new Intent(this, AccessActivity.class));
			break;

		case CALLS:
			startActivity(new Intent(this, CallServiceActivity.class));
			break;

		case CHATS:
			startActivity(new Intent(this, ChatServiceActivity.class));
			break;

		case PUSH:
			startActivity(new Intent(this, PushServiceActivity.class));
			break;

		case PRESENCE:
			startActivity(new Intent(this, PresenceServiceActivity.class));
			break;

		case LOCATION:
			startActivity(new Intent(this, LocationServiceActivity.class));
			break;

		case ADDRESSBOOK:
			startActivity(new Intent(this, AddressBookActivity.class));
			break;
			
		case GROUPS:
			startActivity(new Intent(this, KandyGroupsActivity.class));
			break;
		case CLOUD_STORAGE:
			startActivity(new Intent(this, KandyCloudStorageServiceActivity.class));
			break;
		case MULTI_PARTY_CONFERENCE:
			startActivity(new Intent(this, MultiPartyConferenceActivity.class));
			break;
		default: //will never come here
			break;
		}
	}
}