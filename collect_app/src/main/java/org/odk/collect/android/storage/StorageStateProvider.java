package org.odk.collect.android.storage;

import android.content.SharedPreferences;
import android.os.Environment;

import org.odk.collect.android.application.Collect;

import static org.odk.collect.android.preferences.MetaKeys.KEY_SCOPED_STORAGE_USED;

public class StorageStateProvider {
    private final SharedPreferences metaSharedPreferences;

    public StorageStateProvider() {
        metaSharedPreferences = Collect.getInstance().getComponent().preferencesProvider().getMetaSharedPreferences();
    }

    public boolean isScopedStorageUsed() {
        return metaSharedPreferences.getBoolean(KEY_SCOPED_STORAGE_USED, false);
    }

    public void enableUsingScopedStorage() {
        metaSharedPreferences.edit()
                .putBoolean(KEY_SCOPED_STORAGE_USED, true)
                .apply();
    }

    public void disableUsingScopedStorage() {
        metaSharedPreferences.edit()
                .putBoolean(KEY_SCOPED_STORAGE_USED, false)
                .apply();
    }

    boolean isStorageMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
}
