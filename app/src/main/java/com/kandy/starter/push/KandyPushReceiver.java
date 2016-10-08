
package com.kandy.starter.push;

import com.google.android.gcm.GCMBroadcastReceiver;

import android.content.Context;

public class KandyPushReceiver extends GCMBroadcastReceiver
{
	
	@Override
	protected String getGCMIntentServiceClassName(Context context)
	{
		return KandyPushService.class.getName();
	}

}
