package org.odk.collect.android.widgets.utilities

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.data.GeoPointData
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.widgets.support.GeoWidgetHelpers
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils.convertCoordinatesIntoDegreeFormat
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils.floor
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils.getGeoPointAnswerToDisplay
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils.getGeoPolyAnswerToDisplay
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils.isWithinMapBounds
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils.parseGeometry
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils.parseGeometryPoint
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils.truncateDouble
import org.odk.collect.maps.MapPoint

@RunWith(AndroidJUnit4::class)
class GeoWidgetUtilsTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val answer = GeoPointData(GeoWidgetHelpers.getRandomDoubleArray())

    @Test
    fun getAnswerToDisplay_whenAnswerIsNull_returnsEmptyString() {
        assertEquals(getGeoPointAnswerToDisplay(context, null), "")
    }

    @Test
    fun getAnswerToDisplay_whenAnswerIsNotConvertible_returnsEmptyString() {
        assertEquals(getGeoPointAnswerToDisplay(context, "blah"), "")
    }

    @Test
    fun getAnswerToDisplay_whenAnswerIsNotNullAndConvertible_returnsAnswer() {
        val stringAnswer = answer.displayText
        val parts = stringAnswer.split(" ").toTypedArray()
        assertEquals(
            getGeoPointAnswerToDisplay(context, stringAnswer),
            context.getString(
                org.odk.collect.strings.R.string.gps_result,
                convertCoordinatesIntoDegreeFormat(context, parts[0].toDouble(), "lat"),
                convertCoordinatesIntoDegreeFormat(context, parts[1].toDouble(), "lon"),
                truncateDouble(parts[2]),
                truncateDouble(parts[3])
            )
        )
    }

    @Test // Results confirmed with https://www.sunearthtools.com/dp/tools/conversion.php
    fun convertCoordinatesIntoDegreeFormatTest() {
        assertEquals(
            "N 37°27'5\"",
            convertCoordinatesIntoDegreeFormat(context, 37.45153333333334, "lat")
        )
        assertEquals(
            "W 122°9'19\"",
            convertCoordinatesIntoDegreeFormat(context, -122.15539166666667, "lon")
        )
        assertEquals(
            "N 3°51'4\"",
            convertCoordinatesIntoDegreeFormat(context, 3.8513583333333337, "lat")
        )
        assertEquals(
            "W 70°2'11\"",
            convertCoordinatesIntoDegreeFormat(context, -70.03650333333333, "lon")
        )
        assertEquals(
            "S 31°8'40\"",
            convertCoordinatesIntoDegreeFormat(context, -31.144546666666663, "lat")
        )
        assertEquals(
            "E 138°16'15\"",
            convertCoordinatesIntoDegreeFormat(context, 138.27083666666667, "lon")
        )
        assertEquals(
            "N 61°23'15\"",
            convertCoordinatesIntoDegreeFormat(context, 61.38757333333333, "lat")
        )
        assertEquals(
            "W 150°55'37\"",
            convertCoordinatesIntoDegreeFormat(context, -150.92708666666667, "lon")
        )
        assertEquals("N 0°0'0\"", convertCoordinatesIntoDegreeFormat(context, 0.0, "lat"))
        assertEquals("E 0°0'0\"", convertCoordinatesIntoDegreeFormat(context, 0.0, "lon"))
    }

    @Test
    fun floorTest() {
        assertEquals("5", floor("5"))
        assertEquals("-5", floor("-5"))
        assertEquals("5", floor("5.55"))
        assertEquals("-5", floor("-5.55"))
        assertEquals("", floor(""))
        assertEquals("", floor(null))
        assertEquals("qwerty", floor("qwerty"))
    }

    @Test
    fun parseGeometryPointTest() {
        var gp =
            parseGeometryPoint("37.45153333333334 -122.15539166666667 0.0 20.0")!!
        assertEquals(37.45153333333334, gp[0])
        assertEquals(-122.15539166666667, gp[1])
        assertEquals(0.0, gp[2])
        assertEquals(20.0, gp[3])

        gp = parseGeometryPoint("37.45153333333334")!!
        assertEquals(37.45153333333334, gp[0])
        assertEquals(0.0, gp[1])
        assertEquals(0.0, gp[2])
        assertEquals(0.0, gp[3])

        gp = parseGeometryPoint(" 37.45153333333334 -122.15539166666667 0.0 ")!!
        assertEquals(37.45153333333334, gp[0])
        assertEquals(-122.15539166666667, gp[1])
        assertEquals(0.0, gp[2])
        assertEquals(0.0, gp[3])

        assertEquals(null, parseGeometryPoint("37.45153333333334 -122.15539166666667 0.0 qwerty"))
        assertEquals(null, parseGeometryPoint(""))
        assertEquals(null, parseGeometryPoint(null))
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
    fun parseGeometryTest() {
        assertThat(parseGeometry("1.0 2.0 3 4"), equalTo(listOf(MapPoint(1.0, 2.0, 3.0, 4.0))))
        assertThat(
            parseGeometry("1.0 2.0 3 4; 5.0 6.0 7 8"),
            equalTo(listOf(MapPoint(1.0, 2.0, 3.0, 4.0), MapPoint(5.0, 6.0, 7.0, 8.0)))
        )

        assertThat(parseGeometry("blah"), equalTo(emptyList()))
        assertThat(parseGeometry("1.0 2.0 3 4; blah"), equalTo(emptyList()))
        assertThat(
            parseGeometry("37.45153333333334 -122.15539166666667 0.0 qwerty"),
            equalTo(emptyList())
        )
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
