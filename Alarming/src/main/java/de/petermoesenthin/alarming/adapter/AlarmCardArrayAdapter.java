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
package de.petermoesenthin.alarming.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.List;

import at.markushi.ui.CircleButton;
import de.petermoesenthin.alarming.R;
import de.petermoesenthin.alarming.pref.AlarmGson;


public class AlarmCardArrayAdapter extends ArrayAdapter<AlarmGson> {

	private AdapterCallBacks mAdapterCallacks;
	private Context mContext;
	private List<AlarmGson> mAlarms;

	public AlarmCardArrayAdapter(Context context, int resource,
								 List<AlarmGson> alarms, AdapterCallBacks adapterCallacks) {
		super(context, resource, alarms);
		mContext = context;
		mAdapterCallacks = adapterCallacks;
		mAlarms = alarms;
	}

	public interface AdapterCallBacks {
		ViewHolder onBuildView(ViewHolder viewHolder, AlarmGson alarm);

		void onAlarmTimeClick(ViewHolder viewHolder, int position);

		void onAlarmSetClick(ViewHolder viewHolder, int position);

		void onVibrateClick(ViewHolder viewHolder, int position);

		void onRepeatAlarmClick(ViewHolder viewHolder, int position);

		void onAlarmTextClick(ViewHolder viewHolder, int position);

		void onChooseColorClick(ViewHolder viewHolder, int position);

		void onDeleteAlarmClick(ViewHolder viewHolder, int position);
	}

	public class ViewHolder {
		public TextView alarmTime;
		public TextView am_pm;
		public CircleButton alarmSet;
		public CheckBox vibrate;
		public CheckBox repeatAlarm;
		public TextView alarmText;
		public LinearLayout chooseColor;
		public ImageView deletAlarm;
		public LinearLayout weekdayPanel;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		AlarmGson alarm = getItem(position);
		View v = convertView;

		if (v == null) {
			LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context
					.LAYOUT_INFLATER_SERVICE);
			v = layoutInflater.inflate(R.layout.card_alarm_time, null);
			viewHolder = new ViewHolder();
			viewHolder.alarmTime = (TextView) v.findViewById(R.id.textView_alarmTime);
			viewHolder.am_pm = (TextView) v.findViewById(R.id.textView_am_pm);
			viewHolder.alarmSet = (CircleButton) v.findViewById(R.id.button_alarm_set);
			viewHolder.vibrate = (CheckBox) v.findViewById(R.id.checkBox_vibrate);
			viewHolder.repeatAlarm = (CheckBox) v.findViewById(R.id.checkBox_repeat_alarm);
			viewHolder.alarmText = (TextView) v.findViewById(R.id.textView_alarmText);
			viewHolder.chooseColor = (LinearLayout) v.findViewById(R.id.layout_choose_color);
			viewHolder.deletAlarm = (ImageView) v.findViewById(R.id.button_deleteAlarm);
			viewHolder.weekdayPanel = (LinearLayout) v.findViewById(R.id.layout_weekday_panel);
			v.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) v.getTag();
		}

		if (((mAdapterCallacks != null) && (alarm != null))) {
			mAdapterCallacks.onBuildView(viewHolder, alarm);
		}
		setOnClickListeners(position, viewHolder);

		return v;
	}

	private void setOnClickListeners(final int position, final ViewHolder viewHolder) {
		viewHolder.alarmTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mAdapterCallacks != null) {
					mAdapterCallacks.onAlarmTimeClick(viewHolder, position);
				}
			}
		});

		viewHolder.alarmSet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mAdapterCallacks != null) {
					mAdapterCallacks.onAlarmSetClick(viewHolder, position);
				}
			}
		});

		viewHolder.vibrate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mAdapterCallacks != null) {
					mAdapterCallacks.onVibrateClick(viewHolder, position);
				}
			}
		});

		viewHolder.repeatAlarm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mAdapterCallacks != null) {
					mAdapterCallacks.onRepeatAlarmClick(viewHolder, position);
				}
			}
		});

		viewHolder.alarmText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mAdapterCallacks != null) {
					mAdapterCallacks.onAlarmTextClick(viewHolder, position);
				}
			}
		});

		viewHolder.chooseColor.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mAdapterCallacks != null) {
					mAdapterCallacks.onChooseColorClick(viewHolder, position);
				}
			}
		});
		viewHolder.deletAlarm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mAdapterCallacks != null) {
					mAdapterCallacks.onDeleteAlarmClick(viewHolder, position);
				}
			}
		});
	}
}
