/*
 * Alarming, an alarm app for the Android platform
 *
 * Copyright (C) 2014-2015 Peter Mösenthin <peter.moesenthin@gmail.com>
 *
 * Alarming is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.petermoesenthin.alarming;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
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
import de.petermoesenthin.alarming.pref.PrefUtil;


public class MainActivity extends ActionBarActivity
{

	// Navigation Drawer
	private String[] mDrawerTitles;
	private ListView mDrawerListView;
	private DrawerLayout mDrawerLayout;
	private CharSequence mTitle;
	private CharSequence mDrawerTitle;
	private ActionBarDrawerToggle mDrawerToggle;
	private static final int[] mDrawerImages = {
			R.drawable.ic_drawer_bell,
			R.drawable.ic_drawer_sound};

	// Debug
	public static final String DEBUG_TAG = MainActivity.class.getSimpleName();


	//----------------------------------------------------------------------------------------------
	//                                      LIFECYCLE
	//----------------------------------------------------------------------------------------------

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setUpNavigationDrawer();
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setElevation(1);

		mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.material_yellow_dark));

		transactNewFragment(new SetAlarmFragment());
		checkFirstStart();
	}

	private void checkFirstStart()
	{
		boolean firstStart = PrefUtil.getBoolean(this, PrefKey.APP_FIRST_START, true);
		if (firstStart)
		{
			Log.d(DEBUG_TAG, "First start detected.");
			PrefUtil.updateAlarmSoundUris(this);
			PrefUtil.putBoolean(this, PrefKey.APP_FIRST_START, false);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (mDrawerToggle.onOptionsItemSelected(item))
		{
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	//----------------------------------------------------------------------------------------------
	//                                      UI
	//----------------------------------------------------------------------------------------------

	public void setTitle(CharSequence title)
	{
		this.mTitle = title;
		getSupportActionBar().setTitle(this.mTitle);
	}

	/**
	 * Sets up everything needed for the navigation drawer
	 */
	private void setUpNavigationDrawer()
	{
		Log.d(DEBUG_TAG, "Setting up navigation drawer.");
		mDrawerTitles = getResources().getStringArray(R.array.nav_drawer_titles);
		mDrawerListView = (ListView) findViewById(R.id.drawer_listView);
		mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.material_yellow_dark));
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close)
		{
			public void onDrawerClosed(View view)
			{
				setTitle(mTitle);
			}

			public void onDrawerOpened(View drawerView)
			{
				setTitle(mDrawerTitle);
			}
		};

		mDrawerToggle.setDrawerIndicatorEnabled(true);
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		List<DrawerItem> mDrawerItemList = new ArrayList<DrawerItem>();
		for (int i = 0; i < mDrawerTitles.length; i++)
		{
			DrawerItem drawerItem = new DrawerItem(mDrawerImages[i], mDrawerTitles[i]);
			mDrawerItemList.add(drawerItem);
		}
		mDrawerListView.setAdapter(new DrawerItemArrayAdapter(this, R.layout.listitem_drawer, mDrawerItemList));
		DrawerItemClickListener drawerItemClickListener = new DrawerItemClickListener();
		mDrawerListView.setOnItemClickListener(drawerItemClickListener);

		RelativeLayout settingsButton = (RelativeLayout) findViewById(R.id.drawer_button_settings);
		settingsButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showSettingsActivity();
				mDrawerLayout.closeDrawers();
			}
		});

		RelativeLayout aboutButton = (RelativeLayout) findViewById(R.id.drawer_button_about);
		aboutButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showAboutActivity();
				mDrawerLayout.closeDrawers();
			}
		});
	}

	private void showAboutActivity()
	{
		Intent i = new Intent(this, AboutActivity.class);
		startActivity(i);
	}

	private void showSettingsActivity()
	{
		Intent i = new Intent(this, SettingsActivity.class);
		startActivity(i);
	}

	/**
	 * Replaces the the content of the MainActivity content area with a new fragment.
	 *
	 * @param fragment
	 */
	public void transactNewFragment(Fragment fragment)
	{
		Log.d(DEBUG_TAG, "Transacting new fragment.");
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, fragment)
				.commit();
	}


	//----------------------------------------------------------------------------------------------
	//                                      DRAWER
	//----------------------------------------------------------------------------------------------


	/**
	 * Handles click events in the navigation drawer.
	 */
	private class DrawerItemClickListener implements ListView.OnItemClickListener
	{

		private ListView drawerListView;

		public DrawerItemClickListener()
		{
			drawerListView = mDrawerListView;
		}

		private void setDrawerItemTypefaceDefault()
		{
			TextView textView;
			for (int i = 0; i < drawerListView.getChildCount(); i++)
			{
				textView = (TextView) drawerListView
						.getChildAt(i).findViewById(R.id.drawer_listItem_TextView);
				textView.setTypeface(null, Typeface.NORMAL);
			}
		}

		/**
		 * Swaps Fragments in the main content view
		 *
		 * @param position The selected position
		 */
		private void selectItem(int position)
		{
			// Create/Load Fragment
			Fragment fragment = null;
			switch (position)
			{
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
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			setDrawerItemTypefaceDefault();
			TextView tv = (TextView) view.findViewById(
					R.id.drawer_listItem_TextView);
			tv.setTypeface(null, Typeface.BOLD);
			selectItem(position);
		}
	}

}
