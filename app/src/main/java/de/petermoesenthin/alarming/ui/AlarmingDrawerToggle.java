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

package de.petermoesenthin.alarming.ui;

import android.app.Activity;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

/**
 * This class is used to prevent the drawer toggle from sliding.
 */
public class AlarmingDrawerToggle extends ActionBarDrawerToggle {

    public AlarmingDrawerToggle(Activity activity, DrawerLayout drawerLayout, int drawerImageRes,
                                int openDrawerContentDescRes, int closeDrawerContentDescRes) {
        super(activity, drawerLayout, drawerImageRes, openDrawerContentDescRes,
                closeDrawerContentDescRes);
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset){
        super.onDrawerSlide(drawerView, 0);
    }

}
