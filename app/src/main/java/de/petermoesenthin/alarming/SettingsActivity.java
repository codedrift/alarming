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
package de.petermoesenthin.alarming;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
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
import de.petermoesenthin.alarming.ui.LDialog;
import de.petermoesenthin.alarming.ui.LDialogView;
import de.petermoesenthin.alarming.util.PrefUtil;


public class SettingsActivity extends Activity {

    public static final String DEBUG_TAG = "SettingsActivity";
    public static final boolean D = true;

    private CheckBox checkBox_showNotification;
    private LinearLayout linearLayout_setAlarmVolume;
    private LinearLayout linearLayout_setSnoozeTime;

    //==========================================================================
    // Lifecycle
    //==========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if(getActionBar() != null){
            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle(R.string.activity_title_settings);
        setUpUi();
    }

    //==========================================================================
    // Callbacks
    //==========================================================================

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

    //==========================================================================
    // Ui
    //==========================================================================

    private void setUpUi(){
        checkBox_showNotification = (CheckBox) findViewById(R.id.checkBox_setting_notification);
        checkBox_showNotification.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        Log.d(DEBUG_TAG, "Setting " + PrefKey.SHOW_ALARM_NOTIFICATION + " to " +
                                isChecked);
                        PrefUtil.putBoolean(getApplicationContext(),
                                PrefKey.SHOW_ALARM_NOTIFICATION, isChecked);
                    }
                });
        boolean showNotification = PrefUtil.getBoolean(this, PrefKey.SHOW_ALARM_NOTIFICATION, true);
        checkBox_showNotification.setChecked(showNotification);
        linearLayout_setAlarmVolume = (LinearLayout) findViewById(R.id.linearLayout_setAlarmVolume);
        linearLayout_setAlarmVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showVolumeSetDialog();
            }
        });

        linearLayout_setSnoozeTime = (LinearLayout) findViewById(R.id.linearLayout_setSnoozeTime);
        linearLayout_setSnoozeTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSetSnoozeTimeDialog();
            }
        });
    }

    private void showSetSnoozeTimeDialog() {
        LDialogView dialogView = new LDialogView(this, R.layout.dialog_content_edit_text,
                R.string.dialog_title_set_snooze_time);
        LDialog dialog = new LDialog(this,
                dialogView);
        int snoozeTime = PrefUtil.getInt(this, PrefKey.SNOOZE_TIME, 10);
        final EditText editText_snoozeTime = (EditText) dialogView.getView().findViewById(R.id
                .editText);
        editText_snoozeTime.setText(String.valueOf(snoozeTime));
        editText_snoozeTime.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

        dialog.setPositiveButtonListener(
                new LDialog.LClickListener() {
                    @Override
                    public void onClick(AlertDialog dialog) {
                        String text = editText_snoozeTime.getText().toString();
                        int number = Integer.parseInt(text);
                        PrefUtil.putInt(getApplicationContext(),
                                PrefKey.SNOOZE_TIME, Math.abs(number));
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

    private void showVolumeSetDialog(){
        LDialogView dialogView = new LDialogView(this, R.layout.dialog_content_seekbar,
                R.string.dialog_title_alarm_sound_volume);
        LDialog dialog = new LDialog(this,
                dialogView);
        int volume = PrefUtil.getInt(this,PrefKey.ALARM_SOUND_VOLUME, 80);
        final SeekBar seekBar_volume = (SeekBar) dialogView.getView().findViewById(R.id
                .seekBar);
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
                new LDialog.LClickListener() {
                    @Override
                    public void onClick(AlertDialog dialog) {
                        int newVolume = seekBar_volume.getProgress();
                        if (D) {Log.d(DEBUG_TAG, "Setting " + PrefKey.ALARM_SOUND_VOLUME + " to "
                                + newVolume);}
                        PrefUtil.putInt(getApplicationContext(), PrefKey.ALARM_SOUND_VOLUME,
                                newVolume);
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

}
