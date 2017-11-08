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
            Timber.i("Attempting to delete: %s", instancePath.getParent());

            File parentFile = instancePath.getParentFile();

            int images = mediaDeleter
                    .deleteImagesInFolderFromMediaProvider(parentFile);
            int audio = mediaDeleter
                    .deleteAudioInFolderFromMediaProvider(parentFile);
            int video = mediaDeleter
                    .deleteVideoInFolderFromMediaProvider(parentFile);

            Timber.i("Removed from content providers: %d image files, %d audio files and %d audio files.",
                    images, audio, video);

            File instanceFolder = temporaryInstanceFileManager.getInstanceFolder(instancePath);
            if (instanceFolder.exists() && instanceFolder.isDirectory()) {
                for (File del : instanceFolder.listFiles()) {
                    Timber.i("Deleting file: %s", del.getAbsolutePath());

                    boolean delete = del.delete();
                    if (!delete) {
                        Timber.e("Failed to delete file.");
                    }
                }
                
                boolean delete = instanceFolder.delete();
                if (!delete) {
                    Timber.e("Failed to delete file.");
                }
            }
        }
    }
}
