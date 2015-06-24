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
package de.petermoesenthin.alarming.util;

import android.content.Context;

import java.util.Calendar;

public class StringUtil
{

	/**
	 * Get a zero padded string with two characters
	 * @param number
	 * @return
	 */
	public static String getZeroPaddedString(int number)
	{
		if (number >= 10)
			return String.valueOf(number);
		else
			return "0" + String.valueOf(number);
	}

	/**
	 * Get a zero padded string with two characters
	 * @param number
	 * @return
	 */
	public static String getZeroPaddedString(long number)
	{
		if (number >= 10)
			return String.valueOf(number);
		else
			return "0" + String.valueOf(number);
	}

	/**
	 * Get a time string formatted as 00:00
	 * @param hourOfDay
	 * @param minute
	 * @return
	 */
	public static String getTimeFormatted(int hourOfDay, int minute)
	{
		String alarmTimeFormatted = "";
		alarmTimeFormatted += getZeroPaddedString(hourOfDay)
				+ ":" + getZeroPaddedString(minute);
		return alarmTimeFormatted;
	}

	/**
	 * Get time formatted in the default system style
	 * @param context
	 * @param hour
	 * @param minute
	 * @return
	 */
	public static String getTimeFormattedSystem(Context context, int hour, int minute)
	{
		Calendar c = AlarmUtil.getNextAlarmTimeAbsolute(hour, minute);
		return android.text.format.DateFormat.getTimeFormat(context).format(c.getTime());
	}

	/**
	 * Returns a formatted time string in the form of (hh:)mm:ss generated from seconds
	 *
	 * @param seconds
	 * @return
	 */
	public static String getTimeFormattedFromSeconds(int seconds)
	{
		return getTimeFormattedFromMillis(seconds * 1000);
	}

	/**
	 * Returns a formatted time string in the form of (hh:)mm:ss generated from milliseconds
	 *
	 * @param millis
	 * @return
	 */
	public static String getTimeFormattedFromMillis(long millis)
	{
		long duration = millis / 1000;
		long h = duration / 3600;
		long m = (duration - h * 3600) / 60;
		long s = duration - (h * 3600 + m * 60);
		String durationString = "";
		if (h != 0)
		{
			durationString += StringUtil.getZeroPaddedString(h) + ":";
		}
		durationString += StringUtil.getZeroPaddedString(m) + ":";
		durationString += StringUtil.getZeroPaddedString(s);
		return durationString;
	}

}