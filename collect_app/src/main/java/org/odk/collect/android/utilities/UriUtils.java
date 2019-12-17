package org.odk.collect.android.utilities;

import androidx.annotation.NonNull;

public class UriUtils {
    // Prevent class from being constructed
    private UriUtils() {}

    public  static String stripLeadingUriSlashes(@NonNull String path) {
        return path.replaceAll("^/+", "");
    }
}
