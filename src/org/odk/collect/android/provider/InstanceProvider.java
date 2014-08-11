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

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.ODKSQLiteOpenHelper;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.utilities.MediaUtils;

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
import java.util.Locale;

/**
 *
 */
public class InstanceProvider extends ContentProvider {

    private static final String t = "InstancesProvider";

    private static final String DATABASE_NAME = "instances.db";
    private static final int DATABASE_VERSION = 3;
    private static final String INSTANCES_TABLE_NAME = "instances";

    private static HashMap<String, String> sInstancesProjectionMap;

    private static final int INSTANCES = 1;
    private static final int INSTANCE_ID = 2;

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
           db.execSQL("CREATE TABLE " + INSTANCES_TABLE_NAME + " ("
               + InstanceColumns._ID + " integer primary key, "
               + InstanceColumns.DISPLAY_NAME + " text not null, "
               + InstanceColumns.SUBMISSION_URI + " text, "
               + InstanceColumns.CAN_EDIT_WHEN_COMPLETE + " text, "
               + InstanceColumns.INSTANCE_FILE_PATH + " text not null, "
               + InstanceColumns.JR_FORM_ID + " text not null, "
               + InstanceColumns.JR_VERSION + " text, "
               + InstanceColumns.STATUS + " text not null, "
               + InstanceColumns.LAST_STATUS_CHANGE_DATE + " date not null, "
               + InstanceColumns.DISPLAY_SUBTEXT + " text not null );");
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	int initialVersion = oldVersion;
        	if ( oldVersion == 1 ) {
        		db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
        					InstanceColumns.CAN_EDIT_WHEN_COMPLETE + " text;");
        		db.execSQL("UPDATE " + INSTANCES_TABLE_NAME + " SET " +
        					InstanceColumns.CAN_EDIT_WHEN_COMPLETE + " = '" + Boolean.toString(true) + "' WHERE " +
        					InstanceColumns.STATUS + " IS NOT NULL AND " +
        					InstanceColumns.STATUS + " != '" + InstanceProviderAPI.STATUS_INCOMPLETE + "'");
        		oldVersion = 2;
        	}
        	if ( oldVersion == 2 ) {
        		db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
    					InstanceColumns.JR_VERSION + " text;");
        	}
            Log.w(t, "Successfully upgraded database from version " + initialVersion + " to " + newVersion
                    + ", without destroying all the old data");
        }
    }

    private DatabaseHelper mDbHelper;

    private DatabaseHelper getDbHelper() {
        // wrapper to test and reset/set the dbHelper based upon the attachment state of the device.
        try {
            Collect.createODKDirs();
        } catch (RuntimeException e) {
        	mDbHelper = null;
            return null;
        }

        if (mDbHelper != null) {
        	return mDbHelper;
        }
        mDbHelper = new DatabaseHelper(DATABASE_NAME);
        return mDbHelper;
    }

    @Override
    public boolean onCreate() {
        // must be at the beginning of any activity that can be called from an external intent
        DatabaseHelper h = getDbHelper();
        if ( h == null ) {
        	return false;
        }
        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(INSTANCES_TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
            case INSTANCES:
                qb.setProjectionMap(sInstancesProjectionMap);
                break;

            case INSTANCE_ID:
                qb.setProjectionMap(sInstancesProjectionMap);
                qb.appendWhere(InstanceColumns._ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Get the database and run the query
        SQLiteDatabase db = getDbHelper().getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }


    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case INSTANCES:
                return InstanceColumns.CONTENT_TYPE;

            case INSTANCE_ID:
                return InstanceColumns.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }


    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != INSTANCES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = Long.valueOf(System.currentTimeMillis());

        // Make sure that the fields are all set
        if (values.containsKey(InstanceColumns.LAST_STATUS_CHANGE_DATE) == false) {
            values.put(InstanceColumns.LAST_STATUS_CHANGE_DATE, now);
        }

        if (values.containsKey(InstanceColumns.DISPLAY_SUBTEXT) == false) {
            Date today = new Date();
            String text = getDisplaySubtext(InstanceProviderAPI.STATUS_INCOMPLETE, today);
            values.put(InstanceColumns.DISPLAY_SUBTEXT, text);
        }

        if (values.containsKey(InstanceColumns.STATUS) == false) {
            values.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_INCOMPLETE);
        }

        SQLiteDatabase db = getDbHelper().getWritableDatabase();
        long rowId = db.insert(INSTANCES_TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri instanceUri = ContentUris.withAppendedId(InstanceColumns.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(instanceUri, null);
        	Collect.getInstance().getActivityLogger().logActionParam(this, "insert",
        			instanceUri.toString(), values.getAsString(InstanceColumns.INSTANCE_FILE_PATH));
            return instanceUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    private String getDisplaySubtext(String state, Date date) {
        if (state == null) {
        	return new SimpleDateFormat(getContext().getString(R.string.added_on_date_at_time), Locale.getDefault()).format(date);
        } else if (InstanceProviderAPI.STATUS_INCOMPLETE.equalsIgnoreCase(state)) {
        	return new SimpleDateFormat(getContext().getString(R.string.saved_on_date_at_time), Locale.getDefault()).format(date);
        } else if (InstanceProviderAPI.STATUS_COMPLETE.equalsIgnoreCase(state)) {
        	return new SimpleDateFormat(getContext().getString(R.string.finalized_on_date_at_time), Locale.getDefault()).format(date);
        } else if (InstanceProviderAPI.STATUS_SUBMITTED.equalsIgnoreCase(state)) {
        	return new SimpleDateFormat(getContext().getString(R.string.sent_on_date_at_time), Locale.getDefault()).format(date);
        } else if (InstanceProviderAPI.STATUS_SUBMISSION_FAILED.equalsIgnoreCase(state)) {
        	return new SimpleDateFormat(getContext().getString(R.string.sending_failed_on_date_at_time), Locale.getDefault()).format(date);
        } else {
        	return new SimpleDateFormat(getContext().getString(R.string.added_on_date_at_time), Locale.getDefault()).format(date);
        }
    }

    private void deleteAllFilesInDirectory(File directory) {
        if (directory.exists()) {
        	// do not delete the directory if it might be an
        	// ODK Tables instance data directory. Let ODK Tables
        	// manage the lifetimes of its filled-in form data
        	// media attachments.
            if (directory.isDirectory() && !Collect.isODKTablesInstanceDataDirectory(directory)) {
            	// delete any media entries for files in this directory...
                int images = MediaUtils.deleteImagesInFolderFromMediaProvider(directory);
                int audio = MediaUtils.deleteAudioInFolderFromMediaProvider(directory);
                int video = MediaUtils.deleteVideoInFolderFromMediaProvider(directory);

                Log.i(t, "removed from content providers: " + images
                        + " image files, " + audio + " audio files,"
                        + " and " + video + " video files.");

                // delete all the files in the directory
                File[] files = directory.listFiles();
                for (File f : files) {
                    // should make this recursive if we get worried about
                    // the media directory containing directories
                    f.delete();
                }
            }
            directory.delete();
        }
    }


    /**
     * This method removes the entry from the content provider, and also removes any associated files.
     * files:  form.xml, [formmd5].formdef, formname-media {directory}
     */
    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = getDbHelper().getWritableDatabase();
        int count;

        switch (sUriMatcher.match(uri)) {
            case INSTANCES:
                Cursor del = null;
                try {
                	del = this.query(uri, null, where, whereArgs, null);
                	if (del.getCount() > 0) {
                		del.moveToFirst();
                		do {
		                    String instanceFile = del.getString(del.getColumnIndex(InstanceColumns.INSTANCE_FILE_PATH));
		                    Collect.getInstance().getActivityLogger().logAction(this, "delete", instanceFile);
		                    File instanceDir = (new File(instanceFile)).getParentFile();
		                    deleteAllFilesInDirectory(instanceDir);
                		} while (del.moveToNext());
	                }
                } finally {
                	if ( del != null ) {
                		del.close();
                	}
                }
                count = db.delete(INSTANCES_TABLE_NAME, where, whereArgs);
                break;

            case INSTANCE_ID:
                String instanceId = uri.getPathSegments().get(1);

                Cursor c = null;
                try {
                	c = this.query(uri, null, where, whereArgs, null);
                	if (c.getCount() > 0) {
                		c.moveToFirst();
                		do {
		                    String instanceFile = c.getString(c.getColumnIndex(InstanceColumns.INSTANCE_FILE_PATH));
		                    Collect.getInstance().getActivityLogger().logAction(this, "delete", instanceFile);
		                    File instanceDir = (new File(instanceFile)).getParentFile();
		                    deleteAllFilesInDirectory(instanceDir);
                		} while (c.moveToNext());
	                }
                } finally {
                	if ( c != null ) {
                		c.close();
                	}
                }

                count =
                    db.delete(INSTANCES_TABLE_NAME,
                        InstanceColumns._ID + "=" + instanceId
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
        SQLiteDatabase db = getDbHelper().getWritableDatabase();

        Long now = Long.valueOf(System.currentTimeMillis());

        // Make sure that the fields are all set
        if (values.containsKey(InstanceColumns.LAST_STATUS_CHANGE_DATE) == false) {
            values.put(InstanceColumns.LAST_STATUS_CHANGE_DATE, now);
        }

        int count;
        String status = null;
        switch (sUriMatcher.match(uri)) {
            case INSTANCES:
                if (values.containsKey(InstanceColumns.STATUS)) {
                    status = values.getAsString(InstanceColumns.STATUS);

                    if (values.containsKey(InstanceColumns.DISPLAY_SUBTEXT) == false) {
                        Date today = new Date();
                        String text = getDisplaySubtext(status, today);
                        values.put(InstanceColumns.DISPLAY_SUBTEXT, text);
                    }
                }

                count = db.update(INSTANCES_TABLE_NAME, values, where, whereArgs);
                break;

            case INSTANCE_ID:
                String instanceId = uri.getPathSegments().get(1);

                if (values.containsKey(InstanceColumns.STATUS)) {
                    status = values.getAsString(InstanceColumns.STATUS);

                    if (values.containsKey(InstanceColumns.DISPLAY_SUBTEXT) == false) {
                        Date today = new Date();
                        String text = getDisplaySubtext(status, today);
                        values.put(InstanceColumns.DISPLAY_SUBTEXT, text);
                    }
                }

                count =
                    db.update(INSTANCES_TABLE_NAME, values, InstanceColumns._ID + "=" + instanceId
                            + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(InstanceProviderAPI.AUTHORITY, "instances", INSTANCES);
        sUriMatcher.addURI(InstanceProviderAPI.AUTHORITY, "instances/#", INSTANCE_ID);

        sInstancesProjectionMap = new HashMap<String, String>();
        sInstancesProjectionMap.put(InstanceColumns._ID, InstanceColumns._ID);
        sInstancesProjectionMap.put(InstanceColumns.DISPLAY_NAME, InstanceColumns.DISPLAY_NAME);
        sInstancesProjectionMap.put(InstanceColumns.SUBMISSION_URI, InstanceColumns.SUBMISSION_URI);
        sInstancesProjectionMap.put(InstanceColumns.CAN_EDIT_WHEN_COMPLETE, InstanceColumns.CAN_EDIT_WHEN_COMPLETE);
        sInstancesProjectionMap.put(InstanceColumns.INSTANCE_FILE_PATH, InstanceColumns.INSTANCE_FILE_PATH);
        sInstancesProjectionMap.put(InstanceColumns.JR_FORM_ID, InstanceColumns.JR_FORM_ID);
        sInstancesProjectionMap.put(InstanceColumns.JR_VERSION, InstanceColumns.JR_VERSION);
        sInstancesProjectionMap.put(InstanceColumns.STATUS, InstanceColumns.STATUS);
        sInstancesProjectionMap.put(InstanceColumns.LAST_STATUS_CHANGE_DATE, InstanceColumns.LAST_STATUS_CHANGE_DATE);
        sInstancesProjectionMap.put(InstanceColumns.DISPLAY_SUBTEXT, InstanceColumns.DISPLAY_SUBTEXT);
    }

}
