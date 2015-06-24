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
package de.petermoesenthin.alarming.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import de.petermoesenthin.alarming.R;

public class LDialogView
{

	private int mDialogTitleResource;
	private Context mContext;
	private TextView mPositiveButton;
	private TextView mNegativeButton;
	private View mLayout;

	public LDialogView(Context context, int contentView, int dialogTitleResource)
	{
		mContext = context;
		mDialogTitleResource = dialogTitleResource;
		init(contentView);
	}

	private void init(int contentView)
	{
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mLayout = inflater.inflate(R.layout.l_dialog_view, null);
		View v = inflater.inflate(contentView, null);

		TextView dialogTitle = (TextView) mLayout.findViewById(R.id.l_dialog_textView_title);
		String title = mContext.getResources().getString(mDialogTitleResource);
		dialogTitle.setText(mDialogTitleResource);
		mPositiveButton = (TextView) mLayout.findViewById(R.id.l_textView_positive_button);
		mNegativeButton = (TextView) mLayout.findViewById(R.id.l_textView_negative_button);
		ViewStub stub = (ViewStub) mLayout.findViewById(R.id.l_dialog_content_stub);
		stub.setLayoutResource(contentView);
		stub.inflate();
	}

	public View getView()
	{
		return mLayout;
	}

	public void setPositiveButtonOnClickListener(View.OnClickListener onClickListener)
	{
		mPositiveButton.setOnClickListener(onClickListener);
	}

	public void setNegativeButtonOnClickListener(View.OnClickListener onClickListener)
	{
		mNegativeButton.setOnClickListener(onClickListener);
	}


}
