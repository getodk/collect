
package org.odk.collect.android.smap.local;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.DatabaseContext;
import org.odk.collect.android.exception.ExternalDataException;
import org.odk.collect.android.external.ExternalDataUtil;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.utilities.SQLiteUtils;
import org.odk.collect.android.utilities.TranslationHandler;

import java.io.File;
import java.util.ArrayList;

/**
 * Author: Smap Consulting
 * Date: 22/03/2021
 */
public class LocalSQLiteOpenHelperSmap extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private double sortIdx;

    private FormLoaderTask formLoaderTask;

    public LocalSQLiteOpenHelperSmap(File dbFile) {
        super(new DatabaseContext(dbFile.getParentFile().getAbsolutePath()), dbFile.getName(), null, VERSION);
        sortIdx = 0.0;
    }

    // Add local data
    public void append(ArrayList<ContentValues> data, FormLoaderTask formLoaderTask) throws java.lang.Exception {
        this.formLoaderTask = formLoaderTask;

        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();

            appendLocal(db, ExternalDataUtil.EXTERNAL_DATA_TABLE_NAME, data);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    // Delete local data
    public void deleteLocal(FormLoaderTask formLoaderTask) throws java.lang.Exception {
        this.formLoaderTask = formLoaderTask;

        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();

            // make sure the local column exists - it may not if the user has just upgraded from an older version of fieldTask
            SQLiteUtils.addColumn(db, ExternalDataUtil.EXTERNAL_DATA_TABLE_NAME, ExternalDataUtil.LOCAL_COLUMN_NAME, "integer");

            // Delete existing local data
            String selection = ExternalDataUtil.LOCAL_COLUMN_NAME + " = 1";
            db.delete(ExternalDataUtil.EXTERNAL_DATA_TABLE_NAME, selection, null);

        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    @Override
    // Database should already be created
    public void onCreate(SQLiteDatabase db) {
        throw new ExternalDataException(
                TranslationHandler.getString(Collect.getInstance(), R.string.smap_local_data));
    }

    private void appendLocal(SQLiteDatabase db, String tableName, ArrayList<ContentValues> data) throws Exception {

        onProgress(TranslationHandler.getString(Collect.getInstance(), R.string.smap_local_data));

        for (ContentValues values : data) {
            values.put(ExternalDataUtil.LOCAL_COLUMN_NAME, 1);    // Set local indicator
            values.put(ExternalDataUtil.SORT_COLUMN_NAME, sortIdx--);
            db.insertOrThrow(tableName, null, values);
        }
    }

    protected boolean isCancelled() {
        return formLoaderTask != null && formLoaderTask.isCancelled();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private void onProgress(String message) {
        if (formLoaderTask != null) {
            formLoaderTask.publishExternalDataLoadingProgress(message);
        }
    }

}
