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

    public String[] getODKDirPaths() {
        return new String[]{
                getMainODKDirPath(),
                getDirPath(StorageSubdirectory.FORMS),
                getDirPath(StorageSubdirectory.INSTANCES),
                getDirPath(StorageSubdirectory.CACHE),
                getDirPath(StorageSubdirectory.METADATA),
                getDirPath(StorageSubdirectory.LAYERS)
            };
    }

    private String getStoragePath() {
        return storageStateProvider.isScopedStorageUsed()
                ? getScopedExternalFilesDirPath()
                : getUnscopedExternalFilesDirPath();
    }

    String getScopedExternalFilesDirPath() {
        File primaryStorageFile = Collect.getInstance().getExternalFilesDir(null);
        if (primaryStorageFile != null) {
            return primaryStorageFile.getAbsolutePath();
        }
        return "";
    }

    String getUnscopedExternalFilesDirPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public String getMainODKDirPath() {
        return getStoragePath() + File.separator + "odk";
    }

    public String getDirPath(StorageSubdirectory subdirectory) {
        return getMainODKDirPath() + File.separator + subdirectory.getDirectoryName();
    }

    public String getTmpFilePath() {
        return getDirPath(StorageSubdirectory.CACHE) + File.separator + "tmp.jpg";
    }

    public String getTmpDrawFilePath() {
        return getDirPath(StorageSubdirectory.CACHE) + File.separator + "tmpDraw.jpg";
    }

    public String getInstanceDbPath(String path) {
        return getDbPath(getDirPath(StorageSubdirectory.INSTANCES), path);
    }

    public String getAbsoluteInstanceFilePath(String path) {
        return getAbsoluteFilePath(getDirPath(StorageSubdirectory.INSTANCES), path);
    }

    public String getFormDbPath(String path) {
        return getDbPath(getDirPath(StorageSubdirectory.FORMS), path);
    }

    public String getAbsoluteFormFilePath(String path) {
        return getAbsoluteFilePath(getDirPath(StorageSubdirectory.FORMS), path);
    }

    public String getCacheDbPath(String path) {
        return getDbPath(getDirPath(StorageSubdirectory.CACHE), path);
    }

    public String getAbsoluteCacheFilePath(String path) {
        return getAbsoluteFilePath(getDirPath(StorageSubdirectory.CACHE), path);
    }

    private String getDbPath(String dirPath, String path) {
        String absolutePath;
        String relativePath;
        if (path.startsWith(dirPath)) {
            absolutePath = path;
            relativePath = getRelativeFilePath(dirPath, path);
        } else {
            relativePath = path;
            absolutePath = getAbsoluteFilePath(dirPath, path);
        }

        return storageStateProvider.isScopedStorageUsed()
                ? relativePath
                : absolutePath;
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
