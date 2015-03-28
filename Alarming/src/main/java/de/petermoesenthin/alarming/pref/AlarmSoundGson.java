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

package de.petermoesenthin.alarming.pref;

public class AlarmSoundGson {

	private String path = "";
	private String metaTitle = "";
	private String metaArtist = "";
	private int startMillis = 0;
	private int endMillis = 0;
	private int length = 0;
	private int hash = 0;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getStartMillis() {
		return startMillis;
	}

	public void setStartMillis(int startMillis) {
		this.startMillis = startMillis;
	}

	public int getEndMillis() {
		return endMillis;
	}

	public void setEndMillis(int endMillis) {
		this.endMillis = endMillis;
	}

	public int getHash() {
		return hash;
	}

	public void setHash(int hash) {
		this.hash = hash;
	}

	public String getMetaTitle() {
		return metaTitle;
	}

	public void setMetaTitle(String metaTitle) {
		this.metaTitle = metaTitle;
	}

	public String getMetaArtist() {
		return metaArtist;
	}

	public void setMetaArtist(String metaArtist) {
		this.metaArtist = metaArtist;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}
}
