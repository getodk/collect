/*
 * Copyright (C) 2007 The Android Open Source Project
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

package org.odk.collect.android.provider;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.ODKSQLiteOpenHelper;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.utilities.FileUtils;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * 
 */
public class FormsProvider extends ContentProvider {

    private static final String t = "FormsProvider";

    private static final String DATABASE_NAME = "forms.db";
    private static final int DATABASE_VERSION = 2;
    private static final String FORMS_TABLE_NAME = "forms";

    private static HashMap<String, String> sFormsProjectionMap;

    private static final int FORMS = 1;
    private static final int FORM_ID = 2;

    private static final UriMatcher sUriMatcher;

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends ODKSQLiteOpenHelper {

        DatabaseHelper(String databaseName) {
            super(Collect.METADATA_PATH, databaseName, null, DATABASE_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + FORMS_TABLE_NAME + " (" + FormsColumns._ID
                    + " integer primary key, " + FormsColumns.DISPLAY_NAME + " text not null, "
                    + FormsColumns.DISPLAY_SUBTEXT + " text not null, " + FormsColumns.DESCRIPTION
                    + " text, " + FormsColumns.JR_FORM_ID + " text not null, "
                    + FormsColumns.MODEL_VERSION + " integer, " + FormsColumns.UI_VERSION
                    + " integer, "
                    + FormsColumns.MD5_HASH
                    + " text not null, "
                    + FormsColumns.DATE
                    + " integer not null, " // milliseconds
                    + FormsColumns.FORM_MEDIA_PATH + " text not null, "
                    + FormsColumns.FORM_FILE_PATH + " text not null, " + FormsColumns.LANGUAGE
                    + " text, " + FormsColumns.SUBMISSION_URI + " text, "
                    + FormsColumns.JRCACHE_FILE_PATH + " text not null );");
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(t, "Upgrading database from version " + oldVersion + " to " + newVersion
                    + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS forms");
            onCreate(db);
        }
    }

    private DatabaseHelper mDbHelper;


    @Override
    public boolean onCreate() {
        mDbHelper = new DatabaseHelper(DATABASE_NAME);
        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(FORMS_TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
            case FORMS:
                qb.setProjectionMap(sFormsProjectionMap);
                break;

            case FORM_ID:
                qb.setProjectionMap(sFormsProjectionMap);
                qb.appendWhere(FormsColumns._ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Get the database and run the query
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }


    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case FORMS:
                return FormsColumns.CONTENT_TYPE;

            case FORM_ID:
                return FormsColumns.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }


    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != FORMS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = Long.valueOf(System.currentTimeMillis());

        // Make sure that the necessary fields are all set
        if (values.containsKey(FormsColumns.DATE) == false) {
            values.put(FormsColumns.DATE, now);
        }

        if (values.containsKey(FormsColumns.DISPLAY_SUBTEXT) == false) {
            Date today = new Date();
            String ts = new SimpleDateFormat("EEE, MMM dd, yyyy 'at' HH:mm").format(today);
            values.put(FormsColumns.DISPLAY_SUBTEXT, "Added on " + ts);
        }

        // if we don't have a path to the file, the rest are irrelevant.
        // it should fail anyway because you can't have a null file path.
        if (values.containsKey(FormsColumns.FORM_FILE_PATH) == true) {
            String filePath = values.getAsString(FormsColumns.FORM_FILE_PATH);
            File form = new File(filePath);

            if (values.containsKey(FormsColumns.DISPLAY_NAME) == false) {
                values.put(FormsColumns.DISPLAY_NAME, form.getName());
            }

            // don't let users put in a manual md5 hash
            if (values.containsKey(FormsColumns.MD5_HASH)) {
                values.remove(FormsColumns.MD5_HASH);
            }
            String md5 = FileUtils.getMd5Hash(form);
            values.put(FormsColumns.MD5_HASH, md5);

            if (values.containsKey(FormsColumns.JRCACHE_FILE_PATH) == false) {
                String cachePath = Collect.CACHE_PATH + File.separator + md5 + ".formdef";
                values.put(FormsColumns.JRCACHE_FILE_PATH, cachePath);
            }
            if (values.containsKey(FormsColumns.FORM_MEDIA_PATH) == false) {
                String pathNoExtension = filePath.substring(0, filePath.lastIndexOf("."));
                String mediaPath = pathNoExtension + "-media";
                values.put(FormsColumns.FORM_MEDIA_PATH, mediaPath);
            }

        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long rowId = db.insert(FORMS_TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri formUri = ContentUris.withAppendedId(FormsColumns.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(formUri, null);
            return formUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }


    private void deleteFileOrDir(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            if (file.isDirectory()) {
                // delete all the containing files
                File[] files = file.listFiles();
                for (File f : files) {
                    // should make this recursive if we get worried about
                    // the media directory containing directories
                    Log.i(t, "attempting to delete file: " + f.getAbsolutePath());
                    f.delete();
                }
            }
            file.delete();
            Log.i(t, "attempting to delete file: " + file.getAbsolutePath());
        }
    }


    /**
     * This method removes the entry from the content provider, and also removes any associated
     * files. files: form.xml, [formmd5].formdef, formname-media {directory}
     */
    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count;

        switch (sUriMatcher.match(uri)) {
            case FORMS:
                Cursor del = this.query(uri, null, where, whereArgs, null);
                del.moveToPosition(-1);
                while (del.moveToNext()) {
                    deleteFileOrDir(del.getString(del
                            .getColumnIndex(FormsColumns.JRCACHE_FILE_PATH)));
                    deleteFileOrDir(del.getString(del.getColumnIndex(FormsColumns.FORM_FILE_PATH)));
                    deleteFileOrDir(del.getString(del.getColumnIndex(FormsColumns.FORM_MEDIA_PATH)));
                }
                del.close();
                count = db.delete(FORMS_TABLE_NAME, where, whereArgs);
                break;

            case FORM_ID:
                String formId = uri.getPathSegments().get(1);

                Cursor c = this.query(uri, null, where, whereArgs, null);
                // This should only ever return 1 record.
                c.moveToPosition(-1);
                while (c.moveToNext()) {
                    deleteFileOrDir(c.getString(c.getColumnIndex(FormsColumns.JRCACHE_FILE_PATH)));
                    deleteFileOrDir(c.getString(c.getColumnIndex(FormsColumns.FORM_FILE_PATH)));
                    deleteFileOrDir(c.getString(c.getColumnIndex(FormsColumns.FORM_MEDIA_PATH)));
                }
                c.close();

                count =
                    db.delete(FORMS_TABLE_NAME,
                        FormsColumns._ID + "=" + formId
                                + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),
                        whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }


    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case FORMS:
                // don't let users manually update md5
                if (values.containsKey(FormsColumns.MD5_HASH)) {
                    values.remove(FormsColumns.MD5_HASH);
                }
                // if values contains path, then all filepaths and md5s will get updated
                // this probably isn't a great thing to do.
                if (values.containsKey(FormsColumns.FORM_FILE_PATH)) {
                    String formFile = values.getAsString(FormsColumns.FORM_FILE_PATH);
                    values.put(FormsColumns.MD5_HASH, FileUtils.getMd5Hash(new File(formFile)));
                }

                Cursor c = this.query(uri, null, where, whereArgs, null);

                if (c.getCount() > 0) {
                    c.moveToPosition(-1);
                    while (c.moveToNext()) {
                        // before updating the paths, delete all the files
                        if (values.containsKey(FormsColumns.FORM_FILE_PATH)) {
                            String newFile = values.getAsString(FormsColumns.FORM_FILE_PATH);
                            String delFile =
                                c.getString(c.getColumnIndex(FormsColumns.FORM_FILE_PATH));
                            if (newFile.equalsIgnoreCase(delFile)) {
                                // same file, so don't delete anything
                            } else {
                                // different files, delete the old one
                                deleteFileOrDir(delFile);
                            }

                            // either way, delete the old cache because we'll calculate a new one.
                            deleteFileOrDir(c.getString(c
                                    .getColumnIndex(FormsColumns.JRCACHE_FILE_PATH)));
                        }
                    }
                }

                // Make sure that the necessary fields are all set
                if (values.containsKey(FormsColumns.DATE) == true) {
                    Date today = new Date();
                    String ts = new SimpleDateFormat("EEE, MMM dd, yyyy 'at' HH:mm").format(today);
                    values.put(FormsColumns.DISPLAY_SUBTEXT, "Added on " + ts);
                }

                count = db.update(FORMS_TABLE_NAME, values, where, whereArgs);
                break;

            case FORM_ID:
                String formId = uri.getPathSegments().get(1);
                // Whenever file paths are updated, delete the old files.

                Cursor update = this.query(uri, null, where, whereArgs, null);

                // This should only ever return 1 record.
                if (update.getCount() > 0) {
                    update.moveToFirst();

                    // don't let users manually update md5
                    if (values.containsKey(FormsColumns.MD5_HASH)) {
                        values.remove(FormsColumns.MD5_HASH);
                    }

                    // the order here is important (jrcache needs to be before form file)
                    // because we update the jrcache file if there's a new form file
                    if (values.containsKey(FormsColumns.JRCACHE_FILE_PATH)) {
                        deleteFileOrDir(update.getString(update
                                .getColumnIndex(FormsColumns.JRCACHE_FILE_PATH)));
                    }

                    if (values.containsKey(FormsColumns.FORM_FILE_PATH)) {
                        String formFile = values.getAsString(FormsColumns.FORM_FILE_PATH);
                        String oldFile =
                            update.getString(update.getColumnIndex(FormsColumns.FORM_FILE_PATH));

                        if (formFile != null && formFile.equalsIgnoreCase(oldFile)) {
                            // Files are the same, so we may have just copied over something we had
                            // already
                        } else {
                            // New file name. This probably won't ever happen, though.
                            deleteFileOrDir(oldFile);
                        }

                        // we're updating our file, so update the md5
                        // and get rid of the cache (doesn't harm anything)
                        deleteFileOrDir(update.getString(update
                                .getColumnIndex(FormsColumns.JRCACHE_FILE_PATH)));
                        String newMd5 = FileUtils.getMd5Hash(new File(formFile));
                        values.put(FormsColumns.MD5_HASH, newMd5);
                        values.put(FormsColumns.JRCACHE_FILE_PATH, 
                        		Collect.CACHE_PATH + File.separator + newMd5 + ".formdef");
                    }

                    // Make sure that the necessary fields are all set
                    if (values.containsKey(FormsColumns.DATE) == true) {
                        Date today = new Date();
                        String ts =
                            new SimpleDateFormat("EEE, MMM dd, yyyy 'at' HH:mm").format(today);
                        values.put(FormsColumns.DISPLAY_SUBTEXT, "Added on " + ts);
                    }

                    count =
                        db.update(FORMS_TABLE_NAME, values, FormsColumns._ID + "=" + formId
                                + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),
                            whereArgs);
                } else {
                    Log.e(t, "Attempting to update row that does not exist");
                }
                update.close();
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(FormsProviderAPI.AUTHORITY, "forms", FORMS);
        sUriMatcher.addURI(FormsProviderAPI.AUTHORITY, "forms/#", FORM_ID);

        sFormsProjectionMap = new HashMap<String, String>();
        sFormsProjectionMap.put(FormsColumns._ID, FormsColumns._ID);
        sFormsProjectionMap.put(FormsColumns.DISPLAY_NAME, FormsColumns.DISPLAY_NAME);
        sFormsProjectionMap.put(FormsColumns.DISPLAY_SUBTEXT, FormsColumns.DISPLAY_SUBTEXT);
        sFormsProjectionMap.put(FormsColumns.DESCRIPTION, FormsColumns.DESCRIPTION);
        sFormsProjectionMap.put(FormsColumns.JR_FORM_ID, FormsColumns.JR_FORM_ID);
        sFormsProjectionMap.put(FormsColumns.MODEL_VERSION, FormsColumns.MODEL_VERSION);
        sFormsProjectionMap.put(FormsColumns.UI_VERSION, FormsColumns.UI_VERSION);
        sFormsProjectionMap.put(FormsColumns.SUBMISSION_URI, FormsColumns.SUBMISSION_URI);
        sFormsProjectionMap.put(FormsColumns.MD5_HASH, FormsColumns.MD5_HASH);
        sFormsProjectionMap.put(FormsColumns.DATE, FormsColumns.DATE);
        sFormsProjectionMap.put(FormsColumns.FORM_MEDIA_PATH, FormsColumns.FORM_MEDIA_PATH);
        sFormsProjectionMap.put(FormsColumns.FORM_FILE_PATH, FormsColumns.FORM_FILE_PATH);
        sFormsProjectionMap.put(FormsColumns.JRCACHE_FILE_PATH, FormsColumns.JRCACHE_FILE_PATH);
        sFormsProjectionMap.put(FormsColumns.LANGUAGE, FormsColumns.LANGUAGE);
    }

}
