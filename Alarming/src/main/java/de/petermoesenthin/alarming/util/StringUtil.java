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

import android.content.Context;

import java.util.Calendar;

public class StringUtil {

	public static String getZeroPaddedString(int number) {
		if (number >= 10)
			return String.valueOf(number);
		else
			return "0" + String.valueOf(number);
	}

	public static String getZeroPaddedString(long number) {
		if (number >= 10)
			return String.valueOf(number);
		else
			return "0" + String.valueOf(number);
	}

	public static String getTimeFormatted(int hourOfDay, int minute) {
		String alarmTimeFormatted = "";
		alarmTimeFormatted += getZeroPaddedString(hourOfDay)
				+ ":" + getZeroPaddedString(minute);
		return alarmTimeFormatted;
	}

	public static String getTimeFormattedSystem(Context context, int hour, int minute) {
		Calendar c = AlarmUtil.getNextAlarmTimeAbsolute(hour, minute);
		return android.text.format.DateFormat.getTimeFormat(context).format(c.getTime());
	}

	/**
	 * Returns a formatted time string in the form of (hh:)mm:ss generated from seconds
	 *
	 * @param seconds
	 * @return
	 */
	public static String getTimeFormattedFromSeconds(int seconds) {
		return getTimeFormattedFromMillis(seconds * 1000);
	}

	/**
	 * Returns a formatted time string in the form of (hh:)mm:ss generated from milliseconds
	 *
	 * @param millis
	 * @return
	 */
	public static String getTimeFormattedFromMillis(long millis) {
		long duration = millis / 1000;
		long h = duration / 3600;
		long m = (duration - h * 3600) / 60;
		long s = duration - (h * 3600 + m * 60);
		String durationString = "";
		if (h != 0) {
			durationString += StringUtil.getZeroPaddedString(h) + ":";
		}
		durationString += StringUtil.getZeroPaddedString(m) + ":";
		durationString += StringUtil.getZeroPaddedString(s);
		return durationString;
	}

}