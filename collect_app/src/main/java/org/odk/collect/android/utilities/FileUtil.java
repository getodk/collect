package org.odk.collect.android.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtil {

    public String copyFile(File sourceFile, File destFile) {
        return FileUtils.copyFile(sourceFile, destFile);
    }

    public File getFileAtPath(String path) {
        return new File(path);
    }

    public String getRandomFilename() {
        return Long.toString(System.currentTimeMillis());
    }

    public File getItemsetFile(String mediaFolderPath) {
        return new File(mediaFolderPath + "/itemsets.csv");
    }

    public static String getFileContents(final File smsFile) throws IOException {

        final InputStream inputStream = new FileInputStream(smsFile);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        final StringBuilder stringBuilder = new StringBuilder();

        boolean done = false;

        while (!done) {
            final String line = reader.readLine();
            done = (line == null);

            if (line != null) {
                stringBuilder.append(line);
            }
        }

        reader.close();
        inputStream.close();

        return stringBuilder.toString();
    }

    public static String getSmsInstancePath(String instancePath) {
        return instancePath + ".txt";
    }
}
