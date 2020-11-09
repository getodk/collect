package org.odk.collect.android.storage;

import android.content.SharedPreferences;
import android.os.Environment;
import android.os.StatFs;

import org.odk.collect.android.application.Collect;
import org.odk.collect.utilities.Clock;

import java.io.File;

import timber.log.Timber;

import static org.odk.collect.android.preferences.MetaKeys.KEY_SCOPED_STORAGE_USED;

public class StorageStateProvider {
    private static long lastMigrationAttempt;

    private final SharedPreferences metaSharedPreferences;
    private final Clock clock;

    public StorageStateProvider() {
        this(System::currentTimeMillis);
    }

    public StorageStateProvider(Clock clock) {
        this.clock = clock;
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

    public boolean shouldPerformAutomaticMigration() {
        return !isScopedStorageUsed() && !alreadyTriedToMigrateDataToday(clock.getCurrentTime());
    }

    public boolean alreadyTriedToMigrateDataToday(long currentTime) {
        long oneDayPeriod = 86400000;
        boolean result = currentTime - lastMigrationAttempt < oneDayPeriod;
        if (!result) {
            lastMigrationAttempt = currentTime;
        }
        return result;
    }

    // just for tests
    protected void clearLastMigrationAttemptTime() {
        lastMigrationAttempt = 0;
    }
}
