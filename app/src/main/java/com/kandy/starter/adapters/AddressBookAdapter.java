
package com.kandy.starter.adapters;

import java.util.List;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.addressbook.IKandyContact;
import com.genband.kandy.api.services.addressbook.KandyEmailContactRecord;
import com.genband.kandy.api.services.addressbook.KandyPhoneContactRecord;
import com.kandy.starter.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AddressBookAdapter extends ArrayAdapter<IKandyContact>
{
	int layoutResId;
	
	public AddressBookAdapter(Context context, int resource, List<IKandyContact> items)
	{
		super(context, resource, items);
		this.layoutResId = resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder;
		if (convertView == null)
		{
			Context context = Kandy.getApplicationContext();
			convertView = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(layoutResId, null);//parent, false);

			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.ab_device_contact_name);
			holder.numbersTitle = (TextView) convertView.findViewById(R.id.ab_device_contact_numbers_title);
			holder.emailsTitle = (TextView) convertView.findViewById(R.id.ab_device_contact_emails_title);
			holder.numbers =  (TextView) convertView.findViewById(R.id.ab_device_contact_numbers);
			holder.emails =  (TextView) convertView.findViewById(R.id.ab_device_contact_emails);
			
			convertView.setTag(holder);
			
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		IKandyContact contact = getItem(position);
		holder.name.setText(contact.getDisplayName());
		
		holder.numbersTitle.setVisibility(View.GONE);
		holder.numbers.setVisibility(View.GONE);
		holder.emailsTitle.setVisibility(View.GONE);
		holder.emails.setVisibility(View.GONE);
		
		List<KandyPhoneContactRecord> numbers = contact.getNumbers();
		if (numbers != null && !numbers.isEmpty())
		{
			holder.numbersTitle.setVisibility(View.VISIBLE);
			String text = "";
			for (KandyPhoneContactRecord number : numbers)
			{
				text += number + "\n";
			}
			holder.numbers.setText(text);
			holder.numbers.setVisibility(View.VISIBLE);
		}
		
		List<KandyEmailContactRecord> emails = contact.getEmails();
		if (emails != null && !emails.isEmpty())
		{
			holder.emailsTitle.setVisibility(View.VISIBLE);
			String text = "";
			for (KandyEmailContactRecord email : emails)
			{
				text += email + "\n";
			}
			holder.emails.setText(text);
			holder.emails.setVisibility(View.VISIBLE);
		}
		
		return convertView;
	}
	
	private static class ViewHolder {
		TextView name;
		TextView numbersTitle;
		TextView emailsTitle;
		TextView numbers;
		TextView emails;
	}
	
	public void updateList(List<IKandyContact> contacts)
	{
		clear();
		if (contacts != null)
		{
			addAll(contacts);
		}
	}


}
