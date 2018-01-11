package org.odk.collect.android.location.usecases;

import android.location.Location;
import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.odk.collect.android.injection.config.scopes.PerApplication;

import java.util.Date;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * @author James Knight
 */

@PerApplication
class IsPositionValid {

    private static final int VALID_WITHIN_SECONDS = 5;

    @Inject
    IsPositionValid() {

    }

    boolean isValid(@NonNull Location location) {
        Date now = new Date();

        long millis = now.getTime() - location.getTime();

        Timber.d("Checking location is valid, now: %s, then: %s, difference: %s.", now, new Date(location.getTime()), millis);
        return millis <= VALID_WITHIN_SECONDS * 1_000;
    }
}
