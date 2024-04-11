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
        val lineDescription = LineDescription(emptyList(), null, null, false, false)
        assertThat(lineDescription.getStrokeWidth(), equalTo(MapConsts.DEFAULT_STROKE_WIDTH))
    }

    @Test
    fun `getStrokeWidth returns the default value when the passed one is invalid`() {
        val lineDescription = LineDescription(emptyList(), "blah", null, false, false)
        assertThat(lineDescription.getStrokeWidth(), equalTo(MapConsts.DEFAULT_STROKE_WIDTH))
    }

    @Test
    fun `getStrokeWidth returns the default value when the passed one is not greater than or equal to zero`() {
        val lineDescription = LineDescription(emptyList(), "-1", null, false, false)
        assertThat(lineDescription.getStrokeWidth(), equalTo(MapConsts.DEFAULT_STROKE_WIDTH))
    }

    @Test
    fun `getStrokeWidth returns custom value when the passed one is a valid int number`() {
        val lineDescription = LineDescription(emptyList(), "10", null, false, false)
        assertThat(lineDescription.getStrokeWidth(), equalTo(10f))
    }

    @Test
    fun `getStrokeWidth returns custom value when the passed one is a valid float number`() {
        val lineDescription = LineDescription(emptyList(), "10.5", null, false, false)
        assertThat(lineDescription.getStrokeWidth(), equalTo(10.5f))
    }

    @Test
    fun `getStrokeColor returns the default color when the passed one is null`() {
        val lineDescription = LineDescription(emptyList(), null, null, false, false)
        assertThat(lineDescription.getStrokeColor(), equalTo(-65536))
    }

    @Test
    fun `getStrokeColor returns the default color when the passed one is invalid`() {
        val lineDescription = LineDescription(emptyList(), null, "blah", false, false)
        assertThat(lineDescription.getStrokeColor(), equalTo(-65536))
    }

    @Test
    fun `getStrokeColor returns custom color when it is valid`() {
        val lineDescription = LineDescription(emptyList(), null, "#aaccee", false, false)
        assertThat(lineDescription.getStrokeColor(), equalTo(-5583634))
    }
}
