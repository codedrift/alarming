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

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.doomonafireball.betterpickers.timepicker.TimePickerBuilder;
import com.doomonafireball.betterpickers.timepicker.TimePickerDialogFragment;
import com.faizmalkani.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import at.markushi.ui.CircleButton;
import de.petermoesenthin.alarming.R;
import de.petermoesenthin.alarming.adapter.AlarmCardArrayAdapter;
import de.petermoesenthin.alarming.pref.AlarmGson;
import de.petermoesenthin.alarming.pref.PrefKey;
import de.petermoesenthin.alarming.ui.LDialog;
import de.petermoesenthin.alarming.ui.LDialogView;
import de.petermoesenthin.alarming.util.AlarmUtil;
import de.petermoesenthin.alarming.util.PrefUtil;
import de.petermoesenthin.alarming.util.StringUtil;

public class SetAlarmFragment extends Fragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String DEBUG_TAG = "SetAlarmFragment";
    private static final boolean D = true;

    private Context mContext;
    private ListView mAlarmListView;
    private AlarmCardArrayAdapter mAlarmCardArrayAdapter;
    private List<AlarmGson> mAlarms = new ArrayList<AlarmGson>();
    private FloatingActionButton mFAB;
    private boolean mFlag_create_new = false;

    //----------------------------------------------------------------------------------------------
    //                                      LIFECYCLE
    //----------------------------------------------------------------------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_set_alarm, container, false);
        mAlarmListView = (ListView) rootView.findViewById(R.id.cardListView_alarm);
        mFAB = (FloatingActionButton) rootView.findViewById(R.id.fab_add_alarm);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewAlarm();
            }
        });
        mFAB.listenTo(mAlarmListView);
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
            if (D) {
                Log.d(DEBUG_TAG, "Preferences have changed");
            }
            mAlarmCardArrayAdapter.notifyDataSetChanged();
        }
    }

    //----------------------------------------------------------------------------------------------
    //                                      ALARM
    //----------------------------------------------------------------------------------------------

    private void addNewAlarm() {
        if (D) {
            Log.d(DEBUG_TAG, "Add new alarm");
        }
        AlarmGson alarm = new AlarmGson();
        int alarmID = PrefUtil.getInt(mContext, PrefKey.ALARM_ID_COUNTER, 0);
        alarm.setId(alarmID);
        Random r = new Random();
        String[] messages = getResources().getStringArray(R.array.alarm_texts);
        String message = messages[r.nextInt(messages.length)];
        alarm.setMessage(message);
        //mAlarms.add(0, alarmGson);
        mAlarms.add(alarm);
        mFlag_create_new = true;
        mAlarmCardArrayAdapter.notifyDataSetChanged();
        scrollCardListViewToBottom();
        PrefUtil.putInt(mContext, PrefKey.ALARM_ID_COUNTER, alarmID + 1);
        PrefUtil.setAlarms(mContext, mAlarms);
    }


    private void activateAlarm(AlarmCardArrayAdapter.ViewHolder viewHolder, int position) {
        if (D) {
            Log.d(DEBUG_TAG, "Activate alarm " + position);
        }
        AlarmGson alg = mAlarms.get(position);
        alg.setAlarmSet(true);
        Calendar calendarSet = AlarmUtil.getNextAlarmTimeAbsolute(alg.getHour(), alg.getMinute());
        AlarmUtil.setAlarm(mContext, calendarSet, alg.getId());
        PrefUtil.setAlarms(mContext, mAlarms);
    }

    private void deleteAlarm(int position) {
        if (D) {
            Log.d(DEBUG_TAG, "Delete alarm " + position);
        }
        mAlarms.remove(position);
        PrefUtil.setAlarms(mContext, mAlarms);
        mAlarmCardArrayAdapter.notifyDataSetChanged();
    }

    private void deactivateAlarm(AlarmCardArrayAdapter.ViewHolder viewHolder, int position) {
        if (D) {
            Log.d(DEBUG_TAG, "Deactivate alarm " + position);
        }
        AlarmGson alg = mAlarms.get(position);
        alg.setAlarmSet(false);
        AlarmUtil.deactivateAlarm(mContext, alg.getId());
        PrefUtil.setAlarms(mContext, mAlarms);
    }

    public void setAlarmTime(int position, int hourOfDay, int minute) {
        if (D) {
            Log.d(DEBUG_TAG, "Time picker finished. Setting alarm time at " + position);
        }
        AlarmGson alg = mAlarms.get(position);
        alg.setHour(hourOfDay);
        alg.setMinute(minute);
    }

    //----------------------------------------------------------------------------------------------
    //                                      UI
    //----------------------------------------------------------------------------------------------

    private void setUpListView() {
        if (D) {
            Log.d(DEBUG_TAG, "Set up list view");
        }
        mAlarms = PrefUtil.getAlarms(mContext);
        if (mAlarms.isEmpty()) {
            mAlarms.add(new AlarmGson());
            PrefUtil.putInt(mContext, PrefKey.ALARM_ID_COUNTER, 1);
        }
        createListViewAdapter(mAlarms);
        mAlarmListView.setAdapter(mAlarmCardArrayAdapter);
    }


    private void createListViewAdapter(final List<AlarmGson> alarms) {
        mAlarmCardArrayAdapter = new AlarmCardArrayAdapter
                (mContext, R.layout.card_alarm_time, alarms, new AlarmCardArrayAdapter
                        .AdapterCallBacks() {
                    @Override
                    public AlarmCardArrayAdapter.ViewHolder onBuildView(
                            AlarmCardArrayAdapter.ViewHolder viewHolder, AlarmGson alarm) {
                        if (D) {
                            Log.d(DEBUG_TAG, "onBuildView for " + alarm.getId());
                        }

                        setAlarmTimeView(viewHolder.alarmTime, viewHolder.am_pm, alarm.getHour(),
                                alarm.getMinute());
                        setCircleButtonActive(viewHolder.alarmSet, alarm.isAlarmSet());
                        viewHolder.vibrate.setChecked(alarm.doesVibrate());
                        viewHolder.repeatAlarm.setChecked(alarm.doesRepeat());
                        if (!alarm.getMessage().isEmpty()) {
                            viewHolder.alarmText.setText(alarm.getMessage());
                        }
                        //TODO mViewHolder.chooseColor
                        /*
                        if(position == 0 && mFlag_create_new){
                            mFlag_create_new = false;
                            showTimePicker(position, viewHolder);
                        }
                        */
                        return viewHolder;
                    }

                    @Override
                    public void onAlarmTimeClick(AlarmCardArrayAdapter.ViewHolder viewHolder,
                                                 int position) {
                        if (D) {
                            Log.d(DEBUG_TAG, "AlarmTimeClick  at " + position);
                        }
                        showTimePicker(position, viewHolder);
                    }

                    @Override
                    public void onAlarmSetClick(AlarmCardArrayAdapter.ViewHolder viewHolder,
                                                int position) {
                        if (D) {
                            Log.d(DEBUG_TAG, "AlarmSetClick at " + position);
                        }
                        AlarmGson alg = alarms.get(position);
                        boolean alarmSet = alg.isAlarmSet();
                        if (alarmSet) {
                            deactivateAlarm(viewHolder, position);
                            setCircleButtonActive(viewHolder.alarmSet, false);
                        } else {
                            activateAlarm(viewHolder, position);
                            setCircleButtonActive(viewHolder.alarmSet, true);
                        }
                    }

                    @Override
                    public void onVibrateClick(AlarmCardArrayAdapter.ViewHolder viewHolder,
                                               int position) {
                        if (D) {
                            Log.d(DEBUG_TAG, "VibrateClick at " + position);
                        }
                        AlarmGson alg = mAlarms.get(position);
                        boolean doesVibrate = alg.doesVibrate();
                        alg.setVibrate(!doesVibrate);
                        viewHolder.vibrate.setChecked(!doesVibrate);
                        PrefUtil.setAlarms(mContext, mAlarms);
                    }

                    @Override
                    public void onRepeatAlarmClick(AlarmCardArrayAdapter.ViewHolder viewHolder,
                                                   int position) {
                        if (D) {
                            Log.d(DEBUG_TAG, "RepeatAlarmClick at " + position);
                        }
                        AlarmGson alg = mAlarms.get(position);
                        boolean doesRepeat = alg.doesRepeat();
                        alg.setVibrate(!doesRepeat);
                        viewHolder.repeatAlarm.setChecked(!doesRepeat);
                        PrefUtil.setAlarms(mContext, mAlarms);
                    }

                    @Override
                    public void onAlarmTextClick(AlarmCardArrayAdapter.ViewHolder viewHolder,
                                                 int position) {
                        if (D) {
                            Log.d(DEBUG_TAG, "AlarmTextClick at " + position);
                        }
                        showAlarmMessageDialog(viewHolder, position);
                    }

                    @Override
                    public void onChooseColorClick(AlarmCardArrayAdapter.ViewHolder viewHolder,
                                                   int position) {
                        if (D) {
                            Log.d(DEBUG_TAG, "ChooseColorClick at " + position);
                        }
                        showColorPickerDialog(position);
                    }

                    @Override
                    public void onDeleteAlarmClick(AlarmCardArrayAdapter.ViewHolder viewHolder,
                                                   int position) {
                        if (D) {
                            Log.d(DEBUG_TAG, "DeleteAlarmClick at " + position);
                        }
                        deactivateAlarm(viewHolder, position);
                        deleteAlarm(position);
                    }
                }
                );
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

    private void setCircleButtonActive(CircleButton circleButton, boolean isActive) {
        if (isActive) {
            circleButton.setColor(getResources().getColor(R.color.material_yellow));
            circleButton.setImageResource(R.drawable.ic_bell_ring);
        } else {
            circleButton.setColor(getResources().getColor(R.color.veryLightGray));
            circleButton.setImageResource(R.drawable.ic_bell_outline);
        }
    }

    private void scrollCardListViewToBottom() {
        mAlarmListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                mAlarmListView.setSelection(mAlarmCardArrayAdapter.getCount() - 1);
            }
        });
    }

    private void showColorPickerDialog(int position) {
        LDialog dialog = new LDialog(mContext,
                new LDialogView(mContext, R.layout.dialog_content_color_picker,
                        R.string.dialog_title_set_alarm_color));
        dialog.setPositiveButtonListener(
                new LDialog.LClickListener() {
                    @Override
                    public void onClick(AlertDialog dialog) {
                        dialog.dismiss();
                    }
                });
        dialog.setNegativeButtonListener(
                new LDialog.LClickListener() {
                    @Override
                    public void onClick(AlertDialog dialog) {
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    private void showAlarmMessageDialog(final AlarmCardArrayAdapter.ViewHolder viewHolder,
                                        final int position) {
        if (D) {
            Log.d(DEBUG_TAG, "Showing alarm message dialog");
        }
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
                new LDialog.LClickListener() {
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
                new LDialog.LClickListener() {
                    @Override
                    public void onClick(AlertDialog dialog) {
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    /**
     * Show a time picker to set the alarm time
     *
     * @param position   Reference to the current position in the list view
     * @param viewHolder Associated views in within the alarm card
     */
    private void showTimePicker(int position, final AlarmCardArrayAdapter.ViewHolder viewHolder) {
        if (D) {
            Log.d(DEBUG_TAG, "Showing time picker dialog.");
        }
        TimePickerBuilder tpb = new TimePickerBuilder()
                .setFragmentManager(getChildFragmentManager())
                .setStyleResId(R.style.BetterPicker_Alarming)
                .setReference(position)
                .addTimePickerDialogHandler(new DialogTimeHandler() {
                    @Override
                    public void onDialogTimeSet(int reference, int hourOfDay, int minute) {
                        setAlarmTime(reference, hourOfDay, minute);
                        setAlarmTimeView(viewHolder.alarmTime, viewHolder.am_pm, hourOfDay,
                                minute);
                        activateAlarm(viewHolder, reference);
                    }
                });
        tpb.show();
    }

    private interface DialogTimeHandler extends TimePickerDialogFragment.TimePickerDialogHandler {
        @Override
        public void onDialogTimeSet(int reference, int hourOfDay, int minute);
    }
}
