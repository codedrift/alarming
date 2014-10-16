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
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import de.petermoesenthin.alarming.pref.AlarmGson;
import de.petermoesenthin.alarming.pref.AlarmSoundGson;
import de.petermoesenthin.alarming.pref.PrefKey;
import de.petermoesenthin.alarming.ui.SwipeToDismissTouchListener;
import de.petermoesenthin.alarming.util.AlarmUtil;
import de.petermoesenthin.alarming.util.FileUtil;
import de.petermoesenthin.alarming.util.MediaUtil;
import de.petermoesenthin.alarming.util.NotificationUtil;
import de.petermoesenthin.alarming.util.PrefUtil;

public class AlarmReceiverActivity extends Activity implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnSeekCompleteListener{

    //================================================================================
    // Member
    //================================================================================

    private MediaPlayer mMediaPlayer;
    private Thread mPlayerPositionUpdateThread;
    private boolean mAudioPlaying = false;
    private int mStartMillis;
    private int mEndMillis;
    private String mDataSource;
    private int mAlarmId;
    private AlarmGson mAlarmGson;

    private KeyguardManager mKeyGuardManager;
    private KeyguardManager.KeyguardLock mKeyguardLock;

    private Vibrator mVibrator;

    public static final String DEBUG_TAG = "AlarmReceiverActivity";
    public static final boolean D = true;

    private TextView button_snooze;
    private TextView button_dismiss;
    private TextView textView_alarmMessage;
    private LinearLayout layout_buttons;

    //================================================================================
    // Lifecycle
    //================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if (D) {Log.d(DEBUG_TAG, "onCreate()");}
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        /*
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        */
        setContentView(R.layout.activity_alarm_reciver);

        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
        else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        layout_buttons = (LinearLayout) findViewById(R.id.layout_dismiss_snooze);
        button_dismiss = (TextView) findViewById(R.id.button_dismiss);
        button_snooze = (TextView) findViewById(R.id.button_snooze);
        //textView_alarmMessage = (TextView) findViewById(R.id.textView_alarm_message);

        Intent intent = getIntent();
        mAlarmId = intent.getIntExtra("id", -1);

        //textView_alarmMessage.setText("" + mAlarmId);

