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

import java.io.IOException;
import java.util.Random;

import de.petermoesenthin.alarming.util.NotificationHandler;
import de.petermoesenthin.alarming.util.PrefUtil;

public class AlarmReceiverActivity extends Activity {

    //================================================================================
    // Member
    //================================================================================

    private AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;
    private int mOriginalVolume;

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
        setAlarmVolume();
        NotificationHandler.clearAlarmNotifcation(this);
    }

    private void disableKeyguard(){
        mKeyGuardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        mKeyguardLock = mKeyGuardManager.newKeyguardLock("Alarming");

        if (mKeyGuardManager.inKeyguardRestrictedInputMode()){
            mKeyguardLock.disableKeyguard();
        }
    }

    private void setAlarmVolume(){
        mAudioManager =
                (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        mOriginalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float percent = 0.8f;
        int targetVolume = (int) (maxVolume*percent);
        mAudioManager.setStreamVolume (
                AudioManager.STREAM_MUSIC,
                targetVolume,
                0);
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
        playAlarmSound();
        alert.show();
    }

    /**
     * Does additional work to finish this activity
     */
    public void finishThis(){
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mOriginalVolume, 0);
        mKeyguardLock.reenableKeyguard();
        finish();
    }

    //================================================================================
    // Callbacks
    //================================================================================

    private void playAlarmSound(){
        String[] uris = PrefUtil.getAlarmSoundUris(this);
        Random r = new Random();
        Uri dataSource;
        if(uris != null && uris.length  > 0) {
            dataSource = Uri.parse(uris[r.nextInt(uris.length)]);
        } else {
            if (D) {Log.d(DEBUG_TAG, "No uri availiable, playing default alarm sound");}
            // Play default
            dataSource = Settings.System.DEFAULT_ALARM_ALERT_URI;
        }

        if (D) {Log.d(DEBUG_TAG, "Audio file uri: " + dataSource);}

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(this, dataSource);
        } catch (IOException e) {
            Log.e(DEBUG_TAG, "Failed to access alarm sound uri", e);
            return;
        }
        try {
            mMediaPlayer.prepare();
        } catch (IOException e) {
            Log.e(DEBUG_TAG, "Failed to prepare media player");
            return;
        }
        mMediaPlayer.start();
    }
}
