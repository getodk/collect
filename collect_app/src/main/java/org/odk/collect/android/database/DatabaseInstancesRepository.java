package org.odk.collect.android.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.instances.InstancesRepository;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.storage.StoragePathProvider;

import java.util.List;

import static org.odk.collect.android.dao.InstancesDao.getInstancesFromCursor;
import static org.odk.collect.android.dao.InstancesDao.getValuesFromInstanceObject;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.DELETED_DATE;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.JR_FORM_ID;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.JR_VERSION;

/**
 * Mediates between {@link Instance} objects and the underlying SQLite database that stores them.
 * <p>
 * Uses {@link InstancesDao} to perform database queries. {@link InstancesDao} provides a thin
 * convenience layer over {@link org.odk.collect.android.provider.InstanceProvider} which exposes
 * {@link Cursor} and {@link androidx.loader.content.CursorLoader} objects that need to be managed.
 * This can be advantageous when providing data to Android components (e.g. lists through adapters)
 * but is cumbersome in domain code and makes writing test implementations harder.
 * <p>
 * Over time, we should consider redefining the responsibility split between
 * {@link org.odk.collect.android.provider.InstanceProvider}, {@link InstancesRepository} and
 * {@link InstancesDao}.
 */
public final class DatabaseInstancesRepository implements InstancesRepository {

    @Override
    public Instance get(Long databaseId) {
        String selection = InstanceColumns._ID + "=?";
        String[] selectionArgs = {Long.toString(databaseId)};

        Cursor c = getInstancesCursor(selection, selectionArgs);
        List<Instance> result = getInstancesFromCursor(c);
        return !result.isEmpty() ? result.get(0) : null;
    }

    @Override
    public Instance getOneByPath(String instancePath) {
        Cursor c = getInstancesCursor(InstanceColumns.INSTANCE_FILE_PATH + "=?", new String[]{new StoragePathProvider().getRelativeInstancePath(instancePath)});
        List<Instance> instances = getInstancesFromCursor(c);
        if (instances.size() == 1) {
            return instances.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<Instance> getAll() {
        return getInstancesFromCursor(getInstancesCursor(null, null));
    }

    @Override
    public List<Instance> getAllNotDeleted() {
        String selection = InstanceColumns.DELETED_DATE + " IS NULL ";
        return getInstancesFromCursor(getInstancesCursor(selection, null));
    }

    @Override
    public List<Instance> getAllByStatus(String... status) {
        StringBuilder selection = new StringBuilder(InstanceColumns.STATUS + "=?");
        for (int i = 1; i < status.length; i++) {
            selection.append(" or ").append(InstanceColumns.STATUS).append("=?");
        }

        return getInstancesFromCursor(getInstancesCursor(selection.toString(), status));
    }


    @Override
    public List<Instance> getAllByFormId(String formId) {
        Cursor c = getInstancesCursor(JR_FORM_ID + " = ?", new String[]{formId});
        return getInstancesFromCursor(c);
    }

    @Override
    public List<Instance> getAllNotDeletedByFormIdAndVersion(String jrFormId, String jrVersion) {
        if (jrVersion != null) {
            return getInstancesFromCursor(getInstancesCursor(JR_FORM_ID + " = ? AND " + JR_VERSION + " = ? AND " + DELETED_DATE + " IS NULL", new String[]{jrFormId, jrVersion}));
        } else {
            return getInstancesFromCursor(getInstancesCursor(JR_FORM_ID + " = ? AND " + JR_VERSION + " IS NULL AND " + DELETED_DATE + " IS NULL", new String[]{jrFormId}));
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
        Long instanceId = instance.getId();
        ContentValues values = getValuesFromInstanceObject(instance);

        if (instanceId == null) {
            values.put(InstanceColumns.LAST_STATUS_CHANGE_DATE, System.currentTimeMillis()); // TODO: test this
            Uri uri = Collect.getInstance().getContentResolver().insert(InstanceColumns.CONTENT_URI, values);
            Cursor cursor = Collect.getInstance().getContentResolver().query(uri, null, null, null, null);
            return getInstancesFromCursor(cursor).get(0);
        } else {
            Collect.getInstance().getContentResolver().update(
                    InstanceColumns.CONTENT_URI,
                    values,
                    InstanceColumns._ID + "=?",
                    new String[]{instanceId.toString()}
            );

            return get(instanceId);
        }
    }

    private Cursor getInstancesCursor(String selection, String[] selectionArgs) {
        return Collect.getInstance().getContentResolver().query(InstanceColumns.CONTENT_URI, null, selection, selectionArgs, null);
    }
}
