package org.odk.collect.android.utilities;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import org.apache.commons.io.IOUtils;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import timber.log.Timber;

public class ContentUriProvider {
    private static final String HUAWEI_MANUFACTURER = "Huawei";

    private ContentUriProvider() {
    }

    // https://stackoverflow.com/a/41309223/5479029
    public static Uri getUriForFile(@NonNull Context context, @NonNull String authority, @NonNull File file) {
        if (HUAWEI_MANUFACTURER.equalsIgnoreCase(Build.MANUFACTURER)) {
            Timber.w(ContentUriProvider.class.getSimpleName(), "Using a Huawei device Increased likelihood of failure...");
            try {
                return FileProvider.getUriForFile(context, authority, file);
            } catch (IllegalArgumentException e) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    Timber.w(e, ContentUriProvider.class.getSimpleName(), "Returning Uri.fromFile to avoid Huawei 'external-files-path' bug for pre-N devices");
                    return Uri.fromFile(file);
                } else {
                    Timber.w(e, ContentUriProvider.class.getSimpleName(), "ANR Risk -- Copying the file the location cache to avoid Huawei 'external-files-path' bug for N+ devices");
                    // Note: Periodically clear this cache
                    final File cacheFolder = new File(new StoragePathProvider().getDirPath(StorageSubdirectory.CACHE), HUAWEI_MANUFACTURER);
                    final File cacheLocation = new File(cacheFolder, file.getName());
                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        in = new FileInputStream(file);
                        out = new FileOutputStream(cacheLocation); // appending output stream
                        IOUtils.copy(in, out);
                        Timber.i(ContentUriProvider.class.getSimpleName(), "Completed Android N+ Huawei file copy. Attempting to return the cached file");
                        return FileProvider.getUriForFile(context, authority, cacheLocation);
                    } catch (IOException e1) {
                        Timber.e(e1, ContentUriProvider.class.getSimpleName(), "Failed to copy the Huawei file. Re-throwing exception");
                        throw new IllegalArgumentException("Huawei devices are unsupported for Android N", e1);
                    } finally {
                        IOUtils.closeQuietly(in);
                        IOUtils.closeQuietly(out);
                    }
                }
            }
        } else {
            return FileProvider.getUriForFile(context, authority, file);
        }
    }
}
