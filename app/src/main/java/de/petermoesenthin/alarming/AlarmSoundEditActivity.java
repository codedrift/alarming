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
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.edmodo.rangebar.RangeBar;

import de.petermoesenthin.alarming.callbacks.OnPlaybackChangedListener;
import de.petermoesenthin.alarming.pref.AlarmSoundGson;
import de.petermoesenthin.alarming.util.FileUtil;
import de.petermoesenthin.alarming.util.MediaUtil;
import de.petermoesenthin.alarming.util.PrefUtil;
import de.petermoesenthin.alarming.util.StringUtil;
import de.petermoesenthin.alarming.util.XMediaPlayer;

public class AlarmSoundEditActivity extends Activity{

    //================================================================================
    // Members
    //================================================================================

    public static final String DEBUG_TAG = "AlarmSoundEditActivity";
    private static final boolean D = true;

    private String soundFilePath;
    private int soundMillis;
    private int soundStartMillis;
    private int soundEndMillis;

    private TextView textView_soundTitle;
    private TextView textView_soundArtist;
    private RangeBar rangeBar_soundSelector;
    private TextView textView_soundStart;
    private TextView textView_soundEnd;
    private TextView textView_currentPosition;
    private Button button_playPause;
    private boolean audioPlaying = false;

    private Handler mHandler = new Handler();

    private XMediaPlayer xMediaPlayer = XMediaPlayer.getInstance();

    private Thread playerPositionUpdateThread;

    //================================================================================
    // Lifecycle
    //================================================================================

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // UI
        setContentView(R.layout.activity_alarm_sound_edit);
        this.getActionBar().setHomeButtonEnabled(true);
        this.getActionBar().setDisplayHomeAsUpEnabled(true);
        loadUiResources();
        setTitle(R.string.activity_title_alarmSoundEdit);

        // call after ui setup to load all variables
        soundFilePath = readIntentUri();
        String[] metaData = MediaUtil.getBasicMetaData(soundFilePath);
        String soundArtist = metaData[0];
        String soundTitle = metaData[1];
        soundMillis = Integer.parseInt(metaData[2]);
        readConfig();
        setUpRangeBar();
        textView_soundTitle.setText(soundTitle);
        textView_soundArtist.setText(soundArtist);
        textView_soundStart.setText(StringUtil.getTimeFormattedFromMillis(soundStartMillis));
        textView_soundEnd.setText(StringUtil.getTimeFormattedFromMillis(soundEndMillis));
        textView_currentPosition.setText(StringUtil.getTimeFormattedFromMillis(soundStartMillis));

        button_playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(audioPlaying){
                    pauseAudio();
                } else {
                    playAudio();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        xMediaPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Uri soundUri = Uri.parse(soundFilePath);
        xMediaPlayer.setUp(this, soundUri);
    }

