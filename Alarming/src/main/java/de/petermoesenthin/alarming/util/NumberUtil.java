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

import android.util.Log;

public class NumberUtil
{

	public static final String DEBUG_TAG = "NumberUtil";

	/**
	 * Parses a long number to an integer and caps it to MIN/MAX Integer values
	 *
	 * @param number
	 * @return
	 */
	public static int parseLongToCappedInt(long number)
	{
		Log.d(DEBUG_TAG, "Parsing Long to int");
		int maxInt = Integer.MAX_VALUE;
		int minInt = Integer.MIN_VALUE;
		if (number >= maxInt)
		{
			return maxInt;
		} else if (number <= minInt)
		{
			return minInt;
		} else
		{
			return (int) number;
		}
	}
}
