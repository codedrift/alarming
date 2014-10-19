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

import de.petermoesenthin.alarming.AlarmReceiverActivity;
import de.petermoesenthin.alarming.util.AlarmUtil;

public class AlarmReceiver extends BroadcastReceiver {

    //================================================================================
    // Members
    //================================================================================

    public static final String DEBUG_TAG = "AlarmReceiver";
    private static final boolean D = true;

    //================================================================================
    // Lifecycle
    //================================================================================

    @Override
    public void onReceive(Context context, Intent intent){
        int alarmID = intent.getIntExtra("id", -1);
        if (D) {Log.d(DEBUG_TAG,"Received alarm intent for id" + alarmID);}
        Intent i = new Intent(context, AlarmReceiverActivity.class);
        i.putExtra("id", alarmID);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
