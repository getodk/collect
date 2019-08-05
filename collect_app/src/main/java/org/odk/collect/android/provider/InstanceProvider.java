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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.ODKSQLiteOpenHelper;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.utilities.CustomSQLiteQueryBuilder;
import org.odk.collect.android.utilities.MediaUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

import static android.provider.BaseColumns._ID;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.ACT_LAT;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.ACT_LON;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.DELETED_DATE;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.DISPLAY_NAME;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.FORM_PATH;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.JR_FORM_ID;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.JR_VERSION;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.SCHED_LAT;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.SCHED_LON;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.SOURCE;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.STATUS;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.SUBMISSION_URI;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_ACT_FINISH;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_ACT_START;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_ADDRESS;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_ASS_ID;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_GEOM;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_GEOM_TYPE;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_HIDE;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_IS_SYNC;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_LOCATION_TRIGGER;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_REPEAT;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_SCHED_START;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_SHOW_DIST;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_SURVEY_NOTES;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_TASK_COMMENT;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_TASK_STATUS;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_TITLE;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_UPDATED;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_UPDATEID;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.UUID;
import static org.odk.collect.android.utilities.PermissionUtils.areStoragePermissionsGranted;

public class InstanceProvider extends ContentProvider {


    private static final String DATABASE_NAME = "instances.db";
    private static final int DATABASE_VERSION = 13;		// smap
    private static final String INSTANCES_TABLE_NAME = "instances";

    private static HashMap<String, String> sInstancesProjectionMap;

    private static final int INSTANCES = 1;
    private static final int INSTANCE_ID = 2;

    private static final UriMatcher URI_MATCHER;

