package org.odk.collect.android.location.usecases;

import android.location.Location;
import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.odk.collect.android.injection.config.scopes.PerApplication;

import javax.inject.Inject;

/**
 * @author James Knight
 */

@PerApplication
class IsLocationValid {

    private static final int VALID_WITHIN_SECONDS = 5;

    @Inject
    IsLocationValid() {

    }

    boolean isValid(@NonNull Location location) {
        long millis = DateTime.now().minus(location.getTime()).getMillis();
        return millis <= VALID_WITHIN_SECONDS * 1_000;
    }
}
