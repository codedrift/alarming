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
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;

public class XMediaPlayer {

    //================================================================================
    // Members
    //================================================================================

    public static final String DEBUG_TAG = "XMediaPlayer";
    private static final boolean D = true;

    private static XMediaPlayer instance = null;

    private MediaPlayer mMediaPlayer;
    private int mMediaPlayerHashcode;
    private Context mContext;
    private Uri mDataSource;
    private int mStartMillis;
    private int mEndMillis;

    private OnXSeekCompleteListener mOnXSeekCompleteListener;
    private OnXReachPositionListener mOnXReachPositionListener;
    private OnXPrepareListener mOnXPrepareListener;
    private PositionCheckThread mPositionCheckThread;

    //================================================================================
    // Lifecycle
    //================================================================================

    private XMediaPlayer(){
    }

    public static XMediaPlayer getInstance(){
        if(instance == null){
            return instance = new XMediaPlayer();
        } else {
            return instance;
        }
    }

    //================================================================================
    // Methods
    //================================================================================

    /**
     * Sets the context, data source and prepares the player
     * @param context
     * @param dataSource
     */
    public void setUp(Context context, Uri dataSource){
        if (D) {Log.d(DEBUG_TAG, "Setting up XMediaPlayer.");}
        this.mContext = context;
        this.mDataSource = dataSource;
        mMediaPlayer = new MediaPlayer();
        mMediaPlayerHashcode = mMediaPlayer.hashCode();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        setDataSource(context, dataSource);
    }

    public void setDataSource(Context context, Uri dataSource){
        if (D) {Log.d(DEBUG_TAG, "Setting data source for MediaPlayer " +
                mMediaPlayerHashcode + ".");}
        if(mMediaPlayer != null){
            try {
                mMediaPlayer.setDataSource(context, dataSource);
            } catch (IOException e) {
                if (D) {Log.e(DEBUG_TAG, "Unable to set data source for MediaPlayer " +
                        mMediaPlayerHashcode + ". Problem loading file.", e);}
            } catch (IllegalStateException e){
                if (D) {Log.e(DEBUG_TAG, "Unable to set data source for MediaPlayer " +
                        mMediaPlayerHashcode + ". Illegal state.");}
            } catch (Exception e){
                if (D) {Log.e(DEBUG_TAG, "Unable to set data source for MediaPlayer " +
                        mMediaPlayerHashcode + ".", e);}
            }
        }
    }

    public void prepare(){
        if (D) {Log.d(DEBUG_TAG, "Preparing MediaPlayer " + mMediaPlayerHashcode + ".");}
        try {
            mMediaPlayer.prepare();
        } catch (IOException e) {
            if (D) {Log.e(DEBUG_TAG, "Unable to prepare MediaPlayer " +
                    mMediaPlayerHashcode + ".", e);}
        } catch (IllegalStateException e){
            if (D) {Log.e(DEBUG_TAG, "Unable to prepare MediaPlayer " +
                    mMediaPlayerHashcode + ". Illegal state.");}
            reset();
            prepare();
        } catch (Exception e){
            if (D) {Log.e(DEBUG_TAG, "Unable to prepare MediaPlayer " +
                    mMediaPlayerHashcode + ".", e);}
        }
    }

