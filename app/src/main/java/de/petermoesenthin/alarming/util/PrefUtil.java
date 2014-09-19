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
package de.petermoesenthin.alarming.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;

import de.petermoesenthin.alarming.pref.AlarmGson;
import de.petermoesenthin.alarming.pref.PrefKey;

public class PrefUtil {

    public static final String DEBUG_TAG = "PrefUtil";
    public static final boolean D = false;

    public static void putString(Context context, String key, String value){
        context.getSharedPreferences(PrefKey.PREF_FILE_NAME, Activity.MODE_PRIVATE)
                .edit().putString(
                key,
                value
        ).commit();
    }

    public static void putLong(Context context, String key, long value){
        context.getSharedPreferences(PrefKey.PREF_FILE_NAME, Activity.MODE_PRIVATE)
                .edit().putLong(
                key,
                value
        ).commit();
    }

    public static void putBoolean(Context context, String key, boolean value){
        context.getSharedPreferences(PrefKey.PREF_FILE_NAME, Activity.MODE_PRIVATE)
                .edit().putBoolean(
                key,
                value
        ).commit();
    }

    public static void putInt(Context context, String key, int value){
        context.getSharedPreferences(PrefKey.PREF_FILE_NAME, Activity.MODE_PRIVATE)
                .edit().putInt(
                key,
                value
        ).commit();
    }


    public static String getString(Context context, String key){
        return getApplicationPrefs(context).getString(key, null);
    }
    public static long getLong(Context context, String key){
        return getApplicationPrefs(context).getLong(key, -1);
    }
    public static boolean getBoolean(Context context, String key, boolean defaultValue){
        return getApplicationPrefs(context).getBoolean(key, defaultValue);
    }

    public static int getInt(Context context, String key){
        return getApplicationPrefs(context).getInt(key, -1);
    }

    public static void updateAlarmSoundUris(Context context){
        File[] files = FileUtil.getAlarmDirectoryFileList();
        if(D) { Log.d(DEBUG_TAG, files.length + " audio files found");}
        String[] fileUris;
        if (files == null || files.length == 0){
            fileUris = new String[0];
        } else {
            fileUris = new String[files.length];
            for(int i = 0; i < files.length; i++){
                if(D) { Log.d(DEBUG_TAG, "File " + i +  ":" +  files[i].getAbsolutePath());}
                fileUris[i] = files[i].getPath();
            }
        }
        Gson gson = new Gson();
        String urisJson = gson.toJson(fileUris);
        putString(context, PrefKey.ALARM_SOUND_URIS_GSON, urisJson);
    }

    public static String[] getAlarmSoundUris(Context context){
        String[] files;
        Gson gson = new Gson();
        files = gson.fromJson(getString(context, PrefKey.ALARM_SOUND_URIS_GSON), String[].class);
        return files;
    }

    public static SharedPreferences getApplicationPrefs(Context context){
        return context.getSharedPreferences(PrefKey.PREF_FILE_NAME, Activity.MODE_PRIVATE);
    }

    public static void updateAlarmTime(Context context, int hour, int minute){
        Gson gson = new Gson();
        AlarmGson alg = new AlarmGson(hour,minute);
        String urisJson = gson.toJson(alg);
        PrefUtil.putString(context, PrefKey.ALARM_GSON, urisJson);
    }

    public static AlarmGson getAlarmTimeGson(Context context){
        Gson gson = new Gson();
        AlarmGson alg;
        alg = gson.fromJson(getString(context, PrefKey.ALARM_GSON), AlarmGson.class);
        return alg;
    }

}
