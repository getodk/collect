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
        val polygonDescription = PolygonDescription(strokeWidth = null)
        assertThat(polygonDescription.getStrokeWidth(), equalTo(MapConsts.DEFAULT_STROKE_WIDTH))
    }

    @Test
    fun `getStrokeWidth returns the default value when the passed one is invalid`() {
        val polygonDescription = PolygonDescription(strokeWidth = "blah")
        assertThat(polygonDescription.getStrokeWidth(), equalTo(MapConsts.DEFAULT_STROKE_WIDTH))
    }

    @Test
    fun `getStrokeWidth returns the default value when the passed one is not greater than or equal to zero`() {
        val polygonDescription = PolygonDescription(strokeWidth = "-1")
        assertThat(polygonDescription.getStrokeWidth(), equalTo(MapConsts.DEFAULT_STROKE_WIDTH))
    }

    @Test
    fun `getStrokeWidth returns custom value when the passed one is a valid int number`() {
        val polygonDescription = PolygonDescription(strokeWidth = "10")
        assertThat(polygonDescription.getStrokeWidth(), equalTo(10f))
    }

    @Test
    fun `getStrokeWidth returns custom value when the passed one is a valid float number`() {
        val polygonDescription = PolygonDescription(strokeWidth = "10.5")
        assertThat(polygonDescription.getStrokeWidth(), equalTo(10.5f))
    }

    @Test
    fun `getStrokeColor returns the default color when the passed one is null`() {
        val polygonDescription = PolygonDescription(strokeColor = null)
        assertThat(polygonDescription.getStrokeColor(), equalTo(MapConsts.DEFAULT_STROKE_COLOR))
    }

    @Test
    fun `getStrokeColor returns the default color when the passed one is invalid`() {
        val polygonDescription = PolygonDescription(strokeColor = "blah")
        assertThat(polygonDescription.getStrokeColor(), equalTo(MapConsts.DEFAULT_STROKE_COLOR))
    }

    @Test
    fun `getStrokeColor returns custom color when it is valid`() {
        val polygonDescription = PolygonDescription(strokeColor = "#aaccee")
        assertThat(polygonDescription.getStrokeColor(), equalTo(-5583634))
    }

    @Test
    fun `getFillColor returns the default color when the passed one is null`() {
        val polygonDescription = PolygonDescription(fillColor = null)
        assertThat(polygonDescription.getFillColor(), equalTo(1157562368))
    }

    @Test
    fun `getFillColor returns the default color when the passed one is invalid`() {
        val polygonDescription = PolygonDescription(fillColor = "blah")
        assertThat(polygonDescription.getFillColor(), equalTo(1157562368))
    }

    @Test
    fun `getFillColor returns custom color when it is valid`() {
        val polygonDescription = PolygonDescription(fillColor = "#aaccee")
        assertThat(polygonDescription.getFillColor(), equalTo(1152044270))
    }
}
