package org.odk.collect.android.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.instances.InstancesRepository;
import org.odk.collect.android.provider.InstanceProvider;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.storage.StoragePathProvider;

import java.util.ArrayList;
import java.util.List;

import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.DELETED_DATE;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.JR_FORM_ID;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.JR_VERSION;

/**
 * Mediates between {@link Instance} objects and the underlying SQLite database that stores them.
 */
public final class DatabaseInstancesRepository implements InstancesRepository {

    @Override
    public Instance get(Long databaseId) {
        String selection = InstanceColumns._ID + "=?";
        String[] selectionArgs = {Long.toString(databaseId)};

        List<Instance> result = getInstancesFromCursor(query(selection, selectionArgs));
        return !result.isEmpty() ? result.get(0) : null;
    }

    @Override
    public Instance getOneByPath(String instancePath) {
        String selection = InstanceColumns.INSTANCE_FILE_PATH + "=?";
        String[] args = {new StoragePathProvider().getRelativeInstancePath(instancePath)};
        List<Instance> instances = getInstancesFromCursor(query(selection, args));
        if (instances.size() == 1) {
            return instances.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<Instance> getAll() {
        return getInstancesFromCursor(query(null, null));
    }

    @Override
    public List<Instance> getAllNotDeleted() {
        return getInstancesFromCursor(query(InstanceColumns.DELETED_DATE + " IS NULL ", null));
    }

    @Override
    public List<Instance> getAllByStatus(String... status) {
        Cursor instancesCursor = getCursorForAllByStatus(status);
        return getInstancesFromCursor(instancesCursor);
    }

    @Override
    public int getCountByStatus(String... status) {
        return getCursorForAllByStatus(status).getCount();
    }


    @Override
    public List<Instance> getAllByFormId(String formId) {
        Cursor c = query(JR_FORM_ID + " = ?", new String[]{formId});
        return getInstancesFromCursor(c);
    }

    @Override
    public List<Instance> getAllNotDeletedByFormIdAndVersion(String jrFormId, String jrVersion) {
        if (jrVersion != null) {
            return getInstancesFromCursor(query(JR_FORM_ID + " = ? AND " + JR_VERSION + " = ? AND " + DELETED_DATE + " IS NULL", new String[]{jrFormId, jrVersion}));
        } else {
            return getInstancesFromCursor(query(JR_FORM_ID + " = ? AND " + JR_VERSION + " IS NULL AND " + DELETED_DATE + " IS NULL", new String[]{jrFormId}));
        }
    }

    @Override
    public void delete(Long id) {
        Uri uri = Uri.withAppendedPath(InstanceColumns.CONTENT_URI, id.toString());
        Collect.getInstance().getContentResolver().delete(uri, null, null);
    }

    @Override
    public void deleteAll() {
        Collect.getInstance().getContentResolver().delete(InstanceColumns.CONTENT_URI, null, null);
    }

    @Override
    public Instance save(Instance instance) {
        if (instance.getStatus() == null) {
            instance = new Instance.Builder(instance)
                    .status(Instance.STATUS_INCOMPLETE)
                    .build();
        }

        if (instance.getLastStatusChangeDate() == null) {
            instance = new Instance.Builder(instance)
                    .lastStatusChangeDate(System.currentTimeMillis())
                    .build();
        }

        Long instanceId = instance.getDbId();
        ContentValues values = getValuesFromInstanceObject(instance);

        if (instanceId == null) {
            long insertId = insert(values);
            return get(insertId);
        } else {
            update(instanceId, values);

            return get(instanceId);
        }
    }

    @Override
    public void softDelete(Long id) {
        ContentValues values = new ContentValues();
        values.put(DELETED_DATE, System.currentTimeMillis());
        update(id, values);
    }

    private Cursor getCursorForAllByStatus(String[] status) {
        StringBuilder selection = new StringBuilder(InstanceColumns.STATUS + "=?");
        for (int i = 1; i < status.length; i++) {
            selection.append(" or ").append(InstanceColumns.STATUS).append("=?");
        }

        return query(selection.toString(), status);
    }

    private Cursor query(String selection, String[] selectionArgs) {
        return Collect.getInstance().getContentResolver().query(InstanceColumns.CONTENT_URI, null, selection, selectionArgs, null);
    }

    private long insert(ContentValues values) {
        return InstanceProvider.getDbHelper().getWritableDatabase().insert(
                DatabaseConstants.INSTANCES_TABLE_NAME,
                null,
                values
        );
    }

    private void update(Long instanceId, ContentValues values) {
        InstanceProvider.getDbHelper().getWritableDatabase().update(
                DatabaseConstants.INSTANCES_TABLE_NAME,
                values,
                InstanceColumns._ID + "=?",
                new String[]{instanceId.toString()}
        );
    }

    private static ContentValues getValuesFromInstanceObject(Instance instance) {
        ContentValues values = new ContentValues();
        values.put(InstanceColumns.DISPLAY_NAME, instance.getDisplayName());
        values.put(InstanceColumns.SUBMISSION_URI, instance.getSubmissionUri());
        values.put(InstanceColumns.CAN_EDIT_WHEN_COMPLETE, Boolean.toString(instance.canEditWhenComplete()));
        values.put(InstanceColumns.INSTANCE_FILE_PATH, new StoragePathProvider().getRelativeInstancePath(instance.getInstanceFilePath()));
        values.put(InstanceColumns.JR_FORM_ID, instance.getFormId());
        values.put(InstanceColumns.JR_VERSION, instance.getFormVersion());
        values.put(InstanceColumns.STATUS, instance.getStatus());
        values.put(InstanceColumns.LAST_STATUS_CHANGE_DATE, instance.getLastStatusChangeDate());
        values.put(InstanceColumns.DELETED_DATE, instance.getDeletedDate());
        values.put(InstanceColumns.GEOMETRY, instance.getGeometry());
        values.put(InstanceColumns.GEOMETRY_TYPE, instance.getGeometryType());
        return values;
    }

    public static List<Instance> getInstancesFromCursor(Cursor cursor) {
        List<Instance> instances = new ArrayList<>();
        if (cursor != null) {
            try {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    int displayNameColumnIndex = cursor.getColumnIndex(InstanceColumns.DISPLAY_NAME);
                    int submissionUriColumnIndex = cursor.getColumnIndex(InstanceColumns.SUBMISSION_URI);
                    int canEditWhenCompleteIndex = cursor.getColumnIndex(InstanceColumns.CAN_EDIT_WHEN_COMPLETE);
                    int instanceFilePathIndex = cursor.getColumnIndex(InstanceColumns.INSTANCE_FILE_PATH);
                    int jrFormIdColumnIndex = cursor.getColumnIndex(InstanceColumns.JR_FORM_ID);
                    int jrVersionColumnIndex = cursor.getColumnIndex(InstanceColumns.JR_VERSION);
                    int statusColumnIndex = cursor.getColumnIndex(InstanceColumns.STATUS);
                    int lastStatusChangeDateColumnIndex = cursor.getColumnIndex(InstanceColumns.LAST_STATUS_CHANGE_DATE);
                    int deletedDateColumnIndex = cursor.getColumnIndex(InstanceColumns.DELETED_DATE);
                    int geometryTypeColumnIndex = cursor.getColumnIndex(InstanceColumns.GEOMETRY_TYPE);
                    int geometryColumnIndex = cursor.getColumnIndex(InstanceColumns.GEOMETRY);

                    int databaseIdIndex = cursor.getColumnIndex(InstanceColumns._ID);

                    Instance instance = new Instance.Builder()
                            .displayName(cursor.getString(displayNameColumnIndex))
                            .submissionUri(cursor.getString(submissionUriColumnIndex))
                            .canEditWhenComplete(Boolean.valueOf(cursor.getString(canEditWhenCompleteIndex)))
                            .instanceFilePath(new StoragePathProvider().getAbsoluteInstanceFilePath(cursor.getString(instanceFilePathIndex)))
                            .formId(cursor.getString(jrFormIdColumnIndex))
                            .formVersion(cursor.getString(jrVersionColumnIndex))
                            .status(cursor.getString(statusColumnIndex))
                            .lastStatusChangeDate(cursor.getLong(lastStatusChangeDateColumnIndex))
                            .deletedDate(cursor.isNull(deletedDateColumnIndex) ? null : cursor.getLong(deletedDateColumnIndex))
                            .geometryType(cursor.getString(geometryTypeColumnIndex))
                            .geometry(cursor.getString(geometryColumnIndex))
                            .dbId(cursor.getLong(databaseIdIndex))
                            .build();

                    instances.add(instance);
                }
            } finally {
                cursor.close();
            }
        }
        return instances;
    }
}
