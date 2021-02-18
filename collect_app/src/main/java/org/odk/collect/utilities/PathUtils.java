package org.odk.collect.utilities;

import java.io.File;

public class PathUtils {

    private PathUtils() {

    }

    public static String getRelativeFilePath(String dirPath, String filePath) {
        return filePath.startsWith(dirPath)
                ? filePath.substring(dirPath.length() + 1)
                : filePath;
    }

    public static String getAbsoluteFilePath(String dirPath, String filePath) {
        if (filePath == null) {
            return null;
        }
        return filePath.startsWith(dirPath)
                ? filePath
                : dirPath + File.separator + filePath;
    }
}
