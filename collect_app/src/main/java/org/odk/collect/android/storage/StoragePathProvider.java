package org.odk.collect.android.storage;

import android.os.Environment;

import org.odk.collect.android.application.Collect;

import java.io.File;

public class StoragePathProvider {
    @SuppressWarnings("PMD.DoNotHardCodeSDCard")
    private static final String SD_CARD_PREFIX = "/sdcard/odk";

    private StorageStateProvider storageStateProvider;

    public StoragePathProvider() {
        this(new StorageStateProvider());
    }

    public StoragePathProvider(StorageStateProvider storageStateProvider) {
        this.storageStateProvider = storageStateProvider;
    }

    public String[] getOdkDirPaths() {
        return storageStateProvider.isScopedStorageUsed()
                ? getOdkDirPathsForScopedStorage()
                : getOdkDirPathsForUnScopedStorage();
    }

    private String[] getOdkDirPathsForScopedStorage() {
        return new String[]{
                getDirPath(StorageSubdirectory.FORMS),
                getDirPath(StorageSubdirectory.INSTANCES),
                getDirPath(StorageSubdirectory.CACHE),
                getDirPath(StorageSubdirectory.METADATA),
                getDirPath(StorageSubdirectory.LAYERS)
        };
    }

    private String[] getOdkDirPathsForUnScopedStorage() {
        return new String[]{
                getUnscopedStorageRootDirPath(),
                getDirPath(StorageSubdirectory.FORMS),
                getDirPath(StorageSubdirectory.INSTANCES),
                getDirPath(StorageSubdirectory.CACHE),
                getDirPath(StorageSubdirectory.METADATA),
                getDirPath(StorageSubdirectory.LAYERS)
        };
    }

    public String getScopedStorageRootDirPath() {
        File scopedExternalFilesDirPath = Collect.getInstance().getExternalFilesDir(null);
        return scopedExternalFilesDirPath != null
                ? scopedExternalFilesDirPath.getAbsolutePath()
                : "";
    }

    public String getUnscopedStorageRootDirPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "odk";
    }

    public String getUnscopedStorageDirPath(StorageSubdirectory subdirectory) {
        return getUnscopedStorageRootDirPath() + File.separator + subdirectory.getDirectoryName();
    }

    private String getScopedStorageDirPath(StorageSubdirectory subdirectory) {
        return getScopedStorageRootDirPath() + File.separator + subdirectory.getDirectoryName();
    }

    public String getDirPath(StorageSubdirectory subdirectory) {
        return storageStateProvider.isScopedStorageUsed()
                ? getScopedStorageDirPath(subdirectory)
                : getUnscopedStorageDirPath(subdirectory);
    }

    public String getStorageRootDirPath() {
        return storageStateProvider.isScopedStorageUsed()
                ? getScopedStorageRootDirPath()
                : getUnscopedStorageRootDirPath();
    }

    public String getTmpFilePath() {
        return getDirPath(StorageSubdirectory.CACHE) + File.separator + "tmp.jpg";
    }

    public String getTmpDrawFilePath() {
        return getDirPath(StorageSubdirectory.CACHE) + File.separator + "tmpDraw.jpg";
    }

    public String getInstanceDbPath(String filePath) {
        return getDbPath(getDirPath(StorageSubdirectory.INSTANCES), filePath);
    }

    public String getAbsoluteInstanceFilePath(String filePath) {
        return getAbsoluteFilePath(getDirPath(StorageSubdirectory.INSTANCES), filePath);
    }

    public String getFormDbPath(String filePath) {
        return getDbPath(getDirPath(StorageSubdirectory.FORMS), filePath);
    }

    public String getAbsoluteFormFilePath(String filePath) {
        return getAbsoluteFilePath(getDirPath(StorageSubdirectory.FORMS), filePath);
    }

    public String getCacheDbPath(String filePath) {
        return getDbPath(getDirPath(StorageSubdirectory.CACHE), filePath);
    }

    public String getAbsoluteCacheFilePath(String filePath) {
        return getAbsoluteFilePath(getDirPath(StorageSubdirectory.CACHE), filePath);
    }

    private String getDbPath(String dirPath, String filePath) {
        String absoluteFilePath;
        String relativeFilePath;
        if (filePath.startsWith(dirPath)) {
            absoluteFilePath = filePath;
            relativeFilePath = getRelativeFilePath(dirPath, filePath);
        } else {
            relativeFilePath = filePath;
            absoluteFilePath = getAbsoluteFilePath(dirPath, filePath);
        }

        return storageStateProvider.isScopedStorageUsed()
                ? relativeFilePath
                : absoluteFilePath;
    }

    private String getAbsoluteFilePath(String dirPath, String filePath) {
        if (filePath == null) {
            return null;
        }
        return filePath.startsWith(dirPath)
                ? filePath
                : dirPath + File.separator + filePath;
    }

    public String getRelativeFilePath(String dirPath, String filePath) {
        return filePath.startsWith(dirPath)
                ? filePath.substring(dirPath.length() + 1)
                : filePath;
    }

    public String getRelativeMapLayerPath(String path) {
        if (path == null) {
            return null;
        }
        if (path.startsWith(SD_CARD_PREFIX)) {
            return path.substring(SD_CARD_PREFIX.length());
        } else if (path.startsWith(getUnscopedStorageRootDirPath())) {
            return path.substring(getUnscopedStorageRootDirPath().length());
        } else if (path.startsWith(getScopedStorageRootDirPath())) {
            return path.substring(getScopedStorageRootDirPath().length());
        }
        return path;
    }

    public String getAbsoluteOfflineMapLayerPath(String path) {
        String relativePath = getRelativeMapLayerPath(path);
        return storageStateProvider.isScopedStorageUsed()
                ? getStorageRootDirPath() + (relativePath != null ? relativePath : "")
                : getUnscopedStorageRootDirPath() + (relativePath != null ? relativePath : "");
    }
}
