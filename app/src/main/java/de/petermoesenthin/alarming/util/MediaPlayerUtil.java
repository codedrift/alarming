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

import android.media.MediaMetadataRetriever;
import android.util.Log;

import org.apache.commons.io.FilenameUtils;

public class MediaPlayerUtil {

    public static final String DEBUG_TAG = "MediaPlayerUtil";
    private static final boolean D = true;


    /**
     * Creates an array of basic information about an audio file obtained through the
     * MediaMetaDataRetriever.
     * The array contains information as follows:
     * [0] METADATA_KEY_ARTIST
     * [1] METADATA_KEY_TITLE
     * [2] METADATA_KEY_DURATION
     * @param filePath path to the file
     * @return
     */
    public static String[] getBasicMetaData(String filePath){
        if (D) {Log.d(DEBUG_TAG, "Reading audio metadata for " + filePath);}
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(filePath);
        String[] metaData = new String[3];
        metaData[0] =
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        metaData[1] =
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        metaData[2] =
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        if(metaData[0] == null){
            metaData[0] = "-";
        }
        if(metaData[1] == null){
            String[] pathSep = filePath.split("/");
            metaData[1] = pathSep[pathSep.length -1];
        }
        return metaData;
    }




}
