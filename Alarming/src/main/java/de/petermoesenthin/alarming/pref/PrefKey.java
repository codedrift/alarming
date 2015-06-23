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

public class PrefKey {

	//----------------------------------------------------------------------------------------------
	//                                      GENERAL
	//----------------------------------------------------------------------------------------------

	public static final String PREF_FILE_NAME = "alarming_prefs";
	public static final String APP_FIRST_START = "app_first_start";
	public static final String SHOW_ALARM_NOTIFICATION = "show_alarm_notification";

	//----------------------------------------------------------------------------------------------
	//                                      ALARM
	//----------------------------------------------------------------------------------------------

	public static final String ALARM_GSON = "alarm_gson";
	public static final String ALARMS = "alarms";
	public static final String SNOOZE_TIME = "snooze_time";
	public static final String ALARM_ID_COUNTER = "alarm_id_counter";

	//----------------------------------------------------------------------------------------------
	//                                      SOUND
	//----------------------------------------------------------------------------------------------

	public static final String ALARM_SOUND_VOLUME = "alarm_sound_volume";
	public static final String AUDIO_ORIGINAL_VOLUME = "alarm_original_volume";
	public static final String ALARM_SOUND_URIS_GSON = "alarm_sound_uris";

}
