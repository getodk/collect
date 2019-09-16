package org.odk.collect.android.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.helpers.XPathDatabaseHelper;
import org.odk.collect.android.provider.XPathProviderAPI.XPathsColumns;
import org.odk.collect.android.utilities.MediaUtils;

import java.io.File;
import java.util.HashMap;

import timber.log.Timber;

import static org.odk.collect.android.database.helpers.XPathDatabaseHelper.XPATH_TABLE_NAME;
import static org.odk.collect.android.utilities.PermissionUtils.areStoragePermissionsGranted;

public class XPathExprIndexProvider extends ContentProvider {

    private static XPathDatabaseHelper dbHelper;

    private synchronized XPathDatabaseHelper getDbHelper() {
        // wrapper to test and reset/set the dbHelper based upon the attachment state of the device.
        try {
            Collect.createODKDirs();
        } catch (RuntimeException e) {
            return null;
        }

        if (dbHelper == null) {
            dbHelper = new XPathDatabaseHelper();
        }

        return dbHelper;
    }

    @Override
    public boolean onCreate() {

        if (!areStoragePermissionsGranted(getContext())) {
            Timber.i("Read and write permissions are required for this content provider to function.");
            return false;
        }

        // must be at the beginning of any activity that can be called from an external intent
        XPathDatabaseHelper h = getDbHelper();
        return h != null;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        if (!areStoragePermissionsGranted(getContext())) {
            return null;
        }

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(XPATH_TABLE_NAME);
        qb.setStrict(true);

        Cursor c = null;
        String groupBy = null;
        XPathDatabaseHelper xPathDatabaseHelper = getDbHelper();
        if (xPathDatabaseHelper != null) { c = qb.query(xPathDatabaseHelper.getReadableDatabase(), projection, selection, selectionArgs, groupBy, null, sortOrder);
            // Tell the cursor what uri to watch, so it knows when its source data changes
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return c;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return "xpath_expr";
    }

    @Override
    public synchronized Uri insert(@NonNull Uri uri, ContentValues initialValues) {

        if (!areStoragePermissionsGranted(getContext())) {
            return null;
        }

        XPathDatabaseHelper xPathDatabaseHelper = getDbHelper();
        if (xPathDatabaseHelper != null) {
            ContentValues values;
            if (initialValues != null) {
                values = new ContentValues(initialValues);
            } else {
                values = new ContentValues();
            }

            if (!values.containsKey(XPathsColumns.EVAL_EXPR)) {
                throw new IllegalArgumentException(XPathsColumns.EVAL_EXPR
                        + " must be specified.");
            }

            SQLiteDatabase db = xPathDatabaseHelper.getWritableDatabase();

            long rowId = db.insert(XPATH_TABLE_NAME, null, values);
            if (rowId > 0) {
                Uri formUri = ContentUris.withAppendedId(XPathsColumns.CONTENT_URI,
                        rowId);
                getContext().getContentResolver().notifyChange(formUri, null);
                return formUri;
            }
        }

        throw new SQLException("Failed to insert into the forms database.");
    }

    /**
     * This method removes the entry from the content provider, and also removes
     * any associated files. files: form.xml, [formmd5].formdef, formname-media
     * {directory}
     */
    @Override
    public int delete(@NonNull Uri uri, String where, String[] whereArgs) {

        return -1;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where,
                      String[] whereArgs) {

        return -1;
    }

}
