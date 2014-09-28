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

import de.petermoesenthin.alarming.callbacks.OperationFinishedListener;
import de.petermoesenthin.alarming.pref.AlarmSoundGson;

public class FileUtil {

    public static final String DEBUG_TAG = "FileUtil";
    private static final boolean D = true;

    public static final String APP_EXT_STORAGE_FOLDER = "alarming";
    public static final String AUDIO_METADATA_FILE_EXTENSION = ".alarmingmeta";

    /**
     * Copies a file to the Alarming directory in the external storage
     * @param context Application context
     * @param uri Uri of file to copy
     */
    public static void saveFileToExtAppStorage(final Context context, final Uri uri,
                                               final OperationFinishedListener op){
        final File applicationDirectory = getApplicationDirectory();
        if(!applicationDirectory.exists()){
            applicationDirectory.mkdirs();
        }
        File noMedia = new File(applicationDirectory.getPath() + File.separatorChar +
                ".nomedia");
        if(!noMedia.exists()){
            try {
                noMedia.createNewFile();
                if (D) {Log.e(DEBUG_TAG, "Created .nomedia file in: " + noMedia.getAbsolutePath());}
            } catch (IOException e) {
                if (D) Log.e(DEBUG_TAG, "Unable to create .nomedia file", e);
            }
        }

        String fileName = getFilenameFromUriNoSpace(uri);
        final File destinationFile = new File(applicationDirectory.getPath() + File.separatorChar
                + fileName);

        if (D) { Log.d(DEBUG_TAG, "Source file name: " + fileName);}
        if (D) { Log.d(DEBUG_TAG, "Source file uri: " + uri.toString());}
        if (D) { Log.d(DEBUG_TAG, "Destination file: " + destinationFile.getPath());}

        Thread copyThread = new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedInputStream bis = null;
                BufferedOutputStream bos = null;
                if(isExternalStorageWritable()) {
                    try {
                        InputStream uriStream = context.getContentResolver()
                                .openInputStream(uri);
                        bis = new BufferedInputStream(uriStream);
                        bos = new BufferedOutputStream(new FileOutputStream(destinationFile
                                .getPath(),false));
                        byte[] buf = new byte[1024];
                        while (bis.read(buf) != -1){
                            bos.write(buf);
                        }
                    } catch (IOException e) {
                        if (D) Log.e(DEBUG_TAG, "Unable to copy file from URI", e);

                    } finally {
                        try {
                            if (bis != null) bis.close();
                            if (bos != null) bos.close();
                        } catch (IOException e) {
                            if (D) Log.e(DEBUG_TAG, "Unable to close buffers", e);
                        }
                    }
                }
                if(op != null){
                    op.onOperationFinished();
                }
            }
        });
        copyThread.start();
    }

    /**
     * Checks if the external storage is writeable
     * @return
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the external storage is readable
     * @return
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Returns the filename from a given path and replaces all spaces with underscores
     * @param uri
     * @return
     */
    public static String getFilenameFromUriNoSpace(Uri uri){
        String result = null;
        String path = uri.getPath();
        File f = getFile(path);
        result = f.getName().replaceAll("\\s", "_");
        return result;
    }

    /**
     * Deletes a file on external storage
     * @param path
     */
    public static void deleteFile(String path){
        File file = new File(path);
        if(file.delete()){
            if(D) {Log.d(DEBUG_TAG,file.getName() + " is deleted!");}
        }else{
            if(D) {Log.d(DEBUG_TAG,"Delete operation is failed.");}
        }
    }

    /**
     * @param path Path of file to get
     * @return File with given path
     */
    public static File getFile(String path){
        return new File(path);
    }

    /**
     * @return Application directory in external storage
     */
    public static File getApplicationDirectory(){
        return new File(Environment.getExternalStorageDirectory()
                .getPath() + File.separatorChar + APP_EXT_STORAGE_FOLDER + File.separatorChar);
    }

    /**
     * Returns a file list of all non-hidden files in the application directory.
     * @return
     */
    public static File[] getAlarmDirectoryFileList(){
        return FileUtil.getApplicationDirectory().listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return !f.isHidden();
            }
        });
    }

    /**
     *  Returns a file list of all non-hidden audio files in the application directory.
     * @return
     */
    public static File[] getAlarmDirectoryAudioFileList(){
        return FileUtil.getApplicationDirectory().listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                boolean fileAccepted = true;
                // Early out if hidden
                if(f.isHidden()){
                    return false;
                }
                //Check for mime type
                String mimeType;
                mimeType = getMimeType(f.getPath());
                if(mimeType == null){
                    return false;
                }
                if(!mimeType.startsWith("audio")){
                    fileAccepted = false;
                }
                return fileAccepted;
            }
        });
    }

    /**
     * Writes the sound configuration file based on
     * @param soundFilePath the path of the used sound file and
     * @param alsg the configuration in form of a AlarmSoundGson object
     */
    public static void writeSoundConfigurationFile(String soundFilePath, AlarmSoundGson alsg){
        if (D) {Log.d(DEBUG_TAG, "Writing sound configuration file");}
        String configFilePath =
                getMetaFilePath(soundFilePath);
        File configFile = getFile(configFilePath);
        Gson gs = new Gson();
        String js = gs.toJson(alsg);
        try {
            FileUtils.write(configFile, js, "UTF-8");
        } catch (IOException e) {
            if (D) {Log.e(DEBUG_TAG, "Could not write audio configuration file",e);}
        }
    }

    /**
     * Reads the configuration file for an alarm sound
     * @param soundFilePath Path to the sound file since the configuration is next to it
     * @return
     */
    public static AlarmSoundGson readSoundConfigurationFile(String soundFilePath){
        if (D) {Log.d(DEBUG_TAG, "Reading sound configuration file");}
        Gson gs = new Gson();
        String metaFilePath = getMetaFilePath(soundFilePath);
        File f = getFile(metaFilePath);
        String js = "";
        try {
            js = FileUtils.readFileToString(f, "UTF-8");
        }catch (FileNotFoundException e){
            if (D)  Log.e(DEBUG_TAG, "Could not find configuration file");
            return null;
        }catch (IOException e) {
            if (D)  Log.e(DEBUG_TAG, "Unable to read file");
            return null;
        }
        return gs.fromJson(js,AlarmSoundGson.class);
    }


    public static String getMetaFilePath(String soundFilePath){
        return FilenameUtils.removeExtension(soundFilePath) + AUDIO_METADATA_FILE_EXTENSION;
    }

    /**
     * Returns a mime type for a file in given path.
     * @param path Path to the file.
     * @return Null if no extension is found.
     */
    public static String getMimeType(String path){
        if (D) {Log.d(DEBUG_TAG, "Checking mime type for " + path);}
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
            if (D)  Log.d(DEBUG_TAG, "MIME type is " + type  + " for " + path);
        }
        return type;
    }

}
