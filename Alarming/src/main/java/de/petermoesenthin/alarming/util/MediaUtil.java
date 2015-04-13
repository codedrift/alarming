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

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import de.petermoesenthin.alarming.pref.PrefKey;

public class MediaUtil {

	public static final String DEBUG_TAG = "MediaUtil";

	/**
	 * Creates an array of basic information about an audio file obtained through the
	 * MediaMetaDataRetriever. If a file does not provide MetaData, the filename will be used as
	 * the title.
	 * The array contains information as follows:
	 * [0] METADATA_KEY_ARTIST
	 * [1] METADATA_KEY_TITLE
	 * [2] METADATA_KEY_DURATION
	 *
	 * @param filePath path to the file
	 * @return String array with metadata.
	 */
	public static String[] getBasicMetaData(String filePath) {
		Log.d(DEBUG_TAG, "Reading audio metadata for " + filePath);
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		mmr.setDataSource(filePath);
		String[] metaData = new String[3];
		metaData[0] =
				mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
		metaData[1] =
				mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
		metaData[2] =
				mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
		if (metaData[0] == null) {
			metaData[0] = "-";
		}
		if (metaData[1] == null) {
			String[] pathSep = filePath.split("/");
			metaData[1] = pathSep[pathSep.length - 1];
		}
		return metaData;
	}


	/**
	 * Sets STREAM_ALARM to the user specified volume that will be used to play alarm sounds.
	 *
	 * @param context Application context.
	 */
	public static void setAlarmVolumeFromPreference(Context context) {
		int percent = PrefUtil.getInt(context, PrefKey.ALARM_SOUND_VOLUME, 80);
		int maxVolume = getAudioStreamMaxVolume(context);
		int volume = Math.round(((float) percent / 100) * maxVolume);
		Log.d(DEBUG_TAG, "Setting STREAM_ALARM volume (loaded from preference) to " + volume + " PERCENT=" + percent);
		setStreamMusicVolume(context, volume);
	}


	/**
	 * Sets STREAM_ALARM volume back to the previously set amount (user defined).
	 *
	 * @param context Application context.
	 */
	public static void resetSystemMediaVolume(Context context) {
		int originalVolume = PrefUtil.getInt(context, PrefKey.AUDIO_ORIGINAL_VOLUME, 0);
		Log.d(DEBUG_TAG, "Resetting system STREAM_ALARM volume to " + originalVolume);
		setStreamMusicVolume(context, originalVolume);
	}

	/**
	 * @param context Application context.
	 * @return AudioManger system service.
	 */
	public static AudioManager getAudioManager(Context context) {
		return (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	}


	/**
	 * Saves the current volume percentage of STREAM_ALARM.
	 *
	 * @param context Application context.
	 */
	public static void saveSystemMediaVolume(Context context) {
		int currentVolume = getAudioManager(context).getStreamVolume(AudioManager.STREAM_ALARM);
		Log.d(DEBUG_TAG, "Saving system STREAM_ALARM volume. Found " + currentVolume);
		PrefUtil.putInt(context, PrefKey.AUDIO_ORIGINAL_VOLUME, currentVolume);
	}

	/**
	 * Sets STREAM_ALARM volume to the given value.
	 *
	 * @param context Application context.
	 * @param volume  Value to set the volume to.
	 */
	public static void setStreamMusicVolume(Context context, int volume) {
		getAudioManager(context).setStreamVolume(
				AudioManager.STREAM_ALARM,
				volume,
				0);
	}

	/**
	 * @param context Application context.
	 * @return Maximum value for STREAM_ALARM volume.
	 */
	public static int getAudioStreamMaxVolume(Context context) {
		return getAudioManager(context).getStreamMaxVolume(AudioManager.STREAM_ALARM);
	}

}
