package org.odk.collect.android.support;

import android.content.res.AssetManager;

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
        AssetManager assetManager = InstrumentationRegistry.getInstrumentation().getContext().getAssets();
        try (InputStream input = assetManager.open(fileSourcePath);
             OutputStream output = new FileOutputStream(fileDestPath)) {
            IOUtils.copy(input, output);
        }
    }
}
