/*
 * Copyright (C) 2014-2015 Peter Mösenthin <peter.moesenthin@gmail.com>
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
