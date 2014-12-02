package de.petermoesenthin.alarming.ui;/*
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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import de.petermoesenthin.alarming.R;

public class LDialogView {

    private int mDialogTitleResource;
    private Context mContext;
    private TextView mPositiveButton;
    private TextView mNegativeButton;
    private View mLayout;

    public LDialogView(Context context, int contentView, int dialogTitleResource) {
        mContext = context;
        mDialogTitleResource = dialogTitleResource;
        init(contentView);
    }

    private void init(int contentView){
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

    public View getView(){
        return mLayout;
    }

    public void setPositiveButtonOnClickListener(View.OnClickListener onClickListener){
        mPositiveButton.setOnClickListener(onClickListener);
    }

    public void setNegativeButtonOnClickListener(View.OnClickListener onClickListener){
        mNegativeButton.setOnClickListener(onClickListener);
    }


}
