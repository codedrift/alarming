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

package de.petermoesenthin.alarming.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.petermoesenthin.alarming.pref.AlarmSoundGson;

public class FileUtil
{

	public static final String DEBUG_TAG = FileUtil.class.getSimpleName();

	public static final String APP_EXT_STORAGE_FOLDER = "alarming";
	public static final String AUDIO_METADATA_FILE_EXTENSION = "alarmingmeta";

	public interface OnCopyFinishedListener
	{

		void onOperationFinished();
	}

	/**
	 * Copies a file to the Alarming directory in the external storage
	 *
	 * @param context Application context
	 * @param uri     Uri of file to copy
	 */
	public static void saveFileToExtAppStorage(final Context context, final Uri uri,
											   final OnCopyFinishedListener op)
	{
		final File applicationDirectory = getApplicationDirectory();
		if (!applicationDirectory.exists())
		{
			applicationDirectory.mkdirs();
		}
		File noMedia = new File(applicationDirectory.getPath() + File.separatorChar +
				".nomedia");
		if (!noMedia.exists())
		{
			try
			{
				noMedia.createNewFile();
				Log.e(DEBUG_TAG, "Created .nomedia file in: " + noMedia.getAbsolutePath());
			} catch (IOException e)
			{
				Log.e(DEBUG_TAG, "Unable to create .nomedia file", e);
			}
		}

		String fileName = getFilenameFromUriNoSpace(uri);
		final File destinationFile = new File(applicationDirectory.getPath() + File.separatorChar
				+ fileName);

		Log.d(DEBUG_TAG, "Source file name: " + fileName);
		Log.d(DEBUG_TAG, "Source file uri: " + uri.toString());
		Log.d(DEBUG_TAG, "Destination file: " + destinationFile.getPath());


		Thread copyThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				BufferedInputStream bis = null;
				BufferedOutputStream bos = null;
				if (isExternalStorageWritable())
				{
					try
					{
						InputStream uriStream = context.getContentResolver()
								.openInputStream(uri);
						bis = new BufferedInputStream(uriStream);
						bos = new BufferedOutputStream(new FileOutputStream(destinationFile
								.getPath(), false));
						byte[] buf = new byte[1024];
						while (bis.read(buf) != -1)
						{
							bos.write(buf);
						}
					} catch (IOException e)
					{
						Log.e(DEBUG_TAG, "Unable to copy file from URI", e);

					} finally
					{
						try
						{
							if (bis != null) bis.close();
							if (bos != null) bos.close();
						} catch (IOException e)
						{
							Log.e(DEBUG_TAG, "Unable to close buffers", e);
						}
					}
				}
				if (op != null)
				{
					op.onOperationFinished();
				}
			}
		});
		copyThread.start();
	}

	/**
	 * Checks if the external storage is writeable
	 *
	 * @return
	 */
	public static boolean isExternalStorageWritable()
	{
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state))
		{
			return true;
		}
		return false;
	}

	/**
	 * Checks if the external storage is readable
	 *
	 * @return
	 */
	public static boolean isExternalStorageReadable()
	{
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) ||
				Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
		{
			return true;
		}
		return false;
	}

	/**
	 * Returns the filename from a given path and replaces all spaces with underscores
	 *
	 * @param uri
	 * @return
	 */
	public static String getFilenameFromUriNoSpace(Uri uri)
	{
		String result = null;
		String path = uri.getPath();
		File f = getFile(path);
		result = f.getName().replaceAll("\\s", "_");
		return result;
	}

	/**
	 * Deletes a file on external storage
	 *
	 * @param path
	 */
	public static void deleteFile(String path)
	{
		File file = new File(path);
		if (file.delete())
		{
			Log.d(DEBUG_TAG, file.getName() + " is deleted!");
		} else
		{
			Log.d(DEBUG_TAG, "Delete operation is failed.");
		}
	}

	/**
	 * @param path Path of file to get
	 * @return File with given path
	 */
	public static File getFile(String path)
	{
		return new File(path);
	}

	/**
	 * @return Application directory in external storage
	 */
	public static File getApplicationDirectory()
	{
		return new File(Environment.getExternalStorageDirectory()
				.getPath() + File.separatorChar + APP_EXT_STORAGE_FOLDER + File.separatorChar);
	}

	/**
	 * Returns a file list of all non-hidden files in the application directory.
	 *
	 * @return
	 */
	public static File[] getAlarmDirectoryFileList()
	{
		return FileUtil.getApplicationDirectory().listFiles(new FileFilter()
		{
			@Override
			public boolean accept(File f)
			{
				return !f.isHidden();
			}
		});
	}

	/**
	 * Returns a file list of all non-hidden audio files in the application directory.
	 *
	 * @return
	 */
	public static File[] getAlarmDirectoryAudioFileList(final Context context)
	{
		return FileUtil.getApplicationDirectory().listFiles(new FileFilter()
		{
			@Override
			public boolean accept(File f)
			{
				// Early out if hidden
				if (f.isHidden())
				{
					return false;
				}
				String extension = FilenameUtils.getExtension(f.getPath());
				if (extension.equals(AUDIO_METADATA_FILE_EXTENSION))
				{
					return false;
				}
				return fileIsOK(context, f.getPath());
			}
		});
	}

	public static AlarmSoundGson buildBasicMetaFile(String path)
	{
		AlarmSoundGson alsg = new AlarmSoundGson();
		String[] meta = MediaUtil.getBasicMetaData(path);
		alsg.setMetaArtist(meta[0]);
		alsg.setMetaTitle(meta[1]);
		alsg.setLength(Integer.valueOf(meta[2]));
		//TODO get hash for file (not path!)
		return alsg;
	}

	/**
	 * Writes the sound configuration file based on
	 *
	 * @param soundFilePath the path of the used sound file and
	 * @param alsg          the configuration in form of a AlarmSoundGson object
	 */
	public static void writeSoundConfigurationFile(String soundFilePath, AlarmSoundGson alsg)
	{
		Log.d(DEBUG_TAG, "Writing sound configuration file");
		String configFilePath =
				getMetaFilePath(soundFilePath);
		File configFile = getFile(configFilePath);
		Gson gs = new Gson();
		String js = gs.toJson(alsg);
		try
		{
			FileUtils.write(configFile, js, "UTF-8");
		} catch (IOException e)
		{
			Log.e(DEBUG_TAG, "Could not write audio configuration file", e);
		}
	}

	/**
	 * Reads the configuration file for an alarm sound
	 *
	 * @param soundFilePath Path to the sound file since the configuration is next to it
	 * @return
	 */
	public static AlarmSoundGson readSoundConfigurationFile(String soundFilePath)
	{
		Log.d(DEBUG_TAG, "Reading sound configuration file");
		Gson gs = new Gson();
		String metaFilePath = getMetaFilePath(soundFilePath);
		File f = getFile(metaFilePath);
		String js = "";
		try
		{
			js = FileUtils.readFileToString(f, "UTF-8");
		} catch (FileNotFoundException e)
		{
			Log.e(DEBUG_TAG, "Could not find configuration file");
			return null;
		} catch (IOException e)
		{
			Log.e(DEBUG_TAG, "Unable to read file");
			return null;
		}
		return gs.fromJson(js, AlarmSoundGson.class);
	}


	public static String getMetaFilePath(String soundFilePath)
	{
		return FilenameUtils.removeExtension(soundFilePath) + "." + AUDIO_METADATA_FILE_EXTENSION;
	}


	/**
	 * Checks if a file is ok for the application to use
	 *
	 * @param context
	 * @param path
	 * @return
	 */
	public static boolean fileIsOK(Context context, String path)
	{
		String mimeType;
		mimeType = FileUtil.getMimeType(context, path);
		if (mimeType == null)
		{
			Log.d(DEBUG_TAG, "No MIME type found. Returning.");
			return false;
		}
		if (!mimeType.startsWith("audio"))
		{
			Log.d(DEBUG_TAG, "FileCheck: MIME type does not match requirements");
			return false;
		}
		try
		{
			MediaUtil.getBasicMetaData(path);
		} catch (RuntimeException e)
		{
			Log.e(DEBUG_TAG, "Cannot read file");
			return false;
		}
		return true;
	}

	/**
	 * Returns a mime type for a file in given path.
	 *
	 * @param path Path to the file.
	 * @return Null if no MIME type is found.
	 */
	public static String getMimeType(Context context, String path)
	{
		Log.d(DEBUG_TAG, "Checking mime type for " + path);
		String mimeType = null;
		String extension = FilenameUtils.getExtension(path);
		if (extension != null)
		{
			MimeTypeMap mime = MimeTypeMap.getSingleton();
			mimeType = mime.getMimeTypeFromExtension(extension);
			Log.d(DEBUG_TAG, "MimeTypeMap found " + mimeType + " for extension " + extension);
		}
		if (mimeType == null)
		{
			ContentResolver contentResolver = context.getContentResolver();
			mimeType = contentResolver.getType(Uri.parse(path));
			Log.d(DEBUG_TAG, "ContentResolver found " + mimeType + ".");
		}
		return mimeType;
	}

}
