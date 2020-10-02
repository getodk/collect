package org.odk.collect.android.utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

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

    public List<File> listFiles(File file) {
        if (file != null && file.exists()) {
            return asList(file.listFiles());
        } else {
            return new ArrayList<>();
        }
    }
}
