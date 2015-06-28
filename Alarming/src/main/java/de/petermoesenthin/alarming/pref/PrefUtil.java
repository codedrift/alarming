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
import android.content.res.TypedArray;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.petermoesenthin.alarming.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PrefUtil
{

	public static final String DEBUG_TAG = PrefUtil.class.getSimpleName();

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

	public static String[] getAlarmSoundUris(Context context)
	{
		String[] files;
		Gson gson = new Gson();
		files = gson.fromJson(
				getString(context, PrefKey.ALARM_SOUND_URIS_GSON, null),
				String[].class);
		return files;
	}

	public static List<AlarmPref> getAlarms(Context context)
	{
		Gson gson = new Gson();
		String js = getString(context, PrefKey.ALARMS, null);
		List<AlarmPref> alarms = gson.fromJson(js, new TypeToken<ArrayList<AlarmPref>>()
		{
		}.getType
				());
		if (alarms == null)
		{
			alarms = new ArrayList<AlarmPref>();
		}
		return alarms;
	}

	public static void setAlarms(Context context, List<AlarmPref> alarms)
	{
		Gson gson = new Gson();
		String js = gson.toJson(alarms);
		PrefUtil.putString(context, PrefKey.ALARMS, js);
	}

	public static AlarmPref getAlarmByID(List<AlarmPref> alarms, int id)
	{
		for (AlarmPref alg : alarms)
		{
			if (alg.getId() == id)
			{
				return alg;
			}
		}
		return null;
	}

	/**
	 * Get id for and alarm pref and increment it in the preferences
	 * @param context
	 * @return
	 */
	public static int getIncrementedAlarmID(Context context)
	{
		int alarmID = getInt(context, PrefKey.ALARM_ID_COUNTER, 0);
		putInt(context, PrefKey.ALARM_ID_COUNTER, alarmID + 1);
		return alarmID;
	}

	/**
	 * Get alarm gson object with incremented id and default values
	 *
	 * @param context
	 * @return
	 */
	public static AlarmPref getNewAlarmPref(Context context)
	{
		AlarmPref alarm = new AlarmPref();

		int alarmID = getIncrementedAlarmID(context);
		alarm.setId(alarmID);

		Random r = new Random();
		String[] messages = context.getResources().getStringArray(R.array.alarm_texts);
		String message = messages[r.nextInt(messages.length)];
		alarm.setMessage(message);

		TypedArray colorTypedArray = context.getResources().obtainTypedArray(R.array.alarm_default_colors);
		int[] colors = new int[colorTypedArray.length()];
		for (int i = 0; i < colorTypedArray.length(); i++) {
			colors[i] = colorTypedArray.getColor(i, 0);
		}
		alarm.setColor(colors[r.nextInt(colors.length)]);

		colorTypedArray.recycle();

		return alarm;
	}
}
