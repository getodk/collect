package org.odk.collect.android.storage;

import android.os.Environment;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

import java.io.File;

import timber.log.Timber;

public class StorageManager {
    public static final String SETTINGS = getOdkRoot() + File.separator + "settings";
    public static final String OFFLINE_LAYERS = getOdkRoot() + File.separator + "layers";
    public static final String TMPFILE_PATH = getCachePath() + File.separator + "tmp.jpg";
    public static final String TMPDRAWFILE_PATH = getCachePath() + File.separator + "tmpDraw.jpg";

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

        String[] dirs = {
                getOdkRoot(), getFormsPath(), getInstancesPath(), getCachePath(), getMetadataPath(), OFFLINE_LAYERS
        };

        for (String dirName : dirs) {
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

    public static String getOdkRoot() {
        return Environment.getExternalStorageDirectory() + File.separator + "odk";
    }

    public static String getFormsPath() {
        return getOdkRoot() + File.separator + "forms";
    }

    public static String getInstancesPath() {
        return getOdkRoot() + File.separator + "instances";
    }

    public static String getMetadataPath() {
        return getOdkRoot() + File.separator + "metadata";
    }

    public static String getCachePath() {
        return getOdkRoot() + File.separator + ".cache";
    }
}
