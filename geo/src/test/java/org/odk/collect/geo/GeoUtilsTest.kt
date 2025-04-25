package org.odk.collect.geo

import android.content.Context
import android.location.Location
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.geo.GeoUtils.formatPointsResultString
import org.odk.collect.maps.MapPoint
import org.odk.collect.testshared.LocationTestUtils.createLocation

@RunWith(AndroidJUnit4::class)
class GeoUtilsTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val points = mutableListOf(
        MapPoint(11.0, 12.0, 13.0, 14.0),
        MapPoint(21.0, 22.0, 23.0, 24.0),
        MapPoint(31.0, 32.0, 33.0, 34.0)
    )

    @Test
    fun whenPointsAreNull_formatPoints_returnsEmptyString() {
        assertEquals(formatPointsResultString(emptyList(), true), "")
        assertEquals(formatPointsResultString(emptyList(), false), "")
    }

    @Test
    fun geotraces_areSeparatedBySemicolon_withoutTrialingSemicolon() {
        assertEquals(
            formatPointsResultString(points, false),
            "11.0 12.0 13.0 14.0;21.0 22.0 23.0 24.0;31.0 32.0 33.0 34.0"
        )
    }

    @Test
    fun geoshapes_areSeparatedBySemicolon_withoutTrialingSemicolon_andHaveMatchingFirstAndLastPoints() {
        assertEquals(
            formatPointsResultString(points, true),
            "11.0 12.0 13.0 14.0;21.0 22.0 23.0 24.0;31.0 32.0 33.0 34.0;11.0 12.0 13.0 14.0"
        )
    }

    @Test
    fun test_formatLocationResultString() {
        val location: Location = createLocation("GPS", 1.0, 2.0, 3.0, 4f)
        assertEquals(GeoUtils.formatLocationResultString(location), "1.0 2.0 3.0 4.0")
    }

    @Test
    fun `#formatAccuracy formats accuracy in meters to 2 decimal places`() {
        assertThat(GeoUtils.formatAccuracy(context, 0.000f), equalTo("0 m"))
        assertThat(GeoUtils.formatAccuracy(context, 0.001f), equalTo("0 m"))
        assertThat(GeoUtils.formatAccuracy(context, 0.01f), equalTo("0.01 m"))
        assertThat(GeoUtils.formatAccuracy(context, 0.10f), equalTo("0.1 m"))
        assertThat(GeoUtils.formatAccuracy(context, 1.1f), equalTo("1.1 m"))
    }
}
