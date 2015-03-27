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

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.edmodo.rangebar.RangeBar;
import com.faizmalkani.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

import de.petermoesenthin.alarming.pref.AlarmSoundGson;
import de.petermoesenthin.alarming.util.FileUtil;
import de.petermoesenthin.alarming.util.MediaUtil;
import de.petermoesenthin.alarming.util.PrefUtil;
import de.petermoesenthin.alarming.util.StringUtil;

public class AlarmSoundEditActivity extends ActionBarActivity implements MediaPlayer.OnPreparedListener,
		MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

	public static final String DEBUG_TAG = "AlarmSoundEditActivity";

	// MediaPlayer
	private MediaPlayer mMediaPlayer;
	private Thread mPlayerPositionUpdateThread;
	private String mSoundFilePath;
	private int mSoundMillis;
	private int mStartMillis;
	private int mEndMillis;
	private int mCurrentPlayerMills = -1;

	//Ui
	private TextView textView_soundTitle;
	private TextView textView_soundArtist;
	private RangeBar rangeBar_soundSelector;
	private TextView textView_soundStart;
	private TextView textView_soundEnd;
	private TextView textView_currentPosition;
	private ImageButton button_play_pause;
	private boolean audioPlaying = false;
	private ImageButton button_stop;
	private FloatingActionButton mFAB;

	private Handler mHandler = new Handler();


	//----------------------------------------------------------------------------------------------
	//                                      LIFECYCLE
	//----------------------------------------------------------------------------------------------

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// UI
		setContentView(R.layout.activity_alarm_sound_edit);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setElevation(1);
		loadUiResources();
		setTitle(R.string.activity_title_alarmSoundEdit);
		// call after ui setup to load all variables
		mSoundFilePath = readIntentUri();
		String[] metaData = MediaUtil.getBasicMetaData(mSoundFilePath);
		String soundArtist = metaData[0];
		String soundTitle = metaData[1];
		mSoundMillis = Integer.parseInt(metaData[2]);
		readConfig();
		setUpRangeBar();
		textView_soundTitle.setText(soundTitle);
		textView_soundArtist.setText(soundArtist);
		textView_soundStart.setText(StringUtil.getTimeFormattedFromMillis(mStartMillis));
		textView_soundEnd.setText(StringUtil.getTimeFormattedFromMillis(mEndMillis));
		textView_currentPosition.setText(StringUtil.getTimeFormattedFromMillis(mStartMillis));
		button_play_pause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.d(DEBUG_TAG, "Play button clicked");
				if (audioPlaying) {
					pauseAudio();
				} else {
					playAudio();
				}
			}
		});
		button_stop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				stopAudio();
			}
		});

		mFAB = (FloatingActionButton) findViewById(R.id.fab_save_config);
		mFAB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveConfig();
			}
		});

		enablePlayPauseButton(false);
		enableStopButton(false);
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseMediaPlayer();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMediaPlayer();
	}

	@Override
	protected void onStop() {
		super.onStop();
		releaseMediaPlayer();
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

	//----------------------------------------------------------------------------------------------
	//                                      CONFIGURATION
	//----------------------------------------------------------------------------------------------

	private String readIntentUri() {
		String intentUri = "";
		Intent intent = getIntent();
		int audioId = intent.getIntExtra("audio_id", -1);
		if (audioId == -1) {
			Log.d(DEBUG_TAG, "Intent was empty / did not pass a uri");
		} else {
			intentUri = PrefUtil.getAlarmSoundUris(this)[audioId];
			Log.d(DEBUG_TAG, "Building activity for: " + intentUri);
		}
		return intentUri;
	}

	/**
	 * Reads the sound file configuration for this activity.
	 */
	private void readConfig() {
		Log.d(DEBUG_TAG, "Reading configuration.");
		AlarmSoundGson alsg = FileUtil.readSoundConfigurationFile(mSoundFilePath);
		if (alsg == null) {
			mStartMillis = 0;
			mEndMillis = mSoundMillis;
		} else {
			mStartMillis = alsg.getStartMillis();
			mEndMillis = alsg.getEndMillis();
		}
	}

	/**
	 * Saves the configuration for this activity.
	 */
	private void saveConfig() {
		Log.d(DEBUG_TAG, "Saving configuration.");
		AlarmSoundGson alsg = new AlarmSoundGson();
		alsg.setStartMillis(mStartMillis);
		alsg.setEndMillis(mEndMillis);
		alsg.setPath(mSoundFilePath);
		alsg.setHash(mSoundFilePath.hashCode());
		FileUtil.writeSoundConfigurationFile(mSoundFilePath, alsg);
		Toast.makeText(this, R.string.toast_config_saved, Toast.LENGTH_SHORT).show();
	}

	//----------------------------------------------------------------------------------------------
	//                                      MEDIA PLAYBACK
	//----------------------------------------------------------------------------------------------


	private void playAudio() {
		Log.d(DEBUG_TAG, "Playing audio.");
		createPlayerPositionUpdateThread();
		if (mCurrentPlayerMills > mStartMillis) {
			mMediaPlayer.start();
			startPlayerPositionThread();
			setPlayPauseButtonPlaying(true);
		} else {
			mMediaPlayer.seekTo(mStartMillis);
			enablePlayPauseButton(false);
		}
	}

	private void setUpMediaPlayer() {
		Log.d(DEBUG_TAG, "Setting up MediaPlayer.");
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			mMediaPlayer.setDataSource(mSoundFilePath);
		} catch (IOException e) {
			Log.d(DEBUG_TAG, "Unable to set data source");
		}
		mMediaPlayer.setOnErrorListener(this);
		mMediaPlayer.setOnSeekCompleteListener(this);
		mMediaPlayer.setOnPreparedListener(this);
		mMediaPlayer.prepareAsync();
	}

	@Override
	public void onPrepared(MediaPlayer mediaPlayer) {
		Log.d(DEBUG_TAG, "MediaPlayer is prepared.");
		enablePlayPauseButton(true);
	}

	@Override
	public void onSeekComplete(MediaPlayer mediaPlayer) {
		Log.d(DEBUG_TAG, "MediaPlayer completed seek.");
		mMediaPlayer.start();
		startPlayerPositionThread();
		setPlayPauseButtonPlaying(true);
		enablePlayPauseButton(true);
		enableStopButton(true);
	}


	private void startPlayerPositionThread() {
		Log.d(DEBUG_TAG, "Starting PlayerPositionThread");
		if (mPlayerPositionUpdateThread != null && !mPlayerPositionUpdateThread.isAlive()) {
			mPlayerPositionUpdateThread.start();
		}
	}

	/**
	 * Pauses the audio playback and resets ui components
	 */
	private void pauseAudio() {
		Log.d(DEBUG_TAG, "Pausing audio.");
		if (mPlayerPositionUpdateThread != null) {
			mPlayerPositionUpdateThread.interrupt();
			mPlayerPositionUpdateThread = null;
		}
		mMediaPlayer.pause();
		setPlayPauseButtonPlaying(false);
	}

	private void releaseMediaPlayer() {
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	private void stopAudio() {
		Log.d(DEBUG_TAG, "Stopping audio.");
		if (mPlayerPositionUpdateThread != null) {
			mPlayerPositionUpdateThread.interrupt();
			mPlayerPositionUpdateThread = null;
		}
		setPlayPauseButtonPlaying(false);
		resetPlaybackPosition();
		enableStopButton(false);
		enablePlayPauseButton(false);
		// Uncommented because onPrepared does not get called
		//mMediaPlayer.stop();
		//mMediaPlayer.prepareAsync();
		releaseMediaPlayer();
		setUpMediaPlayer();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.d(DEBUG_TAG, "MediaPlayer completed playback");
		setPlayPauseButtonPlaying(false);
		enableStopButton(false);
		resetPlaybackPosition();
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.d(DEBUG_TAG, "MediaPlayer encountered an error. Resetting");
		releaseMediaPlayer();
		setUpMediaPlayer();
		return false;
	}

	private void createPlayerPositionUpdateThread() {
		mPlayerPositionUpdateThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d(DEBUG_TAG, "Starting player position thread.");
				if (mEndMillis == 0 || mEndMillis < mCurrentPlayerMills) {
					return;
				}
				int currentPlayerMillis = 0;
				while (audioPlaying) {
					try {
						currentPlayerMillis = mMediaPlayer.getCurrentPosition();
					} catch (Exception e) {
						Log.d(DEBUG_TAG, "Unable to update player position. Exiting thread");
						setPlayPauseButtonPlaying(false);
						return;
					}
					final int updateMillis = currentPlayerMillis;
					mCurrentPlayerMills = currentPlayerMillis;
					if (currentPlayerMillis >= mEndMillis) {
						stopAudio();
						return;
					}
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							if (updateMillis > 0) {
								textView_currentPosition.setText(
										StringUtil.getTimeFormattedFromMillis(updateMillis));
							}
						}
					});
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						Log.d(DEBUG_TAG, "Update thread for current position has been interrupted.");
					}
				}
			}
		});
	}


	//----------------------------------------------------------------------------------------------
	//                                      UI
	//----------------------------------------------------------------------------------------------

	private void loadUiResources() {
		textView_soundTitle = (TextView) findViewById(R.id.textView_soundTitle);
		textView_soundArtist = (TextView) findViewById(R.id.textView_soundArtist);
		textView_soundStart = (TextView) findViewById(R.id.textView_startTime);
		textView_soundEnd = (TextView) findViewById(R.id.textView_endTime);
		textView_currentPosition = (TextView) findViewById(R.id.textView_currentPosition);
		rangeBar_soundSelector = (RangeBar) findViewById(R.id.rangebar_audiosection);
		button_play_pause = (ImageButton) findViewById(R.id.button_play_pause);
		button_stop = (ImageButton) findViewById(R.id.button_stop);
	}

	private void resetPlaybackPosition() {
		Log.d(DEBUG_TAG, "Resetting Playback position");
		mCurrentPlayerMills = 0;
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				textView_currentPosition.setText(
						StringUtil.getTimeFormattedFromMillis(mStartMillis));
			}
		});
	}

	private void setPlayPauseButtonPlaying(boolean playing) {
		Log.d(DEBUG_TAG, "Setting PlayPauseButton to player state(" + playing + ")");
		audioPlaying = playing;
		if (playing) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					button_play_pause.setImageResource(R.drawable.ic_pause);
					button_play_pause.setContentDescription(getResources().getString(R.string
							.button_pause));
				}
			});
		} else {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					button_play_pause.setImageResource(R.drawable.ic_play);
					button_play_pause.setContentDescription(getResources().getString(R.string
							.button_play));
				}
			});
		}
	}

	private void setUpRangeBar() {
		Log.d(DEBUG_TAG, "Setting up RangeBar.");
		int tickCount = mSoundMillis / 1000;
		if (tickCount < 2) {
			tickCount = 2;
		}
		int left = mStartMillis / 1000;
		int right = mEndMillis / 1000;
		if (left <= 0) {
			left = 0;
		}
		if (right >= tickCount - 1) {
			right = tickCount - 1;
		}
		rangeBar_soundSelector.setTickCount(tickCount);
		rangeBar_soundSelector.setThumbIndices(left, right);
		rangeBar_soundSelector.setLeft(left);
		rangeBar_soundSelector.setRight(right);
		rangeBar_soundSelector.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
			@Override
			public void onIndexChangeListener(
					RangeBar rangeBar, int leftThumbSec, int rightThumbSec) {
				mStartMillis = leftThumbSec * 1000;
				mEndMillis = rightThumbSec * 1000;
				textView_soundStart.setText(StringUtil.getTimeFormattedFromSeconds(leftThumbSec));
				textView_soundEnd.setText(StringUtil.getTimeFormattedFromSeconds(rightThumbSec));
				textView_currentPosition.setText(
						StringUtil.getTimeFormattedFromSeconds(leftThumbSec));
			}
		});
	}

	private void enableStopButton(final boolean enabled) {
		Log.d(DEBUG_TAG, "ButtonStop enabled: " + enabled);
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				button_stop.setEnabled(enabled);
			}
		});
	}

	private void enablePlayPauseButton(final boolean enabled) {
		Log.d(DEBUG_TAG, "ButtonPlayPause enabled: " + enabled);
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				button_play_pause.setClickable(enabled);
				button_play_pause.setEnabled(enabled);
			}
		});
	}

}

