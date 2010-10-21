/*
 * Copyright (C) 2009 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.database;

import org.odk.collect.android.utilities.FileUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Manages the files the application uses.
 * 
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class FileDbAdapter {

    private final static String t = "FileDbAdapter";

    // database columns
    public static final String KEY_ID = "_id";
    public static final String KEY_FILEPATH = "path";
    public static final String KEY_HASH = "hash";
    public static final String KEY_TYPE = "type";
    public static final String KEY_STATUS = "status";
    public static final String KEY_DISPLAY = "display";
    public static final String KEY_META = "meta";

    // file types
    public static final String TYPE_FORM = "form";
    public static final String TYPE_INSTANCE = "instance";

    // status for instances
    public static final String STATUS_INCOMPLETE = "incomplete";
    public static final String STATUS_COMPLETE = "complete";
    public static final String STATUS_SUBMITTED = "submitted";

    // status for forms
    public static final String STATUS_AVAILABLE = "available";

    private static final String added = "Added";
    private static final String saved = "Saved";
    private static final String finished = "Finished";
    private static final String submitted = "Submitted";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_CREATE =
        "create table files (_id integer primary key autoincrement, " + "path text not null, "
                + "hash text not null, " + "type text not null, " + "status text not null, "
                + "display text not null, " + "meta text not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "files";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_PATH = Environment.getExternalStorageDirectory()
            + "/odk/metadata";

    private static class DatabaseHelper extends ODKSQLiteOpenHelper {

        DatabaseHelper() {
            super(DATABASE_PATH, DATABASE_NAME, null, DATABASE_VERSION);

            // Create database storage directory if it doesn't not already exist.
            File f = new File(DATABASE_PATH);
            f.mkdirs();
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }


        @Override
        // upgrading will destroy all old data
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }


    public FileDbAdapter() {
    }


    public FileDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper();
        mDb = mDbHelper.getWritableDatabase();
        cleanFiles();

        return this;
    }


    public void close() {
        mDbHelper.close();
        mDb.close();
    }


    /**
     * Generate text for the second level of the row display.
     * 
     * @param timestamp date modified of the file
     * @return date modified of the file formatted as human readable string
     */
    private String generateMeta(Long timestamp, String status) {
        String tag = added;
        if (status.equals(STATUS_SUBMITTED)) {
            tag = submitted;
        } else if (status.equals(STATUS_INCOMPLETE)) {
            tag = saved;
        } else if (status.equals(STATUS_COMPLETE)) {
            tag = finished;
        }
        String ts =
            new SimpleDateFormat("EEE, MMM dd, yyyy 'at' HH:mm").format(new Date(timestamp));
        return tag + " on " + ts;
    }


    /**
     * Generate text for the first level of the row display.
     * 
     * @param path path to the file
     * @param type type of the file
     * @return name of the file formatted as human readable string
     */
    private String generateDisplay(String path, String type) {
        String filename = path.substring(path.lastIndexOf("/") + 1);

        if (type.equals(TYPE_INSTANCE)) {
            // remove time stamp from instance
            String r = "\\_[0-9]{4}\\-[0-9]{2}\\-[0-9]{2}\\_[0-9]{2}\\-[0-9]{2}\\-[0-9]{2}\\.xml$";
            Pattern p = Pattern.compile(r);
            return p.split(filename)[0] + " " + "Data";
        } else if (type.equals(TYPE_FORM)) {
            // remove extension from form
            try {
                return filename.substring(0, filename.lastIndexOf(".")) + " " + "Form";

            } catch (StringIndexOutOfBoundsException e) {
                return path;
            }
        } else {
            return filename;
        }
    }


    /**
     * Insert file into the database.
     * 
     * @param path path to the file
     * @param type type of the file
     * @param status status of the file
     * @return id of the new file
     */
    public long createFile(String path, String type, String status) {
        File f = new File(path);
        ContentValues cv = new ContentValues();

        cv.put(KEY_FILEPATH, f.getAbsolutePath());
        cv.put(KEY_TYPE, type);
        cv.put(KEY_STATUS, status);

        // create md5 hash of the file
        cv.put(KEY_HASH, FileUtils.getMd5Hash(f));

        // first row of the row display
        cv.put(KEY_DISPLAY, generateDisplay(f.getAbsolutePath(), type));

        // second row of the row display
        cv.put(KEY_META, generateMeta(f.lastModified(), status));

        long id = -1;
        try {
            id = mDb.insert(DATABASE_TABLE, null, cv);
        } catch (SQLiteConstraintException e) {
            Log.e(t, "Caught SQLiteConstraitException: " + e);
        }

        return id;
    }


    /**
     * Remove the file from the database.
     * 
     * @param id row id
     * @return number of affected rows
     */
    public boolean deleteFile(long id) {
        return mDb.delete(DATABASE_TABLE, KEY_ID + "='" + id + "'", null) > 0;
    }


    /**
     * Remove the file from the database.
     * 
     * @param path path to the file
     * @return number of affected rows
     */
    public boolean deleteFile(String path, String hash) {
        if (hash == null) {
            return mDb.delete(DATABASE_TABLE, KEY_FILEPATH + "='" + path + "'", null) > 0;
        } else if (path == null) {
            return mDb.delete(DATABASE_TABLE, KEY_HASH + "='" + hash + "'", null) > 0;
        } else {
            return mDb.delete(DATABASE_TABLE, KEY_FILEPATH + "='" + path + "'" + " and " + KEY_HASH
                    + "='" + hash + "'", null) > 0;
        }
    }


    /**
     * Get a cursor to a multiple files from the database.
     * 
     * @param path path to the file
     * @param hash hash of the file
     * @return cursor to the file
     * @throws SQLException
     */
    public Cursor fetchFilesByPath(String path, String hash) throws SQLException {
        Cursor c = null;
        if (path == null) {
            // no path given, search using hash
            c = mDb.query(true, DATABASE_TABLE, new String[] {
                    KEY_ID, KEY_FILEPATH, KEY_HASH, KEY_TYPE, KEY_STATUS, KEY_DISPLAY, KEY_META
            }, KEY_HASH + "='" + hash + "'", null, null, null, null, null);
        } else if (hash == null) {
            // no hash given, search using path
            c = mDb.query(true, DATABASE_TABLE, new String[] {
                    KEY_ID, KEY_FILEPATH, KEY_HASH, KEY_TYPE, KEY_STATUS, KEY_DISPLAY, KEY_META
            }, KEY_FILEPATH + "='" + path + "'", null, null, null, null, null);
        } else {
            // search using path and hash
            c =
                mDb.query(true, DATABASE_TABLE, new String[] {
                        KEY_ID, KEY_FILEPATH, KEY_HASH, KEY_TYPE, KEY_STATUS, KEY_DISPLAY, KEY_META
                }, KEY_FILEPATH + "='" + path + "' and " + KEY_HASH + "='" + hash + "'", null,
                    null, null, null, null);
        }
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }


    public Cursor fetchFile(long id) throws SQLException {
        Cursor c = mDb.query(true, DATABASE_TABLE, new String[] {
                KEY_ID, KEY_FILEPATH, KEY_HASH, KEY_TYPE, KEY_STATUS, KEY_DISPLAY, KEY_META
        }, KEY_ID + "='" + id + "'", null, null, null, null, null);

        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }


    /**
     * Get a cursor to multiple files from the database.
     * 
     * @param status status of the file
     * @param type type of the file
     * @return cursor to the files
     * @throws SQLException
     */
    public Cursor fetchFilesByType(String type, String status) throws SQLException {
        // cleanFiles();

        Cursor c = null;
        if (type == null) {
            // no type given, search using status
            c = mDb.query(true, DATABASE_TABLE, new String[] {
                    KEY_ID, KEY_FILEPATH, KEY_HASH, KEY_TYPE, KEY_STATUS, KEY_DISPLAY, KEY_META
            }, KEY_STATUS + "='" + status + "'", null, null, null, null, null);
        } else if (status == null) {
            // no status given, search using type
            c = mDb.query(true, DATABASE_TABLE, new String[] {
                    KEY_ID, KEY_FILEPATH, KEY_HASH, KEY_TYPE, KEY_STATUS, KEY_DISPLAY, KEY_META
            }, KEY_TYPE + "='" + type + "'", null, null, null, null, null);
        } else {
            // search using type and status
            c =
                mDb.query(true, DATABASE_TABLE, new String[] {
                        KEY_ID, KEY_FILEPATH, KEY_HASH, KEY_TYPE, KEY_STATUS, KEY_DISPLAY, KEY_META
                }, KEY_TYPE + "='" + type + "' and " + KEY_STATUS + "='" + status + "'", null,
                    null, null, null, null);
        }

        if (c != null) {
            c.moveToFirst();
        }
        return c;

    }


    public Cursor fetchAllFiles() throws SQLException {
        // cleanFiles();
        Cursor c = null;
        c = mDb.query(true, DATABASE_TABLE, new String[] {
                KEY_ID, KEY_FILEPATH, KEY_HASH, KEY_TYPE, KEY_STATUS, KEY_DISPLAY, KEY_META
        }, null, null, null, null, null, null);

        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }


    /**
     * Update file in the database. Updates the date modified.
     * 
     * @param path path to the file
     * @param status status of the file
     * @return number of affected rows
     */
    public boolean updateFile(String path, String status) {
        File f = new File(path);
        ContentValues cv = new ContentValues();

        cv.put(KEY_FILEPATH, f.getAbsolutePath());
        cv.put(KEY_HASH, FileUtils.getMd5Hash(f));
        cv.put(KEY_STATUS, status);
        cv.put(KEY_META, generateMeta(new Date().getTime(), status));

        return mDb.update(DATABASE_TABLE, cv, KEY_FILEPATH + "='" + path + "'", null) > 0;
    }


    /**
     * Find orphaned files on the file system
     */
    public void cleanFiles() {
        Cursor c = mDb.query(DATABASE_TABLE, new String[] {
                KEY_ID, KEY_FILEPATH, KEY_HASH, KEY_TYPE, KEY_STATUS
        }, null, null, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                String path = c.getString(c.getColumnIndex(KEY_FILEPATH));
                // String hash = c.getString(c.getColumnIndex(KEY_HASH));
                // String type = c.getString(c.getColumnIndex(KEY_TYPE));

                File f = new File(path);
                if (!f.exists()) {
                    // delete entry for file not on sd
                    deleteFile(path, null);
                }
            }
            c.close();
        }
    }


    public void removeOrphanFormDefs() {
        if (FileUtils.createFolder(FileUtils.CACHE_PATH)) {
            ArrayList<String> cachedForms =
                FileUtils.getValidFormsAsArrayList(FileUtils.CACHE_PATH);

            Cursor c = null;
            // remove orphaned form defs
            if (cachedForms != null) {
                for (String cachePath : cachedForms) {

                    try {
                        String hash =
                            cachePath.substring(cachePath.lastIndexOf("/") + 1,
                                cachePath.lastIndexOf("."));

                        // if hash is not in db, delete
                        c = fetchFilesByPath(null, hash);
                        if (c.getCount() == 0 && !(new File(cachePath)).delete()) {
                            Log.i(t, "Failed to delete " + cachePath);
                        }
                        c.close();
                    } catch (StringIndexOutOfBoundsException e) {
                        Log.i(t, "no cached files found");
                    }

                }
            }
            // clean up adapter
            if (c != null) {
                c.close();
            }
        }
    }


    /**
     * Stores new forms in the database
     */
    public void addOrphanForms() {
        // create forms and cache path folder
        if (FileUtils.createFolder(FileUtils.FORMS_PATH)) {

            // full path to the raw xml forms stored on sd card
            ArrayList<String> storedForms =
                FileUtils.getValidFormsAsArrayList(FileUtils.FORMS_PATH);

            String hash = null;
            String path = null;
            Cursor c = null;

            // loop through forms on sdcard.
            if (storedForms != null) {
                for (String formPath : storedForms) {
                    // only add forms
                    if (!(formPath.endsWith(".xml") || formPath.endsWith(".xhtml")))
                        continue;

                    // hash of raw form
                    hash = FileUtils.getMd5Hash(new File(formPath));

                    c = fetchFilesByPath(null, hash);
                    // db has the hash
                    if (c.getCount() > 0) {
                        path = c.getString(c.getColumnIndex((FileDbAdapter.KEY_FILEPATH)));
                        // file path is different, remove file
                        if (!path.equals(formPath) && !(new File(formPath)).delete()) {
                            Log.i(t, "Failed to delete " + formPath);
                        }
                        c.close();
                    } else {
                        // no hash in db, but file path is there.
                        c = fetchFilesByPath(formPath, null);
                        if (c.getCount() > 0) {
                            // delete db entry and hash
                            deleteFile(c.getLong(c.getColumnIndex((FileDbAdapter.KEY_ID))));
                        }
                        c.close();

                        // add this raw form
                        createFile(formPath, FileDbAdapter.TYPE_FORM,
                            FileDbAdapter.STATUS_AVAILABLE);

                    }
                }
            }

            // clean up adapter
            if (c != null) {
                c.deactivate();
                c.close();
            }
        }
    }


    public void removeOrphanForms() {
        if (FileUtils.createFolder(FileUtils.FORMS_PATH)) {

            // full path to the raw xml forms stored on sd card
            ArrayList<String> storedForms =
                FileUtils.getValidFormsAsArrayList(FileUtils.FORMS_PATH);

            String hash = null;
            // String path = null;
            Cursor c = null;

            // loop through forms on sdcard.
            if (storedForms != null) {
                for (String formPath : storedForms) {

                    // hash of raw form
                    hash = FileUtils.getMd5Hash(new File(formPath));

                    c = fetchFilesByPath(null, hash);
                    // db does not the hash
                    if (c.getCount() == 0 && !(new File(formPath)).delete()) {
                        Log.i(t, "Failed to delete " + formPath);
                    }
                    c.close();
                }
            }
            // clean up adapter
            if (c != null) {
                c.close();
            }
        }
    }


    public void removeOrphanInstances(Context ctx) {
        if (FileUtils.createFolder(FileUtils.INSTANCES_PATH)) {
            File fo = null;
            String[] fis = null;
            Cursor c = null;
            ArrayList<String> storedInstances =
                FileUtils.getFoldersAsArrayList(FileUtils.INSTANCES_PATH);

            FilenameFilter ff = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith("xml");
                }
            };

            if (storedInstances != null) {
                for (String instancePath : storedInstances) {
                    fo = new File(instancePath);
                    // delete empty folders
                    if (fo.listFiles().length == 0 && !fo.delete()) {
                        Log.i(t, "Failed to delete " + instancePath);
                    }
                    // find xml file in folder and delete folder
                    fis = fo.list(ff);
                    if (fis != null && fis.length > 0) {
                        c = fetchFilesByPath(instancePath + "/" + fo.list(ff)[0], null);

                        File dir = new File(instancePath);
                        if (dir.exists() && dir.isDirectory()) {
                            File[] files = dir.listFiles();
                            for (File file : files) {
                                if (file.getName().endsWith(".jpg")) {
                                    String[] projection = {
                                        Images.ImageColumns._ID
                                    };
                                    Cursor cd =
                                        ctx.getContentResolver().query(
                                            Images.Media.EXTERNAL_CONTENT_URI, projection,
                                            "_data='" + instancePath + "/" + file.getName() + "'",
                                            null, null);
                                    if (cd.getCount() > 0) {
                                        cd.moveToFirst();
                                        String id =
                                            cd.getString(cd.getColumnIndex(Images.ImageColumns._ID));

                                        Log.e(
                                            t,
                                            "attempting to delete: "
                                                    + Uri.withAppendedPath(
                                                        Images.Media.EXTERNAL_CONTENT_URI, id));
                                        int del =
                                            ctx.getContentResolver().delete(
                                                Uri.withAppendedPath(
                                                    Images.Media.EXTERNAL_CONTENT_URI, id), null,
                                                null);
                                        Log.e(t, "deleted " + del + " image files");
                                    }
                                    c.close();

                                }
                            }
                        }

                        if (c.getCount() == 0 && !FileUtils.deleteFolder(instancePath)) {
                            Log.i(t, "Failed to delete " + instancePath);
                        }
                        c.close();
                    }
                }

            }

            // clean up adapter
            if (c != null) {
                c.close();
            }
        }
    }

}
