package org.odk.collect.android.storage;

import android.os.Environment;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.GeneralSharedPreferences;

import java.io.File;

import timber.log.Timber;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_SCOPED_STORAGE_USED;

public class StorageManager {
    /**
     * Creates required directories on the SDCard (or other external storage)
     *
     * @throws RuntimeException if there is no SDCard or the directory exists as a non directory
     */
    public static void createODKDirs() throws RuntimeException {
        if (!isStorageAvailable()) {
            throw new RuntimeException(
                    Collect.getInstance().getString(R.string.sdcard_unmounted, Environment.getExternalStorageState()));
        }

        for (String dirPath : getODKDirPaths()) {
            File dir = new File(dirPath);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    String message = Collect.getInstance().getString(R.string.cannot_create_directory, dirPath);
                    Timber.w(message);
                    throw new RuntimeException(message);
                }
            } else {
                if (!dir.isDirectory()) {
                    String message = Collect.getInstance().getString(R.string.not_a_directory, dirPath);
                    Timber.w(message);
                    throw new RuntimeException(message);
                }
            }
        }
    }

    private static boolean isStorageAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static String[] getODKDirPaths() {
        return new String[]{
                getMainODKDirPath(),
                getFormsDirPath(),
                getInstancesDirPath(),
                getCacheDirPath(),
                getMetadataDirPath(),
                getOfflineLayersDirPath()
            };
    }

    private static String getStoragePath() {
        return isScopedStorageUsed()
                ? getScopedExternalFilesDirPath()
                : getUnscopedExternalFilesDirPath();
    }

    private static String getScopedExternalFilesDirPath() {
        File primaryStorageFile = Collect.getInstance().getExternalFilesDir(null);
        if (primaryStorageFile != null) {
            return primaryStorageFile.getAbsolutePath();
        }
        return "";
    }

    private static String getUnscopedExternalFilesDirPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static String getMainODKDirPath() {
        return getStoragePath() + File.separator + "odk";
    }

    public static String getFormsDirPath() {
        return getMainODKDirPath() + File.separator + "forms";
    }

    public static String getInstancesDirPath() {
        return getMainODKDirPath() + File.separator + "instances";
    }

    public static String getMetadataDirPath() {
        return getMainODKDirPath() + File.separator + "metadata";
    }

    public static String getCacheDirPath() {
        return getMainODKDirPath() + File.separator + ".cache";
    }

    public static String getOfflineLayersDirPath() {
        return getMainODKDirPath() + File.separator + "layers";
    }

    public static String getSettingsDirPath() {
        return getMainODKDirPath() + File.separator + "settings";
    }

    public static String getTmpFilePath() {
        return getCacheDirPath() + File.separator + "tmp.jpg";
    }

    public static String getTmpDrawFilePath() {
        return getCacheDirPath() + File.separator + "tmpDraw.jpg";
    }

    private static boolean isScopedStorageUsed() {
        return GeneralSharedPreferences.getInstance().getBoolean(KEY_SCOPED_STORAGE_USED, false);
    }

    public static void recordMigrationToScopedStorage() {
        GeneralSharedPreferences.getInstance().save(KEY_SCOPED_STORAGE_USED, true);
    }

    public static String getInstanceDbPath(String path) {
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

    public static String getAbsoluteInstanceFilePath(String filePath) {
        if (filePath == null) {
            return null;
        }
        return filePath.startsWith(getInstancesDirPath())
                ? filePath
                : getInstancesDirPath() + File.separator + filePath;
    }

    public static String getRelativeInstanceFilePath(String filePath) {
        return filePath.startsWith(getInstancesDirPath())
                ? filePath.substring(getInstancesDirPath().length() + 1)
                : filePath;
    }

    public static String getFormDbPath(String path) {
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

    public static String getRelativeFormFilePath(String filePath) {
        return filePath.startsWith(getFormsDirPath())
                ? filePath.substring(getFormsDirPath().length() + 1)
                : filePath;
    }

    public static String getAbsoluteFormFilePath(String filePath) {
        if (filePath == null) {
            return null;
        }
        return filePath.startsWith(getFormsDirPath())
                ? filePath
                : getFormsDirPath() + File.separator + filePath;
    }
}
