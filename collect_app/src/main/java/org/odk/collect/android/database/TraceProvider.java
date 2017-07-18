

package org.odk.collect.android.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.TraceProviderAPI.TraceColumns;

/**
 *
 */
public class TraceProvider extends ContentProvider {

    private static final String t = "TraceProvider";

    private static final String DATABASE_NAME = "trace.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "trace";

    private static final int TRACES = 1;
    private static final int TRACE_ID = 2;

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
           db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
               + TraceColumns._ID + " integer primary key, "
               + TraceColumns.SOURCE + " text, "
               + TraceColumns.LAT + " double not null, "
               + TraceColumns.LON + " double not null, "
               + TraceColumns.TIME + " long not null "
               + ");");
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	int initialVersion = oldVersion;
        	if ( oldVersion == 1 ) {
        		db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " +
        					TraceColumns.SOURCE + " text;");
        	}

            Log.w(t, "Successfully upgraded traces database from version " + initialVersion + " to " + newVersion
                    + ", without destroying all the old data");
        }
    }

    private DatabaseHelper mDbHelper;

    private DatabaseHelper getDbHelper() {

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
    public Cursor query(Uri uri, String[] projection, String where, String[] selectionArgs,
            String sortOrder) {

        SQLiteDatabase db = getDbHelper().getWritableDatabase();

        Cursor c = null;
        switch (sUriMatcher.match(uri)) {
            case TRACES:
                c = db.query(TABLE_NAME, projection, where, selectionArgs, null, null, sortOrder, null);
                break;

            case TRACE_ID:
                String instanceId = uri.getPathSegments().get(1);
                c = db.query(TABLE_NAME, projection, TraceColumns._ID + "=" + instanceId
                        + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), selectionArgs, null, null, sortOrder, null);

                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }


    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case TRACES:
                return TraceColumns.CONTENT_TYPE;

            case TRACE_ID:
                return TraceColumns.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != TRACES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        SQLiteDatabase db = getDbHelper().getWritableDatabase();
        long rowId = db.insert(TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri traceUri = ContentUris.withAppendedId(TraceColumns.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(traceUri, null);
            return traceUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
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
            case TRACES:
                count = db.delete(TABLE_NAME, where, whereArgs);
                break;

            case TRACE_ID:
                String instanceId = uri.getPathSegments().get(1);
                count =
                    db.delete(TABLE_NAME,
                        TraceColumns._ID + "=" + instanceId
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

        int count;
        String status = null;
        switch (sUriMatcher.match(uri)) {
            case TRACES:
                count = db.update(TABLE_NAME, values, where, whereArgs);
                break;

            case TRACE_ID:
                String instanceId = uri.getPathSegments().get(1);

                count =
                    db.update(TABLE_NAME, values, TraceColumns._ID + "=" + instanceId
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
        sUriMatcher.addURI(TraceProviderAPI.AUTHORITY, "trace", TRACES);
        sUriMatcher.addURI(TraceProviderAPI.AUTHORITY, "trace/#", TRACE_ID);
    }
}
