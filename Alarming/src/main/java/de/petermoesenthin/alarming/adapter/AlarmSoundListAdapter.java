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
package de.petermoesenthin.alarming.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import de.petermoesenthin.alarming.R;
import de.petermoesenthin.alarming.ui.AlarmSoundListItem;

public class AlarmSoundListAdapter extends ArrayAdapter<AlarmSoundListItem> {

	private Context mContext;

	public AlarmSoundListAdapter(Context context, int layoutId, List<AlarmSoundListItem> itemList) {
		super(context, layoutId, itemList);
		this.mContext = context;
	}

	//==========================================================================
	// Methods
	//==========================================================================

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		AlarmSoundListItem listItem = getItem(position);
		View v = convertView;

		if (v == null) {
			LayoutInflater layoutInflater = (LayoutInflater) mContext
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			v = layoutInflater.inflate(R.layout.listitem_alarm_sound, null);
			viewHolder = new ViewHolder();
			viewHolder.itemTitle = (TextView) v.findViewById(R.id.listItem_textView_soundTitle);
			viewHolder.itemArtist = (TextView) v.findViewById(R.id.listItem_textView_soundArtist);
			v.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) v.getTag();
		}
		viewHolder.itemTitle.setText(listItem.getTitle());
		viewHolder.itemArtist.setText(listItem.getArtist());

		return v;
	}

	private class ViewHolder {
		TextView itemTitle;
		TextView itemArtist;
	}
}
