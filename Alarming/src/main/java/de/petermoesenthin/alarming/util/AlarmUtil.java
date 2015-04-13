/*
 * Copyright (C) 2014-2015 Peter Mösenthin <peter.moesenthin@gmail.com>
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

package de.petermoesenthin.alarming.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

import de.petermoesenthin.alarming.receiver.AlarmReceiver;

public class AlarmUtil {

	public static final String DEBUG_TAG = "AlarmUtil";


	public static void setAlarm(Context context, Calendar calendar, int id) {
		Log.d(DEBUG_TAG, "Activating Alarming: Time=" + calendar.getTime() + ".");
		Calendar now = Calendar.getInstance();
		if (now.after(calendar)) {
			Log.d(DEBUG_TAG, "Alarm was not set. Cannot set alarm in the past.");
		} else {
			setRTCWakeup(context, calendar, id, false);
			NotificationUtil.showAlarmSetNotification(context, calendar.get(Calendar.HOUR_OF_DAY),
					calendar.get(Calendar.MINUTE), id);
		}
	}

	public static void setSnooze(Context context, Calendar calendar, int id) {
		Log.d(DEBUG_TAG, "Activating Snooze: Time=" + calendar.getTime() + ".");
		Calendar now = Calendar.getInstance();
		if (now.after(calendar)) {
			Log.d(DEBUG_TAG, "Snooze was not set. Cannot set snooze in the past.");
		} else {
			setRTCWakeup(context, calendar, id, true);
			NotificationUtil.showSnoozeNotification(context, calendar.get(Calendar.HOUR_OF_DAY),
					calendar.get(Calendar.MINUTE), id);
		}
	}

	private static void setRTCWakeup(Context context, Calendar calendar, int id, boolean isSnooze) {
		PendingIntent pi = PendingIntent.getBroadcast(context, id, getAlarmIntent(context, id, isSnooze),
				PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager)
				context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(
				AlarmManager.RTC_WAKEUP,
				calendar.getTimeInMillis(),
				pi);
	}

	public static void deactivateAlarm(Context context, int id) {
		Log.d(DEBUG_TAG, "Canceling alarm.");
		PendingIntent.getBroadcast(context, id, getAlarmIntent(context, id, false),
				PendingIntent.FLAG_CANCEL_CURRENT);
		NotificationUtil.clearAlarmNotifcation(context, id);
	}

	public static void deactivateSnooze(Context context, int id) {
		Log.d(DEBUG_TAG, "Canceling snooze.");
		PendingIntent.getBroadcast(context, id, getAlarmIntent(context, id, true),
				PendingIntent.FLAG_CANCEL_CURRENT);
		NotificationUtil.clearAlarmNotifcation(context, id);
	}

	private static Intent getAlarmIntent(Context context, int id, boolean isSnooze) {
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.putExtra("id", id);
		intent.putExtra("isSnooze", isSnooze);
		return intent;
	}

	public static Calendar getNextAlarmTimeAbsolute(int hourOfDay, int minute) {
		Calendar cal = Calendar.getInstance();
		Calendar calNow = (Calendar) cal.clone();

		cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		if (cal.compareTo(calNow) <= 0) {
			//Today's time passed, count to tomorrow
			cal.add(Calendar.DATE, 1);
		}
		return cal;
	}
}
