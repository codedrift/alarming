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

    //================================================================================
    // Members
    //================================================================================

    public static final String DEBUG_TAG = "PrefUtil";
    public static final boolean D = false;

    //================================================================================
    // Write
    //================================================================================

    public static void putString(Context context, String key, String value){
        getApplicationPrefs(context)
                .edit().putString(
                key,
                value
        ).commit();
    }

    public static void putLong(Context context, String key, long value){
        getApplicationPrefs(context)
                .edit().putLong(
                key,
                value
        ).commit();
    }

    public static void putBoolean(Context context, String key, boolean value){
        getApplicationPrefs(context)
                .edit().putBoolean(
                key,
                value
        ).commit();
    }

    public static void putInt(Context context, String key, int value){
        getApplicationPrefs(context)
                .edit().putInt(
                key,
                value
        ).commit();
    }

    public static void putFloat(Context context, String key, float value){
        getApplicationPrefs(context)
                .edit().putFloat(
                key,
                value
        ).commit();
    }

    //================================================================================
    // Read
    //================================================================================

    public static String getString(Context context, String key, String defaultValue){
        return getApplicationPrefs(context).getString(key, defaultValue);
    }

    public static long getLong(Context context, String key, long defaultValue){
        return getApplicationPrefs(context).getLong(key, defaultValue);
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue){
        return getApplicationPrefs(context).getBoolean(key, defaultValue);
    }

    public static int getInt(Context context, String key, int defaultValue){
        return getApplicationPrefs(context).getInt(key, defaultValue);
    }

    public static float getFloat(Context context, String key, float defaultValue){
        return getApplicationPrefs(context).getFloat(key, defaultValue);
    }

    //================================================================================
    // Helper methods
    //================================================================================

    /**
     * Get the shared preferences that are used in this application
     * @param context
     * @return
     */
    public static SharedPreferences getApplicationPrefs(Context context){
        return context.getSharedPreferences(PrefKey.PREF_FILE_NAME, Activity.MODE_PRIVATE);
    }

    public static void updateAlarmSoundUris(Context context){
        File[] files = FileUtil.getAlarmDirectoryAudioFileList(context);
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
        files = gson.fromJson(
                getString(context, PrefKey.ALARM_SOUND_URIS_GSON, null),
                String[].class);
        return files;
    }

    public static void setAlarmGson(Context context, AlarmGson alg){
        Gson gson = new Gson();
        String js = gson.toJson(alg);
        PrefUtil.putString(context, PrefKey.ALARM_GSON, js);
    }

    public static AlarmGson getAlarmGson(Context context){
        Gson gson = new Gson();
        AlarmGson alg;
        String js = getString(context, PrefKey.ALARM_GSON, null);
        alg = gson.fromJson(js, AlarmGson.class);
        if(alg == null){
            alg = new AlarmGson();
        }
        return alg;
    }

}
