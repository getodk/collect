package org.odk.collect.android.utilities;

import android.content.Context;
import android.net.Uri;

/**
 * @author James Knight
 */
public class MediaUtil implements IMediaUtil {

    @Override
    public String getPathFromUri(Context ctxt, Uri uri, String pathKey) {
        return MediaUtils.getPathFromUri(ctxt, uri, pathKey);
    }
}
