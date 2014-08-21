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
    private Context context;

    public AlarmSoundListAdapter(Context context, int layoutId,
                                   List<AlarmSoundListItem> itemList) {
        super(context, layoutId, itemList);
        this.context = context;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        AlarmSoundListItem listItem = getItem(position);

        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = layoutInflater
                    .inflate(R.layout.alarmsound_listitem, null);
            viewHolder = new ViewHolder();
            viewHolder.itemText = (TextView) convertView
                    .findViewById(R.id.listItem_textView_alarmsound);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.itemText.setText(listItem.getText());

        return convertView;
    }

    private class ViewHolder {
        TextView itemText;
    }
}
