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

public class AlarmSoundListAdapter extends ArrayAdapter<AlarmSoundListItem>
{

	private Context mContext;

	public AlarmSoundListAdapter(Context context, int layoutId, List<AlarmSoundListItem> itemList)
	{
		super(context, layoutId, itemList);
		this.mContext = context;
	}

	//==========================================================================
	// Methods
	//==========================================================================

	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder viewHolder = null;
		AlarmSoundListItem listItem = getItem(position);
		View v = convertView;

		if (v == null)
		{
			LayoutInflater layoutInflater = (LayoutInflater) mContext
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			v = layoutInflater.inflate(R.layout.listitem_alarm_sound, null);
			viewHolder = new ViewHolder();
			viewHolder.itemTitle = (TextView) v.findViewById(R.id.listItem_textView_soundTitle);
			viewHolder.itemArtist = (TextView) v.findViewById(R.id.listItem_textView_soundArtist);
			v.setTag(viewHolder);
		} else
		{
			viewHolder = (ViewHolder) v.getTag();
		}

		viewHolder.itemTitle.setText(listItem.getTitle());
		viewHolder.itemArtist.setText(listItem.getArtist());

		return v;
	}

	private class ViewHolder
	{
		TextView itemTitle;
		TextView itemArtist;
	}
}
