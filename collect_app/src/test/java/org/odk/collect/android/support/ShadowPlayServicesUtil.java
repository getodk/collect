package org.odk.collect.android.support;

import android.content.Context;

import org.odk.collect.android.utilities.PlayServicesChecker;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(PlayServicesChecker.class)
public abstract class ShadowPlayServicesUtil {

    @Implementation
    public static boolean isGooglePlayServicesAvailable(Context context) {
        return true;
    }
}

