package org.odk.collect.android.storage;

import android.os.Environment;
import android.os.StatFs;

import org.odk.collect.android.preferences.GeneralSharedPreferences;

import java.io.File;

import timber.log.Timber;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_SCOPED_STORAGE_USED;

public class StorageStateProvider {

    public boolean isScopedStorageUsed() {
        return GeneralSharedPreferences.getInstance().getBoolean(KEY_SCOPED_STORAGE_USED, false);
    }

    public void enableUsingScopedStorage() {
        GeneralSharedPreferences.getInstance().save(KEY_SCOPED_STORAGE_USED, true);
    }

    public void disableUsingScopedStorage() {
        GeneralSharedPreferences.getInstance().save(KEY_SCOPED_STORAGE_USED, false);
    }

    boolean isStorageMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public boolean isEnoughSpaceToPerformMigration(StoragePathProvider storagePathProvider) {
        try {
            return getAvailableScopedStorageSize(storagePathProvider) > getOdkDirSize(storagePathProvider);
        } catch (Exception | Error e) {
            Timber.w(e);
            return false;
        }
    }

    private long getAvailableScopedStorageSize(StoragePathProvider storagePathProvider) {
        String scopedStoragePath = storagePathProvider.getScopedStorageRootDirPath();
        if (scopedStoragePath.isEmpty()) {
            return 0;
        }

        StatFs stat = new StatFs(scopedStoragePath);
        return stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
    }

    private long getOdkDirSize(StoragePathProvider storagePathProvider) {
        return getFolderSize(new File(storagePathProvider.getUnscopedStorageRootDirPath()));
    }

    private long getFolderSize(File directory) {
        long length = 0;
        if (directory != null && directory.exists()) {
            for (File file : directory.listFiles()) {
                if (file != null && file.exists()) {
                    length += file.isFile()
                            ? file.length()
                            : getFolderSize(file);
                }
            }
        }
        return length;
    }
}
