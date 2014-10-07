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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import de.petermoesenthin.alarming.pref.PrefKey;
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
        this.getActionBar().setHomeButtonEnabled(true);
        this.getActionBar().setDisplayHomeAsUpEnabled(true);
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
        AlertDialog.Builder builder;
        AlertDialog alertDialog;
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_set_snooze_time,
                null);
        int snoozeTime = PrefUtil.getInt(this, PrefKey.SNOOZE_TIME, 10);
        final EditText editText_snoozeTime = (EditText) layout.findViewById(R.id
                .editText_snoozeTime);
        editText_snoozeTime.setText(String.valueOf(snoozeTime));
        builder = new AlertDialog.Builder(this);
        builder.setView(layout);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.dialog_button_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String text = editText_snoozeTime.getText().toString();
                int number = Integer.parseInt(text);
                PrefUtil.putInt(getApplicationContext(), PrefKey.SNOOZE_TIME, Math.abs(number));
                dialogInterface.dismiss();
            }
        });
        alertDialog = builder.create();
        alertDialog.show();
    }

    private void showVolumeSetDialog(){
        AlertDialog.Builder builder;
        AlertDialog alertDialog;
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_setting_media_volume,
                null);
        int volume = PrefUtil.getInt(this,PrefKey.ALARM_SOUND_VOLUME, 80);
        final SeekBar seekBar_volume = (SeekBar) layout.findViewById(R.id.seekBar_audioVolume);
        final TextView textView_current_volume = (TextView)
                layout.findViewById(R.id.textView_settings_dialog_inner_title);
        final String dialog_inner_title =
                getResources().getString(R.string.settings_dialog_alarm_sound_volume_inner_title);
        String formatted = String.format(dialog_inner_title, volume + "%");
        textView_current_volume.setText(formatted);
        seekBar_volume.setMax(100);
        seekBar_volume.setProgress(volume);
        seekBar_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                String formatted = String.format(dialog_inner_title, i + "%");
                textView_current_volume.setText(formatted);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Nothing
            }
        });
        builder = new AlertDialog.Builder(this);
        builder.setView(layout);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.dialog_button_cancel,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int newVolume = seekBar_volume.getProgress();
                if (D) {Log.d(DEBUG_TAG, "Setting " + PrefKey.ALARM_SOUND_VOLUME + " to "
                        + newVolume);}
                PrefUtil.putInt(getApplicationContext(), PrefKey.ALARM_SOUND_VOLUME, newVolume);
                dialogInterface.dismiss();
            }
        });
        alertDialog = builder.create();
        alertDialog.show();
    }

}
