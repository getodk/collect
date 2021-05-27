package org.odk.collect.android.storage;

import org.jetbrains.annotations.Nullable;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.projects.CurrentProjectProvider;

import java.io.File;

public class StoragePathProvider {

    private final CurrentProjectProvider currentProjectProvider;
    private final String externalFilesDirPath;

    public StoragePathProvider() {
        currentProjectProvider = DaggerUtils.getComponent(Collect.getInstance()).currentProjectProvider();
        externalFilesDirPath = Collect.getInstance().getExternalFilesDir(null).getAbsolutePath();
    }

    public StoragePathProvider(CurrentProjectProvider currentProjectProvider, String externalFilesDirPath) {
        this.currentProjectProvider = currentProjectProvider;
        this.externalFilesDirPath = externalFilesDirPath;
    }

    public String[] getOdkRootDirPaths() {
        return new String[]{
                getOdkDirPath(StorageSubdirectory.PROJECTS)
        };
    }

    public String[] getProjectDirPaths(String projectId) {
        return new String[]{
                getOdkDirPath(StorageSubdirectory.FORMS, projectId),
                getOdkDirPath(StorageSubdirectory.INSTANCES, projectId),
                getOdkDirPath(StorageSubdirectory.CACHE, projectId),
                getOdkDirPath(StorageSubdirectory.METADATA, projectId),
                getOdkDirPath(StorageSubdirectory.LAYERS, projectId),
                getOdkDirPath(StorageSubdirectory.SETTINGS, projectId)
        };
    }

    public String getOdkRootDirPath() {
        return externalFilesDirPath;
    }

    public String getProjectRootDirPath() {
        return getProjectRootDirPath(null);
    }

    public String getProjectRootDirPath(@Nullable String projectId) {
        if (projectId == null) {
            String currentProjectId = currentProjectProvider.getCurrentProject().getUuid();
            return getOdkDirPath(StorageSubdirectory.PROJECTS) + File.separator + currentProjectId;
        } else {
            return getOdkDirPath(StorageSubdirectory.PROJECTS) + File.separator + projectId;
        }
    }

    public String getOdkDirPath(StorageSubdirectory subdirectory) {
        return getOdkDirPath(subdirectory, null);
    }

    public String getOdkDirPath(StorageSubdirectory subdirectory, String projectId) {
        switch (subdirectory) {
            case FORMS:
            case INSTANCES:
            case CACHE:
            case METADATA:
            case LAYERS:
            case SETTINGS:
                return getProjectRootDirPath(projectId) + File.separator + subdirectory.getDirectoryName();
            case PROJECTS:
                return getOdkRootDirPath() + File.separator + subdirectory.getDirectoryName();
            default:
                throw new IllegalStateException("Unexpected value: " + subdirectory);
        }
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
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        return filePath.startsWith(dirPath)
                ? filePath.substring(dirPath.length() + 1)
                : filePath;
    }

    public static String getAbsoluteFilePath(String dirPath, String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        return filePath.startsWith(dirPath)
                ? filePath
                : dirPath + File.separator + filePath;
    }
}
