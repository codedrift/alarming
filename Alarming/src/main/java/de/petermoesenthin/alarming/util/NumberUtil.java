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

package de.petermoesenthin.alarming.util;

import android.util.Log;

public class NumberUtil {

	public static final String DEBUG_TAG = "NumberUtil";

	/**
	 * Parses a long number to an integer and caps it to MIN/MAX Integer values
	 *
	 * @param number
	 * @return
	 */
	public static int parseLongToCappedInt(long number) {
		Log.d(DEBUG_TAG, "Parsing Long to int");
		int maxInt = Integer.MAX_VALUE;
		int minInt = Integer.MIN_VALUE;
		if (number >= maxInt) {
			return maxInt;
		} else if (number <= minInt) {
			return minInt;
		} else {
			return (int) number;
		}
	}
}
