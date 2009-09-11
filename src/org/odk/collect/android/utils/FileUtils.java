/*
 * Copyright (C) 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.utils;

import org.odk.collect.android.db.FileDbAdapter;
import org.odk.collect.android.logic.GlobalConstants;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Static methods used for common file operations.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * 
 */
public class FileUtils {
    private final static String t = "FileUtils";


    public static ArrayList<String> getFoldersAsArrayList(String path) {
        ArrayList<String> mFolderList = new ArrayList<String>();
        File root = new File(path);

        if (!storageReady()) {
            return null;
        }
        if (!root.exists()) {
            if (!createFolder(path)) {
                return null;
            }
        }
        if (root.isDirectory()) {
            File[] children = root.listFiles();
            for (File child : children) {
                boolean directory = child.isDirectory();
                String filename = child.getName();
                if (directory) {
                    mFolderList.add(child.getAbsolutePath());
                }
            }
        }
        return mFolderList;
    }


    public static ArrayList<String> getFilesAsArrayList(String path) {
        ArrayList<String> mFileList = new ArrayList<String>();
        File root = new File(path);

        if (!storageReady()) {
            return null;
        }
        if (!root.exists()) {
            if (!createFolder(path)) {
                return null;
            }
        }
        if (root.isDirectory()) {
            File[] children = root.listFiles();
            for (File child : children) {
                String filename = child.getName();
                // no hidden files
                if (!filename.startsWith(".")) {
                    mFileList.add(child.getAbsolutePath());
                }
            }
        } else {
            String filename = root.getName();
            // no hidden files
            if (!filename.startsWith(".")) {
                mFileList.add(root.getAbsolutePath());
            }
        }
        return mFileList;
    }


    public static ArrayList<String> getFilesAsArrayListRecursive(String path) {
        ArrayList<String> mFileList = new ArrayList<String>();
        File root = new File(path);
        getFilesAsArrayListRecursiveHelper(root, mFileList);
        return mFileList;
    }


    private static void getFilesAsArrayListRecursiveHelper(File f, ArrayList<String> filelist) {
        if (f.isDirectory()) {
            File[] childs = f.listFiles();
            for (File child : childs) {
                getFilesAsArrayListRecursiveHelper(child, filelist);
            }
            return;
        }
        filelist.add(f.getAbsolutePath());
    }


