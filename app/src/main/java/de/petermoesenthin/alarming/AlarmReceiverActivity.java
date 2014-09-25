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

package de.petermoesenthin.alarming;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import java.util.Random;

import de.petermoesenthin.alarming.util.MediaUtil;
import de.petermoesenthin.alarming.util.PrefUtil;

public class AlarmReceiverActivity extends Activity {

    //================================================================================
    // Member
    //================================================================================

    private MediaPlayer mMediaPlayer;

    KeyguardManager mKeyGuardManager;
    KeyguardManager.KeyguardLock mKeyguardLock;

    public static final String DEBUG_TAG = "AlarmReceiverActivity";
    public static final boolean D = true;

    //================================================================================
    // Lifecycle
    //================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
        else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        disableKeyguard();
    }


    @Override
    public void onAttachedToWindow() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Wake up butthead!").setCancelable(
                false).setPositiveButton("Shut up!",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finishThis();
                    }
                }
        ).setNegativeButton("5min more",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finishThis();
                    }
                }
        );
        AlertDialog alert = builder.create();
        MediaUtil.setMediaVolume(this);
        playAlarmSound();
        alert.show();
    }

    //================================================================================
    // Methods
    //================================================================================

    private void disableKeyguard(){
        mKeyGuardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        mKeyguardLock = mKeyGuardManager.newKeyguardLock("Alarming");

        if (mKeyGuardManager.inKeyguardRestrictedInputMode()){
            mKeyguardLock.disableKeyguard();
        }
    }

    /**
     * Does additional work to finish this activity
     */
    public void finishThis(){
        MediaUtil.stopAudioPlayback(mMediaPlayer);
        MediaUtil.resetMediaVolume(this);
        mKeyguardLock.reenableKeyguard();
        finish();
    }

    private void playAlarmSound(){
        String[] uris = PrefUtil.getAlarmSoundUris(this);
        Uri dataSource;
        if(uris != null && uris.length  > 0) {
            Random r = new Random();
            int rand = r.nextInt(uris.length);
            if (D) {Log.d(DEBUG_TAG, "Found " + uris.length + " alarm sounds. Playing #" + rand);}
            dataSource = Uri.parse(uris[rand]);
        } else {
            if (D) {Log.d(DEBUG_TAG, "No uri availiable, playing default alarm sound");}
            // Play default
            dataSource = Settings.System.DEFAULT_ALARM_ALERT_URI;
        }
        mMediaPlayer = new MediaPlayer();
        MediaUtil.playAudio(this, mMediaPlayer, dataSource);
    }
}
