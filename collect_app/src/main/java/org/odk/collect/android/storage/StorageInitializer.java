package org.odk.collect.android.storage;

import android.os.Environment;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

import java.io.File;

import timber.log.Timber;

public class StorageInitializer {
    /**
     * Creates required directories on the SDCard (or other external storage)
     *
     * @throws RuntimeException if there is no SDCard or the directory exists as a non directory
     */
    public void createODKDirs() throws RuntimeException {
        if (!new StorageStateProvider().isStorageAvailable()) {
            throw new RuntimeException(
                    Collect.getInstance().getString(R.string.sdcard_unmounted, Environment.getExternalStorageState()));
        }

        for (String dirPath : new StoragePathProvider().getODKDirPaths()) {
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
}
