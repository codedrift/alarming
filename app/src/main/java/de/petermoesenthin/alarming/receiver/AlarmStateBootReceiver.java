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

import java.util.Calendar;

import de.petermoesenthin.alarming.pref.PrefKey;
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
            boolean alarmSet = PrefUtil.getBoolean(context, PrefKey.ALARM_SET, false);
            if(alarmSet){
                Calendar calendar = Calendar.getInstance();
                long alarmTimeMillis = PrefUtil.getLong(context,PrefKey.NEXT_ALARM_TIME_MILLIS);
                calendar.setTimeInMillis(alarmTimeMillis);
                AlarmUtil.setAlarm(context,calendar);
                NotificationUtil.showAlarmSetNotification(context);
            }
        }
    }
}
