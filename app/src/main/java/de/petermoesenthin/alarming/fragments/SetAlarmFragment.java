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

package de.petermoesenthin.alarming.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.doomonafireball.betterpickers.timepicker.TimePickerBuilder;
import com.doomonafireball.betterpickers.timepicker.TimePickerDialogFragment;

import java.util.Calendar;

import at.markushi.ui.CircleButton;
import de.petermoesenthin.alarming.R;
import de.petermoesenthin.alarming.pref.AlarmGson;
import de.petermoesenthin.alarming.util.AlarmUtil;
import de.petermoesenthin.alarming.util.NotificationUtil;
import de.petermoesenthin.alarming.util.PrefUtil;
import de.petermoesenthin.alarming.util.StringUtil;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardView;

public class SetAlarmFragment extends Fragment implements
        TimePickerDialogFragment.TimePickerDialogHandler {

    //================================================================================
    // Members
    //================================================================================

    private Calendar mCalendarSet = null;

    public static final String DEBUG_TAG = "SetAlarmFragment";
    private static final boolean D = true;

    private Context fragmentContext;

    private TextView textView_alarmTime;
    private CircleButton circleButton;
    private CheckBox checkBox_vibrate;

    //================================================================================
    // Lifecycle
    //================================================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentContext = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_setalarm, container, false);
        // Card view
        Card card = new Card(fragmentContext, R.layout.card_alarm_time);
        CardView cardView = (CardView) rootView.findViewById(R.id.alarmCard);
        cardView.setCard(card);
        textView_alarmTime = (TextView) cardView.findViewById(R.id.textView_alarmTime);
        textView_alarmTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });

        circleButton = (CircleButton) cardView.findViewById(R.id.button_alarm_set);
        circleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlarmGson alg = PrefUtil.getAlarmGson(fragmentContext);
                boolean alarmSet = alg.isAlarmSet();
                if(alarmSet){
                    deactivateAlarm();
                } else {
                    activateAlarm();
                }

            }
        });

        checkBox_vibrate = (CheckBox) cardView.findViewById(R.id.checkBox_vibrate);
        checkBox_vibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlarmGson alg = PrefUtil.getAlarmGson(fragmentContext);
                boolean vibrate = alg.vibrate();
                alg.setVibrate(!vibrate);
                checkBox_vibrate.setChecked(!vibrate);
                PrefUtil.setAlarmGson(fragmentContext, alg);
            }
        });

        Button test = (Button) rootView.findViewById(R.id.buttonTest);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationUtil.showSnoozeNotification(fragmentContext, 6, 55);
            }
        });


        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAlarmState();
    }

    //================================================================================
    // Methods
    //================================================================================

    /**
     * Update the alarm state view and preference
     */
    private void loadAlarmState(){
        if(D) {Log.d(DEBUG_TAG,"Updating alarm state from preference.");}
        AlarmGson alg = PrefUtil.getAlarmGson(fragmentContext);
        if(alg != null){
            if(D) {Log.d(DEBUG_TAG,"Alarm state found. Preparing views.");}
            /*
            String alarmFormatted = StringUtil.getAlarmTimeFormatted(alg.getHour(),
                    alg.getMinute());
                    */
            String alarmFormatted = getSystemFormatTime(alg.getHour(), alg.getMinute());
            textView_alarmTime.setText(alarmFormatted);
            setCircleButtonActive(alg.isAlarmSet());
            checkBox_vibrate.setChecked(alg.vibrate());
        } else {
            if(D) {Log.d(DEBUG_TAG,"No alarm state found.");}
        }
    }

    private void activateAlarm(){
        AlarmGson alg = PrefUtil.getAlarmGson(fragmentContext);
        alg.setAlarmSet(true);
        PrefUtil.setAlarmGson(fragmentContext, alg);
        mCalendarSet = AlarmUtil.getNextAlarmTimeAbsolute(alg.getHour(), alg.getMinute());
        AlarmUtil.setAlarm(fragmentContext, mCalendarSet);
        setCircleButtonActive(true);
    }

    private void deactivateAlarm(){
        AlarmGson alg = PrefUtil.getAlarmGson(fragmentContext);
        alg.setAlarmSet(false);
        PrefUtil.setAlarmGson(fragmentContext, alg);
        AlarmUtil.deactivateAlarm(fragmentContext);
        setCircleButtonActive(false);
    }

    private String getSystemFormatTime(int hour, int minute){
        Calendar c = AlarmUtil.getNextAlarmTimeAbsolute(hour, minute);
        return android.text.format.DateFormat.getTimeFormat(fragmentContext).format(c.getTime());
    }

    //================================================================================
    // UI
    //================================================================================

    private void setCircleButtonActive(boolean isActive){
        if(isActive){
            circleButton.setColor(getResources().getColor(R.color.yellow_main));
            circleButton.setImageResource(R.drawable.ic_action_alarmclock_light);
        } else {
            circleButton.setColor(getResources().getColor(R.color.veryLightGray));
            circleButton.setImageResource(R.drawable.ic_alarmclock_light_no_bells);
        }

    }

    /**
     * Show a timepicker to set the alarm time
     */
    private void showTimePicker() {
        if(D) {Log.d(DEBUG_TAG,"Showing time picker dialog.");}
        TimePickerBuilder tpb = new TimePickerBuilder()
                .setFragmentManager(getChildFragmentManager())
                .setStyleResId(R.style.BetterPickersDialogFragment_Light)
                .setTargetFragment(SetAlarmFragment.this);
        tpb.show();
    }

    //================================================================================
    // Callbacks
    //================================================================================

    @Override
    public void onDialogTimeSet(int reference, int hourOfDay, int minute) {
        if(D) {Log.d(DEBUG_TAG,"Time picker finished. Setting alarm time.");}
        String alarmTimeFormatted = StringUtil.getAlarmTimeFormatted(hourOfDay, minute);
        textView_alarmTime.setText(alarmTimeFormatted);
        AlarmGson alg = PrefUtil.getAlarmGson(fragmentContext);
        alg.setHour(hourOfDay);
        alg.setMinute(minute);
        PrefUtil.setAlarmGson(fragmentContext, alg);
        activateAlarm();
    }

}
