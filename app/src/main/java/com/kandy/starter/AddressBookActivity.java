
package com.kandy.starter;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AddressBookActivity extends ListActivity {
	
	private String[] items = new String[]
			{ "getDeviceContacts", "getDomainContacts" };

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_addressbook);

		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items));
	}

	@Override
	protected void onListItemClick(ListView list, View view, int position, long id)
	{
		if (position == 0)
		{
			startActivity(new Intent(AddressBookActivity.this, DeviceAddressBookActivity.class));
		}
		else
		{
			startActivity(new Intent(AddressBookActivity.this, DirectoryAddressBookActivity.class));

		}
	}
}
