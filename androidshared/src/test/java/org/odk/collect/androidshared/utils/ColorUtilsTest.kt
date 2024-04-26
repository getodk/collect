package org.odk.collect.androidshared.utils

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ColorUtilsTest {
    @Test
    fun `return null when color is empty`() {
        assertThat("".toColorInt(), equalTo(null))
    }

    @Test
    fun `return null when color is blank`() {
        assertThat(" ".toColorInt(), equalTo(null))
    }

    @Test
    fun `return null when color is invalid`() {
        assertThat("qwerty".toColorInt(), equalTo(null))
    }

    @Test
    fun `return color int for valid hex color with # prefix`() {
        assertThat("#aaccee".toColorInt(), equalTo(-5583634))
    }

    @Test
    fun `return color int for valid hex color without # prefix`() {
        assertThat("aaccee".toColorInt(), equalTo(-5583634))
    }

    @Test
    fun `return color int for valid shorthand hex color with # prefix`() {
        assertThat("#ace".toColorInt(), equalTo(-5583634))
    }

    @Test
    fun `return color int for valid shorthand hex color without # prefix`() {
        assertThat("ace".toColorInt(), equalTo(-5583634))
    }
}
