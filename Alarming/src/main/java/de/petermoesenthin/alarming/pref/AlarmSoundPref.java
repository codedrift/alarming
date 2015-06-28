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

public class AlarmSoundPref
{

	private String path = "";
	private String metaTitle = "";
	private String metaArtist = "";
	private int startMillis = 0;
	private int endMillis = 0;
	private int length = 0;
	private int hash = 0;

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public int getStartMillis()
	{
		return startMillis;
	}

	public void setStartMillis(int startMillis)
	{
		this.startMillis = startMillis;
	}

	public int getEndMillis()
	{
		return endMillis;
	}

	public void setEndMillis(int endMillis)
	{
		this.endMillis = endMillis;
	}

	public int getHash()
	{
		return hash;
	}

	public void setHash(int hash)
	{
		this.hash = hash;
	}

	public String getMetaTitle()
	{
		return metaTitle;
	}

	public void setMetaTitle(String metaTitle)
	{
		this.metaTitle = metaTitle;
	}

	public String getMetaArtist()
	{
		return metaArtist;
	}

	public void setMetaArtist(String metaArtist)
	{
		this.metaArtist = metaArtist;
	}

	public int getLength()
	{
		return length;
	}

	public void setLength(int length)
	{
		this.length = length;
	}

	@Override
	public String toString()
	{
		return "AlarmSoundGson{" +
				"path='" + path + '\'' +
				", metaTitle='" + metaTitle + '\'' +
				", metaArtist='" + metaArtist + '\'' +
				", startMillis=" + startMillis +
				", endMillis=" + endMillis +
				", length=" + length +
				", hash=" + hash +
				'}';
	}
}
