package org.odk.collect.android.storage;

import android.os.Environment;

import org.odk.collect.android.application.Collect;

import java.io.File;

public class StoragePathProvider {
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
        return new StorageStateProvider().isScopedStorageUsed()
                ? getScopedExternalFilesDirPath()
                : getUnscopedExternalFilesDirPath();
    }

    private String getScopedExternalFilesDirPath() {
        File primaryStorageFile = Collect.getInstance().getExternalFilesDir(null);
        if (primaryStorageFile != null) {
            return primaryStorageFile.getAbsolutePath();
        }
        return "";
    }

    private String getUnscopedExternalFilesDirPath() {
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
        return getDbPath(StorageSubdirectory.INSTANCES.getDirectoryName(), path);
    }

    public String getAbsoluteInstanceFilePath(String path) {
        return getAbsoluteFilePath(StorageSubdirectory.INSTANCES.getDirectoryName(), path);
    }

    public String getFormDbPath(String path) {
        return getDbPath(StorageSubdirectory.FORMS.getDirectoryName(), path);
    }

    public String getAbsoluteFormFilePath(String path) {
        return getAbsoluteFilePath(StorageSubdirectory.FORMS.getDirectoryName(), path);
    }

    public String getCacheDbPath(String path) {
        return getDbPath(StorageSubdirectory.CACHE.getDirectoryName(), path);
    }

    public String getAbsoluteCacheFilePath(String path) {
        return getAbsoluteFilePath(StorageSubdirectory.CACHE.getDirectoryName(), path);
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

        return new StorageStateProvider().isScopedStorageUsed()
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
