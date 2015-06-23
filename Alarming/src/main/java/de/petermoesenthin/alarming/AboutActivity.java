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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import de.petermoesenthin.alarming.ui.OpenFontLicense;
import de.psdev.licensesdialog.LicenseResolver;
import de.psdev.licensesdialog.LicensesDialog;


public class AboutActivity extends ActionBarActivity {

	private ListView mListView;
	public static final String DEBUG_TAG = "AboutActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setElevation(1);
		mListView = (ListView) findViewById(R.id.listView_about);
		setUpListView();
	}

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


	private void showInfoDialog(String title, int textRes) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title).setMessage(textRes)
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

	private void showLicensesDialog() {
		LicenseResolver.registerLicense(new OpenFontLicense());
		LicensesDialog.Builder ldb = new LicensesDialog.Builder(this)
				.setNotices(R.raw.notices);
		LicensesDialog ld = ldb.build();
		ld.show();
	}

	private void setUpListView() {
		Log.d(DEBUG_TAG, "Setting up listView");
		final String[] aboutTitles = getResources().getStringArray(R.array.about_actions);
		mListView.setAdapter(new ArrayAdapter<String>(this, R.layout.listitem_about,
				aboutTitles));
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				switch (position) {
					// About
					case 0:
						showInfoDialog(aboutTitles[0], R.string.about_this_app);
						break;
					// Licenses
					case 1:
						showLicensesDialog();
						break;
				}
			}
		});
	}
}
