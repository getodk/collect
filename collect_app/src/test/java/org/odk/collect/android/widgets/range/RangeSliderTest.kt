package org.odk.collect.android.widgets.range

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.androidtest.onAllNodesWithContentDescription
import org.odk.collect.androidtest.onNodeWithContentDescription

@RunWith(AndroidJUnit4::class)
class RangeSliderTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `start, end and current value labels are correctly displayed`() {
        setContent(value = 0.5F)

        composeTestRule
            .onNodeWithContentDescription(org.odk.collect.strings.R.string.slider_start_label)
            .assertIsDisplayed()
            .assertTextEquals("0")

        composeTestRule
            .onNodeWithContentDescription(org.odk.collect.strings.R.string.slider_end_label)
            .assertIsDisplayed()
            .assertTextEquals("10")

        composeTestRule
            .onNodeWithContentDescription(org.odk.collect.strings.R.string.current_slider_value)
            .assertIsDisplayed()
            .assertTextEquals("5")
    }

    @Test
    fun `displays thumb when there is answer`() {
        setContent(value = 0.5F)

        composeTestRule
            .onNodeWithContentDescription(org.odk.collect.strings.R.string.slider_thumb)
            .assertIsDisplayed()
    }

    @Test
    fun `does not display thumb when there is no answer`() {
        setContent(value = null)

        composeTestRule
            .onNodeWithContentDescription(org.odk.collect.strings.R.string.slider_thumb)
            .assertIsNotDisplayed()
    }

    @Test
    fun `displays horizontal slider when isHorizontal is true`() {
        setContent(horizontal = true)

        composeTestRule
            .onNodeWithContentDescription(org.odk.collect.strings.R.string.horizontal_slider)
            .assertIsEnabled()
    }

    @Test
    fun `displays vertical slider when isHorizontal is false`() {
        setContent(horizontal = false)

        composeTestRule
            .onNodeWithContentDescription(org.odk.collect.strings.R.string.vertical_slider)
            .assertIsEnabled()
    }

    @Test
    fun `calls onRangeInvalid when range is invalid`() {
        var onRangeInvalidCalled = false

        setContent(
            valid = false,
            onRangeInvalid = { onRangeInvalidCalled = true }
        )

        assertThat(
            onRangeInvalidCalled,
            equalTo(true)
        )
    }

    @Test
    fun `does not call onRangeInvalid when range is valid`() {
        var onRangeInvalidCalled = false

        setContent(
            valid = true,
            onRangeInvalid = { onRangeInvalidCalled = true }
        )

        assertThat(
            onRangeInvalidCalled,
            equalTo(false)
        )
    }

    @Test
    fun `enables slider when isEnabled is true`() {
        setContent(enabled = true)

        composeTestRule
            .onNodeWithContentDescription(org.odk.collect.strings.R.string.horizontal_slider)
            .assertIsEnabled()
    }

    @Test
    fun `disables slider when isEnabled is false`() {
        setContent(enabled = false)

        composeTestRule
            .onNodeWithContentDescription(org.odk.collect.strings.R.string.horizontal_slider)
            .assertIsNotEnabled()
    }

    @Test
    fun `displays ticks when numOfTicks is greater than 0`() {
        setContent(ticks = 3)

        composeTestRule
            .onAllNodesWithContentDescription(
                org.odk.collect.strings.R.string.slider_tick,
                useUnmergedTree = true
            )
            .assertCountEquals(3)
    }

    @Test
    fun `does not display ticks when numOfTicks is 0`() {
        setContent(ticks = 0)

        composeTestRule
            .onAllNodesWithContentDescription(
                org.odk.collect.strings.R.string.slider_tick,
                useUnmergedTree = true
            )
            .assertCountEquals(0)
    }

    @Test
    fun `calls onValueChange callback with minimum when horizontal slider start is clicked`() {
        var newValue: Float? = null

        setContent(
            value = null,
            horizontal = true,
            onValueChange = { newValue = it }
        )

        composeTestRule
            .onNodeWithContentDescription(org.odk.collect.strings.R.string.horizontal_slider)
            .performTouchInput { click(centerLeft) }

        assertThat(newValue, equalTo(0F))
    }

    @Test
    fun `calls onValueChange callback with midpoint when horizontal slider is clicked at center`() {
        var newValue: Float? = null

        setContent(
            value = null,
            horizontal = true,
            onValueChange = { newValue = it }
        )

        composeTestRule
            .onNodeWithContentDescription(org.odk.collect.strings.R.string.horizontal_slider)
            .performTouchInput { click() }

        assertEquals(0.5f, newValue!!, 0.046f)
    }

    @Test
    fun `calls onValueChange callback with minimum when vertical slider start is clicked`() {
        var newValue: Float? = null

        setContent(
            value = null,
            horizontal = false,
            onValueChange = { newValue = it }
        )

        composeTestRule
            .onNodeWithContentDescription(org.odk.collect.strings.R.string.vertical_slider)
            .performTouchInput { click(bottomCenter) }

        assertThat(newValue, equalTo(0F))
    }

    @Test
    fun `calls onValueChange callback with midpoint when vertical slider is clicked at center`() {
        var newValue: Float? = null

        setContent(
            value = null,
            horizontal = false,
            onValueChange = { newValue = it }
        )

        composeTestRule
            .onNodeWithContentDescription(org.odk.collect.strings.R.string.vertical_slider)
            .performClick()
            .performTouchInput { click() }

        assertEquals(0.5f, newValue!!, 0.046f)
    }

    private fun setContent(
        value: Float? = null,
        valueLabel: String = "5",
        steps: Int = 10,
        ticks: Int = 0,
        enabled: Boolean = true,
        valid: Boolean = true,
        horizontal: Boolean = true,
        startLabel: String = "0",
        endLabel: String = "10",
        onValueChange: (Float) -> Unit = {},
        onRangeInvalid: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            RangeSlider(
                value = value,
                valueLabel = valueLabel,
                steps = steps,
                ticks = ticks,
                enabled = enabled,
                valid = valid,
                horizontal = horizontal,
                startLabel = startLabel,
                endLabel = endLabel,
                onValueChange = onValueChange,
                onValueChangeFinished = {},
                onValueChanging = {},
                onRangeInvalid = onRangeInvalid
            )
        }
    }
}
