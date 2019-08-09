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
import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.ItemsetDbAdapter;
import org.odk.collect.android.database.ODKSQLiteOpenHelper;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.utilities.CustomSQLiteQueryBuilder;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.MediaUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static android.provider.BaseColumns._ID;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.AUTO_DELETE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.AUTO_SEND;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.CONTENT_NEWEST_FORMS_BY_FORMID_URI;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.CONTENT_URI;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DATE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DESCRIPTION;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DISPLAY_NAME;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.FORM_FILE_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JR_FORM_ID;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JR_VERSION;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.LANGUAGE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.LAST_DETECTED_FORM_VERSION_HASH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.MD5_HASH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.PROJECT;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.SOURCE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.SUBMISSION_URI;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.TASKS_ONLY;
import static org.odk.collect.android.utilities.PermissionUtils.areStoragePermissionsGranted;

public class FormsProvider extends ContentProvider {

    private static final String t = "FormsProvider";

    private static final String DATABASE_NAME = "forms.db";
    private static final int DATABASE_VERSION = 11;    // smap must be greater than 7 (the odk version)
    private static final String FORMS_TABLE_NAME = "forms";

    private static HashMap<String, String> sFormsProjectionMap;

    private static final int FORMS = 1;
    private static final int FORM_ID = 2;
    // Forms unique by ID, keeping only the latest one downloaded
    private static final int NEWEST_FORMS_BY_FORM_ID = 3;

