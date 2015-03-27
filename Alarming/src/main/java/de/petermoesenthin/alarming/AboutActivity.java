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
