
package com.kandy.starter.adapters;

import java.util.List;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.groups.KandyGroup;
import com.genband.kandy.api.services.groups.KandyGroupParticipant;
import com.kandy.starter.GroupDetailsActivity;
import com.kandy.starter.R;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * {@link ArrayAdapter} for {@link KandyGroup}.
 * Used to handle and show the KandyGroup items in list and sets to each item 
 * the action onClick
 *
 */
public class GroupAdapter extends ArrayAdapter<KandyGroup> implements OnItemClickListener {

	public GroupAdapter(Context context, int resource, List<KandyGroup> items) {
		super(context, resource, items);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder viewHolder;

		if(convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			convertView = inflater.inflate(R.layout.group_list_item, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.uiGroupNameText = (TextView)convertView.findViewById(R.id.ui_group_list_name);
			viewHolder.uiGroupParticipantsText = (TextView)convertView.findViewById(R.id.ui_group_list_participants_lbl);
			viewHolder.uiGroupParticipantsNumber = (TextView)convertView.findViewById(R.id.ui_group_list_participants_number);
			viewHolder.uiGroupCreatedAt = (TextView)convertView.findViewById(R.id.ui_group_list_created_at);
			viewHolder.uiGroupMuted = (TextView)convertView.findViewById(R.id.ui_group_list_muted);
			viewHolder.uiGroupPermissions = (TextView)convertView.findViewById(R.id.ui_group_list_permissions);
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder)convertView.getTag();
		}

		KandyGroup kandyGroup = getItem(position);

		if(kandyGroup != null) {
			viewHolder.uiGroupNameText.setText(kandyGroup.getGroupName());

			int number = kandyGroup.getGroupParticipants().size();
			viewHolder.uiGroupParticipantsNumber.setText(String.valueOf(number));

			viewHolder.uiGroupCreatedAt.setText(kandyGroup.getCreationDate().toString());
			
			viewHolder.uiGroupMuted.setText(Boolean.toString(kandyGroup.isGroupMuted()));

			String permissions = "read only"; 
			String me = Kandy.getSession().getKandyUser().getUserId();
			List<KandyGroupParticipant> participants = kandyGroup.getGroupParticipants();
			for (KandyGroupParticipant participant : participants)
			{
				if (participant.getParticipant().getUri().equals(me) &&
						participant.isAdmin())
				{
					permissions = "can edit"; 
				}
			}
			viewHolder.uiGroupPermissions.setText(permissions);
		}

		return convertView;
	}

	static class ViewHolder {
		TextView uiGroupNameText;
		TextView uiGroupParticipantsText;
		TextView uiGroupParticipantsNumber;
		TextView uiGroupCreatedAt;
		TextView uiGroupMuted;
		TextView uiGroupPermissions;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		KandyGroup group = (KandyGroup)getItem(position);
		String groupId = group.getGroupId().getUri();
		Intent intent = new Intent(getContext(), GroupDetailsActivity.class);
		intent.putExtra(GroupDetailsActivity.GROUP_ID, groupId);
		getContext().startActivity(intent);
	}
}
