package org.odk.collect.android.database.instances;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import org.apache.commons.io.FileUtils;
import org.odk.collect.android.database.DatabaseConnection;
import org.odk.collect.android.database.DatabaseConstants;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.forms.instances.InstancesRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static android.provider.BaseColumns._ID;
import static org.odk.collect.android.database.DatabaseConstants.INSTANCES_TABLE_NAME;
import static org.odk.collect.android.database.DatabaseObjectMapper.getInstanceFromCurrentCursorPosition;
import static org.odk.collect.android.database.DatabaseObjectMapper.getValuesFromInstance;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.CAN_EDIT_WHEN_COMPLETE;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.DELETED_DATE;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.DISPLAY_NAME;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.GEOMETRY;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.GEOMETRY_TYPE;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.INSTANCE_FILE_PATH;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.JR_FORM_ID;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.JR_VERSION;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.LAST_STATUS_CHANGE_DATE;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.STATUS;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.SUBMISSION_URI;
import static org.odk.collect.shared.PathUtils.getRelativeFilePath;

/**
 * Mediates between {@link Instance} objects and the underlying SQLite database that stores them.
 */
public final class DatabaseInstancesRepository implements InstancesRepository {

    private final DatabaseConnection databaseConnection;
    private final Supplier<Long> clock;
    private final String instancesPath;

    public DatabaseInstancesRepository(Context context, String dbPath, String instancesPath, Supplier<Long> clock) {
        this.databaseConnection = new DatabaseConnection(
                context,
                dbPath,
                DatabaseConstants.INSTANCES_DATABASE_NAME,
                new InstanceDatabaseMigrator(),
                DatabaseConstants.INSTANCES_DATABASE_VERSION
        );

        this.clock = clock;
        this.instancesPath = instancesPath;
    }

    @Override
    public Instance get(Long databaseId) {
        String selection = _ID + "=?";
        String[] selectionArgs = {Long.toString(databaseId)};

        try (Cursor cursor = query(null, selection, selectionArgs, null)) {
            List<Instance> result = getInstancesFromCursor(cursor, instancesPath);
            return !result.isEmpty() ? result.get(0) : null;
        }
    }

    @Override
    public Instance getOneByPath(String instancePath) {
        String selection = INSTANCE_FILE_PATH + "=?";
        String[] args = {getRelativeFilePath(instancesPath, instancePath)};
        try (Cursor cursor = query(null, selection, args, null)) {
            List<Instance> instances = getInstancesFromCursor(cursor, instancesPath);
            if (instances.size() == 1) {
                return instances.get(0);
            } else {
                return null;
            }
        }
    }

    @Override
    public List<Instance> getAll() {
        try (Cursor cursor = query(null, null, null, null)) {
            return getInstancesFromCursor(cursor, instancesPath);
        }
    }

    @Override
    public List<Instance> getAllNotDeleted() {
        try (Cursor cursor = query(null, DELETED_DATE + " IS NULL ", null, null)) {
            return getInstancesFromCursor(cursor, instancesPath);
        }
    }

    @Override
    public List<Instance> getAllByStatus(String... status) {
        try (Cursor instancesCursor = getCursorForAllByStatus(status)) {
            return getInstancesFromCursor(instancesCursor, instancesPath);
        }
    }

    @Override
    public int getCountByStatus(String... status) {
        try (Cursor cursorForAllByStatus = getCursorForAllByStatus(status)) {
            return cursorForAllByStatus.getCount();
        }
    }


    @Override
    public List<Instance> getAllByFormId(String formId) {
        try (Cursor c = query(null, JR_FORM_ID + " = ?", new String[]{formId}, null)) {
            return getInstancesFromCursor(c, instancesPath);
        }
    }

