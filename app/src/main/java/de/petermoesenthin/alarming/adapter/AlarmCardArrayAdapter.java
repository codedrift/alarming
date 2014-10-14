package de.petermoesenthin.alarming.adapter;/*
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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import at.markushi.ui.CircleButton;
import de.petermoesenthin.alarming.R;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;

public class AlarmCardArrayAdapter extends CardArrayAdapter {

    private ViewHolder mViewHolder;
    private List<Card> mCards;
    private AdapterCallacks mAdapterCallacks;


    public interface AdapterCallacks{
        ViewHolder onCreateViews(ViewHolder viewHolder, int position);
        void onAlarmTimeClick(ViewHolder viewHolder, int position);
        void onAlarmSetClick(ViewHolder viewHolder, int position);
        void onVibrateClick(ViewHolder viewHolder, int position);
        void onRepeatAlarmClick(ViewHolder viewHolder, int position);
        void onAlarmTextClick(ViewHolder viewHolder, int position);
        void onChooseColorClick(ViewHolder viewHolder, int position);
    }

    public AlarmCardArrayAdapter(Context context, List<Card> cards,
                                 AdapterCallacks adapterCallacks) {
        super(context, cards);
        this.mAdapterCallacks = adapterCallacks;
        this.mCards = cards;
    }

    @Override
    public int getCount() {
        return mCards.size();
    }

    @Override
    public Card getItem(int position) {
        return mCards.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        if(convertView == null){
            mViewHolder = new ViewHolder();
            mViewHolder.alarmTime = (TextView) v.findViewById(R.id.textView_alarmTime);
            mViewHolder.am_pm = (TextView) v.findViewById(R.id.textView_am_pm);
            mViewHolder.alarmSet = (CircleButton) v.findViewById(R.id.button_alarm_set);
            mViewHolder.vibrate = (CheckBox) v.findViewById(R.id.checkBox_vibrate);
            mViewHolder.repeatAlarm = (CheckBox) v.findViewById(R.id.checkBox_repeat_alarm);
            mViewHolder.alarmText = (TextView) v.findViewById(R.id.textView_alarmText);
            mViewHolder.chooseColor = (LinearLayout) v.findViewById(R.id.layout_choose_color);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        setOnClickListeners(position, mViewHolder);

        if(mAdapterCallacks != null){
            mAdapterCallacks.onCreateViews(mViewHolder, position);
        }

        v.setTag(mViewHolder);
        return v;
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
    }

    public class ViewHolder {
        public TextView alarmTime;
        public TextView am_pm;
        public CircleButton alarmSet;
        public CheckBox vibrate;
        public CheckBox repeatAlarm;
        public TextView alarmText;
        public LinearLayout chooseColor;
    }
}
