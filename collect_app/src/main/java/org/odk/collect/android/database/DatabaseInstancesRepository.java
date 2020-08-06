package org.odk.collect.android.database;

import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.instances.InstancesRepository;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.storage.StoragePathProvider;

import java.util.List;

import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.JR_FORM_ID;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.JR_VERSION;

/**
 * Mediates between {@link Instance} objects and the underlying SQLite database that stores them.
 *
 * Uses {@link InstancesDao} to perform database queries. {@link InstancesDao} provides a thin
 * convenience layer over {@link org.odk.collect.android.provider.InstanceProvider} which exposes
 * {@link Cursor} and {@link androidx.loader.content.CursorLoader} objects that need to be managed.
 * This can be advantageous when providing data to Android components (e.g. lists through adapters)
 * but is cumbersome in domain code and makes writing test implementations harder.
 *
 * Over time, we should consider redefining the responsibility split between
 * {@link org.odk.collect.android.provider.InstanceProvider}, {@link InstancesRepository} and
 * {@link InstancesDao}.
 */
public final class DatabaseInstancesRepository implements InstancesRepository {

    private final InstancesDao dao = new InstancesDao();

    @Override
    public Instance get(long databaseId) {
        Cursor c = dao.getInstancesCursorForId(Long.toString(databaseId));
        List<Instance> result = dao.getInstancesFromCursor(c);
        return !result.isEmpty() ? result.get(0) : null;
    }

    @Override
    public List<Instance> getAllByJrFormId(String formId) {
        Cursor c = dao.getInstancesCursor(JR_FORM_ID + " = ?", new String[] {formId});
        return dao.getInstancesFromCursor(c);
    }

    @Override
    public List<Instance> getAllByJrFormIdAndJrVersion(@NonNull String jrFormId, @Nullable String jrVersion) {
        if (jrVersion != null) {
            return dao.getInstancesFromCursor(dao.getInstancesCursor(JR_FORM_ID + " = ? AND " + JR_VERSION + " = ?", new String[]{jrFormId, jrVersion}));
        } else {
            return dao.getInstancesFromCursor(dao.getInstancesCursor(JR_FORM_ID + " = ? AND " + JR_VERSION + " IS NULL", new String[]{jrFormId}));
        }
    }

    @Override
    public Instance getByPath(String instancePath) {
        Cursor c = dao.getInstancesCursor(InstanceColumns.INSTANCE_FILE_PATH + "=?",
                new String[] {new StoragePathProvider().getInstanceDbPath(instancePath)});
        List<Instance> instances = dao.getInstancesFromCursor(c);
        if (instances.size() == 1) {
            return instances.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void delete(Long id) {
        Uri uri = Uri.withAppendedPath(InstanceColumns.CONTENT_URI, id.toString());
        Collect.getInstance().getContentResolver().delete(uri, null, null);
    }
}
