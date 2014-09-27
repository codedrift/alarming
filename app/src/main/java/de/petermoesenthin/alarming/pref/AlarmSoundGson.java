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

public class AlarmSoundGson {

    private String path;
    private int startTimeMillis;
    private int endTimeMillis;
    private int pathHash = 0;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getStartTimeMillis() {
        return startTimeMillis;
    }

    public void setStartTimeMillis(int startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
    }

    public int getEndTimeMillis() {
        return endTimeMillis;
    }

    public void setEndTimeMillis(int endTimeMillis) {
        this.endTimeMillis = endTimeMillis;
    }

    public int getPathHash() {
        return pathHash;
    }

    public void setPathHash(int pathHash) {
        this.pathHash = pathHash;
    }

}
