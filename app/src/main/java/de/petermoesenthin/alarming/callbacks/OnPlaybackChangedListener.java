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
package de.petermoesenthin.alarming.callbacks;

import android.media.MediaPlayer;

public interface OnPlaybackChangedListener {

    /**
     * Gets called when the MediaPlayer has reached the desired position
     * @param mediaPlayer
     */
    void onPositionReached(MediaPlayer mediaPlayer);

    /**
     * Gets called when the MediaPlayer has finished a full playback
     * @param mediaPlayer
     */
    void onFullPlaybackCompleted(MediaPlayer mediaPlayer);

    /**
     * Gets called when the playback has been interrupted
     * @param mediaPlayer
     */
    void onPlaybackInterrupted(MediaPlayer mediaPlayer);
}
