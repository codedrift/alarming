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

    public AlarmCardArrayAdapter(Context context, List<Card> cards) {
        super(context, cards);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        TextView alarmTime = (TextView) v.findViewById(R.id.textView_alarmTime);
        TextView am_pm = (TextView) v.findViewById(R.id.textView_am_pm);
        CircleButton alarmSet = (CircleButton) v.findViewById(R.id.button_alarm_set);
        CheckBox vibrate = (CheckBox) v.findViewById(R.id.checkBox_vibrate);
        CheckBox repeatAlarm = (CheckBox) v.findViewById(R.id.checkBox_repeat_alarm);
        TextView alarmText = (TextView) v.findViewById(R.id.textView_alarmText);
        LinearLayout chooseColor = (LinearLayout) v.findViewById(R.id.layout_choose_color);
        return v;
    }

    private class ViewHolder {
        TextView alarm_time;
        TextView am_pm;
        CircleButton alarmSet;
        CheckBox vibrate;
        CheckBox repeatAlarm;
        TextView alarmText;
        LinearLayout chooseColor;
    }
}
