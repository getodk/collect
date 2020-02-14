package org.odk.collect.android.storage;

import android.os.Build;
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
        String scopedStoragePath = storagePathProvider.getScopedExternalFilesDirPath();
        if (scopedStoragePath.isEmpty()) {
            return 0;
        }

        StatFs stat = new StatFs(scopedStoragePath);
        long blockSize;
        long availableBlocks;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
            availableBlocks = stat.getAvailableBlocksLong();
        } else {
            blockSize = stat.getBlockSize();
            availableBlocks = stat.getAvailableBlocks();
        }
        return availableBlocks * blockSize;
    }

    private long getOdkDirSize(StoragePathProvider storagePathProvider) {
        return getFolderSize(new File(storagePathProvider.getDirPath(StorageSubdirectory.ODK)));
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
