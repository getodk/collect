package org.odk.collect.android.support;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.platform.app.InstrumentationRegistry;

import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class FileUtils {

    private FileUtils() {
    }

    public static void copyFileFromAssets(String fileSourcePath, String fileDestPath) throws IOException {
        copyStreamToPath(getAssetAsStream(fileSourcePath), fileDestPath);
    }

    public static void copyFileFromResources(String fileSourcePath, String fileDestPath) throws IOException {
        copyStreamToPath(getResourceAsStream(fileSourcePath), fileDestPath);
    }

    @NonNull
    public static InputStream getAssetAsStream(String fileSourcePath) throws IOException {
        return InstrumentationRegistry.getInstrumentation().getContext().getAssets().open(fileSourcePath);
    }

    @Nullable
    public static InputStream getResourceAsStream(String fileSourcePath) {
        return FileUtils.class.getResourceAsStream("/" + fileSourcePath);
    }

    private static void copyStreamToPath(InputStream inputStream, String fileDestPath) throws IOException {
        try (InputStream input = inputStream;
             OutputStream output = new FileOutputStream(fileDestPath)) {
            IOUtils.copy(input, output);
        }
    }
}
