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

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.petermoesenthin.alarming.adapter.DrawerItemArrayAdapter;
import de.petermoesenthin.alarming.fragments.SetAlarmFragment;
import de.petermoesenthin.alarming.fragments.SoundManagerFragment;
import de.petermoesenthin.alarming.pref.PrefKey;
import de.petermoesenthin.alarming.ui.DrawerItem;
import de.petermoesenthin.alarming.util.PrefUtil;


public class MainActivity extends FragmentActivity {

    //================================================================================
    // Members
    //================================================================================

    // Navigation Drawer
    private String[] mDrawerTitles;
    private ListView mDrawerListView;
    private DrawerLayout mDrawerLayout;
    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private static final int[] mDrawerImages = {
            R.drawable.ic_alarmclock,
            R.drawable.ic_audiofile};

    // Debug
    public static final String DEBUG_TAG = "MainActivity";
    public static final boolean D = true;



    //================================================================================
    // Lifecycle
    //================================================================================

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpNavigationDrawer();
        if(getActionBar() != null) {
            getActionBar().setDisplayShowCustomEnabled(true);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }
        transactNewFragment(new SetAlarmFragment());
        checkFirstStart();
    }

    private void checkFirstStart() {
        boolean firstStart = PrefUtil.getBoolean(this, PrefKey.APP_FIRST_START, true);
        if(firstStart){
            if(D) {Log.d(DEBUG_TAG, "First start detected.");}
            PrefUtil.updateAlarmSoundUris(this);
            PrefUtil.putBoolean(this, PrefKey.APP_FIRST_START, false);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    //================================================================================
    // Methods
    //================================================================================

    public void setTitle(CharSequence title) {
        this.mTitle = title;
        if(getActionBar() != null) {
            getActionBar().setTitle(this.mTitle);
        }
    }

    /**
     * Sets up everything needed for the navigation drawer
     */
    private void setUpNavigationDrawer(){
        if(D) {Log.d(DEBUG_TAG, "Setting up navigation drawer.");}
        mDrawerTitles = getResources()
                .getStringArray(R.array.nav_drawer_titles);
        mDrawerListView = (ListView) findViewById(R.id.drawer_listView);
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Set Actionbar toggle
        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                R.drawable.ic_navigation_drawer,
                R.string.drawer_open, // "open drawer" description
                R.string.drawer_close // "close drawer" description
        ){
            public void onDrawerClosed(View view) {setTitle(mTitle);}
            public void onDrawerOpened(View drawerView) {setTitle(mDrawerTitle);}
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        List<DrawerItem> mDrawerItemList = new ArrayList<DrawerItem>();
        for(int i = 0; i < mDrawerTitles.length; i++){
            DrawerItem drawerItem = new DrawerItem(mDrawerImages[i], mDrawerTitles[i]);
            mDrawerItemList.add(drawerItem);
        }
        mDrawerListView.setAdapter(new DrawerItemArrayAdapter(this,
                R.layout.listitem_drawer,
                mDrawerItemList));
        DrawerItemClickListener drawerItemClickListener =
                new DrawerItemClickListener();
        mDrawerListView.setOnItemClickListener(drawerItemClickListener);

        RelativeLayout settingsButton = (RelativeLayout) findViewById(R.id.drawer_button_settings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingsActivity();
                mDrawerLayout.closeDrawers();
            }
        });

        RelativeLayout aboutButton = (RelativeLayout) findViewById(R.id.drawer_button_about);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAboutActivity();
                mDrawerLayout.closeDrawers();
            }
        });
    }

    private void showAboutActivity() {
        Intent i = new Intent(this, AboutActivity.class);
        startActivity(i);
    }

    private void showSettingsActivity() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }



    /**
     * Replaces the the content of the MainActivity content area with a new fragment.
     * @param fragment
     */
    public void transactNewFragment(Fragment fragment){
        if(D) {Log.d(DEBUG_TAG, "Transacting new fragment.");}
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    //================================================================================
    // Callbacks
    //================================================================================

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //================================================================================
    // Inner classes
    //================================================================================

    /**
     *
     * Handles click events in the navigation drawer.
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        private ListView drawerListView;

        public DrawerItemClickListener(){
            drawerListView = mDrawerListView;
        }

        private void setDrawerItemTypefaceDefault() {
            TextView textView;
            for(int i = 0; i < drawerListView.getChildCount(); i++){
                textView = (TextView) drawerListView
                        .getChildAt(i).findViewById(R.id.drawer_listItem_TextView);
                textView.setTypeface(null, Typeface.NORMAL);
            }
        }

        /**
         * Swaps Fragments in the main content view
         * @param position The selected position
         */
        private void selectItem(int position) {
            // Create/Load Fragment
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = new SetAlarmFragment();
                    break;
                case 1:
                    fragment = new SoundManagerFragment();
                    break;
            }

            drawerListView.setItemChecked(position, true);
            transactNewFragment(fragment);
            setTitle(mDrawerTitles[position]);
            mDrawerLayout.closeDrawers();
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            setDrawerItemTypefaceDefault();
            TextView tv = (TextView) view.findViewById(
                    R.id.drawer_listItem_TextView);
            tv.setTypeface(null, Typeface.BOLD);
            selectItem(position);
        }
    }

}
