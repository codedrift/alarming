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
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.edmodo.rangebar.RangeBar;

import de.petermoesenthin.alarming.pref.AlarmSoundGson;
import de.petermoesenthin.alarming.util.FileUtil;
import de.petermoesenthin.alarming.util.MediaUtil;
import de.petermoesenthin.alarming.util.NumberUtil;
import de.petermoesenthin.alarming.util.PrefUtil;
import de.petermoesenthin.alarming.util.StringUtil;

public class AlarmSoundEditActivity extends Activity{

    //================================================================================
    // Members
    //================================================================================

    public static final String DEBUG_TAG = "AlarmSoundEditActivity";
    private static final boolean D = true;

    private String soundFilePath;
    private int tickCount;
    private String soundArtist;
    private String soundTitle;
    private long soundMillis;
    private long soundStartMillis;
    private long soundEndMillis;

    private TextView textView_soundTitle;
    private TextView textView_soundArtist;
    private TextView textView_soundLength;
    private RangeBar rangeBar_soundSelector;
    private TextView textView_soundStart;
    private TextView textView_soundEnd;

    //================================================================================
    // Lifecycle
    //================================================================================

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // UI
        setContentView(R.layout.activity_alarmsoundedit);
        this.getActionBar().setHomeButtonEnabled(true);
        this.getActionBar().setDisplayHomeAsUpEnabled(true);
        loadUiResources();

        // call after ui setup to load all variables
        soundFilePath = readIntentUri();
        String[] metaData = MediaUtil.getBasicMetaData(soundFilePath);
        soundArtist = metaData[0];
        soundTitle = metaData[1];
        soundMillis = Long.parseLong(metaData[2]);

        setUpRangeBar();

        textView_soundTitle.setText(soundTitle);
        textView_soundArtist.setText(soundArtist);
        textView_soundLength.setText(StringUtil.getTimeFormattedFromMillis(soundMillis));
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
                AlarmSoundGson alsg = new AlarmSoundGson();
                alsg.setStartTimeMillis(0);
                alsg.setEndTimeMillis(soundMillis);
                alsg.setPath(soundFilePath);
                alsg.setPathHash(soundFilePath.hashCode());
                FileUtil.writeSoundConfigurationFile(soundFilePath,alsg);
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
        AlarmSoundGson alsg = FileUtil.readSoundConfigurationFile(soundFilePath);
        if(alsg.getPathHash() == 0){
            return;
        } else {
            soundStartMillis = alsg.getStartTimeMillis();
            soundEndMillis = alsg.getEndTimeMillis();
        }
    }

    //================================================================================
    // UI
    //================================================================================

    private void loadUiResources(){
        textView_soundTitle = (TextView) findViewById(R.id.textView_soundTitle);
        textView_soundArtist = (TextView) findViewById(R.id.textView_soundArtist);
        textView_soundLength = (TextView) findViewById(R.id.textView_soundLength);
        textView_soundStart = (TextView) findViewById(R.id.textView_startTime);
        textView_soundEnd = (TextView) findViewById(R.id.textView_endTime);
        rangeBar_soundSelector = (RangeBar) findViewById(R.id.rangebar_audiosection);
    }

    private void setUpRangeBar(){
        long duration = soundMillis / 1000;
        tickCount = NumberUtil.parseLongToCappedInt(duration);
        if(tickCount < 2){
            tickCount = 2;
        }
        rangeBar_soundSelector.setTickCount(tickCount);
        rangeBar_soundSelector.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int i, int i2) {
                textView_soundStart.setText("Start:" + StringUtil.getTimeFormattedFromSeconds(i));
                textView_soundEnd.setText("End:" + StringUtil.getTimeFormattedFromSeconds(i2));
            }
        });
    }


}

