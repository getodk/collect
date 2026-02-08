package org.odk.collect.android.widgets.range

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.javarosa.core.model.Constants
import org.javarosa.core.model.RangeQuestion
import org.javarosa.core.model.data.DecimalData
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.support.WidgetTestActivity
import org.odk.collect.android.widgets.base.QuestionWidgetTest
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
class RangeDecimalWidgetTest : QuestionWidgetTest<RangeDecimalWidget, DecimalData>() {
    @get:Rule
    val composeRule = createAndroidComposeRule<WidgetTestActivity>()

    override fun createWidget(): RangeDecimalWidget {
        val rangeQuestion = mock<RangeQuestion>().apply {
            whenever(rangeStart).thenReturn(BigDecimal.valueOf(1.5))
            whenever(rangeEnd).thenReturn(BigDecimal.valueOf(5.5))
            whenever(rangeStep).thenReturn(BigDecimal.valueOf(0.5))
        }

        whenever(formEntryPrompt.question).thenReturn(rangeQuestion)
        whenever(formEntryPrompt.dataType).thenReturn(Constants.DATATYPE_DECIMAL)

        return RangeDecimalWidget(
            activity,
            QuestionDetails(formEntryPrompt),
            QuestionWidgetHelpers.widgetDependencies()
        ).also {
            composeRule.activity.setContentView(it)
        }
    }

    override fun getNextAnswer() = DecimalData(2.5)

    @Test
    fun clearAnswer_hidesCurrentValueLabelAndThumb() {
        whenever(formEntryPrompt.answerValue).thenReturn(DecimalData(1.5))

        widget.clearAnswer()

        composeRule
            .onNodeWithContentDescription(activity.getString(org.odk.collect.strings.R.string.current_slider_value))
            .assertIsNotDisplayed()

        composeRule
            .onNodeWithContentDescription(activity.getString(org.odk.collect.strings.R.string.slider_thumb))
            .assertIsNotDisplayed()
    }

    @Test
    fun changingSliderValue_showsCurrentValueLabelAndThumb() {
        createWidget()

        composeRule
            .onNodeWithContentDescription(activity.getString(org.odk.collect.strings.R.string.horizontal_slider))
            .performTouchInput { click() }

        composeRule
            .onNodeWithContentDescription(activity.getString(org.odk.collect.strings.R.string.current_slider_value))
            .assertIsDisplayed()
            .assertTextEquals("3.5")

        composeRule
            .onNodeWithContentDescription(activity.getString(org.odk.collect.strings.R.string.slider_thumb))
            .assertIsDisplayed()
    }

    @Test
    fun clearAnswer_callsValueChangeListener() {
        whenever(formEntryPrompt.answerValue).thenReturn(DecimalData(1.5))
        val valueChangedListener = mockValueChangedListener(widget)
        widget.clearAnswer()
        verify(valueChangedListener).widgetValueChanged(widget)
    }

    @Test
    fun changingSliderValue_callsValueChangeListener() {
        val valueChangedListener = mockValueChangedListener(widget)

        composeRule
            .onNodeWithContentDescription(activity.getString(org.odk.collect.strings.R.string.horizontal_slider))
            .performTouchInput { click() }

        verify(valueChangedListener).widgetValueChanged(widget)
    }

    override fun usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withReadOnly(true)
            .build()
        createWidget()

        composeRule
            .onNodeWithContentDescription(activity.getString(org.odk.collect.strings.R.string.horizontal_slider))
            .assertIsNotEnabled()
    }
}
