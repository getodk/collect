package org.odk.collect.androidshared.ui

import androidx.compose.ui.unit.IntOffset
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class OffsetUtilsTest {
    @Test
    fun `calculateOffset returns zero offset when horizontal and value is 0`() {
        val result = OffsetUtils.calculateOffset(
            trackSize = 1000,
            itemSize = 100f,
            value = 0f,
            isVertical = false
        )
        assertThat(result, equalTo(IntOffset(0, 0)))
    }

    @Test
    fun `calculateOffset returns max offset when horizontal and value is 1`() {
        val result = OffsetUtils.calculateOffset(
            trackSize = 1000,
            itemSize = 100f,
            value = 1f,
            isVertical = false
        )
        assertThat(result, equalTo(IntOffset(900, 0)))
    }

    @Test
    fun `calculateOffset returns middle offset when horizontal and value is 0,5`() {
        val result = OffsetUtils.calculateOffset(
            trackSize = 1000,
            itemSize = 100f,
            value = 0.5f,
            isVertical = false
        )
        assertThat(result, equalTo(IntOffset(450, 0)))
    }

    @Test
    fun `calculateOffset returns max offset when vertical and value is 0`() {
        val result = OffsetUtils.calculateOffset(
            trackSize = 1000,
            itemSize = 100f,
            value = 0f,
            isVertical = true
        )
        assertThat(result, equalTo(IntOffset(0, 900)))
    }

    @Test
    fun `calculateOffset returns zero offset when vertical and value is 1`() {
        val result = OffsetUtils.calculateOffset(
            trackSize = 1000,
            itemSize = 100f,
            value = 1f,
            isVertical = true
        )
        assertThat(result, equalTo(IntOffset(0, 0)))
    }

    @Test
    fun `calculateOffset returns middle offset when vertical and value is 0,5`() {
        val result = OffsetUtils.calculateOffset(
            trackSize = 1000,
            itemSize = 100f,
            value = 0.5f,
            isVertical = true
        )
        assertThat(result, equalTo(IntOffset(0, 450)))
    }

    @Test
    fun `calculateOffset returns zero offset when itemWidth equals trackSize`() {
        val result = OffsetUtils.calculateOffset(
            trackSize = 100,
            itemSize = 100f,
            value = 0.5f,
            isVertical = false
        )
        assertThat(result, equalTo(IntOffset(0, 0)))
    }
}
