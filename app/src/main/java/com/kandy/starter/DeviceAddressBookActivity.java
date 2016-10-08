
package com.kandy.starter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.addressbook.IKandyContact;
import com.genband.kandy.api.services.addressbook.KandyAddressBookServiceNotificationListener;
import com.genband.kandy.api.services.addressbook.KandyContactsListener;
import com.genband.kandy.api.services.addressbook.KandyDeviceContactsFilter;
import com.kandy.starter.adapters.AddressBookAdapter;
import com.kandy.starter.utils.UIUtils;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;

public class DeviceAddressBookActivity extends ListActivity implements KandyAddressBookServiceNotificationListener
{
	private static final String TAG = DeviceAddressBookActivity.class.getSimpleName();

	/**
	 * contacts list adapter
	 */
	AddressBookAdapter mAdapter;

	/**
	 * contacts list filter
	 */
	Set<KandyDeviceContactsFilter> mFilter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_addressbook);

		init();
		bindViews();
	}

	private void init()
	{
		mAdapter = new AddressBookAdapter(this, R.layout.list_item_device_addressbook, new ArrayList<IKandyContact>());
		setListAdapter(mAdapter);

		mFilter = new HashSet<KandyDeviceContactsFilter>();
		getDeviceContacts(mFilter);
		
	}

	private void bindViews()
	{
		CheckBox emails = (CheckBox) findViewById(R.id.radioButton_emails);
		emails.setOnCheckedChangeListener(mFilterListener);

		CheckBox favorite = (CheckBox) findViewById(R.id.radioButton_favorite);
		favorite.setOnCheckedChangeListener(mFilterListener);

		CheckBox phones = (CheckBox) findViewById(R.id.radioButton_phones);
		phones.setOnCheckedChangeListener(mFilterListener);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		registerNotificationListener();
	}

	@Override
	protected void onPause()
	{
		unregisterNotificationListener();
		super.onPause();
	}

	
	private void registerNotificationListener() {
		Kandy.getServices().getAddressBookService().registerNotificationListener(this);
	}
	
	private void unregisterNotificationListener() {
		Kandy.getServices().getAddressBookService().unregisterNotificationListener(this);
	}
	
	@Override
	protected void onListItemClick(ListView list, View view, int position, long id)
	{
		Log.d(TAG, "onListItemClick: position: " + position + " id: " + id);
	}

	private void getDeviceContacts(Set<KandyDeviceContactsFilter> filter)
	{
		UIUtils.showProgressDialogWithMessage(DeviceAddressBookActivity.this, getString(R.string.loading));
		KandyDeviceContactsFilter[] items = filter.toArray(new KandyDeviceContactsFilter[filter.size()]);
		Kandy.getServices().getAddressBookService().getDeviceContacts(items, new KandyContactsListener()
		{

			@Override
			public void onRequestFailed(int responseCode, String err)
			{
				UIUtils.handleResultOnUiThread(DeviceAddressBookActivity.this, true, err);
			}

			@Override
			public void onRequestSucceded(final List<IKandyContact> contacts)
			{
				runOnUiThread(new Runnable()
				{

					@Override
					public void run()
					{
						mAdapter.updateList(contacts);
						UIUtils.dismissProgressDialog();
					}
				});
			}
		});
	}
	
	private OnCheckedChangeListener mFilterListener = new OnCheckedChangeListener()
	{

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			Log.d(TAG, "onCheckedChanged: id: " + buttonView.getId() + " isChecked: " + isChecked);

			buttonView.setChecked(isChecked);

			switch (buttonView.getId())
			{
			case R.id.radioButton_emails:
				if (isChecked)
				{
					mFilter.add(KandyDeviceContactsFilter.HAS_EMAIL_ADDRESS);
				}
				else
				{
					mFilter.remove(KandyDeviceContactsFilter.HAS_EMAIL_ADDRESS);
				}
				break;
			case R.id.radioButton_phones:
				if (isChecked)
				{
					mFilter.add(KandyDeviceContactsFilter.HAS_PHONE_NUMBER);
				}
				else
				{
					mFilter.remove(KandyDeviceContactsFilter.HAS_PHONE_NUMBER);
				}
				break;
			case R.id.radioButton_favorite:
				if (isChecked)
				{
					mFilter.add(KandyDeviceContactsFilter.IS_FAVORITE);
				}
				else
				{
					mFilter.remove(KandyDeviceContactsFilter.IS_FAVORITE);
				}
				break;
			}

			getDeviceContacts(mFilter);
		}
	};
	
	@Override
	public void onDeviceAddressBookChanged()
	{
		Log.d(TAG, "onDeviceAddressBookChanged: ");
		getDeviceContacts(mFilter);
	}
}
