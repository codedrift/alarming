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

package de.petermoesenthin.alarming.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

import de.petermoesenthin.alarming.R;

public class LDialog {

	private LDialogView mDialogView;
	private Context mContext;
	private AlertDialog mDialog;
	private LClickListener mPositiveButtonListener;
	private LClickListener mNegativeButtonListener;

	public LDialog(Context context, LDialogView dialogView) {
		mContext = context;
		mDialogView = dialogView;
	}

	public void show() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setView(mDialogView.getView());
		builder.setCancelable(true);
		mDialog = builder.create();
		mDialog.setCanceledOnTouchOutside(true);
		mDialog.show();
	}

	public void setPositiveButtonListener(LClickListener listener) {
		mPositiveButtonListener = listener;
		if (listener != null) {
			mDialogView.setPositiveButtonOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mPositiveButtonListener != null) {
						mPositiveButtonListener.onClick(mDialog);
					}
				}
			});
		}
	}

	public void setNegativeButtonListener(LClickListener listener) {
		mNegativeButtonListener = listener;
		if (listener != null) {
			mDialogView.setNegativeButtonOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mNegativeButtonListener != null) {
						mNegativeButtonListener.onClick(mDialog);
					}
				}
			});
		}
	}

}
