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

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import de.petermoesenthin.alarming.AlarmReceiverActivity;
import de.petermoesenthin.alarming.pref.PrefKey;
import de.petermoesenthin.alarming.util.NotificationUtil;
import de.petermoesenthin.alarming.util.PrefUtil;

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
        if (D) {Log.d(DEBUG_TAG,"Received alarm intent");}
        Intent i = new Intent(context, AlarmReceiverActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
        PrefUtil.putBoolean(context, PrefKey.ALARM_SET, false);
        NotificationUtil.clearAlarmNotifcation(context);
    }


}
