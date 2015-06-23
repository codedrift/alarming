/*
 * Alarming, an alarm app for the Android platform
 *
 * Copyright (C) 2014-2015 Peter Mösenthin <peter.moesenthin@gmail.com>
 *
 * Alarming is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.petermoesenthin.alarming.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.List;

import de.petermoesenthin.alarming.pref.AlarmGson;
import de.petermoesenthin.alarming.util.AlarmUtil;
import de.petermoesenthin.alarming.util.NotificationUtil;
import de.petermoesenthin.alarming.util.PrefUtil;

public class AlarmStateBootReceiver extends BroadcastReceiver {

	//================================================================================
	// Lifecycle
	//================================================================================

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			List<AlarmGson> alarms = PrefUtil.getAlarms(context);
			for (AlarmGson alg : alarms) {
				if (alg.isAlarmSet()) {
					Calendar calendar = AlarmUtil
							.getNextAlarmTimeAbsolute(alg.getHour(), alg.getMinute());
					AlarmUtil.setAlarm(context, calendar, alg.getId());
				}
			}
		}
	}
}
