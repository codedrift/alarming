/*
 * Copyright (C) 2014-2015 Peter Mösenthin <peter.moesenthin@gmail.com>
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

package de.petermoesenthin.alarming.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.faizmalkani.floatingactionbutton.FloatingActionButton;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import at.markushi.ui.CircleButton;
import de.petermoesenthin.alarming.R;
import de.petermoesenthin.alarming.adapter.AlarmCardRecyclerAdapter;
import de.petermoesenthin.alarming.pref.AlarmGson;
import de.petermoesenthin.alarming.pref.PrefKey;
import de.petermoesenthin.alarming.ui.LClickListener;
import de.petermoesenthin.alarming.ui.LDialog;
import de.petermoesenthin.alarming.ui.LDialogView;
import de.petermoesenthin.alarming.util.AlarmUtil;
import de.petermoesenthin.alarming.util.PrefUtil;
import de.petermoesenthin.alarming.util.StringUtil;

public class SetAlarmFragment extends Fragment implements
		SharedPreferences.OnSharedPreferenceChangeListener {

	public static final String DEBUG_TAG = "SetAlarmFragment";

	private Context mContext;
	private RecyclerView mAlarmListView;
	private AlarmCardRecyclerAdapter mAlarmCardRecyclerAdapter;
	private List<AlarmGson> mAlarms = new ArrayList<AlarmGson>();
	private FloatingActionButton mFAB;

	//----------------------------------------------------------------------------------------------
	//                                      LIFECYCLE
	//----------------------------------------------------------------------------------------------

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		mContext = getActivity();
		View rootView = inflater.inflate(R.layout.fragment_set_alarm, container, false);
		mAlarmListView = (RecyclerView) rootView.findViewById(R.id.cardListView_alarm);
		final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		mAlarmListView.setLayoutManager(layoutManager);

		mFAB = (FloatingActionButton) rootView.findViewById(R.id.fab_add_alarm);
		mFAB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addNewAlarm();
			}
		});
		// Fab listening disabled due to recyler view
		//mFAB.listenTo(mAlarmListView);

		setUpListView();
		PrefUtil.getApplicationPrefs(mContext)
				.registerOnSharedPreferenceChangeListener(this);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		PrefUtil.getApplicationPrefs(mContext)
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(PrefKey.ALARMS)) {
			Log.d(DEBUG_TAG, "Preferences have changed");
			mAlarmCardRecyclerAdapter.notifyDataSetChanged();
		}
	}

	//----------------------------------------------------------------------------------------------
	//                                      ALARM
	//----------------------------------------------------------------------------------------------

	private void addNewAlarm() {
		Log.d(DEBUG_TAG, "Add new alarm");
		AlarmGson alarm = new AlarmGson();
		int alarmID = PrefUtil.getInt(mContext, PrefKey.ALARM_ID_COUNTER, 0);
		alarm.setId(alarmID);
		Random r = new Random();
		String[] messages = getResources().getStringArray(R.array.alarm_texts);
		String message = messages[r.nextInt(messages.length)];
		alarm.setMessage(message);
		//mAlarms.add(0, alarmGson);
		mAlarms.add(alarm);
		mAlarmCardRecyclerAdapter.notifyDataSetChanged();
		scrollCardListViewToBottom();
		PrefUtil.putInt(mContext, PrefKey.ALARM_ID_COUNTER, alarmID + 1);
		PrefUtil.setAlarms(mContext, mAlarms);
	}


	private void activateAlarm(AlarmCardRecyclerAdapter.AlarmCardViewHolder viewHolder, int position) {
		Log.d(DEBUG_TAG, "Activate alarm " + position);
		AlarmGson alg = mAlarms.get(position);
		alg.setAlarmSet(true);
		Calendar calendarSet = AlarmUtil.getNextAlarmTimeAbsolute(alg.getHour(), alg.getMinute());
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(System.currentTimeMillis());
		long time = calendarSet.getTime().getTime() - now.getTime().getTime();
		Toast.makeText(mContext,String.format("%d hours %d minutes",
				TimeUnit.MILLISECONDS.toHours(time),
				TimeUnit.MILLISECONDS.toMinutes(time)
		),Toast.LENGTH_SHORT).show();
		AlarmUtil.setAlarm(mContext, calendarSet, alg.getId());
		PrefUtil.setAlarms(mContext, mAlarms);
	}

	private void deleteAlarm(int position) {
		Log.d(DEBUG_TAG, "Delete alarm " + position);
		mAlarms.remove(position);
		PrefUtil.setAlarms(mContext, mAlarms);
		mAlarmCardRecyclerAdapter.notifyDataSetChanged();
	}

	private void deactivateAlarm(AlarmCardRecyclerAdapter.AlarmCardViewHolder viewHolder, int position) {
		Log.d(DEBUG_TAG, "Deactivate alarm " + position);
		AlarmGson alg = mAlarms.get(position);
		alg.setAlarmSet(false);
		AlarmUtil.deactivateAlarm(mContext, alg.getId());
		PrefUtil.setAlarms(mContext, mAlarms);
	}

	public void setAlarmTime(int position, int hourOfDay, int minute) {
		Log.d(DEBUG_TAG, "Time picker finished. Setting alarm time at " + position);
		AlarmGson alg = mAlarms.get(position);
		alg.setHour(hourOfDay);
		alg.setMinute(minute);
	}

	//----------------------------------------------------------------------------------------------
	//                                      UI
	//----------------------------------------------------------------------------------------------

	private void setUpListView() {
		Log.d(DEBUG_TAG, "Set up list view");
		mAlarms = PrefUtil.getAlarms(mContext);
		// No alarms present
		if (mAlarms.isEmpty()) {
			mAlarms.add(new AlarmGson());
			PrefUtil.putInt(mContext, PrefKey.ALARM_ID_COUNTER, 1);
		}

		mAlarmCardRecyclerAdapter = new AlarmCardRecyclerAdapter(mAlarms,
				new AlarmCardRecyclerAdapter.AdapterCallBacks(){

			@Override
			public void onAlarmTimeClick(AlarmCardRecyclerAdapter.AlarmCardViewHolder viewHolder, int position) {
				Log.d(DEBUG_TAG, "AlarmTimeClick  at " + position);
				showTimePicker(position, viewHolder);
			}

			@Override
			public void onAlarmSetClick(AlarmCardRecyclerAdapter.AlarmCardViewHolder viewHolder, int position) {
				Log.d(DEBUG_TAG, "AlarmSetClick at " + position);
				AlarmGson alg = mAlarms.get(position);
				boolean alarmSet = alg.isAlarmSet();
				if (alarmSet) {
					deactivateAlarm(viewHolder, position);
					mAlarmCardRecyclerAdapter.setCircleButtonActive(viewHolder, false);
				} else {
					activateAlarm(viewHolder, position);
					mAlarmCardRecyclerAdapter.setCircleButtonActive(viewHolder, true);
				}
			}

			@Override
			public void onVibrateClick(AlarmCardRecyclerAdapter.AlarmCardViewHolder viewHolder, int position) {
				Log.d(DEBUG_TAG, "VibrateClick at " + position);
				AlarmGson alg = mAlarms.get(position);
				boolean doesVibrate = alg.doesVibrate();
				alg.setVibrate(!doesVibrate);
				viewHolder.vibrate.setChecked(!doesVibrate);
				PrefUtil.setAlarms(mContext, mAlarms);
			}

			@Override
			public void onRepeatAlarmClick(AlarmCardRecyclerAdapter.AlarmCardViewHolder viewHolder, int position) {
				Log.d(DEBUG_TAG, "RepeatAlarmClick at " + position);
				AlarmGson alg = mAlarms.get(position);
				boolean doesRepeat = alg.doesRepeat();
				alg.setRepeat(!doesRepeat);
				PrefUtil.setAlarms(mContext, mAlarms);
				mAlarmCardRecyclerAdapter.showWeekdayPanel(viewHolder, !doesRepeat);
			}

			@Override
			public void onAlarmTextClick(AlarmCardRecyclerAdapter.AlarmCardViewHolder viewHolder, int position) {
				Log.d(DEBUG_TAG, "AlarmTextClick at " + position);
				showAlarmMessageDialog(viewHolder, position);
			}

			@Override
			public void onChooseColorClick(AlarmCardRecyclerAdapter.AlarmCardViewHolder viewHolder, int position) {
				Log.d(DEBUG_TAG, "ChooseColorClick at " + position);
				showColorPickerDialog(viewHolder, position);
			}

			@Override
			public void onDeleteAlarmClick(AlarmCardRecyclerAdapter.AlarmCardViewHolder viewHolder, int position) {
				Log.d(DEBUG_TAG, "DeleteAlarmClick at " + position);
				deactivateAlarm(viewHolder, position);
				deleteAlarm(position);
			}
		});

		mAlarmListView.setAdapter(mAlarmCardRecyclerAdapter);
	}


	private void setAlarmTimeView(TextView alarmTime, TextView am_pm, int hour, int minute) {
		String alarmFormatted = StringUtil.getTimeFormattedSystem(mContext, hour,
				minute);
		String[] timeSplit = alarmFormatted.split(" ");
		am_pm.setVisibility(View.INVISIBLE);
		alarmTime.setText(timeSplit[0]);
		if (timeSplit.length > 1) {
			am_pm.setText(timeSplit[1]);
			am_pm.setVisibility(View.VISIBLE);
		}
	}

	private void scrollCardListViewToBottom() {
		mAlarmListView.post(new Runnable() {
			@Override
			public void run() {
				mAlarmListView.smoothScrollToPosition(mAlarmCardRecyclerAdapter.getItemCount() - 1);
			}
		});
	}

	private void showColorPickerDialog(final AlarmCardRecyclerAdapter.AlarmCardViewHolder viewHolder,
									   final int position) {
		LDialogView dialogView = new LDialogView(mContext,
				R.layout.dialog_content_color_picker,
				R.string.dialog_title_set_alarm_color);
		final LDialog dialog = new LDialog(mContext, dialogView);
		final ColorPicker cp = (ColorPicker) dialogView.getView().findViewById(R.id.dialog_color_picker);
		final AlarmGson alg = mAlarms.get(position);
		int color = alg.getColor();
		if (color == -1) {
			color = getResources().getColor(R.color.material_yellow);
		}
		cp.setOldCenterColor(color);

		dialog.setPositiveButtonListener(
				new LClickListener() {
					@Override
					public void onClick(AlertDialog dialog) {
						int colorChoice = cp.getColor();
						alg.setColor(colorChoice);
						View v = viewHolder.chooseColor.findViewById(R.id.view_color_indicator);
						v.setBackgroundColor(colorChoice);
						PrefUtil.setAlarms(mContext, mAlarms);
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

	private void showAlarmMessageDialog(final AlarmCardRecyclerAdapter.AlarmCardViewHolder viewHolder, final int position) {
		Log.d(DEBUG_TAG, "Showing alarm message dialog");
		LDialogView dialogView = new LDialogView(mContext,
				R.layout.dialog_content_edit_text,
				R.string.dialog_title_set_alarm_message);
		LDialog dialog = new LDialog(mContext, dialogView);
		final EditText editText = (EditText) dialogView.getView().findViewById(R.id
				.editText);
		editText.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
		String prefText = mAlarms.get(position).getMessage();
		editText.setText(prefText);
		dialog.setPositiveButtonListener(
				new LClickListener() {
					@Override
					public void onClick(AlertDialog dialog) {
						String text = editText.getText().toString();
						mAlarms.get(position).setMessage(text);
						viewHolder.alarmText.setText(text);
						PrefUtil.setAlarms(mContext, mAlarms);
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

	private void showTimePicker(final int position, final AlarmCardRecyclerAdapter.AlarmCardViewHolder viewHolder) {
		Calendar cal = Calendar.getInstance();
		String[] time = StringUtil.getTimeFormattedSystem(mContext, 13, 0).split(" ");
		boolean is24h = time.length < 2;
		final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(
				new TimePickerDialog.OnTimeSetListener() {
					@Override
					public void onTimeSet(RadialPickerLayout radialPickerLayout, int h, int m) {
						setAlarmTime(position, h, m);
						setAlarmTimeView(viewHolder.alarmTime, viewHolder.am_pm, h,
								m);
						activateAlarm(viewHolder, position);
					}
				},
				cal.get(Calendar.HOUR_OF_DAY),
				cal.get(Calendar.MINUTE),
				is24h,
				false);
		timePickerDialog.show(getActivity().getSupportFragmentManager(), null);
	}
}
