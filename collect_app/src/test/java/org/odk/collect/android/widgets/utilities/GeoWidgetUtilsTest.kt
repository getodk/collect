package org.odk.collect.android.widgets.utilities

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils.getGeoPointAnswerToDisplay
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils.getGeoPolyAnswerToDisplay
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils.isWithinMapBounds
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils.truncateDouble
import org.odk.collect.maps.MapPoint

@RunWith(AndroidJUnit4::class)
class GeoWidgetUtilsTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun getAnswerToDisplay_whenAnswerIsNull_returnsEmptyString() {
        assertEquals(getGeoPointAnswerToDisplay(context, null), "")
    }

    @Test
    fun getAnswerToDisplay_whenAnswerIsNotConvertible_returnsEmptyString() {
        assertEquals(getGeoPointAnswerToDisplay(context, "blah"), "")
    }

    @Test
    fun getAnswerToDisplay_whenAnswerIsNotNullAndConvertible_returnsCoordinatesInDecimalDegrees() {
        assertEquals(
            getGeoPointAnswerToDisplay(context, "37.451533 -122.155392 100.0 5.5"),
            context.getString(
                org.odk.collect.strings.R.string.gps_result,
                "37.451533°",
                "-122.155392°",
                "100",
                "5.5"
            )
        )
    }

    @Test
    fun getAnswerToDisplay_padsCoordinatesToSixDecimalPlaces() {
        assertEquals(
            getGeoPointAnswerToDisplay(context, "1.5 -2 0.0 0.0"),
            context.getString(
                org.odk.collect.strings.R.string.gps_result,
                "1.500000°",
                "-2.000000°",
                "0",
                "0"
            )
        )
    }

    @Test
    fun truncateDoubleTest() {
        assertEquals("5", truncateDouble("5"))
        assertEquals("-5", truncateDouble("-5"))
        assertEquals("5.12", truncateDouble("5.12"))
        assertEquals("-5.12", truncateDouble("-5.12"))
        assertEquals("5.12", truncateDouble("5.1234"))
        assertEquals("-5.12", truncateDouble("-5.1234"))
        assertEquals("", truncateDouble(""))
        assertEquals("", truncateDouble(null))
        assertEquals("", truncateDouble("qwerty"))
    }

    @Test
    fun isWithinMapBoundsTest() {
        assertThat(isWithinMapBounds(MapPoint(90.0, 0.0, 0.0, 0.0)), equalTo(true))
        assertThat(isWithinMapBounds(MapPoint(-90.0, 0.0, 0.0, 0.0)), equalTo(true))
        assertThat(isWithinMapBounds(MapPoint(0.0, 180.0, 0.0, 0.0)), equalTo(true))
        assertThat(isWithinMapBounds(MapPoint(0.0, -180.0, 0.0, 0.0)), equalTo(true))

        assertThat(isWithinMapBounds(MapPoint(90.1, 0.0, 0.0, 0.0)), equalTo(false))
        assertThat(isWithinMapBounds(MapPoint(-90.1, 0.0, 0.0, 0.0)), equalTo(false))
        assertThat(isWithinMapBounds(MapPoint(0.0, 180.1, 0.0, 0.0)), equalTo(false))
        assertThat(isWithinMapBounds(MapPoint(0.0, -180.1, 0.0, 0.0)), equalTo(false))
    }

    @Test
    fun getGeoPolyAnswerToDisplayTest() {
        assertThat(getGeoPolyAnswerToDisplay(""), equalTo(""))
        assertThat(getGeoPolyAnswerToDisplay(";"), equalTo(""))
        assertThat(getGeoPolyAnswerToDisplay("; "), equalTo(""))
        assertThat(getGeoPolyAnswerToDisplay(";\n"), equalTo(""))
        assertThat(getGeoPolyAnswerToDisplay(";\r"), equalTo(""))
        assertThat(getGeoPolyAnswerToDisplay("12.0 13.0 5 6;"), equalTo("12.0 13.0 5 6"))
    }
}
