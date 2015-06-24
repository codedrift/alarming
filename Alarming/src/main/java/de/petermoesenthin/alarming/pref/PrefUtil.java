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
package de.petermoesenthin.alarming.pref;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.petermoesenthin.alarming.util.FileUtil;

public class PrefUtil
{

	public static final String DEBUG_TAG = "PrefUtil";

	//----------------------------------------------------------------------------------------------
	//                                      WRITE
	//----------------------------------------------------------------------------------------------

	public static void putString(Context context, String key, String value)
	{
		getApplicationPrefs(context)
				.edit().putString(
				key,
				value
		).commit();
	}

	public static void putLong(Context context, String key, long value)
	{
		getApplicationPrefs(context)
				.edit().putLong(
				key,
				value
		).commit();
	}

	public static void putBoolean(Context context, String key, boolean value)
	{
		getApplicationPrefs(context)
				.edit().putBoolean(
				key,
				value
		).commit();
	}

	public static void putInt(Context context, String key, int value)
	{
		getApplicationPrefs(context)
				.edit().putInt(
				key,
				value
		).commit();
	}

	public static void putFloat(Context context, String key, float value)
	{
		getApplicationPrefs(context)
				.edit().putFloat(
				key,
				value
		).commit();
	}

	//----------------------------------------------------------------------------------------------
	//                                      READ
	//----------------------------------------------------------------------------------------------

	public static String getString(Context context, String key, String defaultValue)
	{
		return getApplicationPrefs(context).getString(key, defaultValue);
	}

	public static long getLong(Context context, String key, long defaultValue)
	{
		return getApplicationPrefs(context).getLong(key, defaultValue);
	}

	public static boolean getBoolean(Context context, String key, boolean defaultValue)
	{
		return getApplicationPrefs(context).getBoolean(key, defaultValue);
	}

	public static int getInt(Context context, String key, int defaultValue)
	{
		return getApplicationPrefs(context).getInt(key, defaultValue);
	}

	public static float getFloat(Context context, String key, float defaultValue)
	{
		return getApplicationPrefs(context).getFloat(key, defaultValue);
	}

	//----------------------------------------------------------------------------------------------
	//                                      HELPER METHODS
	//----------------------------------------------------------------------------------------------

	/**
	 * Get the shared preferences that are used in this application
	 *
	 * @param context
	 * @return
	 */
	public static SharedPreferences getApplicationPrefs(Context context)
	{
		return context.getSharedPreferences(PrefKey.PREF_FILE_NAME, Activity.MODE_PRIVATE);
	}

	public static void updateAlarmSoundUris(Context context)
	{
		File[] files = FileUtil.getAlarmDirectoryAudioFileList(context);
		String[] fileUris;
		if (files == null || files.length == 0)
		{
			Log.d(DEBUG_TAG, "No audio files found");
			fileUris = new String[0];
		} else
		{
			fileUris = new String[files.length];
			for (int i = 0; i < files.length; i++)
			{
				Log.d(DEBUG_TAG, "Found file " + i + ":" + files[i].getAbsolutePath());
				fileUris[i] = files[i].getPath();
			}
		}
		Gson gson = new Gson();
		String urisJson = gson.toJson(fileUris);
		putString(context, PrefKey.ALARM_SOUND_URIS_GSON, urisJson);
	}

	public static String[] getAlarmSoundUris(Context context)
	{
		String[] files;
		Gson gson = new Gson();
		files = gson.fromJson(
				getString(context, PrefKey.ALARM_SOUND_URIS_GSON, null),
				String[].class);
		return files;
	}

	/**
	 * Get alarm Gson with incremented id
	 *
	 * @param context
	 * @return
	 */
	public static AlarmGson getNewAlarmGson(Context context)
	{
		AlarmGson alarm = new AlarmGson();
		int alarmID = PrefUtil.getInt(context, PrefKey.ALARM_ID_COUNTER, 0);
		alarm.setId(alarmID);
		PrefUtil.putInt(context, PrefKey.ALARM_ID_COUNTER, alarmID + 1);
		return alarm;
	}

	public static List<AlarmGson> getAlarms(Context context)
	{
		Gson gson = new Gson();
		String js = getString(context, PrefKey.ALARMS, null);
		List<AlarmGson> alarms = gson.fromJson(js, new TypeToken<ArrayList<AlarmGson>>()
		{
		}.getType
				());
		if (alarms == null)
		{
			alarms = new ArrayList<AlarmGson>();
		}
		return alarms;
	}

	public static void setAlarms(Context context, List<AlarmGson> alarms)
	{
		Gson gson = new Gson();
		String js = gson.toJson(alarms);
		PrefUtil.putString(context, PrefKey.ALARMS, js);
	}

	public static AlarmGson findAlarmWithID(List<AlarmGson> alarms, int id)
	{
		for (AlarmGson alg : alarms)
		{
			if (alg.getId() == id)
			{
				return alg;
			}
		}
		return null;
	}

}
