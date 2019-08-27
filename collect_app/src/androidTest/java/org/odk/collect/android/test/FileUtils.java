package org.odk.collect.android.test;

import android.content.res.AssetManager;

import androidx.test.platform.app.InstrumentationRegistry;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    private FileUtils() {
    }

    public static void copyFileFromAssets(String fileSourcePath, String fileDestPath) throws IOException {
        AssetManager assetManager = InstrumentationRegistry.getInstrumentation().getContext().getAssets();
        try (InputStream input = assetManager.open(fileSourcePath);
             OutputStream output = new FileOutputStream(fileDestPath)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
        }
    }
}
