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
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.util.Random;

import de.petermoesenthin.alarming.callbacks.OnPlaybackChangedListener;
import de.petermoesenthin.alarming.pref.AlarmSoundGson;
import de.petermoesenthin.alarming.util.FileUtil;
import de.petermoesenthin.alarming.util.MediaUtil;
import de.petermoesenthin.alarming.util.PrefUtil;

public class AlarmReceiverActivity extends Activity {

    //================================================================================
    // Member
    //================================================================================

    private MediaPlayer mMediaPlayer = new MediaPlayer();

    KeyguardManager mKeyGuardManager;
    KeyguardManager.KeyguardLock mKeyguardLock;

    public static final String DEBUG_TAG = "AlarmReceiverActivity";
    public static final boolean D = true;

    private Button button_snooze;
    private Button button_dismiss;

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
        setContentView(R.layout.activity_alarmreciver);

        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
        else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        disableKeyguard();
        button_dismiss = (Button) findViewById(R.id.button_dismiss);
        button_snooze = (Button) findViewById(R.id.button_snooze);
    }


    @Override
    public void onAttachedToWindow() {
        playAlarmSound();
        button_dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishThis();
            }
        });
        button_snooze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishThis();
            }
        });
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
        MediaUtil.clearMediaPlayer(mMediaPlayer);
        MediaUtil.resetSystemMediaVolume(this);
        mKeyguardLock.reenableKeyguard();
        finish();
    }

    private void playAlarmSound(){
        MediaUtil.saveSystemMediaVolume(this);
        MediaUtil.setAlarmVolumeFromPreference(this);
        String[] uris = PrefUtil.getAlarmSoundUris(this);
        Uri dataSource = null;
        boolean fileOK = false;
        boolean loop  = true;
        int startMillis = 0;
        int endMillis = 0;
        if(uris != null && uris.length  > 0) {
            Random r = new Random();
            int rand = r.nextInt(uris.length);
            if (D) {Log.d(DEBUG_TAG, "Found " + uris.length + " alarm sounds. Playing #" + rand);}
            dataSource = Uri.parse(uris[rand]);
            fileOK = FileUtil.fileIsOK(this, dataSource.getPath());
            AlarmSoundGson alsg = FileUtil.readSoundConfigurationFile(dataSource.getPath());
            if(alsg != null){
                startMillis = alsg.getStartTimeMillis();
                endMillis = alsg.getEndTimeMillis();
            }
        }
        if(!fileOK) {
            if (D) {Log.d(DEBUG_TAG, "No uri available, playing default alarm sound");}
            // Play default
            dataSource = Settings.System.DEFAULT_ALARM_ALERT_URI;
        }

        MediaUtil.playAudio(this, mMediaPlayer, dataSource, startMillis, endMillis,
                new OnPlaybackChangedListener() {

            @Override
            public void onEndPositionReached(MediaPlayer mediaPlayer) {
                finishThis();
            }

            @Override
            public void onPlaybackInterrupted(MediaPlayer mediaPlayer) {
                finishThis();
            }
            });
    }
}
