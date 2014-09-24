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

package de.petermoesenthin.alarming.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;

import de.petermoesenthin.alarming.pref.PrefKey;

public class MediaUtil {

    public static final String DEBUG_TAG = "MediaUtil";
    private static final boolean D = true;

    private static final MediaPlayer mediaPlayer = new MediaPlayer();


    /**
     * Creates an array of basic information about an audio file obtained through the
     * MediaMetaDataRetriever. If a file does not provide MetaData, the filename will be used as
     * the title.
     * The array contains information as follows:
     * [0] METADATA_KEY_ARTIST
     * [1] METADATA_KEY_TITLE
     * [2] METADATA_KEY_DURATION
     * @param filePath path to the file
     * @return
     */
    public static String[] getBasicMetaData(String filePath){
        if (D) {Log.d(DEBUG_TAG, "Reading audio metadata for " + filePath);}
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(filePath);
        String[] metaData = new String[3];
        metaData[0] =
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        metaData[1] =
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        metaData[2] =
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        if(metaData[0] == null){
            metaData[0] = "-";
        }
        if(metaData[1] == null){
            String[] pathSep = filePath.split("/");
            metaData[1] = pathSep[pathSep.length -1];
        }
        return metaData;
    }


    /**
     * Play the audio file at the specified source
     * @param context Application context
     * @param dataSource Audio file
     */
    public static void playAudio(Context context, Uri dataSource){
        if (D) {Log.d(DEBUG_TAG, "Audio file uri: " + dataSource);}
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(context, dataSource);
        } catch (IOException e) {
            if (D) {Log.e(DEBUG_TAG, "Failed to access alarm sound uri", e);}
            return;
        }
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            if (D) {Log.e(DEBUG_TAG, "Failed to prepare media player", e);}
            return;
        }
        mediaPlayer.start();
    }

    public static void stopAudioPlayback(){
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
    }


    /**
     * Sets STREAM_MUSIC to the user specified volume that will be used to play alarm sounds
     * @param context Application context
     */
    public static void setMediaVolume(Context context){
        if (D) {Log.d(DEBUG_TAG, "Setting alarm sound volume");}
        saveStreamMusicVolume(context);
        float percent = 0.8f;
        setStreamMusicVolume(context, percent);
    }


    /**
     * Sets STREAM_MUSIC volume back to the previously set amount (user defined)
     * @param context Application context
     */
    public static void resetMediaVolume(Context context){
        float percentage = PrefUtil.getFloat(context, PrefKey.AUDIO_ORIGINAL_VOLUME, 0f);
        setStreamMusicVolume(context, percentage);
    }

    /**
     * @param context Application context
     * @return AudioManger system service
     */
    public static AudioManager getAudioManager(Context context){
        return (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    }


    /**
     * Saves the current volume percentage of STREAM_MUSIC
     * @param context Application context
     */
    public static void saveStreamMusicVolume(Context context){
        int currentVolume  = getAudioManager(context).getStreamVolume(AudioManager.STREAM_MUSIC);
        float percentage = currentVolume / getAudioStreamMaxVolume(context);
        PrefUtil.putFloat(context, PrefKey.AUDIO_ORIGINAL_VOLUME, percentage);
    }


    /**
     * Sets STREAM_MUSIC volume to the given percentage
     * @param context Application context
     * @param percentage percentage to set the volume to
     */
    public static void setStreamMusicVolume(Context context, float percentage){
        int targetVolume = (int) (getAudioStreamMaxVolume(context) * percentage);
        getAudioManager(context).setStreamVolume(
                AudioManager.STREAM_MUSIC,
                targetVolume,
                0);
    }

    /**
     *
     * @param context Application context
     * @return Maximum value for STREAM_MUSIC volume
     */
    public static int getAudioStreamMaxVolume(Context context){
        return getAudioManager(context).getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

}
