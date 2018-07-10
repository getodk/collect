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
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.helpers.InstancesDatabaseHelper;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.utilities.MediaUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import timber.log.Timber;

import static org.odk.collect.android.database.helpers.InstancesDatabaseHelper.INSTANCES_TABLE_NAME;
import static org.odk.collect.android.utilities.PermissionUtils.checkIfStoragePermissionsGranted;

public class InstanceProvider extends ContentProvider {
    private static HashMap<String, String> sInstancesProjectionMap;

    private static final int INSTANCES = 1;
    private static final int INSTANCE_ID = 2;

    private static final UriMatcher URI_MATCHER;
    
    private InstancesDatabaseHelper getDbHelper() {
        InstancesDatabaseHelper databaseHelper = null;
        // wrapper to test and reset/set the dbHelper based upon the attachment state of the device.
        try {
            Collect.createODKDirs();
        } catch (RuntimeException e) {
            databaseHelper = null;
            return null;
        }

        if (databaseHelper != null) {
            return databaseHelper;
        }
        databaseHelper = new InstancesDatabaseHelper();
        return databaseHelper;
    }

    @Override
    public boolean onCreate() {
        if (!checkIfStoragePermissionsGranted(getContext())) {
            Timber.i("Read and write permissions are required for this content provider to function.");
            return false;
        }

        // must be at the beginning of any activity that can be called from an external intent
        InstancesDatabaseHelper h = getDbHelper();
        return h != null;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        if (!checkIfStoragePermissionsGranted(getContext())) {
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

        Cursor c = null;
        InstancesDatabaseHelper instancesDatabaseHelper = getDbHelper();
        if (instancesDatabaseHelper != null) {
            c = qb.query(instancesDatabaseHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);

            // Tell the cursor what uri to watch, so it knows when its source data changes
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }

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

        if (!checkIfStoragePermissionsGranted(getContext())) {
            return null;
        }

        InstancesDatabaseHelper instancesDatabaseHelper = getDbHelper();
        if (instancesDatabaseHelper != null) {
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

            if (!values.containsKey(InstanceColumns.DISPLAY_SUBTEXT)) {
                Date today = new Date();
                String text = getDisplaySubtext(InstanceProviderAPI.STATUS_INCOMPLETE, today);
                values.put(InstanceColumns.DISPLAY_SUBTEXT, text);
            }

            if (!values.containsKey(InstanceColumns.STATUS)) {
                values.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_INCOMPLETE);
            }

            long rowId = instancesDatabaseHelper.getWritableDatabase().insert(INSTANCES_TABLE_NAME, null, values);
            if (rowId > 0) {
                Uri instanceUri = ContentUris.withAppendedId(InstanceColumns.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(instanceUri, null);
                Collect.getInstance().getActivityLogger().logActionParam(this, "insert",
                        instanceUri.toString(), values.getAsString(InstanceColumns.INSTANCE_FILE_PATH));
                return instanceUri;
            }
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    private String getDisplaySubtext(String state, Date date) {
        try {
            if (state == null) {
                return new SimpleDateFormat(getContext().getString(R.string.added_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else if (InstanceProviderAPI.STATUS_INCOMPLETE.equalsIgnoreCase(state)) {
                return new SimpleDateFormat(getContext().getString(R.string.saved_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else if (InstanceProviderAPI.STATUS_COMPLETE.equalsIgnoreCase(state)) {
                return new SimpleDateFormat(getContext().getString(R.string.finalized_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else if (InstanceProviderAPI.STATUS_SUBMITTED.equalsIgnoreCase(state)) {
                return new SimpleDateFormat(getContext().getString(R.string.sent_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else if (InstanceProviderAPI.STATUS_SUBMISSION_FAILED.equalsIgnoreCase(state)) {
                return new SimpleDateFormat(
                        getContext().getString(R.string.sending_failed_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else {
                return new SimpleDateFormat(getContext().getString(R.string.added_on_date_at_time),
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
     * This method removes the entry from the content provider, and also removes any associated
     * files.
     * files:  form.xml, [formmd5].formdef, formname-media {directory}
     */
    @Override
    public int delete(@NonNull Uri uri, String where, String[] whereArgs) {
        if (!checkIfStoragePermissionsGranted(getContext())) {
            return 0;
        }
        int count = 0;
        InstancesDatabaseHelper instancesDatabaseHelper = getDbHelper();
        if (instancesDatabaseHelper != null) {
            SQLiteDatabase db = instancesDatabaseHelper.getWritableDatabase();

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
                                Collect.getInstance().getActivityLogger().logAction(this, "delete",
                                        instanceFile);
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
                                Collect.getInstance().getActivityLogger().logAction(this, "delete",
                                        instanceFile);
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
        }

        return count;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String where, String[] whereArgs) {
        if (!checkIfStoragePermissionsGranted(getContext())) {
            return 0;
        }
        int count = 0;
        InstancesDatabaseHelper instancesDatabaseHelper = getDbHelper();
        if (instancesDatabaseHelper != null) {
            SQLiteDatabase db = instancesDatabaseHelper.getWritableDatabase();

            Long now = System.currentTimeMillis();

            // Make sure that the fields are all set
            if (!values.containsKey(InstanceColumns.LAST_STATUS_CHANGE_DATE)) {
                values.put(InstanceColumns.LAST_STATUS_CHANGE_DATE, now);
            }

            String status;
            switch (URI_MATCHER.match(uri)) {
                case INSTANCES:
                    if (values.containsKey(InstanceColumns.STATUS)) {
                        status = values.getAsString(InstanceColumns.STATUS);

                        if (!values.containsKey(InstanceColumns.DISPLAY_SUBTEXT)) {
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

                        if (!values.containsKey(InstanceColumns.DISPLAY_SUBTEXT)) {
                            Date today = new Date();
                            String text = getDisplaySubtext(status, today);
                            values.put(InstanceColumns.DISPLAY_SUBTEXT, text);
                        }
                    }

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
        }

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
        sInstancesProjectionMap.put(InstanceColumns.LAST_STATUS_CHANGE_DATE,
                InstanceColumns.LAST_STATUS_CHANGE_DATE);
        sInstancesProjectionMap.put(InstanceColumns.DISPLAY_SUBTEXT,
                InstanceColumns.DISPLAY_SUBTEXT);
        sInstancesProjectionMap.put(InstanceColumns.DELETED_DATE, InstanceColumns.DELETED_DATE);
    }
}
