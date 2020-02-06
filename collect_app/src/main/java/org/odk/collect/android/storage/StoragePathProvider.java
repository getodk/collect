package org.odk.collect.android.storage;

import android.os.Environment;

import org.odk.collect.android.application.Collect;

import java.io.File;

public class StoragePathProvider {

    private StorageStateProvider storageStateProvider;

    public StoragePathProvider() {
        this(new StorageStateProvider());
    }

    public StoragePathProvider(StorageStateProvider storageStateProvider) {
        this.storageStateProvider = storageStateProvider;
    }

    public String[] getOdkDirPaths() {
        return new String[]{
                getDirPath(StorageSubdirectory.ODK),
                getDirPath(StorageSubdirectory.FORMS),
                getDirPath(StorageSubdirectory.INSTANCES),
                getDirPath(StorageSubdirectory.CACHE),
                getDirPath(StorageSubdirectory.METADATA),
                getDirPath(StorageSubdirectory.LAYERS)
            };
    }

    private String getStorageDirPath() {
        return storageStateProvider.isScopedStorageUsed()
                ? getScopedExternalFilesDirPath()
                : getUnscopedExternalFilesDirPath();
    }

    String getScopedExternalFilesDirPath() {
        File scopedExternalFilesDirPath = Collect.getInstance().getExternalFilesDir(null);
        return scopedExternalFilesDirPath != null
                ? scopedExternalFilesDirPath.getAbsolutePath()
                : "";
    }

    String getUnscopedExternalFilesDirPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public String getDirPath(StorageSubdirectory subdirectory) {
        return getStorageDirPath() + File.separator + subdirectory.getDirectoryName();
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

    private String getRelativeFilePath(String dirPath, String filePath) {
        return filePath.startsWith(dirPath)
                ? filePath.substring(dirPath.length() + 1)
                : filePath;
    }
}
