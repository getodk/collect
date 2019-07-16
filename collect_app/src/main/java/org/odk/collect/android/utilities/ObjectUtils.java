package org.odk.collect.android.utilities;

import androidx.annotation.Nullable;
import timber.log.Timber;

public class ObjectUtils {

    private ObjectUtils() {
    }

    /** Implementation of Objects.equals for API levels before 19. */
    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T uncheckedCast(Object object) {
        try {
            return (T) object;

        } catch (ClassCastException e) {
            Timber.e("Object %s could not be cast.", object);
            return null;
        }
    }
}
