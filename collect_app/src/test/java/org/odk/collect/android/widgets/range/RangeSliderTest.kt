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
import org.odk.collect.android.application.RobolectricApplication
import org.odk.collect.androidshared.ui.ToastUtils

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
    fun `displays toast when range is invalid`() {
        val application = ApplicationProvider.getApplicationContext<RobolectricApplication>()
        ToastUtils.alertStore.enabled = true

        setContent(createTestState(isValid = false))

        val latestToast = ToastUtils.alertStore.popAll().lastOrNull()
        assertThat(
            latestToast,
            equalTo(application.getString(org.odk.collect.strings.R.string.invalid_range_widget))
        )
    }

    @Test
    fun `does not display toast when range is valid`() {
        ToastUtils.alertStore.enabled = true

        setContent(createTestState(isValid = true))

        val latestToast = ToastUtils.alertStore.popAll().lastOrNull()
        assertThat(
            latestToast,
            equalTo(null)
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
    fun `calls onValueChange callback with minimum when horizontal slider is clicked`() {
        var changedValue: Float? = null

        composeTestRule.setContent {
            RangeSlider(
                rangeSliderState = createTestState(sliderValue = null, isHorizontal = true),
                onValueChange = { changedValue = it },
                onValueChangeFinished = {},
                onValueChanging = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription(context.getString(org.odk.collect.strings.R.string.horizontal_slider))
            .performTouchInput { click(centerLeft) }

        assertThat(changedValue, equalTo(0F))
    }

    @Test
    fun `calls onValueChange callback with midpoint when horizontal slider is clicked`() {
        var changedValue: Float? = null

        composeTestRule.setContent {
            RangeSlider(
                rangeSliderState = createTestState(sliderValue = null, isHorizontal = true),
                onValueChange = { changedValue = it },
                onValueChangeFinished = {},
                onValueChanging = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription(context.getString(org.odk.collect.strings.R.string.horizontal_slider))
            .performTouchInput { click() }

        assertThat(changedValue, equalTo(0.5F))
    }

    @Test
    fun `calls onValueChange callback with minimum when vertical slider is clicked`() {
        var changedValue: Float? = null

        composeTestRule.setContent {
            RangeSlider(
                rangeSliderState = createTestState(sliderValue = null, isHorizontal = false),
                onValueChange = { changedValue = it },
                onValueChangeFinished = {},
                onValueChanging = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription(context.getString(org.odk.collect.strings.R.string.vertical_slider))
            .performTouchInput { click(bottomCenter) }

        assertThat(changedValue, equalTo(0F))
    }

    @Test
    fun `calls onValueChange callback with midpoint when vertical slider is clicked`() {
        var changedValue: Float? = null

        composeTestRule.setContent {
            RangeSlider(
                rangeSliderState = createTestState(sliderValue = null, isHorizontal = false),
                onValueChange = { changedValue = it },
                onValueChangeFinished = {},
                onValueChanging = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription(context.getString(org.odk.collect.strings.R.string.vertical_slider))
            .performTouchInput { click() }

        assertThat(changedValue, equalTo(0.5F))
    }

    private fun setContent(rangeSliderState: RangeSliderState) {
        composeTestRule.setContent {
            RangeSlider(
                rangeSliderState = rangeSliderState,
                onValueChange = {},
                onValueChangeFinished = {},
                onValueChanging = {}
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
