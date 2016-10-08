
package com.kandy.starter;

import java.util.ArrayList;
import java.util.List;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.addressbook.IKandyContact;
import com.genband.kandy.api.services.addressbook.KandyContactsListener;
import com.genband.kandy.api.services.addressbook.KandyDomainContactFilter;
import com.kandy.starter.adapters.AddressBookAdapter;
import com.kandy.starter.utils.UIUtils;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class DirectoryAddressBookActivity extends ListActivity {
	
	private AddressBookAdapter mAdapter;
	
	private EditText mSearchEditText;
	
	private KandyDomainContactFilter mCurrentFilter = KandyDomainContactFilter.ALL;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_directory_address_book);
	
		init();
	}
	
	private void init() {
		mAdapter = new AddressBookAdapter(this, R.layout.list_item_device_addressbook, new ArrayList<IKandyContact>());
		setListAdapter(mAdapter);
		
		getAllDomainDirectoryContacts();
		
		mSearchEditText = (EditText)findViewById(R.id.ui_activity_directory_ab_search_edit);
		
		ImageButton uiSearchButton = (ImageButton)findViewById(R.id.ui_activity_directory_ab_search_btn);
		uiSearchButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String searchString = mSearchEditText.getText().toString();
				getDomainDirectoryContactsWithFilter(mCurrentFilter, searchString);
			}
		});
		
		RadioGroup filterRadioGroup = (RadioGroup)findViewById(R.id.activity_directory_ab_radiogroup);
		filterRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {

				if(R.id.radioButton1 == checkedId) {
					mCurrentFilter = KandyDomainContactFilter.ALL;
				} else if(R.id.radioButton2 == checkedId) {
					mCurrentFilter = KandyDomainContactFilter.FIRST_AND_LAST_NAME;
				} else if(R.id.radioButton3 == checkedId) {
					mCurrentFilter = KandyDomainContactFilter.USER_ID;
				} else {
					mCurrentFilter = KandyDomainContactFilter.PHONE;
				}
			}
		});
	}
	
	private void getAllDomainDirectoryContacts() {
		UIUtils.showProgressDialogWithMessage(this, getString(R.string.activity_directory_ab_progress_get_all_contacts));
	
		Kandy.getServices().getAddressBookService().getDomainDirectoryContacts(new KandyContactsListener() {
			
			@Override
			public void onRequestFailed(int responseCode, String err) {
				UIUtils.handleResultOnUiThread(DirectoryAddressBookActivity.this, true, err);
			}
			
			@Override
			public void onRequestSucceded(final List<IKandyContact> contacts) {
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
	
	private void getDomainDirectoryContactsWithFilter(KandyDomainContactFilter filter, String searchString) {
		UIUtils.showProgressDialogWithMessage(this, getString(R.string.activity_directory_ab_progress_search_contacts) + " \"" + searchString + "\"");
		
		Kandy.getServices().getAddressBookService().getFilteredDomainDirectoryContacts(filter, false, searchString, new KandyContactsListener() {
			
			@Override
			public void onRequestFailed(int responseCode, String err) {
				UIUtils.handleResultOnUiThread(DirectoryAddressBookActivity.this, true, err);
			}
			
			@Override
			public void onRequestSucceded(final List<IKandyContact> contacts) {
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
}
