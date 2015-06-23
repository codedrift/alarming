/*
 * Alarming, an alarm app for the Android platform
 *
 * Copyright (C) 2014-2015 Peter Mösenthin <peter.moesenthin@gmail.com>
 *
 * Alarming is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.petermoesenthin.alarming.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.fima.glowpadview.GlowPadView;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import de.petermoesenthin.alarming.R;
import de.petermoesenthin.alarming.pref.AlarmGson;
import de.petermoesenthin.alarming.pref.AlarmSoundGson;
import de.petermoesenthin.alarming.util.FileUtil;
import de.petermoesenthin.alarming.util.MediaUtil;
import de.petermoesenthin.alarming.util.PrefUtil;

public class AlarmService extends Service
		implements
		MediaPlayer.OnPreparedListener,
		MediaPlayer.OnSeekCompleteListener,
		GlowPadView.OnTriggerListener {

	public static final String DEBUG_TAG = "AlarmService";
	private Context mContext;
	private WindowManager mWindowManager;
	private View mView;
	private Vibrator mVibrator;
	private MediaPlayer mMediaPlayer;
	private Thread mPlayerPositionUpdateThread;
	private boolean mAudioPlaying = false;
	private int mStartMillis;
	private int mEndMillis;
	private String mDataSource;

	private Animation mAnimation;

	private GlowPadView mGlowPadView;

	//Alarm
	private int mAlarmId;
	private AlarmGson mAlarmGson;
	private List<AlarmGson> mAlarms;

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(DEBUG_TAG, "onBind called");
		return null;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(DEBUG_TAG, "onStartCommand called");
		mAlarmId = intent.getIntExtra("id", -1);
		mContext = this;
		registerSystemActionReceiver();
		//playAlarmSound();
		//AlarmReceiver.completeWakefulIntent(intent);
		//showLockScreenView();
		return START_STICKY_COMPATIBILITY;
	}


	private void showLockScreenView() {
		Log.i(DEBUG_TAG, "Showing LockScreen view");
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		mView = inflater.inflate(R.layout.alarm_alert, null);
		mView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideLockScreenView();
				unregisterSystemActionReceiver();
				stopSelf();
			}
		});


		//SetUp GlowPadView
		mGlowPadView = (GlowPadView) mView.findViewById(R.id.glow_pad_view);

		mGlowPadView.setOnTriggerListener(this);


		final WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT, 0, 0,
				WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				PixelFormat.RGBA_8888);


		mView.setVisibility(View.VISIBLE);
		mAnimation = AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in);
		mView.setAnimation(mAnimation);
		mWindowManager.addView(mView, mLayoutParams);
	}

	private void hideLockScreenView() {
		Log.i(DEBUG_TAG, "Hiding LockScreen view");
		if (mView != null && mWindowManager != null) {
			mWindowManager.removeView(mView);
			mView = null;
		}
	}


	private void registerSystemActionReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(systemActionReceiver, filter);
	}

	@Override
	public void onDestroy() {
		Log.d(DEBUG_TAG, "onDestroy called");
		unregisterSystemActionReceiver();
		super.onDestroy();
	}

	private void unregisterSystemActionReceiver() {
		hideLockScreenView();
		unregisterReceiver(systemActionReceiver);
	}

	private BroadcastReceiver systemActionReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_SCREEN_ON)) {
				Log.d(DEBUG_TAG, "Received ACTION_SCREEN_ON");
				showLockScreenView();
			} else if (action.equals(Intent.ACTION_USER_PRESENT)) {
				Log.d(DEBUG_TAG, "Received ACTION_USER_PRESENT");
				hideLockScreenView();
			} else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
				Log.d(DEBUG_TAG, "Received ACTION_SCREEN_OFF");
				hideLockScreenView();
			}
		}
	};

	private void startVibration() {
		Log.d(DEBUG_TAG, "Starting vibration");
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		// Start without a delay
		// Vibrate for 500 milliseconds
		// Sleep for 500 milliseconds
		long[] pattern = {0, 500, 500};
		if (mVibrator.hasVibrator()) {
			mVibrator.vibrate(pattern, 0);
		}
	}

	private void stopVibration() {
		Log.d(DEBUG_TAG, "Stopping vibration");
		if (mVibrator != null) {
			mVibrator.cancel();
		}
	}

	//----------------------------------------------------------------------------------------------
	//                                      AUDIO PLAYBACk
	//----------------------------------------------------------------------------------------------

	/**
	 * Prepares alarm sound playback
	 */
	private void playAlarmSound() {
		Log.d(DEBUG_TAG, "Playing alarm sound");
		MediaUtil.saveSystemMediaVolume(this);
		MediaUtil.setAlarmVolumeFromPreference(this);
		String[] uris = PrefUtil.getAlarmSoundUris(this);
		boolean fileOK = false;
		if (uris != null && uris.length > 0) {
			Random r = new Random();
			int rand = r.nextInt(uris.length);
				Log.d(DEBUG_TAG, "Found " + uris.length + " alarm sounds. Playing #" + rand + ".");
			mDataSource = uris[rand];
			fileOK = FileUtil.fileIsOK(this, mDataSource);
			AlarmSoundGson alsg = FileUtil.readSoundConfigurationFile(mDataSource);
			if (alsg != null) {
				mStartMillis = alsg.getStartMillis();
				mEndMillis = alsg.getEndMillis();
				//TODO loopAudio = alsg.isLooping();
			} else {
				mStartMillis = 0;
				mEndMillis = 0;
			}
		}
		if (!fileOK) {
			Log.d(DEBUG_TAG, "No uri available, playing default alarm sound.");
			// Play default alarm sound
			mDataSource = Settings.System.DEFAULT_ALARM_ALERT_URI.getPath();
		}
		startMediaPlayer();
	}

	private void startMediaPlayer() {
		Log.d(DEBUG_TAG, "Starting media player.");
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
		//mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
		try {
			mMediaPlayer.setDataSource(mDataSource);
		} catch (IOException e) {
			Log.d(DEBUG_TAG, "Unable to set data source");
		}
		mMediaPlayer.setOnPreparedListener(this);
		mMediaPlayer.setOnSeekCompleteListener(this);
		mMediaPlayer.prepareAsync();
	}

	private void createPlayerPositionUpdateThread() {
		mPlayerPositionUpdateThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d(DEBUG_TAG, "Starting player position thread.");
				int currentPlayerMillis = 0;
				if (mEndMillis == 0) {
					mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
						@Override
						public void onCompletion(MediaPlayer mp) {
							loopAudio();
						}
					});
					return;
				}
				while (mAudioPlaying) {
					try {
						currentPlayerMillis = mMediaPlayer.getCurrentPosition();
					} catch (Exception e) {
						Log.d(DEBUG_TAG, "Unable to update player position." +" Exiting thread");
						return;
					}
					if (currentPlayerMillis > mEndMillis) {
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

	private void loopAudio() {
		if (mPlayerPositionUpdateThread != null) {
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

	//----------------------------------------------------------------------------------------------
	//                                      GLOWPAD
	//----------------------------------------------------------------------------------------------


	@Override
	public void onGrabbed(View v, int handle) {
		Log.d(DEBUG_TAG, "onGrabbed");
	}

	@Override
	public void onReleased(View v, int handle) {
		Log.d(DEBUG_TAG, "onReleased");
		mGlowPadView.ping();
	}

	@Override
	public void onTrigger(View v, int target) {
		Log.d(DEBUG_TAG, "onTrigger");
	}

	@Override
	public void onGrabbedStateChange(View v, int handle) {
		Log.d(DEBUG_TAG, "onGrabbedStateChange");
	}

	@Override
	public void onFinishFinalAnimation() {
		Log.d(DEBUG_TAG, "onFinishFinalAnimation");
	}
}
