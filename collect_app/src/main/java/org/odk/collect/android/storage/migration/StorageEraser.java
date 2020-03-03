package org.odk.collect.android.storage.migration;

import org.odk.collect.android.database.ItemsetDbAdapter;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;

import java.io.File;

import static org.odk.collect.android.utilities.FileUtils.deleteDirectory;

public class StorageEraser {

    private final StoragePathProvider storagePathProvider;

    public StorageEraser(StoragePathProvider storagePathProvider) {
        this.storagePathProvider = storagePathProvider;
    }

    void clearCache() {
        deleteDirectory(new File(storagePathProvider.getDirPath(StorageSubdirectory.CACHE)));
    }

    void removeItemsetsDb() {
        removeFile(storagePathProvider.getDirPath(StorageSubdirectory.CACHE) + File.separator + ItemsetDbAdapter.DATABASE_NAME);
    }

    void clearOdkDirOnScopedStorage() {
        deleteDirectory(new File(storagePathProvider.getScopedStorageRootDirPath()));
    }

    void deleteOdkDirFromUnscopedStorage() {
        deleteDirectory(new File(storagePathProvider.getUnscopedStorageRootDirPath()));
    }

    void removeFile(String filePath) {
        new File(filePath).delete();
    }
}
