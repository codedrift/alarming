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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.faizmalkani.floatingactionbutton.FloatingActionButton;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SVBar;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import de.petermoesenthin.alarming.R;
import de.petermoesenthin.alarming.adapter.AlarmCardRecyclerAdapter;
import de.petermoesenthin.alarming.pref.AlarmPref;
import de.petermoesenthin.alarming.pref.PrefKey;
import de.petermoesenthin.alarming.pref.PrefUtil;
import de.petermoesenthin.alarming.ui.LClickListener;
import de.petermoesenthin.alarming.ui.LDialog;
import de.petermoesenthin.alarming.ui.LDialogView;
import de.petermoesenthin.alarming.util.AlarmUtil;
import de.petermoesenthin.alarming.util.StringUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SetAlarmFragment extends Fragment implements
		SharedPreferences.OnSharedPreferenceChangeListener
{

	public static final String DEBUG_TAG = SetAlarmFragment.class.getSimpleName();

	private Context mContext;
	private RecyclerView mAlarmListView;
	private AlarmCardRecyclerAdapter mAlarmCardRecyclerAdapter;
	private List<AlarmPref> mAlarmPrefs = new ArrayList<AlarmPref>();
	private FloatingActionButton mFAB;

	//----------------------------------------------------------------------------------------------
	//                                      LIFECYCLE
	//----------------------------------------------------------------------------------------------

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		mContext = getActivity();
		View rootView = inflater.inflate(R.layout.fragment_set_alarm, container, false);
		mAlarmListView = (RecyclerView) rootView.findViewById(R.id.cardListView_alarm);
		final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		mAlarmListView.setLayoutManager(layoutManager);

		mFAB = (FloatingActionButton) rootView.findViewById(R.id.fab_add_alarm);
		mFAB.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				SetAlarmFragment.this.addNewAlarm();
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
	public void onResume()
	{
		super.onResume();
	}

	@Override
	public void onPause()
	{
		super.onPause();
	}

	@Override
	public void onStop()
	{
		super.onStop();
		PrefUtil.getApplicationPrefs(mContext)
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if (key.equals(PrefKey.ALARMS))
		{
			Log.d(DEBUG_TAG, "Preferences have changed");
			mAlarmCardRecyclerAdapter.notifyDataSetChanged();
		}
	}

	//----------------------------------------------------------------------------------------------
	//                                      ALARM
	//----------------------------------------------------------------------------------------------

	private void addNewAlarm()
	{
		Log.d(DEBUG_TAG, "Add new alarm");
		AlarmPref alarm = PrefUtil.getNewAlarmPref(mContext);
		mAlarmPrefs.add(alarm);
		mAlarmCardRecyclerAdapter.notifyDataSetChanged();
		scrollCardListViewToBottom();
		PrefUtil.setAlarms(mContext, mAlarmPrefs);
	}


	private void activateAlarm(AlarmCardRecyclerAdapter.AlarmCardViewHolder viewHolder, int position)
	{
		Log.d(DEBUG_TAG, "Activate alarm " + position);
		AlarmPref alarm = mAlarmPrefs.get(position);
		alarm.setAlarmSet(true);
		Calendar calendarSet = AlarmUtil.getNextAlarmTimeAbsolute(alarm.getHour(), alarm.getMinute());
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(System.currentTimeMillis());
		long time = calendarSet.getTime().getTime() - now.getTime().getTime();
		long hours = TimeUnit.MILLISECONDS.toHours(time);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(time);
		if (minutes == 0)
		{
			Toast.makeText(mContext, String.format("Alarm goes off in %d seconds", seconds - hours * 60 - minutes * 60),
					Toast.LENGTH_SHORT).show();
		}
		else if (hours == 0)
		{
			Toast.makeText(mContext,
					String.format("Alarm goes off in %d minutes %d seconds",
							minutes - hours * 60,
							seconds - hours * 60 - minutes * 60),
					Toast.LENGTH_SHORT).show();
		} else
		{
			Toast.makeText(mContext, String.format("Alarm goes off in %d hours %d minutes", hours, minutes - hours * 60),
					Toast.LENGTH_SHORT).show();
		}
		AlarmUtil.setAlarm(mContext, calendarSet, alarm.getId());
		PrefUtil.setAlarms(mContext, mAlarmPrefs);
	}

	private void deleteAlarm(int position)
	{
		Log.d(DEBUG_TAG, "Delete alarm " + position);
		mAlarmPrefs.remove(position);
		PrefUtil.setAlarms(mContext, mAlarmPrefs);
		mAlarmCardRecyclerAdapter.notifyDataSetChanged();
	}

	private void deactivateAlarm(AlarmCardRecyclerAdapter.AlarmCardViewHolder viewHolder, int position)
	{
		Log.d(DEBUG_TAG, "Deactivate alarm " + position);
		AlarmPref alarm = mAlarmPrefs.get(position);
		alarm.setAlarmSet(false);
		AlarmUtil.deactivateAlarm(mContext, alarm.getId());
		PrefUtil.setAlarms(mContext, mAlarmPrefs);
	}

	public void setAlarmTime(int position, int hourOfDay, int minute)
	{
		Log.d(DEBUG_TAG, "Time picker finished. Setting alarm time at " + position);
		AlarmPref alarm = mAlarmPrefs.get(position);
		alarm.setHour(hourOfDay);
		alarm.setMinute(minute);
	}

	//----------------------------------------------------------------------------------------------
	//                                      UI
	//----------------------------------------------------------------------------------------------

	private void setUpListView()
	{
		Log.d(DEBUG_TAG, "Set up list view");
		mAlarmPrefs = PrefUtil.getAlarms(mContext);
		// No alarms present
		if (mAlarmPrefs.isEmpty())
		{
			mAlarmPrefs.add(new AlarmPref());
			PrefUtil.putInt(mContext, PrefKey.ALARM_ID_COUNTER, 1);
		}

		setUpAlarmCardAdapter();

		mAlarmListView.setAdapter(mAlarmCardRecyclerAdapter);
	}

	private void setUpAlarmCardAdapter()
	{
		mAlarmCardRecyclerAdapter = new AlarmCardRecyclerAdapter(mAlarmPrefs,
				new AlarmCardRecyclerAdapter.AdapterCallBacks()
				{

					@Override
					public void onAlarmTimeClick(AlarmCardRecyclerAdapter.AlarmCardViewHolder viewHolder, int position)
					{
						Log.d(DEBUG_TAG, "AlarmTimeClick  at " + position);
						showTimePicker(position, viewHolder);
					}

					@Override
					public void onAlarmSetClick(AlarmCardRecyclerAdapter.AlarmCardViewHolder viewHolder, int position)
					{
						Log.d(DEBUG_TAG, "AlarmSetClick at " + position);
						AlarmPref alarm = mAlarmPrefs.get(position);
						boolean alarmSet = alarm.isAlarmSet();
						if (alarmSet)
						{
							deactivateAlarm(viewHolder, position);
							mAlarmCardRecyclerAdapter.setCircleButtonActive(viewHolder, false);
						} else
						{
							activateAlarm(viewHolder, position);
							mAlarmCardRecyclerAdapter.setCircleButtonActive(viewHolder, true);
						}
					}

					@Override
					public void onVibrateClick(AlarmCardRecyclerAdapter.AlarmCardViewHolder viewHolder, int position)
					{
						Log.d(DEBUG_TAG, "VibrateClick at " + position);
						AlarmPref alarm = mAlarmPrefs.get(position);
						boolean doesVibrate = alarm.doesVibrate();
						alarm.setVibrate(!doesVibrate);
						viewHolder.vibrate.setChecked(!doesVibrate);
						PrefUtil.setAlarms(mContext, mAlarmPrefs);
					}

					@Override
					public void onRepeatAlarmClick(AlarmCardRecyclerAdapter.AlarmCardViewHolder viewHolder, int position)
					{
						Log.d(DEBUG_TAG, "RepeatAlarmClick at " + position);
						AlarmPref alarm = mAlarmPrefs.get(position);
						boolean doesRepeat = alarm.doesRepeat();
						alarm.setRepeat(!doesRepeat);
						PrefUtil.setAlarms(mContext, mAlarmPrefs);
						mAlarmCardRecyclerAdapter.showWeekdayPanel(viewHolder, !doesRepeat);
					}

					@Override
					public void onAlarmTextClick(AlarmCardRecyclerAdapter.AlarmCardViewHolder viewHolder, int position)
					{
						Log.d(DEBUG_TAG, "AlarmTextClick at " + position);
						showAlarmMessageDialog(viewHolder, position);
					}

					@Override
					public void onChooseColorClick(AlarmCardRecyclerAdapter.AlarmCardViewHolder viewHolder, int position)
					{
						Log.d(DEBUG_TAG, "ChooseColorClick at " + position);
						showColorPickerDialog(viewHolder, position);
					}

					@Override
					public void onDeleteAlarmClick(AlarmCardRecyclerAdapter.AlarmCardViewHolder viewHolder, int position)
					{
						Log.d(DEBUG_TAG, "DeleteAlarmClick at " + position);
						deactivateAlarm(viewHolder, position);
						deleteAlarm(position);
					}
				});
	}

	private void setAlarmTimeView(TextView alarmTime, TextView am_pm, int hour, int minute)
	{
		String alarmFormatted = StringUtil.getTimeFormattedSystem(mContext, hour,
				minute);
		String[] timeSplit = alarmFormatted.split(" ");
		am_pm.setVisibility(View.INVISIBLE);
		alarmTime.setText(timeSplit[0]);
		if (timeSplit.length > 1)
		{
			am_pm.setText(timeSplit[1]);
			am_pm.setVisibility(View.VISIBLE);
		}
	}

	private void scrollCardListViewToBottom()
	{
		mAlarmListView.post(new Runnable()
							{
								@Override
								public void run()
								{
									mAlarmListView.smoothScrollToPosition(
											mAlarmCardRecyclerAdapter.getItemCount() - 1);
								}
							}
		);
	}

	private void showColorPickerDialog(final AlarmCardRecyclerAdapter.AlarmCardViewHolder viewHolder,
									   final int position)
	{
		LDialogView dialogView = new LDialogView(mContext,
				R.layout.dialog_content_color_picker,
				R.string.dialog_title_set_alarm_color);
		final LDialog colorPickerDialog = new LDialog(mContext, dialogView);
		final ColorPicker cp = (ColorPicker) dialogView.getView().findViewById(R.id.dialog_color_picker);
		final SVBar svBar = (SVBar) dialogView.getView().findViewById(R.id.colorDialog_svBar);
		cp.addSVBar(svBar);
		final AlarmPref alarm = mAlarmPrefs.get(position);
		int color = alarm.getColor();
		if (color == -1)
		{
			color = getResources().getColor(R.color.material_yellow);
		}
		cp.setOldCenterColor(color);

		colorPickerDialog.setPositiveButtonListener(
				new LClickListener()
				{
					@Override
					public void onClick(AlertDialog dialog)
					{
						int colorChoice = cp.getColor();
						alarm.setColor(colorChoice);
						View v = viewHolder.chooseColor.findViewById(R.id.view_color_indicator);
						v.setBackgroundColor(colorChoice);
						PrefUtil.setAlarms(mContext, mAlarmPrefs);
						dialog.dismiss();
					}
				});
		colorPickerDialog.setNegativeButtonListener(
				new LClickListener()
				{
					@Override
					public void onClick(AlertDialog dialog)
					{
						dialog.dismiss();
					}
				});
		colorPickerDialog.show();
	}

	private void showAlarmMessageDialog(final AlarmCardRecyclerAdapter.AlarmCardViewHolder viewHolder, final int position)
	{
		Log.d(DEBUG_TAG, "Showing alarm message dialog");
		LDialogView dialogView = new LDialogView(mContext,
				R.layout.dialog_content_edit_text,
				R.string.dialog_title_set_alarm_message);
		LDialog alarmMessageDialog = new LDialog(mContext, dialogView);
		final EditText editText = (EditText) dialogView.getView().findViewById(R.id
				.editText);
		editText.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
		String prefText = mAlarmPrefs.get(position).getMessage();
		editText.setText(prefText);
		alarmMessageDialog.setPositiveButtonListener(
				new LClickListener()
				{
					@Override
					public void onClick(AlertDialog dialog1)
					{
						String text = editText.getText().toString();
						mAlarmPrefs.get(position).setMessage(text);
						viewHolder.alarmText.setText(text);
						PrefUtil.setAlarms(mContext, mAlarmPrefs);
						dialog1.dismiss();
					}
				});
		alarmMessageDialog.setNegativeButtonListener(
				new LClickListener()
				{
					@Override
					public void onClick(AlertDialog dialog)
					{
						dialog.dismiss();
					}
				});
		alarmMessageDialog.show();
	}

	private void showTimePicker(final int position, final AlarmCardRecyclerAdapter.AlarmCardViewHolder viewHolder)
	{
		String[] time = StringUtil.getTimeFormattedSystem(mContext, 13, 0).split(" ");
		boolean is24h = time.length < 2;
		AlarmPref alarmPref = mAlarmPrefs.get(position);
		final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(
				new TimePickerDialog.OnTimeSetListener()
				{
					@Override
					public void onTimeSet(RadialPickerLayout radialPickerLayout, int h, int m)
					{
						setAlarmTime(position, h, m);
						setAlarmTimeView(viewHolder.alarmTime, viewHolder.am_pm, h,
								m);
						activateAlarm(viewHolder, position);
					}
				},
				alarmPref.getHour(),
				alarmPref.getMinute(),
				is24h,
				false);
		timePickerDialog.show(getActivity().getSupportFragmentManager(), null);
	}
}
