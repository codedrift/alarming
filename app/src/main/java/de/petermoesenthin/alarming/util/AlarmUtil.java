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
    private static final boolean D = false;

    public static final int ALARM_ID = 6661;


    public static void setAlarm(Context context, Calendar calendar){
        if(D) {Log.d(DEBUG_TAG, "Activating Alarming: Time=" + calendar.getTime());}
        Calendar now = Calendar.getInstance();
        if(now.after(calendar)){
            if(D) {Log.d(DEBUG_TAG, "Alarm was not set. Cannot set alarm in the past.");}
        }else {
            PendingIntent pi = PendingIntent.getBroadcast(context,ALARM_ID, getAlarmIntent(context),
                    PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager)
                    context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pi);
            NotificationUtil.showAlarmSetNotification(context);
        }
    }

    public static void deactivateAlarm(Context context){
        if(D) {Log.d(DEBUG_TAG,"Canceling alarm");}
        PendingIntent.getBroadcast(context, ALARM_ID, getAlarmIntent(context),
                PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationUtil.clearAlarmNotifcation(context);
    }

    private static Intent getAlarmIntent(Context context){
        return new Intent(context, AlarmReceiver.class);
    }

    public static Calendar getNextAlarmTimeAbsolute(int hourOfDay, int minute){
        Calendar cal = Calendar.getInstance();
        Calendar calNow = (Calendar) cal.clone();

        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        if(cal.compareTo(calNow) <= 0){
            //Todays time passed, count to tomorrow
            cal.add(Calendar.DATE, 1);
        }
        return cal;
    }
}
