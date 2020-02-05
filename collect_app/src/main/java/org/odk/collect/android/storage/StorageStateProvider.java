package org.odk.collect.android.storage;

import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import org.odk.collect.android.preferences.GeneralSharedPreferences;

import java.io.File;

import timber.log.Timber;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_SCOPED_STORAGE_USED;

class StorageStateProvider {

    private StoragePathProvider storagePathProvider;

    StorageStateProvider() {
        this(new StoragePathProvider());
    }

    private StorageStateProvider(StoragePathProvider storagePathProvider) {
        this.storagePathProvider = storagePathProvider;
    }

    boolean isScopedStorageUsed() {
        return GeneralSharedPreferences.getInstance().getBoolean(KEY_SCOPED_STORAGE_USED, false);
    }

    void enableUsingScopedStorage() {
        GeneralSharedPreferences.getInstance().save(KEY_SCOPED_STORAGE_USED, true);
    }

    void disableUsingScopedStorage() {
        GeneralSharedPreferences.getInstance().save(KEY_SCOPED_STORAGE_USED, false);
    }

    boolean isStorageMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    boolean isEnoughSpaceToPerformMigartion() {
        try {
            return getAvailableScopedStorageSize() > getOdkDirSize();
        } catch (Exception | Error e) {
            Timber.w(e);
            return false;
        }
    }

    private long getAvailableScopedStorageSize() {
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

    private long getOdkDirSize() {
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
