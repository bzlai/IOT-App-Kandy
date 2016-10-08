
package com.kandy.starter;

import java.util.ArrayList;
import java.util.List;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.groups.IKandyGroupDestroyed;
import com.genband.kandy.api.services.groups.IKandyGroupParticipantJoined;
import com.genband.kandy.api.services.groups.IKandyGroupParticipantKicked;
import com.genband.kandy.api.services.groups.IKandyGroupParticipantLeft;
import com.genband.kandy.api.services.groups.IKandyGroupUpdated;
import com.genband.kandy.api.services.groups.KandyGroup;
import com.genband.kandy.api.services.groups.KandyGroupParams;
import com.genband.kandy.api.services.groups.KandyGroupResponseListener;
import com.genband.kandy.api.services.groups.KandyGroupServiceNotificationListener;
import com.genband.kandy.api.services.groups.KandyGroupsResponseListener;
import com.kandy.starter.adapters.GroupAdapter;
import com.kandy.starter.utils.UIUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class KandyGroupsActivity extends Activity implements OnItemClickListener, KandyGroupServiceNotificationListener{

	private List<KandyGroup> mGroups;

	private GroupAdapter  mGroupAdapter;

	private EditText uiGroupNameEdit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kandy_groups);

		initViews();
		
		getListOfGroups();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Kandy.getServices().getGroupService().registerNotificationListener(KandyGroupsActivity.this);
	}

	@Override
	protected void onPause()
	{
		Kandy.getServices().getGroupService().unregisterNotificationListener(KandyGroupsActivity.this);
		super.onPause();
	}

	private void initViews() {
		mGroups = new ArrayList<KandyGroup>();
		mGroupAdapter = new GroupAdapter(KandyGroupsActivity.this, R.layout.group_list_item, mGroups);
		uiGroupNameEdit = (EditText)findViewById(R.id.ui_activity_groups_list_group_name_edit);
		
		((ListView)findViewById(R.id.activity_groups_list)).setAdapter(mGroupAdapter);
		((ListView)findViewById(R.id.activity_groups_list)).setOnItemClickListener(KandyGroupsActivity.this);

		((Button)findViewById(R.id.ui_activity_groups_list_group_create_btn)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				createGroup();
			}

		});
	}

	private void createGroup() {
		UIUtils.showProgressDialogWithMessage(KandyGroupsActivity.this, getString(R.string.groups_list_activity_create_group_msg));
		KandyGroupParams params = new KandyGroupParams();
		String name = uiGroupNameEdit.getText().toString();
		params.setGroupName(name);
		
		Kandy.getServices().getGroupService().createGroup(params, new KandyGroupResponseListener() {

			@Override
			public void onRequestFailed(int responseCode, String error) {
				UIUtils.handleResultOnUiThread(KandyGroupsActivity.this, true, error);
			}

			@Override
			public void onRequestSucceded(KandyGroup kandyGroup) {
				mGroups.add(kandyGroup);
				updateUI();
				UIUtils.handleResultOnUiThread(KandyGroupsActivity.this, false, getString(R.string.groups_list_activity_group_created));
			}
		});
	}
	
	private void getListOfGroups() {

		UIUtils.showProgressDialogWithMessage(KandyGroupsActivity.this, getString(R.string.groups_list_activity_get_groups_msg));
		Kandy.getServices().getGroupService().getMyGroups(new KandyGroupsResponseListener() {

			@Override
			public void onRequestFailed(int responseCode, String error) {
				UIUtils.handleResultOnUiThread(KandyGroupsActivity.this, true, error);
			}

			@Override
			public void onRequestSucceded(List<KandyGroup> groupList) {
				mGroups.clear();
				mGroups.addAll(groupList);
				updateUI();
				UIUtils.dismissProgressDialog();
			}
		});
	}

	private void updateUI() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mGroupAdapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		KandyGroup group = (KandyGroup)mGroupAdapter.getItem(position);
		String groupId = group.getGroupId().getUri();
		Intent intent = new Intent(KandyGroupsActivity.this , GroupDetailsActivity.class);
		intent.putExtra(GroupDetailsActivity.GROUP_ID, groupId);
		startActivity(intent);

	}

	@Override
	public void onGroupDestroyed(IKandyGroupDestroyed message)
	{
		runOnUiThread(new Runnable()
		{
			
			@Override
			public void run()
			{
				getListOfGroups();
			}
		});
		
	}

	@Override
	public void onGroupUpdated(IKandyGroupUpdated message)
	{
		runOnUiThread(new Runnable()
		{
			
			@Override
			public void run()
			{
				getListOfGroups();
			}
		});
	}

	@Override
	public void onParticipantJoined(IKandyGroupParticipantJoined message)
	{
		runOnUiThread(new Runnable()
		{
			
			@Override
			public void run()
			{
				getListOfGroups();
			}
		});
	}

	@Override
	public void onParticipantKicked(IKandyGroupParticipantKicked message)
	{
		runOnUiThread(new Runnable()
		{
			
			@Override
			public void run()
			{
				getListOfGroups();
			}
		});
	}

	@Override
	public void onParticipantLeft(IKandyGroupParticipantLeft message)
	{
		runOnUiThread(new Runnable()
		{
			
			@Override
			public void run()
			{
				getListOfGroups();
			}
		});
	}
}
