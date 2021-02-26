package org.odk.collect.android.storage;

import org.odk.collect.android.application.Collect;

import java.io.File;

public class StoragePathProvider {
    public String[] getOdkDirPaths() {
        return new String[]{
                getOdkDirPath(StorageSubdirectory.FORMS),
                getOdkDirPath(StorageSubdirectory.INSTANCES),
                getOdkDirPath(StorageSubdirectory.CACHE),
                getOdkDirPath(StorageSubdirectory.METADATA),
                getOdkDirPath(StorageSubdirectory.LAYERS)
        };
    }

    public String getOdkRootDirPath() {
        File odkDirPath = Collect.getInstance().getExternalFilesDir(null);
        return odkDirPath != null
                ? odkDirPath.getAbsolutePath()
                : "";
    }

    public String getOdkDirPath(StorageSubdirectory subdirectory) {
        return getOdkRootDirPath() + File.separator + subdirectory.getDirectoryName();
    }

    public String getCustomSplashScreenImagePath() {
        return getOdkRootDirPath() + File.separator + "customSplashScreenImage.jpg";
    }

    public String getTmpImageFilePath() {
        return getOdkDirPath(StorageSubdirectory.CACHE) + File.separator + "tmp.jpg";
    }

    public String getTmpVideoFilePath() {
        return getOdkDirPath(StorageSubdirectory.CACHE) + File.separator + "tmp.mp4";
    }

    public String getRelativeInstancePath(String filePath) {
        return getRelativeFilePath(getOdkDirPath(StorageSubdirectory.INSTANCES), filePath);
    }

    public String getAbsoluteInstanceFilePath(String filePath) {
        return getAbsoluteFilePath(getOdkDirPath(StorageSubdirectory.INSTANCES), filePath);
    }

    public String getRelativeFormPath(String filePath) {
        return getRelativeFilePath(getOdkDirPath(StorageSubdirectory.FORMS), filePath);
    }

    public String getAbsoluteFormFilePath(String filePath) {
        return getAbsoluteFilePath(getOdkDirPath(StorageSubdirectory.FORMS), filePath);
    }

    public String getRelativeCachePath(String filePath) {
        return getRelativeFilePath(getOdkDirPath(StorageSubdirectory.CACHE), filePath);
    }

    public String getAbsoluteCacheFilePath(String filePath) {
        return getAbsoluteFilePath(getOdkDirPath(StorageSubdirectory.CACHE), filePath);
    }

    public String getRelativeMapLayerPath(String filePath) {
        return getRelativeFilePath(getOdkDirPath(StorageSubdirectory.LAYERS), filePath);
    }

    public String getAbsoluteOfflineMapLayerPath(String filePath) {
        return getAbsoluteFilePath(getOdkDirPath(StorageSubdirectory.LAYERS), filePath);
    }

    public static String getRelativeFilePath(String dirPath, String filePath) {
        if (filePath == null) {
            return null;
        }
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