    public static boolean deleteFolder(String path) {
        // not recursive
        if (path != null && storageReady()) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                for (File file : files) {
                    if (!file.delete()) {
                        Log.i(t, "Failed to delete " + file);
                    }
                }
            }
            return dir.delete();
        } else {
            return false;
        }
    }


    public static boolean createFolder(String path) {
        if (storageReady()) {
            boolean made = true;
            File dir = new File(path);
            if (!dir.exists()) {
                made = dir.mkdirs();
            }
            return made;
        } else {
            return false;
        }
    }


    public static boolean deleteFile(String path) {
        if (storageReady()) {
            File f = new File(path);
            return f.delete();
        } else {
            return false;
        }
    }


    public static byte[] getFileAsBytes(File file) {

        byte[] bytes = null;
        InputStream is = null;
        try {
            is = new FileInputStream(file);

            // Get the size of the file
            long length = file.length();
            if (length > Integer.MAX_VALUE) {
                Log.e(t, "File " + file.getName() + "is too large");
                return null;
            }

            // Create the byte array to hold the data
            bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int read = 0;
            try {
                while (offset < bytes.length && read >= 0) {
                    read = is.read(bytes, offset, bytes.length - offset);
                    offset += read;
                }
            } catch (IOException e) {
                Log.e(t, "Cannot read " + file.getName());
                e.printStackTrace();
                return null;
            }

            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                try {
                    throw new IOException("Could not completely read file " + file.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            return bytes;

        } catch (FileNotFoundException e) {
            Log.e(t, "Cannot find " + file.getName());
            e.printStackTrace();
            return null;

        } finally {
            // Close the input stream
            try {
                is.close();
            } catch (IOException e) {
                Log.e(t, "Cannot close input stream for " + file.getName());
                e.printStackTrace();
                return null;
            }
        }



    }


    private static boolean storageReady() {
        String cardstatus = Environment.getExternalStorageState();
        if (cardstatus.equals(Environment.MEDIA_REMOVED)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTABLE)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTED)
                || cardstatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            return false;
        } else {
            return true;
        }
    }


    public static String getMd5Hash(File file) {

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(getFileAsBytes(file));
            BigInteger number = new BigInteger(1, messageDigest);
            String md5 = number.toString(16);
            while (md5.length() < 32)
                md5 = "0" + md5;
            return md5;

        } catch (NoSuchAlgorithmException e) {
            Log.e("MD5", e.getMessage());
            return null;

        }
    }


    public static void removeOrphans(Context context) {

        if (createFolder(GlobalConstants.FORMS_PATH)
                && createFolder(GlobalConstants.INSTANCES_PATH)) {

            // full path to the files
            ArrayList<String> storedForms =
                    FileUtils.getFilesAsArrayList(GlobalConstants.FORMS_PATH);
            ArrayList<String> storedInstances =
                    FileUtils.getFilesAsArrayListRecursive(GlobalConstants.INSTANCES_PATH);
            HashMap<String, String> availableFiles = getAvailableFiles(context);

            final FileDbAdapter fda = new FileDbAdapter(context);
            fda.open();

            // loop through forms on sdcard. add and remove as necessary.
            if (storedForms != null) {
                for (String formPath : storedForms) {

                    String hash = FileUtils.getMd5Hash(new File(formPath));
                    // if hash is not in db, remove the form.
                    if (!availableFiles.containsKey(hash) && !(new File(formPath)).delete()) {
                        Log.i(t, "Failed to delete " + formPath);
                    }

                    // if duplicate form found on sd card, remove it.
                    if (availableFiles.containsKey(hash)
                            && !formPath.equals(availableFiles.get(hash))
                            && !(new File(formPath)).delete()) {
                        Log.i(t, "Failed to delete " + formPath);
                    }

                }
            }

            if (storedInstances != null) {
                for (String instancePath : storedInstances) {
                    // if path is not in db, remove the instance folder.
                    if (instancePath.endsWith(".xml")) {
                        String hash = FileUtils.getMd5Hash(new File(instancePath));
                        if (!availableFiles.containsKey(hash)
                                && !deleteFolder((new File(instancePath)).getParent())) {
                            Log.i(t, "Failed to delete " + instancePath);
                        }
                    }

                }
            }

            fda.close();
        }
    }


    private static HashMap<String, String> getAvailableFiles(Context context) {

        if (createFolder(GlobalConstants.CACHE_PATH)) {

            // the hashes of the forms in the db
            HashMap<String, String> availableFiles = new HashMap<String, String>();
            ArrayList<String> cachedForms =
                    FileUtils.getFilesAsArrayList(GlobalConstants.CACHE_PATH);

            final FileDbAdapter fda = new FileDbAdapter(context);
            fda.open();

            // find all forms in database and grab all their hashes and
            // filenames
            Cursor c = null;
            c = fda.fetchAllFiles();
            if (c != null) {
                if (c.moveToFirst()) {
                    int i = c.getColumnIndex(FileDbAdapter.KEY_HASH);
                    int j = c.getColumnIndex(FileDbAdapter.KEY_FILEPATH);
                    do {
                        availableFiles.put(c.getString(i), c.getString(j));
                    } while (c.moveToNext());
                }
            }
            // clean up cursor
            if (c != null) {
                c.close();
            }

            // remove orphaned form defs
            if (cachedForms != null) {
                for (String cachePath : cachedForms) {
                    String hash =
                            cachePath.substring(cachePath.lastIndexOf("/") + 1, cachePath
                                    .lastIndexOf("."));
                    if (!availableFiles.containsKey(hash) && !(new File(cachePath)).delete()) {
                        Log.i(t, "Failed to delete " + cachePath);
                    }
                }
            }

            fda.close();

            return availableFiles;
        } else {
            return null;
        }
    }


    /**
     * Stores new forms in the database
     */
    public static void addOrphans(Context context) {

        if (createFolder(GlobalConstants.FORMS_PATH)) {
            // full path to the files
            ArrayList<String> storedForms =
                    FileUtils.getFilesAsArrayList(GlobalConstants.FORMS_PATH);
            HashMap<String, String> availableFiles = getAvailableFiles(context);

            final FileDbAdapter fda = new FileDbAdapter(context);
            fda.open();

            // loop through forms on sdcard. add and remove as necessary.
            if (storedForms != null) {
                for (String formPath : storedForms) {
                    String hash = FileUtils.getMd5Hash(new File(formPath));
                    // if hash is not in db, add the form.
                    if (!availableFiles.containsKey((hash))) {
                        fda.createFile(formPath, FileDbAdapter.TYPE_FORM,
                                FileDbAdapter.STATUS_AVAILABLE);
                    }
                }
            }

            // clean up adapter
            fda.close();

            removeOrphans(context);


        }
    }


}
