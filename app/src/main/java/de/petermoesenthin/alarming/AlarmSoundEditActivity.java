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
import android.view.MenuItem;
import android.widget.TextView;

import de.petermoesenthin.alarming.util.PrefUtil;

public class AlarmSoundEditActivity extends Activity{

    //================================================================================
    // Members
    //================================================================================

    public static final String DEBUG_TAG = "AlarmSoundEditActivity";
    private static final boolean D = true;

    private String alarmFilePath;

    private TextView soundTitle;
    private TextView soundArtist;
    private TextView soundLength;


    //================================================================================
    // Lifecycle
    //================================================================================

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarmsoundedit);
        this.getActionBar().setHomeButtonEnabled(true);
        this.getActionBar().setDisplayHomeAsUpEnabled(true);
        soundTitle = (TextView) findViewById(R.id.textView_soundTitle);
        soundArtist = (TextView) findViewById(R.id.textView_soundArtist);
        soundLength = (TextView) findViewById(R.id.textView_soundLength);
        alarmFilePath = readIntentUri();
        loadAudioMetaDataToViews(alarmFilePath);
    }


    private String readIntentUri(){
        String intentUri = "";
        Intent intent = getIntent();
        int audioId = intent.getIntExtra("audio_id",-1);
        if (audioId == -1) {
            if (D) {Log.d(DEBUG_TAG, "Intent was empty / did not pass a uri");}
        } else {
            intentUri = PrefUtil.getAlarmSoundUris(this)[audioId];
        }
        return intentUri;
    }


    private void loadAudioMetaDataToViews(String filePath){
        if (D) {Log.d(DEBUG_TAG, "Reading audio metadata");}
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(filePath);
        String artistName =
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String title =
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String length =
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        soundTitle.setText(title);
        soundArtist.setText(artistName);
        soundLength.setText(convertMediaDuration(length));
    }

    private String convertMediaDuration(String length){
        long durationMs = Long.parseLong(length);
        long duration = durationMs / 1000;
        long h = duration / 3600;
        long m = (duration - h * 3600) / 60;
        long s = duration - (h * 3600 + m * 60);
        String durationString = "";
        if(h != 0) {
            durationString += h + ":";
        }
        if(m != 0){
            durationString += m + ":";
        }
        durationString += s;

        return durationString;
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

