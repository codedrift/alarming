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

package de.petermoesenthin.alarming;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import de.petermoesenthin.alarming.pref.AlarmGson;
import de.petermoesenthin.alarming.pref.AlarmSoundGson;
import de.petermoesenthin.alarming.pref.PrefKey;
import de.petermoesenthin.alarming.ui.SwipeToDismissTouchListener;
import de.petermoesenthin.alarming.util.AlarmUtil;
import de.petermoesenthin.alarming.util.FileUtil;
import de.petermoesenthin.alarming.util.MediaUtil;
import de.petermoesenthin.alarming.util.NotificationUtil;
import de.petermoesenthin.alarming.pref.PrefUtil;

public class AlarmReceiverActivity extends Activity implements MediaPlayer.OnPreparedListener,
		MediaPlayer.OnSeekCompleteListener
{

	//Audio
	private MediaPlayer mMediaPlayer;
	private Thread mPlayerPositionUpdateThread;
	private boolean mAudioPlaying = false;
	private int mStartMillis;
	private int mEndMillis;
	private String mDataSource;

	//Alarm
	private int mAlarmId;
	private AlarmGson mAlarmGson;
	private List<AlarmGson> mAlarms;
	private boolean flag_user_call = false;

	//System
	private KeyguardManager mKeyGuardManager;
	private KeyguardManager.KeyguardLock mKeyguardLock;
	private Vibrator mVibrator;
	private PowerManager.WakeLock mWakeLock;

	public static final String DEBUG_TAG = "AlarmReceiverActivity";

	private TextView button_snooze;
	private TextView button_dismiss;
	private TextView textView_alarmMessage;
	private LinearLayout layout_buttons;

	//----------------------------------------------------------------------------------------------
	//                                      LIFECYCLE
	//----------------------------------------------------------------------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.d(DEBUG_TAG, "onCreate called");
		setWindowSettings();
		setContentView(R.layout.activity_alarm_reciver);
		layout_buttons = (LinearLayout) findViewById(R.id.layout_dismiss_snooze);
		button_dismiss = (TextView) findViewById(R.id.button_dismiss);
		button_snooze = (TextView) findViewById(R.id.button_snooze);
		textView_alarmMessage = (TextView) findViewById(R.id.textView_alarm_message);
		readIntent();
		mAlarms = PrefUtil.getAlarms(this);
		mAlarmGson = PrefUtil.findAlarmWithID(mAlarms, mAlarmId);
		setUpViews();
		//acquireWakeLock();
		//disableKeyguard();
		playAlarmSound();
		if (mAlarmGson.doesVibrate())
		{
			startVibration();
		}
	}

	private void readIntent()
	{
		Intent intent = getIntent();
		mAlarmId = intent.getIntExtra("id", -1);
		Log.d(DEBUG_TAG, "Received alarm intent for id " + mAlarmId);
	}

	@Override
	public void onAttachedToWindow()
	{
		Log.d(DEBUG_TAG, "onAttachedToWindow called");
	}

	@Override
	public void onResume()
	{
		super.onResume();
		Log.d(DEBUG_TAG, "onResume called");
	}

	@Override
	public void onStop()
	{
		super.onStop();
		Log.d(DEBUG_TAG, "onStop called");
		if (mPlayerPositionUpdateThread != null)
		{
			mPlayerPositionUpdateThread.interrupt();
			mPlayerPositionUpdateThread = null;
		}
		if (mMediaPlayer != null)
		{
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		MediaUtil.resetSystemMediaVolume(this);
		clearWindowSettings();
		reEnableKeyGuard();
		releaseWakeLock();
		if (!flag_user_call)
		{
			Log.d(DEBUG_TAG, "Activity was not paused by user. snoozing");
			snoozeAlarm();
		}

	}

	@Override
	public void onPause()
	{
		super.onPause();
		Log.d(DEBUG_TAG, "onPause called");

	}

	/**
	 * Does work to finish this activity
	 */
	public void finishActivity()
	{
		Log.d(DEBUG_TAG, "Preparing to finish the Activity");
		// Stop vibration
		stopVibration();
		// System
		// Finish Activity
		Log.d(DEBUG_TAG, "Finishing Activity");
		finish();
		this.overridePendingTransition(android.R.anim.fade_out, android.R.anim.fade_out);
	}

	//----------------------------------------------------------------------------------------------
	//                                      SYSTEM
	//----------------------------------------------------------------------------------------------

	private void disableKeyguard()
	{
		Log.d(DEBUG_TAG, "Disabling Keyguard");
		mKeyGuardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		mKeyguardLock = mKeyGuardManager.newKeyguardLock("Alarming_Keyguard");
		if (mKeyGuardManager.inKeyguardRestrictedInputMode())
		{
			mKeyguardLock.disableKeyguard();
		}
	}

	private void reEnableKeyGuard()
	{
		Log.d(DEBUG_TAG, "Reenabling keyguard");
		if (mKeyguardLock != null)
		{
			mKeyguardLock.reenableKeyguard();
		}
	}

	public void acquireWakeLock()
	{
		Log.d(DEBUG_TAG, "Acquiring wakelock");
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
				| PowerManager.ON_AFTER_RELEASE
				| PowerManager.PARTIAL_WAKE_LOCK, "alarming_wakelock");
		mWakeLock.acquire();
	}

	public void releaseWakeLock()
	{
		Log.d(DEBUG_TAG, "Releasing wakelock");
		if (mWakeLock != null)
		{
			if (mWakeLock.isHeld())
			{
				mWakeLock.release();
				mWakeLock = null;
			}
		}
	}

	private void startVibration()
	{
		Log.d(DEBUG_TAG, "Starting vibration");
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		// Start without a delay
		// Vibrate for 500 milliseconds
		// Sleep for 500 milliseconds
		long[] pattern = {0, 500, 500};
		if (mVibrator.hasVibrator())
		{
			mVibrator.vibrate(pattern, 0);
		}
	}

	private void stopVibration()
	{
		Log.d(DEBUG_TAG, "Stopping vibration");
		if (mVibrator != null)
		{
			mVibrator.cancel();
		}
	}

	private void setWindowSettings()
	{
		Log.d(DEBUG_TAG, "Setting window parameters");
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		/* This requires an extra tap for the activity to regain focus
		View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        */
		int currentOrientation = getResources().getConfiguration().orientation;
		if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		} else
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		}
	}

	private void clearWindowSettings()
	{
		Log.d(DEBUG_TAG, "Clearing window parameters");
		//getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		//getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		//getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
	}

	//----------------------------------------------------------------------------------------------
	//                                      ALARM
	//----------------------------------------------------------------------------------------------

	private void snoozeAlarm()
	{
		Log.d(DEBUG_TAG, "snoozeAlarm called");
		clearAlarmSetting();
		int snoozeTime = PrefUtil.getInt(this, PrefKey.SNOOZE_TIME, 10);
		Calendar snoozetime = Calendar.getInstance();
		snoozetime.setTimeInMillis(System.currentTimeMillis());
		snoozetime.add(Calendar.MINUTE, snoozeTime);
		AlarmUtil.setSnooze(this, snoozetime, mAlarmId);
		finishActivity();
	}

	private void dismissAlarm()
	{
		Log.d(DEBUG_TAG, "dismissAlarm called");
		clearAlarmSetting();
		finishActivity();
	}


	private void clearAlarmSetting()
	{
		Log.d(DEBUG_TAG, "Clearing alarm setting.");
		// Clear any pending notifications
		NotificationUtil.clearAlarmNotifcation(this, mAlarmId);
		NotificationUtil.clearSnoozeNotification(this, mAlarmId);
		NotificationUtil.clearSnoozeNotification(this, mAlarmId);
		AlarmUtil.deactivateSnooze(this, mAlarmId);
		// Unset alarm from preferences
		mAlarmGson.setAlarmSet(false);
		PrefUtil.setAlarms(this, mAlarms);
	}

	//----------------------------------------------------------------------------------------------
	//                                      MEDIA PLAYBACK
	//----------------------------------------------------------------------------------------------

	/**
	 * Prepares alarm sound playback
	 */
	private void playAlarmSound()
	{
		Log.d(DEBUG_TAG, "Play alarm sound");
		MediaUtil.saveSystemMediaVolume(this);
		MediaUtil.setAlarmVolumeFromPreference(this);
		String[] uris = PrefUtil.getAlarmSoundUris(this);
		boolean fileOK = false;
		if (uris != null && uris.length > 0)
		{
			Random r = new Random();
			int rand = r.nextInt(uris.length);
			Log.d(DEBUG_TAG, "Found " + uris.length + " alarm sounds. Playing #" + rand + ".");
			mDataSource = uris[rand];
			fileOK = FileUtil.fileIsOK(this, mDataSource);
			AlarmSoundGson alsg = FileUtil.readSoundConfigurationFile(mDataSource);
			if (alsg != null)
			{
				mStartMillis = alsg.getStartMillis();
				mEndMillis = alsg.getEndMillis();
				//TODO loopAudio = alsg.isLooping();
			} else
			{
				mStartMillis = 0;
				mEndMillis = 0;
			}
		}
		if (!fileOK)
		{
			Log.d(DEBUG_TAG, "No uri available, playing default alarm sound.");
			// Play default alarm sound
			mDataSource = Settings.System.DEFAULT_ALARM_ALERT_URI.getPath();
		}
		startMediaPlayer();
	}

	private void startMediaPlayer()
	{
		Log.d(DEBUG_TAG, "Starting media player.");
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
		mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
		try
		{
			mMediaPlayer.setDataSource(mDataSource);
		} catch (IOException e)
		{
			Log.d(DEBUG_TAG, "Unable to set data source");
		}
		mMediaPlayer.setOnPreparedListener(this);
		mMediaPlayer.setOnSeekCompleteListener(this);
		mMediaPlayer.prepareAsync();
	}

	private void createPlayerPositionUpdateThread()
	{
		mPlayerPositionUpdateThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Log.d(DEBUG_TAG, "Starting player position thread.");
				int currentPlayerMillis = 0;
				if (mEndMillis == 0)
				{
					mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
					{
						@Override
						public void onCompletion(MediaPlayer mp)
						{
							loopAudio();
						}
					});
					return;
				}
				while (mAudioPlaying)
				{
					try
					{
						currentPlayerMillis = mMediaPlayer.getCurrentPosition();
					} catch (Exception e)
					{
						Log.d(DEBUG_TAG, "Unable to update player position. Exiting thread");
						return;
					}
					if (currentPlayerMillis > mEndMillis)
					{
						loopAudio();
						return;
					}
					try
					{
						Thread.sleep(50);
					} catch (InterruptedException e)
					{
						Log.e(DEBUG_TAG,
								"Update thread for current position has been interrupted.");
					}
				}
			}
		});
	}

	private void loopAudio()
	{
		if (mPlayerPositionUpdateThread != null)
		{
			mPlayerPositionUpdateThread.interrupt();
			mPlayerPositionUpdateThread = null;
		}
		mMediaPlayer.pause();
		mMediaPlayer.seekTo(mStartMillis);
	}

	@Override
	public void onPrepared(MediaPlayer mediaPlayer)
	{
		mMediaPlayer.seekTo(mStartMillis);
	}

	@Override
	public void onSeekComplete(MediaPlayer mediaPlayer)
	{
		mMediaPlayer.start();
		mAudioPlaying = true;
		createPlayerPositionUpdateThread();
		mPlayerPositionUpdateThread.start();
	}


	//----------------------------------------------------------------------------------------------
	//                                      UI
	//----------------------------------------------------------------------------------------------

	private void setUpViews()
	{
		button_dismiss.setOnTouchListener(new SwipeToDismissTouchListener(button_dismiss, null,
				new SwipeToDismissTouchListener.DismissCallbacks()
				{
					@Override
					public boolean canDismiss(Object token)
					{
						return true;
					}

					@Override
					public void onDismiss(View view, Object token)
					{
						Log.d(DEBUG_TAG, "Alarm has been dismissed");
						flag_user_call = true;
						button_snooze.setVisibility(View.GONE);
						layout_buttons.removeView(button_dismiss);
						dismissAlarm();
					}
				}
		));
		int snoozeTime = PrefUtil.getInt(this, PrefKey.SNOOZE_TIME, 10);
		String text_snooze = getResources().getString(R.string.button_snooze);
		String formatted = String.format(text_snooze, snoozeTime);
		if (snoozeTime == 1)
		{
			formatted += " " + getResources().getString(R.string.minute);
		} else
		{
			formatted += " " + getResources().getString(R.string.minutes);
		}

		button_snooze.setText(formatted);
		button_snooze.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Log.d(DEBUG_TAG, "Alarm has been snoozed. ya biscuit");
				flag_user_call = true;
				snoozeAlarm();
			}
		});

		RelativeLayout rl = (RelativeLayout) findViewById(R.id.layout_bg_alarm_receiver);
		int color = mAlarmGson.getColor();
		if (color == -1)
		{
			color = getResources().getColor(R.color.material_yellow);
		}
		rl.setBackgroundColor(color);
		textView_alarmMessage.setText(mAlarmGson.getMessage());
	}


}
