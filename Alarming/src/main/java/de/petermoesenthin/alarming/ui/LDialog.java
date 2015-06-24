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

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

import de.petermoesenthin.alarming.R;

public class LDialog
{

	private LDialogView mDialogView;
	private Context mContext;
	private AlertDialog mDialog;
	private LClickListener mPositiveButtonListener;
	private LClickListener mNegativeButtonListener;

	public LDialog(Context context, LDialogView dialogView)
	{
		mContext = context;
		mDialogView = dialogView;
	}

	public void show()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setView(mDialogView.getView());
		builder.setCancelable(true);
		mDialog = builder.create();
		mDialog.setCanceledOnTouchOutside(true);
		mDialog.show();
	}

	public void setPositiveButtonListener(LClickListener listener)
	{
		mPositiveButtonListener = listener;
		if (listener != null)
		{
			mDialogView.setPositiveButtonOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (mPositiveButtonListener != null)
					{
						mPositiveButtonListener.onClick(mDialog);
					}
				}
			});
		}
	}

	public void setNegativeButtonListener(LClickListener listener)
	{
		mNegativeButtonListener = listener;
		if (listener != null)
		{
			mDialogView.setNegativeButtonOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (mNegativeButtonListener != null)
					{
						mNegativeButtonListener.onClick(mDialog);
					}
				}
			});
		}
	}

}
