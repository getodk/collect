package org.odk.collect.android.storage;

import android.os.Environment;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

import java.io.File;

import timber.log.Timber;

public class StorageManager {
    /**
     * Creates required directories on the SDCard (or other external storage)
     *
     * @throws RuntimeException if there is no SDCard or the directory exists as a non directory
     */
    public static void createODKDirs() throws RuntimeException {
        String cardstatus = Environment.getExternalStorageState();
        if (!cardstatus.equals(Environment.MEDIA_MOUNTED)) {
            throw new RuntimeException(
                    Collect.getInstance().getString(R.string.sdcard_unmounted, cardstatus));
        }

        for (String dirName : getODKDirPaths()) {
            File dir = new File(dirName);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    String message = Collect.getInstance().getString(R.string.cannot_create_directory, dirName);
                    Timber.w(message);
                    throw new RuntimeException(message);
                }
            } else {
                if (!dir.isDirectory()) {
                    String message = Collect.getInstance().getString(R.string.not_a_directory, dirName);
                    Timber.w(message);
                    throw new RuntimeException(message);
                }
            }
        }
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

    public static String getMainODKDirPath() {
        return Environment.getExternalStorageDirectory() + File.separator + "odk";
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
}
