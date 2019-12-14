package org.odk.collect.android.utilities;

import androidx.annotation.NonNull;

public class UriUtils {
    // Prevent class from being constructed
    private UriUtils() {}

    // Leading slashes are removed from paths to support minSdkVersion < 18:
    // https://developer.android.com/reference/android/content/UriMatcher
    public  static String stripLeadingUriSlashes(@NonNull String path) {
        return path.replaceAll("^/+", "");
    }
}
