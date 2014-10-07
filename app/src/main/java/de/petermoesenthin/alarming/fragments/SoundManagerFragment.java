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


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.petermoesenthin.alarming.AlarmSoundEditActivity;
import de.petermoesenthin.alarming.R;
import de.petermoesenthin.alarming.adapter.AlarmSoundListAdapter;
import de.petermoesenthin.alarming.callbacks.OnCopyFinishedListener;
import de.petermoesenthin.alarming.ui.AlarmSoundListItem;
import de.petermoesenthin.alarming.util.FileUtil;
import de.petermoesenthin.alarming.util.MediaUtil;
import de.petermoesenthin.alarming.util.PrefUtil;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class SoundManagerFragment extends Fragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    //================================================================================
    // Members
    //================================================================================

    public static final String DEBUG_TAG = "SoundManagerFragment";
    private static final boolean D = true;

    private ListView mListView;
    private AlertDialog mOptionsDialog;
    private int mListItemCount = 0;
    private CircularProgressBar mProgressBar;
    private Handler mHandler = new Handler();
    private Context fragmentContext;

    private AdapterView.OnItemClickListener mListClickListener =
            new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position == mListItemCount) {
                        startAudioFileIntent();
                    }else {
                        showItemActionDialog(position);
                    }
                }
            };

    //================================================================================
    // Lifecycle
    //================================================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentContext = container.getContext();
        View rootView = inflater.inflate(R.layout.fragment_sound_manager, container, false);
        mListView = (ListView) rootView.findViewById(R.id.listView_alarmSounds);
        mProgressBar =
                (CircularProgressBar) rootView.findViewById(R.id.circleProgressBar_SoundList);
        setHasOptionsMenu(true);
        setupListView();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_soundmanager, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume(){
        super.onResume();
        PrefUtil.getApplicationPrefs(fragmentContext)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        PrefUtil.getApplicationPrefs(fragmentContext)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    //================================================================================
    // Callbacks
    //================================================================================

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_add_new_sound:
                startAudioFileIntent();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (D) {Log.d(DEBUG_TAG,"Preferences changed");}
        setupListView();
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            if(resultCode == Activity.RESULT_OK){
                if (D) {Log.d(DEBUG_TAG,"File chosen");}
                //the selected audio file
                Uri uri = data.getData();
                if(!FileUtil.fileIsOK(fragmentContext, uri.getPath())){
                    showWrongFileTypeDialog();
                    return;
                }
                mListView.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                FileUtil.saveFileToExtAppStorage(fragmentContext.getApplicationContext(), uri,
                        new OnCopyFinishedListener(){
                    @Override
                    public void onOperationFinished() {
                        PrefUtil.updateAlarmSoundUris(fragmentContext);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mProgressBar.setVisibility(View.GONE);
                                mListView.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
            }
        }
    }

    //================================================================================
    // Methods
    //================================================================================

    /**
     * Show a dialog to interact with an audio file.
     * @param itemPosition Selected item in parent listView
     */
    private void showItemActionDialog(final int itemPosition) {
        if (D) {Log.d(DEBUG_TAG,"Showing item options dialog");}
        AlertDialog.Builder builder = new AlertDialog.Builder(fragmentContext);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_alarm_sound_options, null);
        ListView optionsListView =
                (ListView) dialogView.findViewById(R.id.listView_alarmSoundOptions);
        String[] options =
                fragmentContext.getResources().getStringArray(R.array.sound_action_options);
        final ArrayList<String> list = new ArrayList<String>();
        Collections.addAll(list, options);
        ListAdapter adapter = new ArrayAdapter<String>(fragmentContext,
                android.R.layout.simple_list_item_1, list);
        optionsListView.setAdapter(adapter);
        optionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        if (D) {Log.d(DEBUG_TAG, "Starting AlarmSoundEditActivity");}
                        Intent i = new Intent(fragmentContext, AlarmSoundEditActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.putExtra("audio_id",itemPosition);
                        fragmentContext.startActivity(i);
                        break;
                    case 1:
                        if (D) {Log.d(DEBUG_TAG, "Deleting sound file");}
                        showDeleteFileDialog(itemPosition);
                        break;
                }
                if (mOptionsDialog != null) {
                    mOptionsDialog.dismiss();
                }
            }
        });
        builder.setView(dialogView);
        builder.setCancelable(true);
        mOptionsDialog = builder.create();
        mOptionsDialog.setCanceledOnTouchOutside(true);
        mOptionsDialog.show();
    }

    /**
     * Start an intent to load an audio file
     */
    private void startAudioFileIntent(){
        if (D) {Log.d(DEBUG_TAG,"Starting file intent");}
        Intent audioIntent = new Intent();
        audioIntent.setType("file/*");
        audioIntent.setAction(Intent.ACTION_GET_CONTENT);
        try {
            startActivityForResult(audioIntent, 1);
        }catch (ActivityNotFoundException e){
            if (D) {Log.e(DEBUG_TAG,"No activity for file intents available",e);}
        }
    }

    /**
     * Show a dialog to inform the user that a wrong file type has been selected
     */
    private void showWrongFileTypeDialog(){
        if (D) {Log.d(DEBUG_TAG,"Showing wrong file type dialog");}
        AlertDialog.Builder builder = new AlertDialog.Builder(fragmentContext);
        builder.setTitle(R.string.alertTitle_wrongFileType).setMessage(R.string.alert_wrongFileType)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_button_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showDeleteFileDialog(final int itemPosition){
        if (D) {Log.d(DEBUG_TAG,"Showing delete file type dialog");}
        AlertDialog.Builder builder = new AlertDialog.Builder(fragmentContext);
        builder.setTitle(R.string.alertTitle_delete_file).setMessage(R.string.alert_delete_file)
                .setCancelable(true)
                .setNegativeButton(R.string.dialog_button_cancel,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.dialog_button_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                FileUtil.deleteFile(
                                        PrefUtil.getAlarmSoundUris(fragmentContext)[itemPosition]);
                                PrefUtil.updateAlarmSoundUris(fragmentContext);
                                dialog.dismiss();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    //================================================================================
    // UI setup
    //================================================================================

    /**
     * Setup the listView containing all alarm sounds
     */
    private void setupListView(){
        if (D) {Log.d(DEBUG_TAG,"Setting up sound listView");}
        mListView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        Thread listViewThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final List<AlarmSoundListItem> listItems = new ArrayList<AlarmSoundListItem>();
                String[] uris = PrefUtil.getAlarmSoundUris(fragmentContext);
                if(uris != null){
                    mListItemCount = uris.length;
                    for (String uri : uris) {
                        String[] metaData = MediaUtil.getBasicMetaData(uri);
                        listItems.add(new AlarmSoundListItem(metaData[0], metaData[1]));
                    }
                } else {
                    mProgressBar.setVisibility(View.GONE);
                    return;
                }
                mListView.setOnItemClickListener(mListClickListener);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mListView.setAdapter(new AlarmSoundListAdapter(fragmentContext,
                                        R.layout.listItem_drawer,
                                        listItems
                                )
                        );
                        mProgressBar.setVisibility(View.GONE);
                        mListView.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
        listViewThread.start();
    }
}

