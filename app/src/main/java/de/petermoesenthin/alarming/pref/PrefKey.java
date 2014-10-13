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

package de.petermoesenthin.alarming.pref;

public class PrefKey {

    //================================================================================
    // General
    //================================================================================

    public static final String PREF_FILE_NAME = "alarming_prefs";
    public static final String APP_FIRST_START = "app_first_start";
    public static final String SHOW_ALARM_NOTIFICATION = "show_alarm_notification";

    //================================================================================
    // Alarm
    //================================================================================

    public static final String ALARM_GSON = "alarm_gson";
    public static final String SNOOZE_TIME = "snooze_time";

    //================================================================================
    // Sound
    //================================================================================

    public static final String ALARM_SOUND_VOLUME = "alarm_sound_volume";
    public static final String AUDIO_ORIGINAL_VOLUME = "alarm_original_volume";
    public static final String ALARM_SOUND_URIS_GSON = "alarm_sound_uris";

}
