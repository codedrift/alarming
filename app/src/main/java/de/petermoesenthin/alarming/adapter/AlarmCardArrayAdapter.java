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
package de.petermoesenthin.alarming.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import at.markushi.ui.CircleButton;
import de.petermoesenthin.alarming.R;
import de.petermoesenthin.alarming.pref.AlarmGson;


public class AlarmCardArrayAdapter extends ArrayAdapter<AlarmGson>{

    private ViewHolder mViewHolder;
    private AdapterCallBacks mAdapterCallacks;
    private Context mContext;
    private List<AlarmGson> mAlarms;

    public AlarmCardArrayAdapter(Context context, int resource,
                                 List<AlarmGson> alarms, AdapterCallBacks adapterCallacks) {
        super(context, resource);
        mContext = context;
        mAdapterCallacks = adapterCallacks;
        mAlarms = alarms;
    }

    public interface AdapterCallBacks {
        ViewHolder onCreateViews(ViewHolder viewHolder, int position);
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
    }

    @Override
    public int getCount() {
        return mAlarms.size();
    }

    @Override
    public AlarmGson getItem(int position) {
        return mAlarms.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public List<AlarmGson> getAlarms() {
        return mAlarms;
    }

    public void setAlarms(List<AlarmGson> mAlarms) {
        this.mAlarms = mAlarms;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.card_alarm_time, null);
            mViewHolder = new ViewHolder();
            mViewHolder.alarmTime = (TextView) convertView.findViewById(R.id.textView_alarmTime);
            mViewHolder.am_pm = (TextView) convertView.findViewById(R.id.textView_am_pm);
            mViewHolder.alarmSet = (CircleButton) convertView.findViewById(R.id.button_alarm_set);
            mViewHolder.vibrate = (CheckBox) convertView.findViewById(R.id.checkBox_vibrate);
            mViewHolder.repeatAlarm = (CheckBox) convertView.findViewById(R.id.checkBox_repeat_alarm);
            mViewHolder.alarmText = (TextView) convertView.findViewById(R.id.textView_alarmText);
            mViewHolder.chooseColor = (LinearLayout) convertView.findViewById(R.id.layout_choose_color);
            mViewHolder.deletAlarm = (ImageView) convertView.findViewById(R.id.button_deleteAlarm);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        if(mAdapterCallacks != null){
            mAdapterCallacks.onCreateViews(mViewHolder, position);
        }
        setOnClickListeners(position, mViewHolder);

        return convertView;
    }

    private void setOnClickListeners(final int position, final ViewHolder viewHolder){
        mViewHolder.alarmTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAdapterCallacks != null){
                    mAdapterCallacks.onAlarmTimeClick(viewHolder, position);
                }
            }
        });

        mViewHolder.alarmSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAdapterCallacks != null){
                    mAdapterCallacks.onAlarmSetClick(viewHolder, position);
                }
            }
        });

        mViewHolder.vibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAdapterCallacks != null){
                    mAdapterCallacks.onVibrateClick(viewHolder, position);
                }
            }
        });

        mViewHolder.repeatAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAdapterCallacks != null){
                    mAdapterCallacks.onRepeatAlarmClick(viewHolder, position);
                }
            }
        });

        mViewHolder.alarmText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAdapterCallacks != null){
                    mAdapterCallacks.onAlarmTextClick(viewHolder, position);
                }
            }
        });

        mViewHolder.chooseColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAdapterCallacks != null){
                    mAdapterCallacks.onChooseColorClick(viewHolder, position);
                }
            }
        });
        mViewHolder.deletAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAdapterCallacks != null){
                    mAdapterCallacks.onDeleteAlarmClick(viewHolder, position);
                }
            }
        });
    }
}
