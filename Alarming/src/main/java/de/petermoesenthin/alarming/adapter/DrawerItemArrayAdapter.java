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
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.petermoesenthin.alarming.R;
import de.petermoesenthin.alarming.ui.DrawerItem;

public class DrawerItemArrayAdapter extends ArrayAdapter<DrawerItem> {

	private Context mContext;

	public DrawerItemArrayAdapter(Context context, int layoutId, List<DrawerItem> drawerItemList) {
		super(context, layoutId, drawerItemList);
		this.mContext = context;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		DrawerItem drawerItem = getItem(position);
		View v = convertView;

		LayoutInflater layoutInflater = (LayoutInflater)
				mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		if (v == null) {
			v = layoutInflater.inflate(R.layout.listitem_drawer,
					null);
			viewHolder = new ViewHolder();
			viewHolder.itemImage = (ImageView) v.findViewById(R.id.drawer_listItem_ImageView);
			viewHolder.itemText = (TextView) v.findViewById(R.id.drawer_listItem_TextView);
			if (position == 0) {
				viewHolder.itemText.setTypeface(null, Typeface.BOLD);
			}
			v.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) v.getTag();
		}

		viewHolder.itemText.setText(drawerItem.getTitle());
		viewHolder.itemImage.setImageResource(drawerItem.getImageId());

		return v;
	}

	private class ViewHolder {
		ImageView itemImage;
		TextView itemText;
	}

}
