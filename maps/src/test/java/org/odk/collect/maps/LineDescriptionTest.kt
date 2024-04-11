package org.odk.collect.maps

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LineDescriptionTest {
    @Test
    fun `getStrokeColor returns the default color when the passed one is null`() {
        val lineDescription = LineDescription(emptyList(), null, false, false)
        assertThat(lineDescription.getStrokeColor(), equalTo(-65536))
    }

    @Test
    fun `getStrokeColor returns the default color when the passed one is invalid`() {
        val lineDescription = LineDescription(emptyList(), "blah", false, false)
        assertThat(lineDescription.getStrokeColor(), equalTo(-65536))
    }

    @Test
    fun `getStrokeColor returns custom color when it is valid`() {
        val lineDescription = LineDescription(emptyList(), "#aaccee", false, false)
        assertThat(lineDescription.getStrokeColor(), equalTo(-5583634))
    }
}
