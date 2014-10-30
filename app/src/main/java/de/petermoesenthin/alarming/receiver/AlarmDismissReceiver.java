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

import java.util.List;

import de.petermoesenthin.alarming.pref.AlarmGson;
import de.petermoesenthin.alarming.util.AlarmUtil;
import de.petermoesenthin.alarming.util.NotificationUtil;
import de.petermoesenthin.alarming.util.PrefUtil;

public class AlarmDismissReceiver extends BroadcastReceiver{

    public static final String DEBUG_TAG = "AlarmDismissReceiver";
    private static final boolean D = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(NotificationUtil.ACTION_DISMISS_ALARM)) {
            if(D) {Log.d(DEBUG_TAG, "Received intent to dismiss alarm");}
            int id = intent.getIntExtra("id", -1);
            if(id == -1){
                if(D) {Log.d(DEBUG_TAG, "Received invalid id to dismiss alarm. Returning.");}
                return;
            }
            AlarmUtil.deactivateAlarm(context, id);
            List<AlarmGson> alarms = PrefUtil.getAlarms(context);
            AlarmGson alg = PrefUtil.findAlarmWithID(alarms, id);
            alg.setAlarmSet(false);
            PrefUtil.setAlarms(context, alarms);
        }
    }
}
