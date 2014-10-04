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

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Calendar;

import de.petermoesenthin.alarming.MainActivity;
import de.petermoesenthin.alarming.R;
import de.petermoesenthin.alarming.pref.AlarmGson;
import de.petermoesenthin.alarming.pref.PrefKey;

public class NotificationUtil {

    private static final int ALARM_NOTIFICATION_ID = 0x006661234;
    private static final int SNOOZE_NOTIFICATION_ID = 0x006661235;


    public static final String DEBUG_TAG = "NotificationUtil";
    private static final boolean D = true;

    /**
     * Shows a persistent notification indicating the alarm time if is set.
     */
    public static void showAlarmSetNotification(Context context){
        //Early out if a notification is not wanted
        boolean showNotification = PrefUtil.getBoolean(context,
                PrefKey.SHOW_ALARM_NOTIFICATION, true);
        if(!showNotification){
            if (D) {Log.d(DEBUG_TAG, "Notifications disabled. Returning.");}
            return;
        }

        //Build and show Notification
        AlarmGson alg = PrefUtil.getAlarmGson(context);
        String alarmFormatted = StringUtil.getAlarmTimeFormatted(alg.getHour(),alg.getMinute());
        NotificationCompat.Builder notBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_alarmclock_light)
                .setOngoing(true)
                .setContentTitle(
                        context.getResources().getString(R.string.notification_alarmActivated))
                .setContentText(context.getResources().getString(R.string.notification_timeSetTo) +
                        " " + alarmFormatted);
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        notBuilder.setContentIntent(contentIntent);
        notificationIntent.addFlags(Notification.FLAG_ONGOING_EVENT);
        getNotificationManager(context).notify(ALARM_NOTIFICATION_ID, notBuilder.build());
    }

    public static void setSnoozeNotification(Context context, int hour, int minute){
        String alarmFormatted = StringUtil.getAlarmTimeFormatted(hour, minute);
        NotificationCompat.Builder notBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_alarmclock_light)
                .setOngoing(true)
                .setContentTitle(
                        context.getResources().getString(R.string.notification_snoozeActivated))
                .setContentText(context.getResources().getString(R.string.notification_timeSetTo) +
                        " " + alarmFormatted);
        // TODO set button in notification to cancel snooze
        /*
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        notBuilder.setContentIntent(contentIntent);
        notificationIntent.addFlags(Notification.FLAG_ONGOING_EVENT);
        */
        getNotificationManager(context).notify(SNOOZE_NOTIFICATION_ID, notBuilder.build());

    }

    public static void clearAlarmNotifcation(Context context){
        getNotificationManager(context).cancel(ALARM_NOTIFICATION_ID);
    }

    public static void clearSnoozeNotification(Context context){
        getNotificationManager(context).cancel(SNOOZE_NOTIFICATION_ID);
    }

    public static NotificationManager getNotificationManager(Context context){
        return (NotificationManager) context.getSystemService(
                Activity.NOTIFICATION_SERVICE);
    }

}
