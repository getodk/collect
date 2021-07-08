package org.odk.collect.location

import android.location.LocationManager
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsNull.nullValue
import org.junit.Test
import org.odk.collect.location.LocationUtils.sanitizeAccuracy
import org.odk.collect.testshared.LocationTestUtils.createLocation

class LocationUtilsTest {

    @Test
    fun whenAccuracyIsNegative_shouldBeSetToZeroAfterSanitizing() {
        val location = createLocation(LocationManager.GPS_PROVIDER, 7.0, 2.0, 3.0, -1.0f)
        val sanitizedLocation = sanitizeAccuracy(location)
        assertThat(sanitizedLocation!!.latitude, `is`(7.0))
        assertThat(sanitizedLocation.longitude, `is`(2.0))
        assertThat(sanitizedLocation.altitude, `is`(3.0))
        assertThat(sanitizedLocation.accuracy, `is`(0.0f))
    }

    @Test
    fun whenLocationIsMocked_shouldAccuracyBeSetToZeroAfterSanitizing() {
        val location = createLocation(LocationManager.GPS_PROVIDER, 7.0, 2.0, 3.0, 5.0f, true)
        val sanitizedLocation = sanitizeAccuracy(location)
        assertThat(sanitizedLocation!!.latitude, `is`(7.0))
        assertThat(sanitizedLocation.longitude, `is`(2.0))
        assertThat(sanitizedLocation.altitude, `is`(3.0))
        assertThat(sanitizedLocation.accuracy, `is`(0.0f))
    }

    @Test
    fun whenLocationIsNull_shouldNullBeReturnedAfterSanitizing() {
        assertThat(sanitizeAccuracy(null), nullValue())
    }
}
