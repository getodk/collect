package org.odk.collect.android.utilities.tempinstance;

import android.support.annotation.NonNull;

import java.io.File;

import timber.log.Timber;

public class TempInstanceRemover {

    @NonNull
    private final TemporaryInstanceFileManager temporaryInstanceFileManager;

    @NonNull
    private final ExistingInstanceChecker existingInstanceChecker;

    @NonNull
    private final MediaDeleter mediaDeleter;

    public TempInstanceRemover() {
        this(
                new TemporaryInstanceFileManager(),
                new ExistingInstanceChecker(),
                new MediaDeleter()
        );
    }

    TempInstanceRemover(@NonNull TemporaryInstanceFileManager temporaryInstanceFileManager,
                        @NonNull ExistingInstanceChecker existingInstanceChecker,
                        @NonNull MediaDeleter mediaDeleter) {

        this.temporaryInstanceFileManager = temporaryInstanceFileManager;
        this.existingInstanceChecker = existingInstanceChecker;
        this.mediaDeleter = mediaDeleter;
    }

    public void removeTempInstanceAtInstancePath(@NonNull File instancePath) {

        // Dependency injected:
        File temp = temporaryInstanceFileManager.getSavePointFileForInstancePath(instancePath);

        if (temp.exists()) {
            boolean delete = temp.delete();
            if (!delete) {
                Timber.e("Failed to delete file.");
            }
        }

        // Dependency injected:
        boolean shouldErase = existingInstanceChecker.shouldDeleteExistingInstance(instancePath);

        // if it's not already saved, erase everything
        if (shouldErase) {
            // delete media first
            String instanceFolder = instancePath.getParent();
            Timber.i("Attempting to delete: %s", instanceFolder);

            File parentFile = instancePath.getParentFile();

            // Dependency injected:
            int images = mediaDeleter
                    .deleteImagesInFolderFromMediaProvider(parentFile);
            int audio = mediaDeleter
                    .deleteAudioInFolderFromMediaProvider(parentFile);
            int video = mediaDeleter
                    .deleteVideoInFolderFromMediaProvider(parentFile);

            Timber.i("Removed from content providers: %d image files, %d audio files and %d audio files.",
                    images, audio, video);

            // Dependency injected:
            File f = temporaryInstanceFileManager.getInstanceFolder(instancePath);
            if (f.exists() && f.isDirectory()) {
                for (File del : f.listFiles()) {
                    Timber.i("Deleting file: %s", del.getAbsolutePath());
                    boolean delete = del.delete();
                    if (!delete) {
                        Timber.e("Failed to delete file.");
                    }
                }

                boolean delete = f.delete();
                if (!delete) {
                    Timber.e("Failed to delete file.");
                }
            }
        }
    }
}
