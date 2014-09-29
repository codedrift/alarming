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

import de.petermoesenthin.alarming.callbacks.OnPlaybackChangedListener;
import de.petermoesenthin.alarming.pref.PrefKey;

public class MediaUtil {

    public static final String DEBUG_TAG = "MediaUtil";
    private static final boolean D = true;


    /**
     * Creates an array of basic information about an audio file obtained through the
     * MediaMetaDataRetriever. If a file does not provide MetaData, the filename will be used as
     * the title.
     * The array contains information as follows:
     * [0] METADATA_KEY_ARTIST
     * [1] METADATA_KEY_TITLE
     * [2] METADATA_KEY_DURATION
     * @param filePath path to the file
     * @return String array with metadata.
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
     * Plays an audio source from the specified start time to the specified end time.
     *
     * @param context Application context.
     * @param mediaPlayer The MediaPLayer instance that will playback the audio.
     * @param dataSource The path to the audio file.
     * @param startMillis If set to 0 MediaPlayer will play from the beginning.
     * @param endMillis If set to 0 MediaPlayer will play until the end.
     * @param playbackChangedListener Interface to indicate playback changes to the caller.
     */
    public static void playAudio(Context context, MediaPlayer mediaPlayer, Uri dataSource,
                                 int startMillis, int endMillis,
                                 final OnPlaybackChangedListener playbackChangedListener){

        final int playerHash = mediaPlayer.hashCode();
        final PositionCheckThread positionCheckThread =
                new PositionCheckThread(mediaPlayer, playbackChangedListener, endMillis);

        if (D) {Log.d(DEBUG_TAG, "Playing audio file " + dataSource
                + " with MediaPlayer " + playerHash  + ".");}
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                playbackChangedListener.onEndPositionReached(mediaPlayer);
            }
        });
        try {
            mediaPlayer.setDataSource(context, dataSource);
        } catch (IOException e) {
            if (D) {Log.e(DEBUG_TAG, "Failed to set sound uri for MediaPlayer "
                    + playerHash + ".");}
            return;
        }
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            if (D) {Log.e(DEBUG_TAG, "Failed to prepare MediaPlayer " + playerHash
                    + ". Returning.");}
            return;
        }
        if(startMillis == 0){
            if (D) {Log.d(DEBUG_TAG, "No start time specified for MediaPlayer "
                    + playerHash  + ". Starting playback.");}
            mediaPlayer.start();
            positionCheckThread.start();
            return;
        }
        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mediaPlayer) {
                if (D) {Log.d(DEBUG_TAG, "Seek completed. Starting playback for MediaPlayer "
                        + playerHash);}
                mediaPlayer.start();
                positionCheckThread.start();
            }
        });
        mediaPlayer.seekTo(startMillis);
    }

    /**
     * Thread to check if a MediaPlayer has reached its end position
     */
    public static class PositionCheckThread extends Thread {

        private OnPlaybackChangedListener mPlaybackChangedListener;
        private MediaPlayer mMediaPlayer;
        private int playerHash;
        private int positionMillis;
        public boolean isChecking = true;

        private static final int YIELD_MILLIS = 10;


        public PositionCheckThread(MediaPlayer mediaPlayer,
                                   OnPlaybackChangedListener playbackChangedListener,
                                   int positionMillis){
            this.mPlaybackChangedListener = playbackChangedListener;
            this.mMediaPlayer = mediaPlayer;
            this.playerHash = mediaPlayer.hashCode();
            this.positionMillis = positionMillis;
        }

        public void run() {
            // Early out if no end position is specified
            if (positionMillis == 0){
                isChecking = false;
            }
            // Check loop
            while(isChecking){
                if(!mMediaPlayer.isPlaying()){
                    if (D) {Log.d(DEBUG_TAG, "MediaPlayer " + playerHash
                            + " is not running. Stopping check.");}
                    mPlaybackChangedListener.onPlaybackInterrupted(mMediaPlayer);
                    isChecking = false;
                }
                int playerMillis = mMediaPlayer.getCurrentPosition();
                if(playerMillis >= positionMillis){
                    if (D) {Log.d(DEBUG_TAG, "MediaPlayer " + playerHash
                            + " has reached position. Stopping check.");}
                    mPlaybackChangedListener.onEndPositionReached(mMediaPlayer);
                    isChecking = false;
                }
                try {
                    Thread.sleep(YIELD_MILLIS);
                } catch (InterruptedException e) {
                    if (D) {Log.e(DEBUG_TAG, "Failed to sleep in PositionCheckThread. " +
                            "Thread has been interrupted. Stopping check.");}
                    mPlaybackChangedListener.onPlaybackInterrupted(mMediaPlayer);
                }
            }
        }
    }

    /**
     * Clears the MediaPlayer instance.
     * @param mediaPlayer Instance to be cleared.
     */
    public static void clearMediaPlayer(MediaPlayer mediaPlayer){
        if (D) {Log.d(DEBUG_TAG,
                "Stopping media playback for MediaPlayer " + mediaPlayer.hashCode() + ".");}
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
    }


    /**
     * Sets STREAM_MUSIC to the user specified volume that will be used to play alarm sounds.
     * @param context Application context.
     */
    public static void loadMediaVolumeFromPreference(Context context){
        if (D) {Log.d(DEBUG_TAG, "Setting alarm sound volume to user defined value.");}
        saveStreamMusicVolume(context);
        float percent = PrefUtil.getFloat(context,PrefKey.AUDIO_VOLUME,0.8f);
        setStreamMusicVolume(context, percent);
    }


    /**
     * Sets STREAM_MUSIC volume back to the previously set amount (user defined).
     * @param context Application context.
     */
    public static void resetMediaVolume(Context context){
        if (D) {Log.d(DEBUG_TAG, "Resetting media volume to original value.");}
        float percentage = PrefUtil.getFloat(context, PrefKey.AUDIO_ORIGINAL_VOLUME, 0f);
        setStreamMusicVolume(context, percentage);
    }

    /**
     * @param context Application context.
     * @return AudioManger system service.
     */
    public static AudioManager getAudioManager(Context context){
        return (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    }


    /**
     * Saves the current volume percentage of STREAM_MUSIC.
     * @param context Application context.
     */
    public static void saveStreamMusicVolume(Context context){
        if (D) {Log.d(DEBUG_TAG, "Saving current STREAM_MUSIC volume.");}
        int currentVolume  = getAudioManager(context).getStreamVolume(AudioManager.STREAM_MUSIC);
        float percentage = currentVolume / getAudioStreamMaxVolume(context);
        PrefUtil.putFloat(context, PrefKey.AUDIO_ORIGINAL_VOLUME, percentage);
    }


    /**
     * Sets STREAM_MUSIC volume to the given percentage.
     * @param context Application context.
     * @param percentage percentage to set the volume to.
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
     * @param context Application context.
     * @return Maximum value for STREAM_MUSIC volume.
     */
    public static int getAudioStreamMaxVolume(Context context){
        return getAudioManager(context).getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

}
