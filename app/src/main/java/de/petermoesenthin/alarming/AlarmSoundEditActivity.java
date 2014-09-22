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
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.edmodo.rangebar.RangeBar;
import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

import de.petermoesenthin.alarming.pref.AlarmSoundGson;
import de.petermoesenthin.alarming.util.FileUtil;
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
        loadAudioMetaDataToViews(soundFilePath);

        setUpRangeBar();

        textView_soundTitle.setText(soundTitle);
        textView_soundArtist.setText(soundArtist);
        textView_soundLength.setText(getTimeFormattedFromMillis(soundMillis));
        textView_soundStart.setText("Start: " + getTimeFormattedFromSeconds(0));
        textView_soundEnd.setText("End: " + getTimeFormattedFromMillis(soundMillis));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_alarmsoundedit, menu);
        return super.onCreateOptionsMenu(menu);
    }


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


    private void loadAudioMetaDataToViews(String filePath){
        if (D) {Log.d(DEBUG_TAG, "Reading audio metadata");}
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(filePath);
        soundArtist =
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        soundTitle =
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        soundMillis =
                Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
    }

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
        tickCount = parseLongToIntMaxValue(duration);
        rangeBar_soundSelector.setTickCount(tickCount);
        rangeBar_soundSelector.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int i, int i2) {
                textView_soundStart.setText("Start:" + getTimeFormattedFromSeconds(i));
                textView_soundEnd.setText("End:" + getTimeFormattedFromSeconds(i2));
            }
        });
    }

    private int parseLongToIntMaxValue(long number){
        int maxInt = Integer.MAX_VALUE;
        if (number >= maxInt){
            return maxInt;
        } else {
            return (int) number;
        }
    }

    private String getTimeFormattedFromSeconds(int seconds){
        return getTimeFormattedFromMillis(seconds * 1000);
    }

    private String getTimeFormattedFromMillis(long millis){
        long duration = millis / 1000;
        long h = duration / 3600;
        long m = (duration - h * 3600) / 60;
        long s = duration - (h * 3600 + m * 60);
        String durationString = "";
        if(h != 0) {
            durationString += StringUtil.getZeroPaddedString(h) + ":";
        }
        durationString += StringUtil.getZeroPaddedString(m) + ":";
        durationString += StringUtil.getZeroPaddedString(s);
        return durationString;
    }

    private void writeMetaFile(){
        String metaFilePath = FilenameUtils.removeExtension(soundFilePath) + ".alarming";
        File mf = FileUtil.getFile(metaFilePath);
        AlarmSoundGson alsg = new AlarmSoundGson();
        alsg.setUri(soundFilePath);
        alsg.setLastHashFromUri("" + soundFilePath.hashCode());
        alsg.setStartTimeMillis(0);
        alsg.setEndTimeMillis(soundMillis);
        Gson gs = new Gson();
        String js = gs.toJson(alsg);
        try {
            FileUtils.write(mf,js,"UTF-8");
        } catch (IOException e) {
            if (D) {Log.e(DEBUG_TAG, "Could not write metadata",e);}
        }
    }

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
                writeMetaFile();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

