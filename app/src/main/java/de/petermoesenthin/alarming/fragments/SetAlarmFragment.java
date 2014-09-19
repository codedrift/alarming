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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.doomonafireball.betterpickers.timepicker.TimePickerBuilder;
import com.doomonafireball.betterpickers.timepicker.TimePickerDialogFragment;

import java.util.Calendar;

import de.petermoesenthin.alarming.R;
import de.petermoesenthin.alarming.pref.AlarmGson;
import de.petermoesenthin.alarming.pref.PrefKey;
import de.petermoesenthin.alarming.util.AlarmUtil;
import de.petermoesenthin.alarming.util.PrefUtil;
import de.petermoesenthin.alarming.util.StringUtil;

public class SetAlarmFragment extends Fragment implements
        TimePickerDialogFragment.TimePickerDialogHandler {

    //================================================================================
    // Members
    //================================================================================

    Calendar mCalendarSet = null;

    public static final String DEBUG_TAG = "SetAlarmFragment";
    private static final boolean D = true;

    private TextView textView_alarmTime;
    private Switch switch_alarm;

    //================================================================================
    // Lifecycle
    //================================================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_setalarm, container, false);
        textView_alarmTime = (TextView) rootView.findViewById(R.id.textView_alarmTime);
        textView_alarmTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });
        switch_alarm = (Switch) rootView.findViewById(R.id.switch_alarm);
        setSwitchCheckedListener();
        updateAlarmState();
        updateSwitch();
        return rootView;
    }

    //================================================================================
    // Methods
    //================================================================================

    /**
     * Show a timepicker to set the alarm time
     */
    private void showTimePicker() {
        if(D) {Log.d(DEBUG_TAG,"Showing timpicker dialog");}
        TimePickerBuilder tpb = new TimePickerBuilder()
                .setFragmentManager(getChildFragmentManager())
                .setStyleResId(R.style.BetterPickersDialogFragment_Light)
                .setTargetFragment(SetAlarmFragment.this);
        tpb.show();
    }

    /**
     * Set the alarm switch state from preference
     */
    private void updateSwitch() {
        if(D) {Log.d(DEBUG_TAG,"Updating alarm switch state");}
        boolean alarmSet = PrefUtil.getBoolean(getActivity(), PrefKey.ALARM_SET, false);
        switch_alarm.setChecked(alarmSet);
    }


    /**
     * Update the alarm state view and preference
     */
    private void updateAlarmState(){
        if(D) {Log.d(DEBUG_TAG,"Getting alarm state");}
        AlarmGson alg = PrefUtil.getAlarmTimeGson(getActivity());
        if(alg != null){
            textView_alarmTime.setText(alg.getFormatted());
            mCalendarSet = AlarmUtil.getNextAlarmTimeAbsolute(alg.getHour(), alg.getMinute());
            AlarmUtil.setAlarm(getActivity(), mCalendarSet);
        } else {
            if(D) {Log.d(DEBUG_TAG,"No alarm state found");}
        }
    }

    //================================================================================
    // UI
    //================================================================================

    private void setSwitchCheckedListener(){
        switch_alarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtil.putBoolean(getActivity(), PrefKey.ALARM_SET, isChecked);
                if (isChecked) {
                    if(mCalendarSet != null){
                        AlarmUtil.setAlarm(getActivity(), mCalendarSet);
                    } else {
                        Toast.makeText(getActivity(), R.string.toast_alarmTimeNotSet,
                                Toast.LENGTH_SHORT).show();
                        buttonView.setChecked(false);
                    }
                } else {
                    AlarmUtil.deactivateAlarm(getActivity());
                }
            }
        });
    }

    //================================================================================
    // Callbacks
    //================================================================================

    @Override
    public void onDialogTimeSet(int reference, int hourOfDay, int minute) {
        if(D) {Log.d(DEBUG_TAG,"Timepicker finished. Setting alarm time.");}
        String alarmTime = StringUtil.getAlarmTimeFormatted(hourOfDay, minute);
        textView_alarmTime.setText(alarmTime);
        mCalendarSet = AlarmUtil.getNextAlarmTimeAbsolute(hourOfDay,minute);
        PrefUtil.updateAlarmTime(getActivity(), hourOfDay, minute);
        updateAlarmState();
    }
}
