package org.odk.collect.android.utilities.tempinstance;


import org.odk.collect.android.utilities.MediaUtils;

import java.io.File;

public class MediaDeleter {
    public int deleteImagesInFolderFromMediaProvider(File folder) {
        return MediaUtils.deleteImagesInFolderFromMediaProvider(folder);
    }

    public int deleteAudioInFolderFromMediaProvider(File folder) {
        return MediaUtils.deleteAudioInFolderFromMediaProvider(folder);
    }

    public int deleteVideoInFolderFromMediaProvider(File folder) {
        return MediaUtils.deleteVideoInFolderFromMediaProvider(folder);
    }
}
