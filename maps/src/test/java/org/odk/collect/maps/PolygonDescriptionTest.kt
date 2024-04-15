package org.odk.collect.maps

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PolygonDescriptionTest {
    @Test
    fun `getStrokeWidth returns the default value when the passed one is null`() {
        val polygonDescription = PolygonDescription(emptyList(), null, null, null)
        assertThat(polygonDescription.getStrokeWidth(), equalTo(MapConsts.DEFAULT_STROKE_WIDTH))
    }

    @Test
    fun `getStrokeWidth returns the default value when the passed one is invalid`() {
        val polygonDescription = PolygonDescription(emptyList(), "blah", null, null)
        assertThat(polygonDescription.getStrokeWidth(), equalTo(MapConsts.DEFAULT_STROKE_WIDTH))
    }

    @Test
    fun `getStrokeWidth returns the default value when the passed one is not greater than or equal to zero`() {
        val polygonDescription = PolygonDescription(emptyList(), "-1", null, null)
        assertThat(polygonDescription.getStrokeWidth(), equalTo(MapConsts.DEFAULT_STROKE_WIDTH))
    }

    @Test
    fun `getStrokeWidth returns custom value when the passed one is a valid int number`() {
        val polygonDescription = PolygonDescription(emptyList(), "10", null, null)
        assertThat(polygonDescription.getStrokeWidth(), equalTo(10f))
    }

    @Test
    fun `getStrokeWidth returns custom value when the passed one is a valid float number`() {
        val polygonDescription = PolygonDescription(emptyList(), "10.5", null, null)
        assertThat(polygonDescription.getStrokeWidth(), equalTo(10.5f))
    }

    @Test
    fun `getStrokeColor returns the default color when the passed one is null`() {
        val polygonDescription = PolygonDescription(emptyList(), "0", null, null)
        assertThat(polygonDescription.getStrokeColor(), equalTo(MapConsts.DEFAULT_STROKE_COLOR))
    }

    @Test
    fun `getStrokeColor returns the default color when the passed one is invalid`() {
        val polygonDescription = PolygonDescription(emptyList(), "0", "blah", null)
        assertThat(polygonDescription.getStrokeColor(), equalTo(MapConsts.DEFAULT_STROKE_COLOR))
    }

    @Test
    fun `getStrokeColor returns custom color when it is valid`() {
        val polygonDescription = PolygonDescription(emptyList(), "0", "#aaccee", null)
        assertThat(polygonDescription.getStrokeColor(), equalTo(-5583634))
    }

    @Test
    fun `getFillColor returns the default color when the passed one is null`() {
        val polygonDescription = PolygonDescription(emptyList(), "0", null, null)
        assertThat(polygonDescription.getFillColor(), equalTo(1157562368))
    }

    @Test
    fun `getFillColor returns the default color when the passed one is invalid`() {
        val polygonDescription = PolygonDescription(emptyList(), "0", null, "blah")
        assertThat(polygonDescription.getFillColor(), equalTo(1157562368))
    }

    @Test
    fun `getFillColor returns custom color when it is valid`() {
        val polygonDescription = PolygonDescription(emptyList(), "0", null, "#aaccee")
        assertThat(polygonDescription.getFillColor(), equalTo(1152044270))
    }
}
