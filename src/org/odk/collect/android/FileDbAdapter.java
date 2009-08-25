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

package org.odk.collect.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Manages the files the application uses.
 * 
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class FileDbAdapter {

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
    public static final String STATUS_SAVED = "saved";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_SUBMITTED = "submitted";

    // status for forms
    public static final String STATUS_AVAILABLE = "available";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_CREATE =
            "create table files (_id integer primary key autoincrement, "
                    + "path text not null, hash text not null unique, type text not null, status text not null, display text not null, meta text not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "files";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }


        @Override
        // upgrading will destroy all old data
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS data");
            onCreate(db);
        }

    }


    public FileDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }


    public FileDbAdapter open() throws SQLException {

        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }


    public void close() {
        mDbHelper.close();
    }


    /**
     * Generate text for the second level of the row display.
     * 
     * @param timestamp date modified of the file
     * @return date modified of the file formatted as human readable string
     */
    private String generateMeta(Long timestamp) {
        Date d = new Date(timestamp);
        return new SimpleDateFormat("'Modified on' EEE, MMM dd, yyyy 'at' HH:mm").format(d);
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
            return p.split(filename)[0] + " " + mCtx.getString(R.string.data);
        } else if (type.equals(TYPE_FORM)) {
            // remove extension from form
            return filename.substring(0, filename.lastIndexOf(".")) + " "
                    + mCtx.getString(R.string.form);
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
        cv.put(KEY_META, generateMeta(f.lastModified()));

        long id = -1;
        try {
            id = mDb.insert(DATABASE_TABLE, null, cv);
        } catch (SQLiteConstraintException e) {
        }

        return id;
    }


    /**
     * Remove the file from the database.
     * 
     * @param path path to the file
     * @return number of affected rows
     */
    public boolean deleteFile(String path) {
        return mDb.delete(DATABASE_TABLE, KEY_FILEPATH + "='" + path + "'", null) > 0;
    }


    /**
     * Get a cursor to a single file from the database.
     * 
     * @param path path to the file
     * @param hash hash of the file
     * @return cursor to the file
     * @throws SQLException
     */
    public Cursor fetchFile(String path, String hash) throws SQLException {
        Cursor c = null;
        if (path == null) {
            // no path given, search using hash
            c =
                    mDb.query(true, DATABASE_TABLE, new String[] {KEY_ID, KEY_FILEPATH, KEY_HASH,
                            KEY_TYPE, KEY_STATUS, KEY_DISPLAY, KEY_META}, KEY_HASH + "='" + hash
                            + "'", null, null, null, null, null);
        } else if (hash == null) {
            // no hash given, search using path
            c =
                    mDb.query(true, DATABASE_TABLE, new String[] {KEY_ID, KEY_FILEPATH, KEY_HASH,
                            KEY_TYPE, KEY_STATUS, KEY_DISPLAY, KEY_META}, KEY_FILEPATH + "='"
                            + path + "'", null, null, null, null, null);
        } else {
            // search using path and hash
            c =
                    mDb.query(true, DATABASE_TABLE, new String[] {KEY_ID, KEY_FILEPATH, KEY_HASH,
                            KEY_TYPE, KEY_STATUS, KEY_DISPLAY, KEY_META}, KEY_FILEPATH + "='"
                            + path + "' and " + KEY_HASH + "='" + hash + "'", null, null, null,
                            null, null);
        }
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
    public Cursor fetchFiles(String type, String status) throws SQLException {
        cleanFiles();

        Cursor c = null;
        if (type == null) {
            // no type given, search using status
            c =
                    mDb.query(true, DATABASE_TABLE, new String[] {KEY_ID, KEY_FILEPATH, KEY_HASH,
                            KEY_TYPE, KEY_STATUS, KEY_DISPLAY, KEY_META}, KEY_STATUS + "='"
                            + status + "'", null, null, null, null, null);
        } else if (status == null) {
            // no status given, search using type
            c =
                    mDb.query(true, DATABASE_TABLE, new String[] {KEY_ID, KEY_FILEPATH, KEY_HASH,
                            KEY_TYPE, KEY_STATUS, KEY_DISPLAY, KEY_META}, KEY_TYPE + "='" + type
                            + "'", null, null, null, null, null);
        } else {
            // search using type and status
            c =
                    mDb.query(true, DATABASE_TABLE, new String[] {KEY_ID, KEY_FILEPATH, KEY_HASH,
                            KEY_TYPE, KEY_STATUS, KEY_DISPLAY, KEY_META}, KEY_TYPE + "='" + type
                            + "' and " + KEY_STATUS + "='" + status + "'", null, null, null, null,
                            null);
        }

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
        cv.put(KEY_META, generateMeta(new Date().getTime()));

        return mDb.update(DATABASE_TABLE, cv, KEY_FILEPATH + "='" + path + "'", null) > 0;
    }


    /**
     * Find orphaned files on the file system
     */
    private void cleanFiles() {
        Cursor c =
                mDb.query(DATABASE_TABLE, new String[] {KEY_ID, KEY_FILEPATH, KEY_STATUS}, null,
                        null, null, null, null);

        while (c.moveToNext()) {
            String path = c.getString(c.getColumnIndex(KEY_FILEPATH));
            File f = new File(path);
            if (!f.exists()) {
                deleteFile(path);
            }
        }
        c.close();
    }
}
