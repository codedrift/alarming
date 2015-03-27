/*
 * Copyright (C) 2014 Peter Mösenthin <peter.moesenthin@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.petermoesenthin.alarming.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.petermoesenthin.alarming.util.AlarmUtil;
import de.petermoesenthin.alarming.util.NotificationUtil;


public class SnoozeDismissReceiver extends BroadcastReceiver {

	public static final String DEBUG_TAG = "SnoozeDismissReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(NotificationUtil.ACTION_DISMISS_SNOOZE)) {
			Log.d(DEBUG_TAG, "Received intent to dismiss snooze");
			int id = intent.getIntExtra("id", -1);
			if (id == -1) {
				Log.d(DEBUG_TAG, "Received invalid id to dismiss snooze. Returning.");
				return;
			}
			AlarmUtil.deactivateSnooze(context, id);
		}
	}
}
