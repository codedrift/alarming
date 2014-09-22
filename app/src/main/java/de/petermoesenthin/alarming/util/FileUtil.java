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

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {

    public static final String DEBUG_TAG = "FileUtil";
    private static final boolean D = false;

    public static final String APP_EXT_STORAGE_FOLDER = "alarming";

    public static void saveFileToExtAppStorage(final Context context, final Uri uri){
        final File applicationDirectory = getApplicationDirectory();
        if(!applicationDirectory.exists()){
            applicationDirectory.mkdirs();
        }
        File noMedia = new File(applicationDirectory.getPath() + File.separatorChar +
                ".nomedia");
        if(!noMedia.exists()){
            try {
                noMedia.createNewFile();
                if (D) {Log.e(DEBUG_TAG, "Created .nomedia file in: "  + noMedia.getAbsolutePath());}
            } catch (IOException e) {
                if (D) Log.e(DEBUG_TAG, "Unable to create .nomedia file", e);
            }
        }

        String fileName = getFilenameFromUri(uri);
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
                PrefUtil.updateAlarmSoundUris(context);
            }
        });
        copyThread.start();
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static String getFilenameFromUri(Uri uri){
        String result = null;
        String path = uri.getPath();
        File f = getFile(path);
        // Replace all space chars with underscores
        result = f.getName().replaceAll("\\s", "_");
        return result;
    }

    public static void deleteFile(String path){
        File file = new File(path);
        if(file.delete()){
            if(D) {Log.d(DEBUG_TAG,file.getName() + " is deleted!");}
        }else{
            if(D) {Log.d(DEBUG_TAG,"Delete operation is failed.");}
        }
    }

    public static File getFile(String path){
        return new File(path);
    }

    public static File getApplicationDirectory(){
        return new File(Environment.getExternalStorageDirectory()
                .getPath() + File.separatorChar + APP_EXT_STORAGE_FOLDER + File.separatorChar);
    }

    public static File[] getAlarmDirectoryFileList(){
        File[] files = FileUtil.getApplicationDirectory().listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return !f.isHidden();
            }
        });
        return files;
    }

    public static String getMimeType(String url){
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }

}
