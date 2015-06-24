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

public class AlarmGson
{

	private int hour = 0;
	private int minute = 0;
	private boolean alarmSet = false;
	private boolean repeatAlarm = false;
	private boolean vibrate = true;
	private String message = "";
	private int color = -1;
	private int id = 0;

	public AlarmGson()
	{
	}

	public int getHour()
	{
		return hour;
	}

	public void setHour(int hour)
	{
		this.hour = hour;
	}

	public int getMinute()
	{
		return minute;
	}

	public void setMinute(int minute)
	{
		this.minute = minute;
	}

	public boolean isAlarmSet()
	{
		return alarmSet;
	}

	public void setAlarmSet(boolean alarmSet)
	{
		this.alarmSet = alarmSet;
	}

	public boolean doesVibrate()
	{
		return vibrate;
	}

	public void setVibrate(boolean vibrate)
	{
		this.vibrate = vibrate;
	}

	public boolean doesRepeat()
	{
		return repeatAlarm;
	}

	public void setRepeat(boolean repeatAlarm)
	{
		this.repeatAlarm = repeatAlarm;
	}

	public int getColor()
	{
		return color;
	}

	public void setColor(int color)
	{
		this.color = color;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}


	@Override
	public String toString()
	{
		return "AlarmGson{" +
				"hour=" + hour +
				", minute=" + minute +
				", alarmSet=" + alarmSet +
				", repeatAlarm=" + repeatAlarm +
				", vibrate=" + vibrate +
				", message='" + message + '\'' +
				", color=" + color +
				", id=" + id +
				'}';
	}
}
