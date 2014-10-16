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

import de.petermoesenthin.alarming.MainActivity;
import de.petermoesenthin.alarming.R;
import de.petermoesenthin.alarming.pref.AlarmGson;
import de.petermoesenthin.alarming.pref.PrefKey;

public class NotificationUtil {

    private static final int ALARM_NOTIFICATION_ID = 0x006661234;
    private static final int SNOOZE_NOTIFICATION_ID = 0x006661235;

    private static final int INTENT_SNOOZE_DISMISS_ID = 0x006661236;
    private static final int INTENT_ALARM_DISMISS_ID = 0x006661237;

    public static final String ACTION_DISMISS_SNOOZE =
            "de.petermoesenthin.alarming.ACTION_DISMISS_SNOOZE";

    public static final String ACTION_DISMISS_ALARM =
            "de.petermoesenthin.alarming.ACTION_DISMISS_ALARM";


    public static final String DEBUG_TAG = "NotificationUtil";
    private static final boolean D = true;

    /**
     * Shows a persistent notification indicating the alarm time if is set.
     */
    public static void showAlarmSetNotification(Context context, int hour, int minute, int id){
        //Early out if a notification is not wanted
        boolean showNotification = PrefUtil.getBoolean(context,
                PrefKey.SHOW_ALARM_NOTIFICATION, true);
        if(!showNotification){
            if (D) {Log.d(DEBUG_TAG, "Notifications disabled. Returning.");}
            return;
        }
        // Build dismiss intent
        Intent intent = new Intent();
        intent.setAction(ACTION_DISMISS_ALARM);
        intent.putExtra("id", id);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, INTENT_ALARM_DISMISS_ID + id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        //Build and show Notification
        String alarmFormatted = StringUtil.getTimeFormattedSystem(context, hour, minute);
        NotificationCompat.Builder notBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_alarmclock_light)
                .setOngoing(true)
                .setContentTitle(
                        context.getResources().getString(R.string.notification_alarmActivated))
                .setContentText(context.getResources().getString(R.string.notification_timeSetTo) +
                        " " + alarmFormatted)
                .addAction(R.drawable.ic_action_cancel,
                        context.getResources().getString(R.string.notification_cancelSnooze),
                        pIntent);
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        notBuilder.setContentIntent(contentIntent);
        notificationIntent.addFlags(Notification.FLAG_ONGOING_EVENT);
        getNotificationManager(context).notify(ALARM_NOTIFICATION_ID + id, notBuilder.build());
    }

    public static void showSnoozeNotification(Context context, int hour, int minute, int id){
        Intent intent = new Intent();
        intent.setAction(ACTION_DISMISS_SNOOZE);
        intent.putExtra("id", id);
        // Build dismiss intent
        PendingIntent pIntent = PendingIntent.getBroadcast(context, INTENT_SNOOZE_DISMISS_ID + id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        String alarmFormatted = StringUtil.getTimeFormatted(hour, minute);
        NotificationCompat.Builder notBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_alarmclock_light)
                .setOngoing(true)
                .setContentTitle(
                        context.getResources().getString(R.string.notification_snoozeActivated))
                .setContentText(context.getResources().getString(R.string.notification_timeSetTo) +
                        " " + alarmFormatted)
                .addAction(R.drawable.ic_action_cancel,
                        context.getResources().getString(R.string.notification_cancelSnooze),
                        pIntent);
        getNotificationManager(context).notify(SNOOZE_NOTIFICATION_ID + id, notBuilder.build());
    }

    public static void clearAlarmNotifcation(Context context, int id){
        getNotificationManager(context).cancel(ALARM_NOTIFICATION_ID + id);
    }

    public static void clearSnoozeNotification(Context context, int id){
        getNotificationManager(context).cancel(SNOOZE_NOTIFICATION_ID + id);
    }

    public static NotificationManager getNotificationManager(Context context){
        return (NotificationManager) context.getSystemService(
                Activity.NOTIFICATION_SERVICE);
    }

}
