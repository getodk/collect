package org.odk.collect.android.storage;

import android.os.Environment;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.GeneralSharedPreferences;

import java.io.File;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_SCOPED_STORAGE_USED;

public class StoragePathProvider {
    public String[] getODKDirPaths() {
        return new String[]{
                getMainODKDirPath(),
                getFormsDirPath(),
                getInstancesDirPath(),
                getCacheDirPath(),
                getMetadataDirPath(),
                getOfflineLayersDirPath()
            };
    }

    private String getStoragePath() {
        return isScopedStorageUsed()
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

    public String getFormsDirPath() {
        return getMainODKDirPath() + File.separator + "forms";
    }

    public String getInstancesDirPath() {
        return getMainODKDirPath() + File.separator + "instances";
    }

    public String getMetadataDirPath() {
        return getMainODKDirPath() + File.separator + "metadata";
    }

    public String getCacheDirPath() {
        return getMainODKDirPath() + File.separator + ".cache";
    }

    public String getOfflineLayersDirPath() {
        return getMainODKDirPath() + File.separator + "layers";
    }

    public String getSettingsDirPath() {
        return getMainODKDirPath() + File.separator + "settings";
    }

    public String getTmpFilePath() {
        return getCacheDirPath() + File.separator + "tmp.jpg";
    }

    public String getTmpDrawFilePath() {
        return getCacheDirPath() + File.separator + "tmpDraw.jpg";
    }

    private boolean isScopedStorageUsed() {
        return GeneralSharedPreferences.getInstance().getBoolean(KEY_SCOPED_STORAGE_USED, false);
    }

    public void recordMigrationToScopedStorage() {
        GeneralSharedPreferences.getInstance().save(KEY_SCOPED_STORAGE_USED, true);
    }

    public String getInstanceDbPath(String path) {
        String absolutePath;
        String relativePath;
        if (path.startsWith(getInstancesDirPath())) {
            absolutePath = path;
            relativePath = getRelativeInstanceFilePath(path);
        } else {
            relativePath = path;
            absolutePath = getAbsoluteInstanceFilePath(path);
        }

        return isScopedStorageUsed()
                ? relativePath
                : absolutePath;
    }

    public String getAbsoluteInstanceFilePath(String filePath) {
        if (filePath == null) {
            return null;
        }
        return filePath.startsWith(getInstancesDirPath())
                ? filePath
                : getInstancesDirPath() + File.separator + filePath;
    }

    public String getRelativeInstanceFilePath(String filePath) {
        return filePath.startsWith(getInstancesDirPath())
                ? filePath.substring(getInstancesDirPath().length() + 1)
                : filePath;
    }

    public String getFormDbPath(String path) {
        String absolutePath;
        String relativePath;
        if (path.startsWith(getFormsDirPath())) {
            absolutePath = path;
            relativePath = getRelativeFormFilePath(path);
        } else {
            relativePath = path;
            absolutePath = getAbsoluteFormFilePath(path);
        }

        return isScopedStorageUsed()
                ? relativePath
                : absolutePath;
    }

    public String getRelativeFormFilePath(String filePath) {
        return filePath.startsWith(getFormsDirPath())
                ? filePath.substring(getFormsDirPath().length() + 1)
                : filePath;
    }

    public String getAbsoluteFormFilePath(String filePath) {
        if (filePath == null) {
            return null;
        }
        return filePath.startsWith(getFormsDirPath())
                ? filePath
                : getFormsDirPath() + File.separator + filePath;
    }

    public String getCacheDbPath(String path) {
        String absolutePath;
        String relativePath;
        if (path.startsWith(getCacheDirPath())) {
            absolutePath = path;
            relativePath = getRelativeCacheFilePath(path);
        } else {
            relativePath = path;
            absolutePath = getAbsoluteCacheFilePath(path);
        }

        return isScopedStorageUsed()
                ? relativePath
                : absolutePath;
    }

    public String getAbsoluteCacheFilePath(String filePath) {
        if (filePath == null) {
            return null;
        }
        return filePath.startsWith(getCacheDirPath())
                ? filePath
                : getCacheDirPath() + File.separator + filePath;
    }

    public String getRelativeCacheFilePath(String filePath) {
        return filePath.startsWith(getCacheDirPath())
                ? filePath.substring(getCacheDirPath().length() + 1)
                : filePath;
    }
}
