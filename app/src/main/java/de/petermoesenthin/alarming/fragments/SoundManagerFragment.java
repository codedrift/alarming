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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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
import de.petermoesenthin.alarming.callbacks.OperationFinishedListener;
import de.petermoesenthin.alarming.ui.AlarmSoundListItem;
import de.petermoesenthin.alarming.util.FileUtil;
import de.petermoesenthin.alarming.util.MediaUtil;
import de.petermoesenthin.alarming.util.PrefUtil;

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
        View rootView = inflater.inflate(R.layout.fragment_soundmanager, container, false);
        mListView = (ListView) rootView.findViewById(R.id.listView_alarmSounds);
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
        PrefUtil.getApplicationPrefs(getActivity())
                .registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause(){
        super.onPause();
        PrefUtil.getApplicationPrefs(getActivity())
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
        updateListItems();
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            if(resultCode == Activity.RESULT_OK){
                if (D) {Log.d(DEBUG_TAG,"File chosen");}
                //the selected audio file
                Uri uri = data.getData();
                if(!fileOK(uri)){
                    showWrongFileTypeDialog();
                    return;
                }
                FileUtil.saveFileToExtAppStorage(getActivity().getApplicationContext(), uri,
                        new OperationFinishedListener(){
                    @Override
                    public void onOperationFinished() {
                        PrefUtil.updateAlarmSoundUris(getActivity());
                    }
                });
            }
        }
    }

    private boolean fileOK(Uri uri){
        String mimeType;
        mimeType = FileUtil.getMimeType(uri.toString());
        if(mimeType == null){
            return false;
        }
        if(!mimeType.startsWith("audio")){
            return false;
        }
        try{
            MediaUtil.getBasicMetaData(uri.getPath());
        }catch (RuntimeException e){
            if (D) {Log.e(DEBUG_TAG,"No activity for file intents availiable",e);}
            return false;
        }
        return true;
    }

    //================================================================================
    // Methods
    //================================================================================

    /**
     * Update list item in the listview containing all alarm sounds
     */
    private void updateListItems(){
        if (D) {Log.d(DEBUG_TAG,"Updating sound item list");}
        List<AlarmSoundListItem> listItems = new ArrayList<AlarmSoundListItem>();
        String[] uris = PrefUtil.getAlarmSoundUris(getActivity());
        if(uris != null){
            mListItemCount = uris.length;
            for (String uri : uris) {
                String[] metaData = MediaUtil.getBasicMetaData(uri);
                listItems.add(new AlarmSoundListItem(metaData[0], metaData[1]));
            }
        }
        mListView.setAdapter(new AlarmSoundListAdapter(getActivity(),
                        R.layout.drawer_list_item,
                        listItems
                )
        );
    }

    /**
     * Show a dialog to interact with an audio file.
     * @param itemPosition Selected item in parent listView
     */
    private void showItemActionDialog(final int itemPosition) {
        if (D) {Log.d(DEBUG_TAG,"Showing item options dialog");}
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_alarmsoundoptions, null);
        ListView optionsListView =
                (ListView) dialogView.findViewById(R.id.listView_alarmSoundOptions);
        String[] options =
                getActivity().getResources().getStringArray(R.array.sound_action_options);
        final ArrayList<String> list = new ArrayList<String>();
        Collections.addAll(list, options);
        ListAdapter adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, list);
        optionsListView.setAdapter(adapter);
        optionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        if (D) {Log.d(DEBUG_TAG, "Starting AlarmSoundEditActivity");}
                        Intent i = new Intent(getActivity(), AlarmSoundEditActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.putExtra("audio_id",itemPosition);
                        getActivity().startActivity(i);
                        break;
                    case 1:
                        if (D) {Log.d(DEBUG_TAG, "Deleting sound file");}
                        FileUtil.deleteFile(
                                PrefUtil.getAlarmSoundUris(getActivity())[itemPosition]);
                        PrefUtil.updateAlarmSoundUris(getActivity());
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
            if (D) {Log.e(DEBUG_TAG,"No activity for file intents availiable",e);}
        }
    }

    /**
     * Show a dialog to inform the user that a wrong file type has been selected
     */
    private void showWrongFileTypeDialog(){
        if (D) {Log.d(DEBUG_TAG,"Showing wrong file type dialog");}
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.alertTitle_wrongFileType).setMessage(R.string.alert_wrongFileType)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
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
        if (D) {Log.d(DEBUG_TAG,"Setting up ListView");}
        mListView.setOnItemClickListener(mListClickListener);
        updateListItems();
    }
}