    public void start(){
        if (D) {Log.d(DEBUG_TAG, "Starting MediaPlayer " + mMediaPlayerHashcode + ".");}
        try {
            mMediaPlayer.start();
            if(mPositionCheckThread != null){
                mPositionCheckThread.start();
            }
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mOnXReachPositionListener.onPositionReached();
                    mPositionCheckThread.interrupt();
                    mPositionCheckThread = null;
                }
            });
        } catch (IllegalStateException e){
            if (D) {Log.e(DEBUG_TAG, "Unable to start MediaPlayer " +
                    mMediaPlayerHashcode + ". Illegal state.");}
        } catch (Exception e){
            if (D) {Log.e(DEBUG_TAG, "Unable to start MediaPlayer " +
                    mMediaPlayerHashcode + ".", e);}
        }
    }

    public void seek(){
        if (D) {Log.d(DEBUG_TAG, "Seeking to position " + mStartMillis + " in MediaPlayer " +
                mMediaPlayerHashcode + ".");}
        // Early out if no value is set
        if(mStartMillis >= 0){
            if(mOnXSeekCompleteListener != null){
                mOnXSeekCompleteListener.onSeekComplete();
            }
            return;
        }
        try {
            mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mediaPlayer) {
                    if(mOnXSeekCompleteListener != null){
                        mOnXSeekCompleteListener.onSeekComplete();
                    }
                }
            });
            mMediaPlayer.seekTo(mStartMillis);
        } catch (IllegalStateException e){
            if (D) {Log.e(DEBUG_TAG, "Unable to seek position in MediaPlayer " +
                    mMediaPlayerHashcode + ". Illegal state.");}
        } catch (Exception e){
            if (D) {Log.e(DEBUG_TAG, "Unable to seek position in MediaPlayer " +
                    mMediaPlayerHashcode + ".", e);}
        }
    }

    public int getCurrentPosition(){
        int currentPosition = -1;
        try{
            currentPosition = mMediaPlayer.getCurrentPosition();
        } catch (IllegalStateException e){
            if (D) {Log.e(DEBUG_TAG, "Unable to get current position for MediaPlayer " +
                    mMediaPlayerHashcode + ". Illegal state.");}
        } catch (Exception e){
            if (D) {Log.e(DEBUG_TAG, "Unable to get current position for MediaPlayer " +
                    mMediaPlayerHashcode + ".", e);}
        }
        return currentPosition;
    }

    public void stop(){
        if (D) {Log.d(DEBUG_TAG, "Stopping MediaPlayer " + mMediaPlayerHashcode + ".");}
        try {
            mMediaPlayer.stop();
            mPositionCheckThread = null;
        } catch (IllegalStateException e){
            if (D) {Log.e(DEBUG_TAG, "Unable to stop MediaPlayer " + mMediaPlayerHashcode
                    + ". Illegal state.");}
        } catch (Exception e){
            if (D) {Log.e(DEBUG_TAG, "Unable to stop MediaPlayer " + mMediaPlayerHashcode
                    + ".", e);}
        }
    }

    public void release(){
        if (D) {Log.d(DEBUG_TAG, "Releasing MediaPlayer " + mMediaPlayerHashcode + ".");}
        try {
            mMediaPlayer.release();
        } catch (IllegalStateException e){
            if (D) {Log.e(DEBUG_TAG, "Unable to release MediaPlayer " + mMediaPlayerHashcode
                    + ". Illegal state.");}
        } catch (Exception e){
            if (D) {Log.e(DEBUG_TAG, "Unable to release MediaPlayer " + mMediaPlayerHashcode
                    + ".", e);}
        }
    }

    public void reset(){
        if (D) {Log.d(DEBUG_TAG, "Resetting MediaPlayer " + mMediaPlayerHashcode + ".");}
        try {
            mMediaPlayer.reset();
        } catch (IllegalStateException e){
            if (D) {Log.e(DEBUG_TAG, "Unable to reset MediaPlayer " + mMediaPlayerHashcode
                    + ". Illegal state.");}
        } catch (Exception e){
            if (D) {Log.e(DEBUG_TAG, "Unable to reset MediaPlayer " + mMediaPlayerHashcode
                    + ".", e);}
        }
    }

    public void pause(){
        if (D) {Log.d(DEBUG_TAG, "Pausing MediaPlayer " + mMediaPlayerHashcode + ".");}
        try{
            mMediaPlayer.pause();
        } catch (IllegalStateException e){
            if (D) {Log.e(DEBUG_TAG, "Unable to pause MediaPlayer " + mMediaPlayerHashcode
                    + ". Illegal state.");}
        } catch (Exception e){
            if (D) {Log.e(DEBUG_TAG, "Unable to pause MediaPlayer " + mMediaPlayerHashcode
                    + ".", e);}
        }
    }

    public boolean isPlaying(){
        if (D) {Log.d(DEBUG_TAG, "Checking MediaPlayer " + mMediaPlayerHashcode + " is playing.");}
        boolean isPlaying = false;
        try {
            isPlaying = mMediaPlayer.isPlaying();
        } catch (IllegalStateException e){
            if (D) {Log.e(DEBUG_TAG, "Unable to check if MediaPlayer " + mMediaPlayerHashcode
                    + " is playing. Illegal state.");}
        } catch (Exception e){
            if (D) {Log.e(DEBUG_TAG, "Unable to check if MediaPlayer " + mMediaPlayerHashcode
                    + " is playing.", e);}
        }
        return isPlaying;
    }


    //================================================================================
    // Interfaces
    //================================================================================

    public interface OnXSeekCompleteListener{
        void onSeekComplete();
    }

    public interface OnXReachPositionListener{
        void onPositionReached();
    }

    public interface OnXPrepareListener{
        void onPrepared();
    }

    //================================================================================
    // Getter / Setter
    //================================================================================

    public void setOnXSeekCompleteListener(OnXSeekCompleteListener onXSeekCompleteListener){
        this.mOnXSeekCompleteListener = onXSeekCompleteListener;
        mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mediaPlayer) {
                if(mOnXSeekCompleteListener != null){
                    mOnXSeekCompleteListener.onSeekComplete();
                }
            }
        });
    }

    public void setOnXReachPositionListener(OnXReachPositionListener onXReachPositionListener){
        this.mOnXReachPositionListener = onXReachPositionListener;
        mPositionCheckThread = new PositionCheckThread();
    }

    public void setOnXPrepareListener(OnXPrepareListener onXPrepareListener){
        this.mOnXPrepareListener = onXPrepareListener;
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                if(mOnXPrepareListener != null){
                    mOnXPrepareListener.onPrepared();
                }
            }
        });
    }

    public void setStartMillis(int milliseconds){
        if(milliseconds >= 0){
            this.mStartMillis = milliseconds;
        }
    }

    public void setEndMillis(int milliseconds){
        if(milliseconds >= 0){
            this.mEndMillis = milliseconds;
        }
    }

    //================================================================================
    // Inner classes
    //================================================================================

   private class PositionCheckThread extends Thread {
        public boolean check = true;

        private static final int YIELD_MILLIS = 10;

        public void run() {
            // Early out if no end position is specified
            if (mEndMillis == 0){
                check = false;
            }
            // Check loop
            while(check){

                // Stop if not playing
                if(!isPlaying()){
                    check = false;
                    return;
                }
                //Check position
                int playerMillis = getCurrentPosition();
                if(playerMillis >= mEndMillis){
                    if(mOnXReachPositionListener != null){
                        mOnXReachPositionListener.onPositionReached();
                    }
                    check = false;
                    return;
                }

                // Yield Thread
                try {
                    Thread.sleep(YIELD_MILLIS);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

}
