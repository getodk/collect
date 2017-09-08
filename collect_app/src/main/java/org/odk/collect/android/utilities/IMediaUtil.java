package org.odk.collect.android.utilities;

import android.content.Context;
import android.net.Uri;

/**
 * @author James Knight
 */

public interface IMediaUtil {
    String getPathFromUri(Context ctxt, Uri uri, String pathKey);
}
