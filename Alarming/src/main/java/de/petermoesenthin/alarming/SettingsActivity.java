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

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import de.petermoesenthin.alarming.pref.PrefKey;
import de.petermoesenthin.alarming.ui.LClickListener;
import de.petermoesenthin.alarming.ui.LDialog;
import de.petermoesenthin.alarming.ui.LDialogView;
import de.petermoesenthin.alarming.util.PrefUtil;


public class SettingsActivity extends ActionBarActivity {

	public static final String DEBUG_TAG = "SettingsActivity";

	//----------------------------------------------------------------------------------------------
	//                                      LIFECYCLE
	//----------------------------------------------------------------------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setElevation(1);
		setTitle(R.string.activity_title_settings);
		setUpUi();
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
	//                                      UI
	//----------------------------------------------------------------------------------------------

	private void setUpUi() {
		CheckBox checkBox_showNotification = (CheckBox) findViewById(
				R.id.checkBox_setting_notification);
		checkBox_showNotification.setOnCheckedChangeListener(
				new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
						Log.d(DEBUG_TAG, "Setting " + PrefKey.SHOW_ALARM_NOTIFICATION + " to " + isChecked);
						PrefUtil.putBoolean(getApplicationContext(), PrefKey.SHOW_ALARM_NOTIFICATION, isChecked);
					}
				});
		boolean showNotification = PrefUtil.getBoolean(this, PrefKey.SHOW_ALARM_NOTIFICATION, true);
		checkBox_showNotification.setChecked(showNotification);
		LinearLayout linearLayout_setAlarmVolume = (LinearLayout) findViewById(R.id.linearLayout_setAlarmVolume);
		linearLayout_setAlarmVolume.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showVolumeSetDialog();
			}
		});

		LinearLayout linearLayout_setSnoozeTime = (LinearLayout) findViewById(R.id.linearLayout_setSnoozeTime);
		linearLayout_setSnoozeTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showSetSnoozeTimeDialog();
			}
		});
	}

	private void showSetSnoozeTimeDialog() {
		LDialogView dialogView = new LDialogView(this,
				R.layout.dialog_content_edit_text,
				R.string.dialog_title_set_snooze_time);
		LDialog dialog = new LDialog(this, dialogView);
		int snoozeTime = PrefUtil.getInt(this, PrefKey.SNOOZE_TIME, 10);
		final EditText editText_snoozeTime = (EditText) dialogView.getView().findViewById(R.id.editText);
		editText_snoozeTime.setText(String.valueOf(snoozeTime));
		editText_snoozeTime.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

		dialog.setPositiveButtonListener(
				new LClickListener() {
					@Override
					public void onClick(AlertDialog dialog) {
						String text = editText_snoozeTime.getText().toString();
						int number = Integer.parseInt(text);
						PrefUtil.putInt(getApplicationContext(), PrefKey.SNOOZE_TIME, Math.abs(number));
						dialog.dismiss();
					}
				});
		dialog.setNegativeButtonListener(
				new LClickListener() {
					@Override
					public void onClick(AlertDialog dialog) {
						dialog.dismiss();
					}
				});
		dialog.show();
	}

	private void showVolumeSetDialog() {
		LDialogView dialogView = new LDialogView(this,
				R.layout.dialog_content_seekbar,
				R.string.dialog_title_alarm_sound_volume);
		LDialog dialog = new LDialog(this, dialogView);
		int volume = PrefUtil.getInt(this, PrefKey.ALARM_SOUND_VOLUME, 80);
		final SeekBar seekBar_volume = (SeekBar) dialogView.getView().findViewById(R.id.seekBar);
		seekBar_volume.setMax(100);
		seekBar_volume.setProgress(volume);
		seekBar_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// Nothing
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				//TODO play test audio
			}
		});
		dialog.setPositiveButtonListener(
				new LClickListener() {
					@Override
					public void onClick(AlertDialog dialog) {
						int newVolume = seekBar_volume.getProgress();
						Log.d(DEBUG_TAG, "Setting " + PrefKey.ALARM_SOUND_VOLUME + " to " + newVolume);
						PrefUtil.putInt(getApplicationContext(), PrefKey.ALARM_SOUND_VOLUME, newVolume);
						dialog.dismiss();
					}
				});
		dialog.setNegativeButtonListener(
				new LClickListener() {
					@Override
					public void onClick(AlertDialog dialog) {
						dialog.dismiss();
					}
				});
		dialog.show();
	}

}