    @Override
    public List<Instance> getAllNotDeletedByFormIdAndVersion(String jrFormId, String jrVersion) {
        if (jrVersion != null) {
            try (Cursor cursor = query(null, JR_FORM_ID + " = ? AND " + JR_VERSION + " = ? AND " + DELETED_DATE + " IS NULL", new String[]{jrFormId, jrVersion}, null)) {
                return getInstancesFromCursor(cursor, instancesPath);
            }
        } else {
            try (Cursor cursor = query(null, JR_FORM_ID + " = ? AND " + JR_VERSION + " IS NULL AND " + DELETED_DATE + " IS NULL", new String[]{jrFormId}, null)) {
                return getInstancesFromCursor(cursor, instancesPath);
            }
        }
    }

    @Override
    public void delete(Long id) {
        Instance instance = get(id);

        databaseConnection.getWriteableDatabase().delete(
                INSTANCES_TABLE_NAME,
                _ID + "=?",
                new String[]{String.valueOf(id)}
        );

        deleteInstanceFiles(instance);
    }

    @Override
    public void deleteAll() {
        List<Instance> instances = getAll();

        databaseConnection.getWriteableDatabase().delete(
                INSTANCES_TABLE_NAME,
                null,
                null
        );

        for (Instance instance : instances) {
            deleteInstanceFiles(instance);
        }
    }

    @Override
    public Instance save(Instance instance) {
        if (instance.getStatus() == null) {
            instance = new Instance.Builder(instance)
                    .status(Instance.STATUS_INCOMPLETE)
                    .build();
        }

        if (instance.getDbId() == null) {
            if (instance.getLastStatusChangeDate() == null) {
                instance = new Instance.Builder(instance)
                        .lastStatusChangeDate(clock.get())
                        .build();
            }

            long insertId = insert(getValuesFromInstance(instance, instancesPath));
            return get(insertId);
        } else {
            if (instance.getDeletedDate() == null) {
                instance = new Instance.Builder(instance)
                        .lastStatusChangeDate(clock.get())
                        .build();
            }

            update(instance.getDbId(), getValuesFromInstance(instance, instancesPath));
            return get(instance.getDbId());
        }
    }

    @Override
    public void deleteWithLogging(Long id) {
        ContentValues values = new ContentValues();
        values.putNull(GEOMETRY);
        values.putNull(GEOMETRY_TYPE);
        values.put(DELETED_DATE, clock.get());
        update(id, values);

        Instance instance = get(id);
        deleteInstanceFiles(instance);
    }

    public Cursor rawQuery(String[] projection, String selection, String[] selectionArgs, String sortOrder, String groupBy) {
        return query(projection, selection, selectionArgs, sortOrder);
    }

    private Cursor getCursorForAllByStatus(String[] status) {
        StringBuilder selection = new StringBuilder(STATUS + "=?");
        for (int i = 1; i < status.length; i++) {
            selection.append(" or ").append(STATUS).append("=?");
        }

        return query(null, selection.toString(), status, null);
    }

    private Cursor query(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase readableDatabase = databaseConnection.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(INSTANCES_TABLE_NAME);

        if (projection == null) {
            /*
             For some reason passing null as the projection doesn't always give us all the
             columns so we hardcode them here so it's explicit that we need these all back.
             */
            projection = new String[]{
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
                    GEOMETRY,
                    GEOMETRY_TYPE
            };
        }

        return qb.query(readableDatabase, projection, selection, selectionArgs, null, null, sortOrder);
    }

    private long insert(ContentValues values) {
        return databaseConnection.getWriteableDatabase().insertOrThrow(
                INSTANCES_TABLE_NAME,
                null,
                values
        );
    }

    private void update(Long instanceId, ContentValues values) {
        databaseConnection.getWriteableDatabase().update(
                INSTANCES_TABLE_NAME,
                values,
                _ID + "=?",
                new String[]{instanceId.toString()}
        );
    }

    private void deleteInstanceFiles(Instance instance) {
        try {
            FileUtils.deleteDirectory(new File(instance.getInstanceFilePath()).getParentFile());
        } catch (IOException e) {
            // Ignored
        }
    }

    private static List<Instance> getInstancesFromCursor(Cursor cursor, String instancesPath) {
        List<Instance> instances = new ArrayList<>();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            Instance instance = getInstanceFromCurrentCursorPosition(cursor, instancesPath);
            instances.add(instance);
        }

        return instances;
    }
}
