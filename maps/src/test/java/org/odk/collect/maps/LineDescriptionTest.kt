package org.odk.collect.maps

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LineDescriptionTest {
    @Test
    fun `getStrokeWidth returns the default value when the passed one is null`() {
        val lineDescription = LineDescription(strokeWidth = null)
        assertThat(lineDescription.getStrokeWidth(), equalTo(MapConsts.DEFAULT_STROKE_WIDTH))
    }

    @Test
    fun `getStrokeWidth returns the default value when the passed one is invalid`() {
        val lineDescription = LineDescription(strokeWidth = "blah")
        assertThat(lineDescription.getStrokeWidth(), equalTo(MapConsts.DEFAULT_STROKE_WIDTH))
    }

    @Test
    fun `getStrokeWidth returns the default value when the passed one is not greater than or equal to zero`() {
        val lineDescription = LineDescription(strokeWidth = "-1")
        assertThat(lineDescription.getStrokeWidth(), equalTo(MapConsts.DEFAULT_STROKE_WIDTH))
    }

    @Test
    fun `getStrokeWidth returns custom value when the passed one is a valid int number`() {
        val lineDescription = LineDescription(strokeWidth = "10")
        assertThat(lineDescription.getStrokeWidth(), equalTo(10f))
    }

    @Test
    fun `getStrokeWidth returns custom value when the passed one is a valid float number`() {
        val lineDescription = LineDescription(strokeWidth = "10.5")
        assertThat(lineDescription.getStrokeWidth(), equalTo(10.5f))
    }

    @Test
    fun `getStrokeColor returns the default color when the passed one is null`() {
        val lineDescription = LineDescription(strokeColor = null)
        assertThat(lineDescription.getStrokeColor(), equalTo(MapConsts.DEFAULT_STROKE_COLOR))
    }

    @Test
    fun `getStrokeColor returns the default color when the passed one is invalid`() {
        val lineDescription = LineDescription(strokeColor = "blah")
        assertThat(lineDescription.getStrokeColor(), equalTo(MapConsts.DEFAULT_STROKE_COLOR))
    }

    @Test
    fun `getStrokeColor returns custom color when it is valid`() {
        val lineDescription = LineDescription(strokeColor = "#aaccee")
        assertThat(lineDescription.getStrokeColor(), equalTo(-5583634))
    }
}
