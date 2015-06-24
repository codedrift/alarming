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
package de.petermoesenthin.alarming.ui;

import android.content.Context;

import de.petermoesenthin.alarming.R;
import de.psdev.licensesdialog.licenses.License;

public class OpenFontLicense extends License
{

	private static final long serialVersionUID = 4854000061990891449L;

	@Override
	public String getName()
	{
		return "SIL OPEN FONT LICENSE Version 1.1";
	}

	@Override
	public String getSummaryText(Context context)
	{
		return getContent(context, R.raw.ofl_1_1);
	}

	@Override
	public String getFullText(Context context)
	{
		return getContent(context, R.raw.ofl_1_1);
	}

	@Override
	public String getVersion()
	{
		return "1.1";
	}

	@Override
	public String getUrl()
	{
		return "http://scripts.sil.org/OFL";
	}
}
