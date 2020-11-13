package org.odk.collect.android.utilities;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;

/**
 * @author James Knight
 */
public class MediaUtil {

    public String getPathFromUri(@NonNull Context ctxt, @NonNull Uri uri, @NonNull String pathKey) {
        return MediaUtils.getPathFromUri(ctxt, uri, pathKey);
    }
}
