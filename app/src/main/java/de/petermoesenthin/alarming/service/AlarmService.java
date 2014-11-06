package de.petermoesenthin.alarming.service;/*
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

import android.app.IntentService;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import de.petermoesenthin.alarming.AlarmReceiverActivity;
import de.petermoesenthin.alarming.pref.AlarmGson;
import de.petermoesenthin.alarming.pref.AlarmSoundGson;
import de.petermoesenthin.alarming.receiver.AlarmReceiver;
import de.petermoesenthin.alarming.util.FileUtil;
import de.petermoesenthin.alarming.util.MediaUtil;
import de.petermoesenthin.alarming.util.PrefUtil;

public class AlarmService extends IntentService implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnSeekCompleteListener{

    public static final String DEBUG_TAG = "AlarmService";
    public static final boolean D = true;
    private Context mContext;
    private KeyguardManager mKeyGuardManager;
    private KeyguardManager.KeyguardLock mKeyguardLock;
    private Vibrator mVibrator;
    private MediaPlayer mMediaPlayer;
    private Thread mPlayerPositionUpdateThread;
    private boolean mAudioPlaying = false;
    private int mStartMillis;
    private int mEndMillis;
    private String mDataSource;

    //Alarm
    private int mAlarmId;
    private AlarmGson mAlarmGson;
    private List<AlarmGson> mAlarms;

    public AlarmService() {
        super(DEBUG_TAG);
        mContext = this;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (D) {Log.d(DEBUG_TAG, "onHandleEvent called");}
        mAlarmId = intent.getIntExtra("id", -1);
        registerScreenOnReceiver();
        //disableKeyguard();
        playAlarmSound();
        AlarmReceiver.completeWakefulIntent(intent);
    }

    private void registerScreenOnReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(screenOnReceiver, filter);
    }

    @Override
    public void onDestroy() {
        if (D) {Log.d(DEBUG_TAG, "onDestroy called");}
        unregisterScreenOnReceiver();
        //reEnableKeyGuard();
        super.onDestroy();
    }

    private void unregisterScreenOnReceiver() {
        unregisterReceiver(screenOnReceiver);
    }

    private BroadcastReceiver screenOnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                if (D) {Log.d(DEBUG_TAG, "Received ACTION_SCREEN_ON");}
                Intent i = new Intent(context, AlarmReceiverActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("id", mAlarmId);
                context.startActivity(i);
            }
        }
    };

    private void disableKeyguard(){
        if (D) {Log.d(DEBUG_TAG, "Disabling Keyguard");}
        mKeyGuardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        mKeyguardLock = mKeyGuardManager.newKeyguardLock("Alarming_Keyguard");
        if (mKeyGuardManager.inKeyguardRestrictedInputMode()){
            mKeyguardLock.disableKeyguard();
        }
    }

    private void reEnableKeyGuard(){
        if (D) {Log.d(DEBUG_TAG, "Reenabling keyguard");}
        if(mKeyguardLock != null){
            mKeyguardLock.reenableKeyguard();
        }
    }

    private void startVibration(){
        if (D) {Log.d(DEBUG_TAG, "Starting vibration");}
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
        if (D) {Log.d(DEBUG_TAG, "Stopping vibration");}
        if(mVibrator != null){
            mVibrator.cancel();
        }
    }

    /**
     * Prepares alarm sound playback
     */
    private void playAlarmSound(){
        if (D) {Log.d(DEBUG_TAG, "Play alarm sound");}
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
                //TODO loopAudio = alsg.isLooping();
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