    private static final String[] COLUMN_NAMES_V13 = new String[] {
            _ID,
            DISPLAY_NAME,
            SUBMISSION_URI,
            CAN_EDIT_WHEN_COMPLETE,
            INSTANCE_FILE_PATH,
            JR_FORM_ID,
            JR_VERSION,
            STATUS,
            LAST_STATUS_CHANGE_DATE,
            DELETED_DATE,
            SOURCE,             // smap
            FORM_PATH,          // smap
            ACT_LON,            // smap
            ACT_LAT,            // smap
            SCHED_LON,          // smap
            SCHED_LAT,          // smap
            T_TITLE,            // smap
            T_SCHED_START,      // smap
            T_ACT_START,        // smap
            T_ACT_FINISH,       // smap
            T_ADDRESS,          // smap
            T_GEOM,             // smap
            T_GEOM_TYPE,        // smap
            T_IS_SYNC,          // smap
            T_ASS_ID,           // smap
            T_TASK_STATUS,      // smap
            T_TASK_COMMENT,     // smap
            T_REPEAT,           // smap
            T_UPDATEID,         // smap
            T_LOCATION_TRIGGER, // smap
            T_SURVEY_NOTES,     // smap
            UUID,               // smap
            T_UPDATED,          // smap
            T_SHOW_DIST,        // smap
            T_HIDE              // smap

    };
    static final String[] CURRENT_VERSION_COLUMN_NAMES = COLUMN_NAMES_V13;

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends ODKSQLiteOpenHelper {

        DatabaseHelper(String databaseName) {
            super(Collect.METADATA_PATH, databaseName, null, DATABASE_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {

            createInstancesTableV13(db, INSTANCES_TABLE_NAME);
           /*
           db.execSQL("CREATE TABLE " + INSTANCES_TABLE_NAME + " ("
               + InstanceColumns._ID + " integer primary key, "
               + InstanceColumns.DISPLAY_NAME + " text not null, "
               + InstanceColumns.SUBMISSION_URI + " text, "
               + InstanceColumns.CAN_EDIT_WHEN_COMPLETE + " text, "
               + InstanceColumns.INSTANCE_FILE_PATH + " text not null, "
               + InstanceColumns.JR_FORM_ID + " text not null, "
               + InstanceColumns.JR_VERSION + " text, "
               + SOURCE + " text, "		        // smap
               + FORM_PATH + " text, "		    // smap
               + ACT_LON + " double, "		    // smap
               + ACT_LAT + " double, "		    // smap
               + SCHED_LON + " double, "		// smap
               + SCHED_LAT + " double, "		// smap
               + T_TITLE + " text, "		    // smap
               + T_SCHED_START + " long, "		// smap
               + T_ACT_START + " long, "		// smap
               + T_ACT_FINISH + " long, "		// smap
               + T_ADDRESS + " text, "		    // smap
               + T_GEOM + " text, "		        // smap
               + T_GEOM_TYPE + " text, "		// smap
               + T_IS_SYNC + " text, "		    // smap
               + T_ASS_ID + " long, "		    // smap
               + T_TASK_STATUS + " text, "		// smap
               + T_TASK_COMMENT + " text, "		// smap
               + T_REPEAT + " integer, "		// smap
               + T_UPDATEID + " text, "		    // smap
               + T_LOCATION_TRIGGER + " text, " // smap
               + T_SURVEY_NOTES + " text, "     // smap
               + UUID + " text, "		        // smap
               + T_UPDATED + " integer, "       // smap
               + InstanceColumns.STATUS + " text not null, "
               + InstanceColumns.LAST_STATUS_CHANGE_DATE + " date not null, "
               + T_SHOW_DIST + " integer, "   // smap
               + T_HIDE + " integer, "        // smap
               + InstanceColumns.DELETED_DATE + " date );" );
               */
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	int initialVersion = oldVersion;
        	/*
        	if ( oldVersion < 2 ) {
        		db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
        					InstanceColumns.CAN_EDIT_WHEN_COMPLETE + " text;");
        		db.execSQL("UPDATE " + INSTANCES_TABLE_NAME + " SET " +
        					InstanceColumns.CAN_EDIT_WHEN_COMPLETE + " = '" + Boolean.toString(true) + "' WHERE " +
        					InstanceColumns.STATUS + " IS NOT NULL AND " +
        					InstanceColumns.STATUS + " != '" + InstanceProviderAPI.STATUS_INCOMPLETE + "'");
        	}
        	if ( oldVersion < 3 ) {
        		db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
    					InstanceColumns.JR_VERSION + " text;");
        	}
            // Smap Start
        	if ( oldVersion < 4 ) {
        		db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
    					SOURCE + " text;");
        	}
            if ( oldVersion < 5 ) {
                db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
                        FORM_PATH + " text;");
                db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
                        ACT_LON + " double;");
                db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
                        ACT_LAT + " double;");
                db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
                        SCHED_LON + " double;");
                db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
                        SCHED_LAT + " double;");
                db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
                        T_TITLE + " text;");
                db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
                        T_SCHED_START + " long;");
                db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
                        T_ACT_START + " long;");
                db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
                        T_ACT_FINISH + " long;");
                db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
                        T_ADDRESS + " text;");
                db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
                        T_GEOM + " text;");
                db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
                        T_GEOM_TYPE + " text;");
                db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
                        T_IS_SYNC + " text;");
                db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
                        T_ASS_ID + " long;");
                db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
                        T_TASK_STATUS + " text;");
                db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
                        UUID + " text;");
            }
            if ( oldVersion < 6 ) {
                try {
                    db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
                            T_REPEAT + " integer;");
                    db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
                            T_UPDATEID + " text;");
                } catch(Exception e) {
                    // Catch errors, its possible the user upgraded then downgraded
                    Timber.w("Error in upgrading to database version 6");
                    e.printStackTrace();
                }
            }
            if ( oldVersion < 7 ) {
                try {
                    db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
                            T_LOCATION_TRIGGER + " text;");
                } catch(Exception e) {
                    // Catch errors, its possible the user upgraded then downgraded
                    Timber.w("Error in upgrading to database version 7");
                    e.printStackTrace();
                }
            }
            if ( oldVersion < 8 ) {
                try {
                    db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
                            T_SURVEY_NOTES + " text;");
                } catch(Exception e) {
                    // Catch errors, its possible the user upgraded then downgraded
                    Timber.w("Error in upgrading to database version 8");
                    e.printStackTrace();
                }
            }
            if (oldVersion < 9) {
                try {
                    db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
                            InstanceColumns.DELETED_DATE + " date;");
                } catch(Exception e) {
                    // Catch errors, its possible the user upgraded then downgraded
                    Timber.w("Error in upgrading to database version 9");
                    e.printStackTrace();
                }

            }
            if (oldVersion < 10) {
        	    try {
                    db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
                            T_UPDATED + " integer;");
                } catch(Exception e) {
                    // Catch errors, its possible the user upgraded then downgraded
                    Timber.w("Error in upgrading to database version 10");
                    e.printStackTrace();
                }
            }
            if (oldVersion < 11) {
                try {
                    db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
                            T_SHOW_DIST + " integer;");
                    db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
                            T_HIDE + " integer;");
                } catch(Exception e) {
                    // Catch errors, its possible the user upgraded then downgraded
                    Timber.w("Error in upgrading to database version 11");
                    e.printStackTrace();
                }
            }
            if (oldVersion < 12) {
                try {
                    db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN " +
                            T_TASK_COMMENT + " text;");
                } catch(Exception e) {
                    // Catch errors, its possible the user upgraded then downgraded
                    Timber.w("Error in upgrading to database version 12");
                    e.printStackTrace();
                }
            }
            */
            if (oldVersion < 13) {
                try {
                    moveInstancesTableToVersion13(db);
                } catch(Exception e) {
                    // Catch errors, its possible the user upgraded then downgraded
                    Timber.w("Error in upgrading to database version 13");
                    e.printStackTrace();
                }
            }
            Timber.w("Successfully upgraded database from version %d to %d, without destroying all the old data",
                    initialVersion, newVersion);
        }
    }

    private DatabaseHelper databaseHelper;

    private DatabaseHelper getDbHelper() {
        // wrapper to test and reset/set the dbHelper based upon the attachment state of the device.
        try {
            Collect.createODKDirs();
        } catch (RuntimeException e) {
            databaseHelper = null;
            Timber.e(e);    // smap
        }

        if (databaseHelper != null) {
            return databaseHelper;
        }
        databaseHelper = new DatabaseHelper(DATABASE_NAME);     // smap instance of InstanceDatabaseHelper

        return databaseHelper;
    }

    @Override
    public boolean onCreate() {
        if (!areStoragePermissionsGranted(getContext())) {
            Timber.i("Read and write permissions are required for this content provider to function.");
            return false;
        }

        // must be at the beginning of any activity that can be called from an external intent
        DatabaseHelper h = getDbHelper();
        return h != null;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        if (!areStoragePermissionsGranted(getContext())) {
            return null;
        }

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(INSTANCES_TABLE_NAME);
        qb.setProjectionMap(sInstancesProjectionMap);
        qb.setStrict(true);

        switch (URI_MATCHER.match(uri)) {
            case INSTANCES:
                break;

            case INSTANCE_ID:
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
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case INSTANCES:
                return InstanceColumns.CONTENT_TYPE;

            case INSTANCE_ID:
                return InstanceColumns.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (URI_MATCHER.match(uri) != INSTANCES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (!areStoragePermissionsGranted(getContext())) {
            return null;
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = System.currentTimeMillis();

        // Make sure that the fields are all set
        if (!values.containsKey(InstanceColumns.LAST_STATUS_CHANGE_DATE)) {
            values.put(InstanceColumns.LAST_STATUS_CHANGE_DATE, now);
        }

            if (!values.containsKey(InstanceColumns.STATUS)) {
                values.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_INCOMPLETE);
            }

        SQLiteDatabase db = getDbHelper().getWritableDatabase();
        long rowId = db.insert(INSTANCES_TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri instanceUri = ContentUris.withAppendedId(InstanceColumns.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(instanceUri, null);
            return instanceUri;
        }

        throw new SQLException("Failed to insert into the instances database.");
    }

    public static String getDisplaySubtext(Context context, String state, Date date) {
        try {
            if (state == null) {
                return new SimpleDateFormat(context.getString(R.string.added_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else if (InstanceProviderAPI.STATUS_INCOMPLETE.equalsIgnoreCase(state)) {
                return new SimpleDateFormat(context.getString(R.string.saved_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else if (InstanceProviderAPI.STATUS_COMPLETE.equalsIgnoreCase(state)) {
                return new SimpleDateFormat(context.getString(R.string.finalized_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else if (InstanceProviderAPI.STATUS_SUBMITTED.equalsIgnoreCase(state)) {
                return new SimpleDateFormat(context.getString(R.string.sent_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else if (InstanceProviderAPI.STATUS_SUBMISSION_FAILED.equalsIgnoreCase(state)) {
                return new SimpleDateFormat(
                        context.getString(R.string.sending_failed_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else {
                return new SimpleDateFormat(context.getString(R.string.added_on_date_at_time),
                        Locale.getDefault()).format(date);
            }
        } catch (IllegalArgumentException e) {
            Timber.e(e);
            return "";
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

                Timber.i("removed from content providers: %d image files, %d audio files,"
                        + " and %d video files.", images, audio, video);

                // delete all the files in the directory
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File f : files) {
                        // should make this recursive if we get worried about
                        // the media directory containing directories
                        f.delete();
                    }
                }
            }
            directory.delete();
        }
    }

    /**
     * This method removes the entry from the content provider, and also removes any associated
     * files.
     * files:  form.xml, [formmd5].formdef, formname-media {directory}
     */
    @Override
    public int delete(@NonNull Uri uri, String where, String[] whereArgs) {
        if (!areStoragePermissionsGranted(getContext())) {
            return 0;
        }
        int count = 0;

        SQLiteDatabase db = getDbHelper().getWritableDatabase();
        switch (URI_MATCHER.match(uri)) {
            case INSTANCES:
                Cursor del = null;
                try {
                    del = this.query(uri, null, where, whereArgs, null);
                    if (del != null && del.getCount() > 0) {
                        del.moveToFirst();
                        do {
                            String instanceFile = del.getString(
                                    del.getColumnIndex(InstanceColumns.INSTANCE_FILE_PATH));
                            File instanceDir = (new File(instanceFile)).getParentFile();
                            deleteAllFilesInDirectory(instanceDir);
                        } while (del.moveToNext());
                    }
                } finally {
                    if (del != null) {
                        del.close();
                    }
                }
                count = db.delete(INSTANCES_TABLE_NAME, where, whereArgs);
                break;

            case INSTANCE_ID:
                String instanceId = uri.getPathSegments().get(1);

                Cursor c = null;
                String status = null;
                try {
                    c = this.query(uri, null, where, whereArgs, null);
                    if (c != null && c.getCount() > 0) {
                        c.moveToFirst();
                        status = c.getString(c.getColumnIndex(InstanceColumns.STATUS));
                        do {
                            String instanceFile = c.getString(
                                    c.getColumnIndex(InstanceColumns.INSTANCE_FILE_PATH));
                            File instanceDir = (new File(instanceFile)).getParentFile();
                            deleteAllFilesInDirectory(instanceDir);
                        } while (c.moveToNext());
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }

                //We are going to update the status, if the form is submitted
                //We will not delete the record in table but we will delete the file
                if (status != null && status.equals(InstanceProviderAPI.STATUS_SUBMITTED)) {
                    ContentValues cv = new ContentValues();
                    cv.put(InstanceColumns.DELETED_DATE, System.currentTimeMillis());
                    count = Collect.getInstance().getContentResolver().update(uri, cv, null, null);
                } else {
                        String[] newWhereArgs;
                        if (whereArgs == null || whereArgs.length == 0) {
                            newWhereArgs = new String[] {instanceId};
                        } else {
                            newWhereArgs = new String[(whereArgs.length + 1)];
                            newWhereArgs[0] = instanceId;
                            System.arraycopy(whereArgs, 0, newWhereArgs, 1, whereArgs.length);
                        }

                    count =
                            db.delete(INSTANCES_TABLE_NAME,
                                        InstanceColumns._ID
                                                + "=?"
                                                + (!TextUtils.isEmpty(where) ? " AND ("
                                                + where + ')' : ""), newWhereArgs);
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String where, String[] whereArgs) {
        if (!areStoragePermissionsGranted(getContext())) {
            return 0;
        }
        int count = 0;

        SQLiteDatabase db = getDbHelper().getWritableDatabase();

        Long now = System.currentTimeMillis();

            // Don't update last status change date if an instance is being deleted
            if (values.containsKey(InstanceColumns.DELETED_DATE)) {
                values.remove(InstanceColumns.LAST_STATUS_CHANGE_DATE);
            }

            switch (URI_MATCHER.match(uri)) {
                case INSTANCES:
                    count = db.update(INSTANCES_TABLE_NAME, values, where, whereArgs);
                    break;

            case INSTANCE_ID:
                String instanceId = uri.getPathSegments().get(1);

                    String[] newWhereArgs;
                    if (whereArgs == null || whereArgs.length == 0) {
                        newWhereArgs = new String[] {instanceId};
                    } else {
                        newWhereArgs = new String[(whereArgs.length + 1)];
                        newWhereArgs[0] = instanceId;
                        System.arraycopy(whereArgs, 0, newWhereArgs, 1, whereArgs.length);
                    }

                count =
                            db.update(INSTANCES_TABLE_NAME,
                                    values,
                                    InstanceColumns._ID
                                            + "=?"
                                            + (!TextUtils.isEmpty(where) ? " AND ("
                                            + where + ')' : ""), newWhereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(InstanceProviderAPI.AUTHORITY, "instances", INSTANCES);
        URI_MATCHER.addURI(InstanceProviderAPI.AUTHORITY, "instances/#", INSTANCE_ID);

        sInstancesProjectionMap = new HashMap<>();
        sInstancesProjectionMap.put(InstanceColumns._ID, InstanceColumns._ID);
        sInstancesProjectionMap.put(InstanceColumns.DISPLAY_NAME, InstanceColumns.DISPLAY_NAME);
        sInstancesProjectionMap.put(InstanceColumns.SUBMISSION_URI, InstanceColumns.SUBMISSION_URI);
        sInstancesProjectionMap.put(InstanceColumns.CAN_EDIT_WHEN_COMPLETE,
                InstanceColumns.CAN_EDIT_WHEN_COMPLETE);
        sInstancesProjectionMap.put(InstanceColumns.INSTANCE_FILE_PATH,
                InstanceColumns.INSTANCE_FILE_PATH);
        sInstancesProjectionMap.put(InstanceColumns.JR_FORM_ID, InstanceColumns.JR_FORM_ID);
        sInstancesProjectionMap.put(InstanceColumns.JR_VERSION, InstanceColumns.JR_VERSION);
        sInstancesProjectionMap.put(InstanceColumns.STATUS, InstanceColumns.STATUS);
        sInstancesProjectionMap.put(T_REPEAT, T_REPEAT);                // smap
        sInstancesProjectionMap.put(T_UPDATEID, T_UPDATEID);            // smap
        sInstancesProjectionMap.put(T_LOCATION_TRIGGER, T_LOCATION_TRIGGER);    // smap
        sInstancesProjectionMap.put(T_SURVEY_NOTES, T_SURVEY_NOTES);    // smap
        sInstancesProjectionMap.put(T_UPDATED, T_UPDATED);              // smap
        sInstancesProjectionMap.put(InstanceColumns.LAST_STATUS_CHANGE_DATE,
                InstanceColumns.LAST_STATUS_CHANGE_DATE);
        sInstancesProjectionMap.put(SOURCE, SOURCE);                // smap
        sInstancesProjectionMap.put(FORM_PATH, FORM_PATH);          // smap
        sInstancesProjectionMap.put(ACT_LON, ACT_LON);              // smap
        sInstancesProjectionMap.put(ACT_LAT, ACT_LAT);              // smap
        sInstancesProjectionMap.put(SCHED_LON, SCHED_LON);          // smap
        sInstancesProjectionMap.put(SCHED_LAT, SCHED_LAT);          // smap
        sInstancesProjectionMap.put(T_TITLE, T_TITLE);              // smap
        sInstancesProjectionMap.put(T_SCHED_START, T_SCHED_START);  // smap
        sInstancesProjectionMap.put(T_ACT_FINISH, T_ACT_FINISH);    // smap
        sInstancesProjectionMap.put(T_ADDRESS, T_ADDRESS);          // smap
        sInstancesProjectionMap.put(T_GEOM, T_GEOM);                // smap
        sInstancesProjectionMap.put(T_GEOM_TYPE, T_GEOM_TYPE);      // smap
        sInstancesProjectionMap.put(T_IS_SYNC, T_IS_SYNC);          // smap
        sInstancesProjectionMap.put(T_ASS_ID, T_ASS_ID);            // smap
        sInstancesProjectionMap.put(T_TASK_STATUS, T_TASK_STATUS);  // smap
        sInstancesProjectionMap.put(T_TASK_COMMENT, T_TASK_COMMENT);  // smap
        sInstancesProjectionMap.put(T_SHOW_DIST, T_SHOW_DIST);      // smap
        sInstancesProjectionMap.put(T_HIDE, T_HIDE);                // smap
        sInstancesProjectionMap.put(UUID, UUID);                    // smap
        sInstancesProjectionMap.put(InstanceColumns.DELETED_DATE, InstanceColumns.DELETED_DATE);
    }


    private static void moveInstancesTableToVersion13(SQLiteDatabase db) {
        List<String> columnNamesPrev = getInstancesColumnNames(db);

        String temporaryTableName = INSTANCES_TABLE_NAME + "_tmp";

        // onDowngrade in Collect v1.22 always failed to clean up the temporary table so remove it now.
        // Going from v1.23 to v1.22 and back to v1.23 will result in instance status information
        // being lost.
        CustomSQLiteQueryBuilder
                .begin(db)
                .dropIfExists(temporaryTableName)
                .end();

        createInstancesTableV13(db, temporaryTableName);

        // Only select columns from the existing table that are also relevant to v13
        columnNamesPrev.retainAll(new ArrayList<>(Arrays.asList(COLUMN_NAMES_V13)));

        CustomSQLiteQueryBuilder
                .begin(db)
                .insertInto(temporaryTableName)
                .columnsForInsert(columnNamesPrev.toArray(new String[0]))
                .select()
                .columnsForSelect(columnNamesPrev.toArray(new String[0]))
                .from(INSTANCES_TABLE_NAME)
                .end();

        CustomSQLiteQueryBuilder
                .begin(db)
                .dropIfExists(INSTANCES_TABLE_NAME)
                .end();

        CustomSQLiteQueryBuilder
                .begin(db)
                .renameTable(temporaryTableName)
                .to(INSTANCES_TABLE_NAME)
                .end();
    }

    private static void createInstancesTableV13(SQLiteDatabase db, String name) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + name + " ("
                + _ID + " integer primary key, "
                + DISPLAY_NAME + " text not null, "
                + SUBMISSION_URI + " text, "
                + CAN_EDIT_WHEN_COMPLETE + " text, "
                + INSTANCE_FILE_PATH + " text not null, "
                + JR_FORM_ID + " text not null, "
                + JR_VERSION + " text, "
                + STATUS + " text not null, "
                + LAST_STATUS_CHANGE_DATE + " date not null, "
                + DELETED_DATE + " date, "
                + SOURCE + " text, "		    // smap
                + FORM_PATH + " text, "		    // smap
                + ACT_LON + " double, "		    // smap
                + ACT_LAT + " double, "		    // smap
                + SCHED_LON + " double, "		// smap
                + SCHED_LAT + " double, "		// smap
                + T_TITLE + " text, "		    // smap
                + T_SCHED_START + " long, "		// smap
                + T_ACT_START + " long, "		// smap
                + T_ACT_FINISH + " long, "		// smap
                + T_ADDRESS + " text, "		    // smap
                + T_GEOM + " text, "		    // smap
                + T_GEOM_TYPE + " text, "		// smap
                + T_IS_SYNC + " text, "		    // smap
                + T_ASS_ID + " long, "		    // smap
                + T_TASK_STATUS + " text, "		// smap
                + T_TASK_COMMENT + " text, "    // smap
                + T_REPEAT + " integer, "		// smap
                + T_UPDATEID + " text, "		// smap
                + T_LOCATION_TRIGGER + " text, " // smap
                + T_SURVEY_NOTES + " text, "    // smap
                + UUID + " text, "		        // smap
                + T_UPDATED + " integer, "      // smap
                + T_SHOW_DIST + " integer, "   // smap
                + T_HIDE + " integer "        // smap
                + ");");
    }

    static List<String> getInstancesColumnNames(SQLiteDatabase db) {
        String[] columnNames;
        try (Cursor c = db.query(INSTANCES_TABLE_NAME, null, null, null, null, null, null)) {
            columnNames = c.getColumnNames();
        }

        // Build a full-featured ArrayList rather than the limited array-backed List from asList
        return new ArrayList<>(Arrays.asList(columnNames));
    }
}