    private static final UriMatcher URI_MATCHER;

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends ODKSQLiteOpenHelper {
        // These exist in database versions 2 and 3, but not in 4...
        private static final String TEMP_FORMS_TABLE_NAME = "forms_v4";
        private static final String MODEL_VERSION = "modelVersion";

        DatabaseHelper(String databaseName) {
            super(Collect.METADATA_PATH, databaseName, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            onCreateNamed(db);
        }

        private void onCreateNamed(SQLiteDatabase db) {
            createFormsTableV11(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            int initialVersion = oldVersion;

            if (oldVersion < 11) {
                try {
                    upgradeToVersion11(db);
                } catch (Exception e) {
                    // Catch errors, its possible the user upgraded then downgraded
                    Timber.w("Error in upgrading to forms database version 11");
                    e.printStackTrace();
                }
            }
        }
    }

    // smap
    private static void upgradeToVersion11(SQLiteDatabase db) {
        String temporaryTable = FORMS_TABLE_NAME + "_tmp";
        String[] formsTableColumnsInV11 = new String[] {
                _ID,
                DISPLAY_NAME,
                DESCRIPTION,
                JR_FORM_ID,
                JR_VERSION,
                MD5_HASH,
                DATE,
                FORM_MEDIA_PATH,
                FORM_FILE_PATH,
                LANGUAGE,
                SUBMISSION_URI,
                BASE64_RSA_PUBLIC_KEY,
                JRCACHE_FILE_PATH,
                AUTO_SEND,
                AUTO_DELETE,
                LAST_DETECTED_FORM_VERSION_HASH,
                PROJECT,        // smap
                TASKS_ONLY,     // smap
                SOURCE          // smap
        };

        CustomSQLiteQueryBuilder
                .begin(db)
                .renameTable(FORMS_TABLE_NAME)
                .to(temporaryTable)
                .end();

        createFormsTableV11(db);

        CustomSQLiteQueryBuilder
                .begin(db)
                .insertInto(FORMS_TABLE_NAME)
                .columnsForInsert(formsTableColumnsInV11)
                .select()
                .columnsForSelect(formsTableColumnsInV11)
                .from(temporaryTable)
                .end();

        CustomSQLiteQueryBuilder
                .begin(db)
                .dropIfExists(temporaryTable)
                .end();
    }

    private static void createFormsTableV11(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + FORMS_TABLE_NAME + " ("
                + _ID + " integer primary key, "
                + DISPLAY_NAME + " text not null, "
                + DESCRIPTION + " text, "
                + JR_FORM_ID + " text not null, "
                + JR_VERSION + " text, "
                + MD5_HASH + " text not null, "
                + DATE + " integer not null, " // milliseconds
                + FORM_MEDIA_PATH + " text not null, "
                + FORM_FILE_PATH + " text not null, "
                + LANGUAGE + " text, "
                + SUBMISSION_URI + " text, "
                + BASE64_RSA_PUBLIC_KEY + " text, "
                + JRCACHE_FILE_PATH + " text not null, "
                + AUTO_SEND + " text, "
                + AUTO_DELETE + " text, "
                + LAST_DETECTED_FORM_VERSION_HASH + " text,"
                + PROJECT + " text,"
                + TASKS_ONLY + " text,"
                + SOURCE + " text,"

                + "displaySubtext text "   // Smap keep for downgrading
                +");");
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
        mDbHelper = new DatabaseHelper(DATABASE_NAME);      // smap
        return mDbHelper;
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
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        if (!areStoragePermissionsGranted(getContext())) {
            return null;
        }

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(FORMS_TABLE_NAME);
        qb.setProjectionMap(sFormsProjectionMap);
        qb.setStrict(true);

        String groupBy = null;
        switch (URI_MATCHER.match(uri)) {
            case FORMS:
                break;

            case FORM_ID:
                qb.appendWhere(_ID + "="
                        + uri.getPathSegments().get(1));
                break;

                // Only include the latest form that was downloaded with each form_id
                case NEWEST_FORMS_BY_FORM_ID:
                    Map<String, String> filteredProjectionMap = new HashMap<>(sFormsProjectionMap);
                    filteredProjectionMap.put(DATE, FormsColumns.MAX_DATE);

                    qb.setProjectionMap(filteredProjectionMap);
                    groupBy = JR_FORM_ID;
                    break;

            default:
                //throw new IllegalArgumentException("Unknown URI " + uri);     smap don't throw exception this prevents crash when launching from fill blank form
        }

        Cursor c = qb.query(getDbHelper().getReadableDatabase(), projection, selection, selectionArgs, groupBy, null, sortOrder);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case FORMS:
            case NEWEST_FORMS_BY_FORM_ID:
                return FormsColumns.CONTENT_TYPE;

            case FORM_ID:
                return FormsColumns.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public synchronized Uri insert(@NonNull Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (URI_MATCHER.match(uri) != FORMS) {
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

        if (!values.containsKey(FORM_FILE_PATH)) {
            throw new IllegalArgumentException(FORM_FILE_PATH
                    + " must be specified.");
        }

        // Normalize the file path.
        // (don't trust the requester).
        String filePath = values.getAsString(FORM_FILE_PATH);
        File form = new File(filePath);
        filePath = form.getAbsolutePath(); // normalized
        values.put(FORM_FILE_PATH, filePath);

        Long now = System.currentTimeMillis();

        // Make sure that the necessary fields are all set
        if (!values.containsKey(DATE)) {
            values.put(DATE, now);
        }

            if (!values.containsKey(DISPLAY_NAME)) {
                values.put(DISPLAY_NAME, form.getName());
            }

        // don't let users put in a manual md5 hash
        if (values.containsKey(MD5_HASH)) {
            values.remove(MD5_HASH);
        }
        String md5 = FileUtils.getMd5Hash(form);
        values.put(MD5_HASH, md5);

            if (!values.containsKey(JRCACHE_FILE_PATH)) {
                String cachePath = Collect.CACHE_PATH + File.separator + md5
                        + ".formdef";
                values.put(JRCACHE_FILE_PATH, cachePath);
            }
            if (!values.containsKey(FORM_MEDIA_PATH)) {
                values.put(FORM_MEDIA_PATH, FileUtils.constructMediaPath(filePath));
            }

        SQLiteDatabase db = getDbHelper().getWritableDatabase();

        // first try to see if a record with this filename already exists...
        String[] projection = {_ID, FORM_FILE_PATH};
        String[] selectionArgs = {filePath};
        String selection = FORM_FILE_PATH + "=?";
        Cursor c = null;
        try {
            c = db.query(FORMS_TABLE_NAME, projection, selection,
                    selectionArgs, null, null, null);
            if (c.getCount() > 0) {
                // already exists
                throw new SQLException("FAILED Insert into " + uri
                        + " -- row already exists for form definition file: "
                        + filePath);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        long rowId = db.insert(FORMS_TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri formUri = ContentUris.withAppendedId(CONTENT_URI,
                    rowId);
            getContext().getContentResolver().notifyChange(formUri, null);
                getContext().getContentResolver().notifyChange(FormsColumns.CONTENT_NEWEST_FORMS_BY_FORMID_URI, null);
            return formUri;
        }

        throw new SQLException("Failed to insert into the forms database.");
    }

    private void deleteFileOrDir(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            if (file.isDirectory()) {
                // delete any media entries for files in this directory...
                int images = MediaUtils
                        .deleteImagesInFolderFromMediaProvider(file);
                int audio = MediaUtils
                        .deleteAudioInFolderFromMediaProvider(file);
                int video = MediaUtils
                        .deleteVideoInFolderFromMediaProvider(file);

                Timber.i("removed from content providers: %d image files, %d audio files, and %d"
                        + " video files.", images, audio, video);

                // delete all the containing files
                File[] files = file.listFiles();
                if (files != null) {
                    for (File f : files) {
                        // should make this recursive if we get worried about
                        // the media directory containing directories
                        Timber.i("attempting to delete file: %s", f.getAbsolutePath());
                        f.delete();
                    }
                }
            }
            file.delete();
            Timber.i("attempting to delete file: %s", file.getAbsolutePath());
        }
    }

    /**
     * This method removes the entry from the content provider, and also removes
     * any associated files. files: form.xml, [formmd5].formdef, formname-media
     * {directory}
     */
    @Override
    public int delete(@NonNull Uri uri, String where, String[] whereArgs) {
        if (!areStoragePermissionsGranted(getContext())) {
            return 0;
        }
        SQLiteDatabase db = getDbHelper().getWritableDatabase();
        int count;

            switch (URI_MATCHER.match(uri)) {
            case FORMS:
                Cursor del = null;
                try {
                    del = this.query(uri, null, where, whereArgs, null);
                        if (del != null && del.getCount() > 0) {
                        del.moveToFirst();
                        do {
                            deleteFileOrDir(del
                                    .getString(del
                                            .getColumnIndex(JRCACHE_FILE_PATH)));
                            String formFilePath = del.getString(del
                                    .getColumnIndex(FORM_FILE_PATH));
                            deleteFileOrDir(formFilePath);
                            deleteFileOrDir(del.getString(del
                                    .getColumnIndex(FORM_MEDIA_PATH)));
                        } while (del.moveToNext());
                    }
                } finally {
                    if (del != null) {
                        del.close();
                    }
                }
                count = db.delete(FORMS_TABLE_NAME, where, whereArgs);
                break;

            case FORM_ID:
                String formId = uri.getPathSegments().get(1);

                Cursor c = null;
                try {
                    c = this.query(uri, null, where, whereArgs, null);
                    // This should only ever return 1 record.
                        if (c != null && c.getCount() > 0) {
                        c.moveToFirst();
                        do {
                            deleteFileOrDir(c.getString(c
                                    .getColumnIndex(JRCACHE_FILE_PATH)));
                            String formFilePath = c.getString(c
                                    .getColumnIndex(FORM_FILE_PATH));
                            deleteFileOrDir(formFilePath);
                            deleteFileOrDir(c.getString(c
                                    .getColumnIndex(FORM_MEDIA_PATH)));

                            try {
                                // get rid of the old tables
                                ItemsetDbAdapter ida = new ItemsetDbAdapter();
                                ida.open();
                                ida.delete(c.getString(c
                                        .getColumnIndex(FORM_MEDIA_PATH))
                                        + "/itemsets.csv");
                                ida.close();
                            } catch (Exception e) {
                                // if something else is accessing the provider this may not exist
                                // so catch it and move on.
                            }

                        } while (c.moveToNext());
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }

                count = db.delete(
                        FORMS_TABLE_NAME,
                        _ID
                                    + "=?"
                                + (!TextUtils.isEmpty(where) ? " AND (" + where
                                    + ')' : ""), prepareWhereArgs(whereArgs, formId));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        getContext().getContentResolver().notifyChange(CONTENT_NEWEST_FORMS_BY_FORMID_URI, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where,
                      String[] whereArgs) {

        if (!areStoragePermissionsGranted(getContext())) {
            return 0;
        }
        SQLiteDatabase db = getDbHelper().getWritableDatabase();
        int count = 0;
        switch (URI_MATCHER.match(uri)) {
            case FORMS:
                // don't let users manually update md5
                if (values.containsKey(MD5_HASH)) {
                    values.remove(MD5_HASH);
                }
                // if values contains path, then all filepaths and md5s will get
                // updated
                // this probably isn't a great thing to do.
                if (values.containsKey(FORM_FILE_PATH)) {
                    String formFile = values
                            .getAsString(FORM_FILE_PATH);
                    values.put(MD5_HASH,
                            FileUtils.getMd5Hash(new File(formFile)));
                }

                Cursor c = null;
                try {
                    c = this.query(uri, null, where, whereArgs, null);

                    if (c != null && c.getCount() > 0) {
                        c.moveToPosition(-1);
                        while (c.moveToNext()) {
                            // before updating the paths, delete all the files
                            if (values.containsKey(FORM_FILE_PATH)) {
                                String newFile = values
                                        .getAsString(FORM_FILE_PATH);
                                String delFile = c
                                        .getString(c
                                                .getColumnIndex(FORM_FILE_PATH));
                                    if (!newFile.equalsIgnoreCase(delFile)) {
                                    deleteFileOrDir(delFile);
                                }

                                // either way, delete the old cache because we'll
                                // calculate a new one.
                                deleteFileOrDir(c
                                        .getString(c
                                                .getColumnIndex(JRCACHE_FILE_PATH)));
                            }
                        }
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }

                count = db.update(FORMS_TABLE_NAME, values, where, whereArgs);
                break;

            case FORM_ID:
                String formId = uri.getPathSegments().get(1);
                // Whenever file paths are updated, delete the old files.

                Cursor update = null;
                try {
                    update = this.query(uri, null, where, whereArgs, null);

                    // This should only ever return 1 record.
                    if (update != null && update.getCount() > 0) {
                        update.moveToFirst();

                        // don't let users manually update md5
                        if (values.containsKey(MD5_HASH)) {
                            values.remove(MD5_HASH);
                        }

                        // the order here is important (jrcache needs to be before
                        // form file)
                        // because we update the jrcache file if there's a new form
                        // file
                        if (values.containsKey(JRCACHE_FILE_PATH)) {
                            deleteFileOrDir(update
                                    .getString(update
                                            .getColumnIndex(JRCACHE_FILE_PATH)));
                        }

                        if (values.containsKey(FORM_FILE_PATH)) {
                            String formFile = values
                                    .getAsString(FORM_FILE_PATH);
                            String oldFile = update.getString(update
                                    .getColumnIndex(FORM_FILE_PATH));

                            if (formFile == null || !formFile.equalsIgnoreCase(oldFile)) {
                                deleteFileOrDir(oldFile);
                            }

                                // we're updating our file, so update the md5
                                // and get rid of the cache (doesn't harm anything)
                                deleteFileOrDir(update
                                        .getString(update
                                                .getColumnIndex(JRCACHE_FILE_PATH)));
                                String newMd5 = FileUtils
                                        .getMd5Hash(new File(formFile));
                                values.put(MD5_HASH, newMd5);
                                values.put(JRCACHE_FILE_PATH,
                                        Collect.CACHE_PATH + File.separator + newMd5
                                                + ".formdef");
                            }

                            count = db.update(
                                    FORMS_TABLE_NAME,
                                    values,
                                    _ID
                                            + "=?"
                                        + (!TextUtils.isEmpty(where) ? " AND ("
                                            + where + ')' : ""), prepareWhereArgs(whereArgs, formId));
                    } else {
                        Timber.e("Attempting to update row that does not exist");
                    }
                } finally {
                    if (update != null) {
                        update.close();
                    }
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        getContext().getContentResolver().notifyChange(FormsColumns.CONTENT_NEWEST_FORMS_BY_FORMID_URI, null);
        return count;
    }

    @NonNull
    private String[] prepareWhereArgs(String[] whereArgs, String formId) {
        String[] newWhereArgs;
        if (whereArgs == null || whereArgs.length == 0) {
            newWhereArgs = new String[] {formId};
        } else {
            newWhereArgs = new String[(whereArgs.length + 1)];
            newWhereArgs[0] = formId;
            System.arraycopy(whereArgs, 0, newWhereArgs, 1, whereArgs.length);
        }
        return newWhereArgs;
    }

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(FormsProviderAPI.AUTHORITY, "forms", FORMS);
        URI_MATCHER.addURI(FormsProviderAPI.AUTHORITY, "forms/#", FORM_ID);
        // Only available for query and type
        URI_MATCHER.addURI(FormsProviderAPI.AUTHORITY, CONTENT_NEWEST_FORMS_BY_FORMID_URI.getPath().replaceAll("^/+", ""), NEWEST_FORMS_BY_FORM_ID);

        sFormsProjectionMap = new HashMap<>();
        sFormsProjectionMap.put(_ID, _ID);
        sFormsProjectionMap.put(DISPLAY_NAME, DISPLAY_NAME);
        sFormsProjectionMap.put(DESCRIPTION, DESCRIPTION);
        sFormsProjectionMap.put(JR_FORM_ID, JR_FORM_ID);
        sFormsProjectionMap.put(JR_VERSION, JR_VERSION);
        sFormsProjectionMap.put(SUBMISSION_URI, SUBMISSION_URI);
        sFormsProjectionMap.put(PROJECT, PROJECT);                              // smap
        sFormsProjectionMap.put(TASKS_ONLY, TASKS_ONLY);                        // smap
        sFormsProjectionMap.put(SOURCE, SOURCE);                                // smap
        sFormsProjectionMap.put(BASE64_RSA_PUBLIC_KEY, BASE64_RSA_PUBLIC_KEY);
        sFormsProjectionMap.put(MD5_HASH, MD5_HASH);
        sFormsProjectionMap.put(DATE, DATE);
        sFormsProjectionMap.put(FORM_MEDIA_PATH, FORM_MEDIA_PATH);
        sFormsProjectionMap.put(FORM_FILE_PATH, FORM_FILE_PATH);
        sFormsProjectionMap.put(JRCACHE_FILE_PATH, JRCACHE_FILE_PATH);
        sFormsProjectionMap.put(LANGUAGE, LANGUAGE);
        sFormsProjectionMap.put(AUTO_DELETE, AUTO_DELETE);
        sFormsProjectionMap.put(AUTO_SEND, AUTO_SEND);
        sFormsProjectionMap.put(LAST_DETECTED_FORM_VERSION_HASH, LAST_DETECTED_FORM_VERSION_HASH);
    }
}