    @Override
    protected void onStop() {
        super.onStop();
        xMediaPlayer.stop();
        xMediaPlayer.reset();
        xMediaPlayer.release();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_alarmsoundedit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //================================================================================
    // Callbacks
    //================================================================================

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.action_save_sound_config:
                saveConfig();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //================================================================================
    // Methods
    //================================================================================

    private String readIntentUri(){
        String intentUri = "";
        Intent intent = getIntent();
        int audioId = intent.getIntExtra("audio_id",-1);
        if (audioId == -1) {
            if (D) {Log.d(DEBUG_TAG, "Intent was empty / did not pass a uri");}
        } else {
            intentUri = PrefUtil.getAlarmSoundUris(this)[audioId];
            if (D) {Log.d(DEBUG_TAG, "Building activity for: " + intentUri);}
        }
        return intentUri;
    }

    private void readConfig(){
        if (D) {Log.d(DEBUG_TAG, "Reading configuration.");}
        AlarmSoundGson alsg = FileUtil.readSoundConfigurationFile(soundFilePath);
        if(alsg == null){
            soundStartMillis = 0;
            soundEndMillis = soundMillis;
        } else {
            soundStartMillis = alsg.getStartTimeMillis();
            soundEndMillis = alsg.getEndTimeMillis();
        }
    }

    private void saveConfig(){
        if (D) {Log.d(DEBUG_TAG, "Saving configuration.");}
        AlarmSoundGson alsg = new AlarmSoundGson();
        alsg.setStartTimeMillis(soundStartMillis);
        alsg.setEndTimeMillis(soundEndMillis);
        alsg.setPath(soundFilePath);
        alsg.setPathHash(soundFilePath.hashCode());
        FileUtil.writeSoundConfigurationFile(soundFilePath,alsg);
        Toast.makeText(this,R.string.toast_config_saved,Toast.LENGTH_SHORT).show();
    }

    private void playAudio(){
        if (D) {Log.d(DEBUG_TAG, "Playing audio");}
        setPlayButton(true);
        xMediaPlayer.setOnXPrepareListener(new XMediaPlayer.OnXPrepareListener() {
            @Override
            public void onPrepared() {
                xMediaPlayer.seek();
            }
        });
        xMediaPlayer.setOnXSeekCompleteListener(new XMediaPlayer.OnXSeekCompleteListener() {
            @Override
            public void onSeekComplete() {
                xMediaPlayer.start();
                playerPositionUpdateThread.start();
            }
        });
        xMediaPlayer.setOnXReachPositionListener(new XMediaPlayer.OnXReachPositionListener() {
            @Override
            public void onPositionReached() {
                xMediaPlayer.pause();
                setPlayButton(false);
            }
        });
        xMediaPlayer.prepare();
        playerPositionUpdateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(audioPlaying) {
                    final int currentMillis = xMediaPlayer.getCurrentPosition();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(currentMillis > 0) {
                                textView_currentPosition.setText(
                                        StringUtil.getTimeFormattedFromMillis(currentMillis));
                            }
                        }
                    });
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

    private void pauseAudio(){
        if (D) {Log.d(DEBUG_TAG, "Pausing audio.");}
        playerPositionUpdateThread.interrupt();
        playerPositionUpdateThread = null;
        xMediaPlayer.pause();
        setPlayButton(false);
        resetUIPlaybackPosition();
    }


    //================================================================================
    // UI
    //================================================================================

    private void loadUiResources(){
        textView_soundTitle = (TextView) findViewById(R.id.textView_soundTitle);
        textView_soundArtist = (TextView) findViewById(R.id.textView_soundArtist);
        textView_soundStart = (TextView) findViewById(R.id.textView_startTime);
        textView_soundEnd = (TextView) findViewById(R.id.textView_endTime);
        textView_currentPosition = (TextView) findViewById(R.id.textView_currentPosition);
        rangeBar_soundSelector = (RangeBar) findViewById(R.id.rangebar_audiosection);
        button_playPause = (Button) findViewById(R.id.button_play_pause);
    }

    private void resetUIPlaybackPosition(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                textView_currentPosition.setText(
                        StringUtil.getTimeFormattedFromMillis(soundStartMillis));
            }
        });
    }

    private void setPlayButton(boolean playing){
        audioPlaying = playing;
        if(playing){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    button_playPause.setText(R.string.button_stop);
                }
            });
        }else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    button_playPause.setText(R.string.button_play);
                }
            });
        }
    }

    private void setUpRangeBar(){
        if (D) {Log.d(DEBUG_TAG, "Setting up RangeBar.");}
        int tickCount = soundMillis / 1000;
        if(tickCount < 2){
            tickCount = 2;
        }
        int left = soundStartMillis / 1000;
        int right =  soundEndMillis / 1000;
        if(left <= 0){
            left = 0;
        }
        if(right >= tickCount -1){
            right = tickCount -1;
        }
        rangeBar_soundSelector.setTickCount(tickCount);
        rangeBar_soundSelector.setThumbIndices(left, right);
        rangeBar_soundSelector.setLeft(left);
        rangeBar_soundSelector.setRight(right);
        rangeBar_soundSelector.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onIndexChangeListener(
                    RangeBar rangeBar, int leftThumbSec, int rightThumbSec) {
                soundStartMillis = leftThumbSec * 1000;
                soundEndMillis = rightThumbSec * 1000;
                textView_soundStart.setText(StringUtil.getTimeFormattedFromSeconds(leftThumbSec));
                textView_soundEnd.setText(StringUtil.getTimeFormattedFromSeconds(rightThumbSec));
                textView_currentPosition.setText(
                        StringUtil.getTimeFormattedFromSeconds(leftThumbSec));
            }
        });
    }


}

