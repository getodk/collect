package org.odk.collect.maps

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.nullValue
import org.hamcrest.core.Is.`is`
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.maps.markers.MarkerIconDescription

@RunWith(AndroidJUnit4::class)
class MarkerIconDescriptionTest {

    @Test
    fun `return null when color is null`() {
        val markerIconDescription = MarkerIconDescription(0, null)
        assertThat(markerIconDescription.getColor(), `is`(nullValue()))
    }

    @Test
    fun `return null when color is empty`() {
        val markerIconDescription = MarkerIconDescription(0, "")
        assertThat(markerIconDescription.getColor(), `is`(nullValue()))
    }

    @Test
    fun `return null when color is invalid`() {
        val markerIconDescription = MarkerIconDescription(0, "qwerty")
        assertThat(markerIconDescription.getColor(), `is`(nullValue()))
    }

    @Test
    fun `return color int for valid hex color with # prefix`() {
        val markerIconDescription = MarkerIconDescription(0, "#aaccee")
        assertThat(markerIconDescription.getColor(), `is`(-5583634))
    }

    @Test
    fun `return color int for valid hex color without # prefix`() {
        val markerIconDescription = MarkerIconDescription(0, "aaccee")
        assertThat(markerIconDescription.getColor(), `is`(-5583634))
    }

    @Test
    fun `return color int for valid shorthand hex color with # prefix`() {
        val markerIconDescription = MarkerIconDescription(0, "#ace")
        assertThat(markerIconDescription.getColor(), `is`(-5583634))
    }

    @Test
    fun `return color int for valid shorthand hex color without # prefix`() {
        val markerIconDescription = MarkerIconDescription(0, "ace")
        assertThat(markerIconDescription.getColor(), `is`(-5583634))
    }

    @Test
    fun `return null when symbol is null`() {
        val markerIconDescription = MarkerIconDescription(0, symbol = null)
        assertThat(markerIconDescription.getSymbol(), `is`(nullValue()))
    }

    @Test
    fun `return null when symbol is empty`() {
        val markerIconDescription = MarkerIconDescription(0, symbol = "")
        assertThat(markerIconDescription.getSymbol(), `is`(nullValue()))
    }

    @Test
    fun `return first char when symbol consists of multiple chars`() {
        val markerIconDescription = MarkerIconDescription(0, symbol = "Blah")
        assertThat(markerIconDescription.getSymbol(), `is`("B"))
    }

    @Test
    fun `return uppercase symbol`() {
        val markerIconDescription = MarkerIconDescription(0, symbol = "b")
        assertThat(markerIconDescription.getSymbol(), `is`("B"))
    }

    @Test
    fun `return emoji symbol`() {
        val markerIconDescription = MarkerIconDescription(0, symbol = "\uD83E\uDDDB")
        assertThat(markerIconDescription.getSymbol(), `is`("\uD83E\uDDDB"))
    }
}