        button_dismiss.setOnTouchListener(new SwipeToDismissTouchListener(button_dismiss, null,
                new SwipeToDismissTouchListener.DismissCallbacks(){
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }

                    @Override
                    public void onDismiss(View view, Object token) {
                        if (D) {Log.d(DEBUG_TAG, "Alarm has been dismissed.");}
                        clearAlarmSet();
                        button_snooze.setVisibility(View.GONE);
                        layout_buttons.removeView(button_dismiss);
                        finishThis();
                    }
                }
                ));
        int snoozeTime  = PrefUtil.getInt(this, PrefKey.SNOOZE_TIME, 10);
        String text_snooze = getResources().getString(R.string.button_snooze);
        String formatted = String.format(text_snooze, snoozeTime);
        if(snoozeTime == 1){
            formatted += " " + getResources().getString(R.string.minute);
        } else {
            formatted += " " + getResources().getString(R.string.minutes);
        }

        button_snooze.setText(formatted);
        button_snooze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (D) {
                    Log.d(DEBUG_TAG, "Alarm has been snoozed. ya biscuit.");
                }
                setSnooze();
                finishThis();
            }
        });
        List<AlarmGson> alarms = PrefUtil.getAlarms(this);
        mAlarmGson = PrefUtil.findAlarmWithID(alarms, mAlarmId);
    }

    @Override
    public void onAttachedToWindow() {
        if (D) {Log.d(DEBUG_TAG, "onAttachedToWindow()");}
        disableKeyguard();
        if(mAlarmGson.doesVibrate()){
            startVibration();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (D) {Log.d(DEBUG_TAG, "onResume()");}
        playAlarmSound();
    }

    @Override
    public void onStop(){
        super.onStop();
        if (D) {Log.d(DEBUG_TAG, "onStop()");}
        if(mPlayerPositionUpdateThread != null){
            mPlayerPositionUpdateThread.interrupt();
            mPlayerPositionUpdateThread = null;
        }
        if(mMediaPlayer != null){
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        // Media volume
        MediaUtil.resetSystemMediaVolume(this);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }

    @Override
    public void onPause(){
        super.onPause();
        if (D) {Log.d(DEBUG_TAG, "onPause()");}
        if(mMediaPlayer != null){
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /**
     * Does additional work to finish this activity
     */
    public void finishThis(){
        // Stop vibration
        stopVibration();
        // System
        reEnableKeyGuard();
        // Finish Activity
        if (D) {Log.d(DEBUG_TAG, "Finishing Activity.");}
        finish();
        this.overridePendingTransition(android.R.anim.fade_out, android.R.anim.fade_out);
    }

    //================================================================================
    // Methods
    //================================================================================

    /**
     * Disables the keyguard.
     */
    private void disableKeyguard(){
        if (D) {Log.d(DEBUG_TAG, "Disabling Keyguard.");}
        mKeyGuardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        mKeyguardLock = mKeyGuardManager.newKeyguardLock("Alarming_Keyguard");
        if (mKeyGuardManager.inKeyguardRestrictedInputMode()){
            mKeyguardLock.disableKeyguard();
        }
    }

    private void reEnableKeyGuard(){
        if (D) {Log.d(DEBUG_TAG, "Reenabling keyguard.");}
        if(mKeyguardLock != null){
            mKeyguardLock.reenableKeyguard();
        }
    }

    private void startVibration(){
        if (D) {Log.d(DEBUG_TAG, "Starting vibration.");}
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Start without a delay
        // Vibrate for 500 milliseconds
        // Sleep for 500 milliseconds
        long[] pattern = {0, 500, 500};
        if(mVibrator.hasVibrator()){
            mVibrator.vibrate(pattern, 0);
        }
    }

    private void stopVibration(){
        if (D) {Log.d(DEBUG_TAG, "Stopping vibration.");}
        if(mVibrator != null){
            mVibrator.cancel();
        }
    }

    private void clearAlarmSet(){
        // Clear any pending notifications
        NotificationUtil.clearAlarmNotifcation(this, mAlarmId);
        NotificationUtil.clearSnoozeNotification(this, mAlarmId);
        AlarmUtil.deactivateSnooze(this, mAlarmId);
        // Unset alarm from preferences
        AlarmGson alg = PrefUtil.getAlarmGson(this);
        alg.setAlarmSet(false);
        PrefUtil.setAlarmGson(this, alg);
    }

    private void setSnooze(){
        clearAlarmSet();
        int snoozeTime  = PrefUtil.getInt(this, PrefKey.SNOOZE_TIME, 10);
        Calendar snoozetime = Calendar.getInstance();
        snoozetime.setTimeInMillis(System.currentTimeMillis());
        snoozetime.add(Calendar.MINUTE, snoozeTime);
        AlarmUtil.setSnooze(this, snoozetime, mAlarmId);
    }

    /**
     * Prepares alarm sound playback
     */
    private void playAlarmSound(){
        MediaUtil.saveSystemMediaVolume(this);
        MediaUtil.setAlarmVolumeFromPreference(this);
        String[] uris = PrefUtil.getAlarmSoundUris(this);
        boolean fileOK = false;
        if(uris != null && uris.length  > 0) {
            Random r = new Random();
            int rand = r.nextInt(uris.length);
            if (D) {Log.d(DEBUG_TAG, "Found " + uris.length + " alarm sounds. Playing #"
                    + rand + ".");}
            mDataSource = uris[rand];
            fileOK = FileUtil.fileIsOK(this, mDataSource);
            AlarmSoundGson alsg = FileUtil.readSoundConfigurationFile(mDataSource);
            if(alsg != null){
                mStartMillis = alsg.getStartMillis();
                mEndMillis = alsg.getEndMillis();
                //loopAudio = alsg.isLooping();
            } else{
                mStartMillis = 0;
                mEndMillis = 0;
            }
        }
        if(!fileOK) {
            if (D) {Log.d(DEBUG_TAG, "No uri available, playing default alarm sound.");}
            // Play default alarm sound
            mDataSource = Settings.System.DEFAULT_ALARM_ALERT_URI.getPath();
        }
        startMediaPlayer();
    }

    private void startMediaPlayer(){
        if (D) {Log.d(DEBUG_TAG, "Starting media player.");}
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        //mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        try {
            mMediaPlayer.setDataSource(mDataSource);
        } catch (IOException e) {
            if(D) {Log.d(DEBUG_TAG, "Unable to set data source");}
        }
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
        mMediaPlayer.prepareAsync();
    }

    private void createPlayerPositionUpdateThread(){
        mPlayerPositionUpdateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (D) {Log.d(DEBUG_TAG, "Starting player position thread.");}
                int currentPlayerMillis = 0;
                if(mEndMillis == 0){
                    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            loopAudio();
                        }
                    });
                    return;
                }
                while(mAudioPlaying) {
                    try {
                        currentPlayerMillis = mMediaPlayer.getCurrentPosition();
                    } catch (Exception e){
                        if (D) {Log.d(DEBUG_TAG, "Unable to update player position." +
                                " Exiting thread");}
                        return;
                    }
                    if(currentPlayerMillis > mEndMillis){
                        loopAudio();
                        return;
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Log.e(DEBUG_TAG,
                                "Update thread for current position has been interrupted.");
                    }
                }
            }
        });
    }

    private void loopAudio(){
        if(mPlayerPositionUpdateThread != null){
            mPlayerPositionUpdateThread.interrupt();
            mPlayerPositionUpdateThread = null;
        }
        mMediaPlayer.pause();
        mMediaPlayer.seekTo(mStartMillis);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mMediaPlayer.seekTo(mStartMillis);
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        mMediaPlayer.start();
        mAudioPlaying = true;
        createPlayerPositionUpdateThread();
        mPlayerPositionUpdateThread.start();
    }
}
