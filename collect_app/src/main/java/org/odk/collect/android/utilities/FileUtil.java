package org.odk.collect.android.utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class FileUtil {

    public File getFileAtPath(String path) {
        return new File(path);
    }

    public List<File> listFiles(File file) {
        if (file != null && file.exists()) {
            return asList(file.listFiles());
        } else {
            return new ArrayList<>();
        }
    }
}
