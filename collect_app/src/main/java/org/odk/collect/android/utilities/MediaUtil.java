package org.odk.collect.android.utilities;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * @author James Knight
 */
public class MediaUtil {

    public String getPathFromUri(@NonNull Context ctxt, @NonNull Uri uri, @NonNull String pathKey) {
        return MediaUtils.getPathFromUri(ctxt, uri, pathKey);
    }

    public int deleteVideoFileFromMediaProvider(String videoFile) {
        return MediaUtils.deleteVideoFileFromMediaProvider(videoFile);
    }

    public int deleteAudioFileFromMediaProvider(String audioFile) {
        return MediaUtils.deleteAudioFileFromMediaProvider(audioFile);
    }
}
