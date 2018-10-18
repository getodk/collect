package org.odk.collect.android.upload;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.dto.Instance;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.utilities.ApplicationConstants;

import java.util.ArrayList;
import java.util.List;

import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.AUTO_DELETE;

public abstract class InstanceUploader {
    /**
     * Returns a list of Instance objects corresponding to the database IDs passed in.
     */
    public List<Instance> getInstancesFromIds(Long... instanceDatabaseIds) {
        List<Instance> instancesToUpload = new ArrayList<>();
        InstancesDao dao = new InstancesDao();

        // Split the queries to avoid exceeding SQLITE_MAX_VARIABLE_NUMBER
        int counter = 0;
        while (counter * ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER < instanceDatabaseIds.length) {
            int low = counter * ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER;
            int high = (counter + 1) * ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER;
            if (high > instanceDatabaseIds.length) {
                high = instanceDatabaseIds.length;
            }

            StringBuilder selectionBuf = new StringBuilder(InstanceProviderAPI.InstanceColumns._ID + " IN (");
            String[] selectionArgs = new String[high - low];
            for (int i = 0; i < (high - low); i++) {
                if (i > 0) {
                    selectionBuf.append(',');
                }
                selectionBuf.append('?');
                selectionArgs[i] = instanceDatabaseIds[i + low].toString();
            }

            selectionBuf.append(')');
            String selection = selectionBuf.toString();

            Cursor c = dao.getInstancesCursor(selection, selectionArgs);
            instancesToUpload.addAll(dao.getInstancesFromCursor(c));

            counter++;
        }

        return instancesToUpload;
    }

    void saveSuccessStatusToDatabase(Instance instance) {
        Uri instanceDatabaseUri = Uri.withAppendedPath(InstanceProviderAPI.InstanceColumns.CONTENT_URI,
                instance.getDatabaseId().toString());

        ContentValues contentValues = new ContentValues();
        contentValues.put(InstanceProviderAPI.InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMITTED);
        Collect.getInstance().getContentResolver().update(instanceDatabaseUri, contentValues, null, null);
    }

    void saveFailedStatusToDatabase(Instance instance) {
        Uri instanceDatabaseUri = Uri.withAppendedPath(InstanceProviderAPI.InstanceColumns.CONTENT_URI,
                instance.getDatabaseId().toString());

        ContentValues contentValues = new ContentValues();
        contentValues.put(InstanceProviderAPI.InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
        Collect.getInstance().getContentResolver().update(instanceDatabaseUri, contentValues, null, null);
    }

    /**
     * Returns whether instances of the form specified should be auto-deleted after successful
     * update.
     *
     * If the form explicitly sets the auto-delete property, then it overrides the preference.
     */
    public static boolean formShouldBeAutoDeleted(String jrFormId, boolean isAutoDeleteAppSettingEnabled) {
        Cursor cursor = new FormsDao().getFormsCursorForFormId(jrFormId);
        String autoDelete = null;
        if (cursor != null && cursor.moveToFirst()) {
            try {
                int autoDeleteColumnIndex = cursor.getColumnIndex(AUTO_DELETE);
                autoDelete = cursor.getString(autoDeleteColumnIndex);
            } finally {
                cursor.close();
            }
        }
        return autoDelete == null ? isAutoDeleteAppSettingEnabled : Boolean.valueOf(autoDelete);
    }
}
