package org.odk.collect.android.widgets.range

import android.app.Application
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RangeSliderTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `start, end and current value labels are correctly displayed for integer data`() {
        setContent(
            createTestState(
                sliderValue = 0.5f,
                rangeStart = 0f,
                rangeEnd = 10f,
                isDiscrete = true
            )
        )

        composeTestRule
            .onNodeWithContentDescription(context.getString(org.odk.collect.strings.R.string.slider_start_label))
            .assertIsDisplayed()
            .assertTextEquals("0")

        composeTestRule
            .onNodeWithContentDescription(context.getString(org.odk.collect.strings.R.string.slider_end_label))
            .assertIsDisplayed()
            .assertTextEquals("10")

        composeTestRule
            .onNodeWithContentDescription(context.getString(org.odk.collect.strings.R.string.current_slider_value))
            .assertIsDisplayed()
            .assertTextEquals("5")
    }

    @Test
    fun `start, end and current value labels are correctly displayed for decimal data`() {
        setContent(
            createTestState(
                sliderValue = 0.5f,
                rangeStart = 0f,
                rangeEnd = 10f,
                isDiscrete = false
            )
        )

        composeTestRule
            .onNodeWithContentDescription(context.getString(org.odk.collect.strings.R.string.slider_start_label))
            .assertIsDisplayed()
            .assertTextEquals("0.0")

        composeTestRule
            .onNodeWithContentDescription(context.getString(org.odk.collect.strings.R.string.slider_end_label))
            .assertIsDisplayed()
            .assertTextEquals("10.0")

        composeTestRule
            .onNodeWithContentDescription(context.getString(org.odk.collect.strings.R.string.current_slider_value))
            .assertIsDisplayed()
            .assertTextEquals("5.0")
    }

    @Test
    fun `does not display current value label when there is no answer`() {
        setContent(createTestState(sliderValue = null))

        composeTestRule
            .onNodeWithContentDescription(context.getString(org.odk.collect.strings.R.string.current_slider_value))
            .assertIsNotDisplayed()
    }

    @Test
    fun `displays thumb when there is answer`() {
        setContent(createTestState(sliderValue = 0.5f))

        composeTestRule
            .onNodeWithContentDescription(context.getString(org.odk.collect.strings.R.string.slider_thumb))
            .assertIsDisplayed()
    }

    @Test
    fun `does not display thumb when there is no answer`() {
        setContent(createTestState(sliderValue = null))

        composeTestRule
            .onNodeWithContentDescription(context.getString(org.odk.collect.strings.R.string.slider_thumb))
            .assertIsNotDisplayed()
    }

    @Test
    fun `displays horizontal slider when isHorizontal is true`() {
        setContent(createTestState(isHorizontal = true))

        composeTestRule
            .onNodeWithContentDescription(context.getString(org.odk.collect.strings.R.string.horizontal_slider))
            .assertIsEnabled()
    }

    @Test
    fun `displays vertical slider when isHorizontal is false`() {
        setContent(createTestState(isHorizontal = false))

        composeTestRule
            .onNodeWithContentDescription(context.getString(org.odk.collect.strings.R.string.vertical_slider))
            .assertIsEnabled()
    }

    @Test
    fun `calls onRangeInvalid when range is invalid`() {
        var onRangeInvalidCalled = false

        composeTestRule.setContent {
            RangeSliderFactory(
                initialState = createTestState(isValid = false),
                onValueChangeFinished = {},
                onValueChanging = {},
                onRangeInvalid = { onRangeInvalidCalled = true }
            )
        }

        assertThat(
            onRangeInvalidCalled,
            equalTo(true)
        )
    }

    @Test
    fun `does not call onRangeInvalid when range is valid`() {
        var onRangeInvalidCalled = false

        composeTestRule.setContent {
            RangeSliderFactory(
                initialState = createTestState(isValid = true),
                onValueChangeFinished = {},
                onValueChanging = {},
                onRangeInvalid = { onRangeInvalidCalled = true }
            )
        }

        assertThat(
            onRangeInvalidCalled,
            equalTo(false)
        )
    }

    @Test
    fun `enables slider when isEnabled is true`() {
        setContent(createTestState(isEnabled = true))

        composeTestRule
            .onNodeWithContentDescription(context.getString(org.odk.collect.strings.R.string.horizontal_slider))
            .assertIsEnabled()
    }

    @Test
    fun `disables slider when isEnabled is false`() {
        setContent(createTestState(isEnabled = false))

        composeTestRule
            .onNodeWithContentDescription(context.getString(org.odk.collect.strings.R.string.horizontal_slider))
            .assertIsNotEnabled()
    }

    @Test
    fun `displays ticks when numOfTicks is greater than 0`() {
        setContent(createTestState(numOfTicks = 3))

        composeTestRule
            .onAllNodesWithContentDescription(
                context.getString(org.odk.collect.strings.R.string.slider_tick),
                useUnmergedTree = true
            )
            .assertCountEquals(3)
    }

    @Test
    fun `does not display ticks when numOfTicks is 0`() {
        setContent(createTestState(numOfTicks = 0))

        composeTestRule
            .onAllNodesWithContentDescription(
                context.getString(org.odk.collect.strings.R.string.slider_tick),
                useUnmergedTree = true
            )
            .assertCountEquals(0)
    }

    @Test
    fun `calls onValueChangeFinished callback with minimum when horizontal slider start is clicked`() {
        var newState: RangeSliderState? = null

        composeTestRule.setContent {
            RangeSliderFactory(
                initialState = createTestState(sliderValue = null, isHorizontal = true),
                onValueChangeFinished = {
                    newState = it
                },
                onValueChanging = {},
                onRangeInvalid = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription(context.getString(org.odk.collect.strings.R.string.horizontal_slider))
            .performTouchInput { click(centerLeft) }

        assertThat(newState?.sliderValue, equalTo(0F))
    }

    @Test
    fun `calls onValueChangeFinished callback with midpoint when horizontal slider is clicked at center`() {
        var newState: RangeSliderState? = null

        composeTestRule.setContent {
            RangeSliderFactory(
                initialState = createTestState(sliderValue = null, isHorizontal = true),
                onValueChangeFinished = {
                    newState = it
                },
                onValueChanging = {},
                onRangeInvalid = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription(context.getString(org.odk.collect.strings.R.string.horizontal_slider))
            .performTouchInput { click() }

        assertThat(newState?.sliderValue, equalTo(0.5F))
    }

    @Test
    fun `calls onValueChangeFinished callback with minimum when vertical slider start is clicked`() {
        var newState: RangeSliderState? = null

        composeTestRule.setContent {
            RangeSliderFactory(
                initialState = createTestState(sliderValue = null, isHorizontal = false),
                onValueChangeFinished = {
                    newState = it
                },
                onValueChanging = {},
                onRangeInvalid = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription(context.getString(org.odk.collect.strings.R.string.vertical_slider))
            .performTouchInput { click(bottomCenter) }

        assertThat(newState?.sliderValue, equalTo(0F))
    }

    @Test
    fun `calls onValueChangeFinished callback with midpoint when vertical slider is clicked at center`() {
        var newState: RangeSliderState? = null

        composeTestRule.setContent {
            RangeSliderFactory(
                initialState = createTestState(sliderValue = null, isHorizontal = false),
                onValueChangeFinished = {
                    newState = it
                },
                onValueChanging = {},
                onRangeInvalid = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription(context.getString(org.odk.collect.strings.R.string.vertical_slider))
            .performTouchInput { click() }

        assertThat(newState?.sliderValue, equalTo(0.5F))
    }

    private fun setContent(sliderState: RangeSliderState) {
        composeTestRule.setContent {
            RangeSliderFactory(
                initialState = sliderState,
                onValueChangeFinished = {},
                onValueChanging = {},
                onRangeInvalid = {}
            )
        }
    }

    private fun createTestState(
        sliderValue: Float? = 0.5f,
        rangeStart: Float = 0f,
        rangeEnd: Float = 10f,
        numOfSteps: Int = 9,
        isDiscrete: Boolean = true,
        isHorizontal: Boolean = true,
        isValid: Boolean = true,
        isEnabled: Boolean = true,
        numOfTicks: Int = 0
    ): RangeSliderState {
        return RangeSliderState(
            sliderValue = sliderValue,
            rangeStart = rangeStart,
            rangeEnd = rangeEnd,
            numOfSteps = numOfSteps,
            isDiscrete = isDiscrete,
            isHorizontal = isHorizontal,
            isValid = isValid,
            isEnabled = isEnabled,
            numOfTicks = numOfTicks
        )
    }
}
