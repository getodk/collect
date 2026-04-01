package org.odk.collect.android.widgets.range

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.Constants.DATATYPE_DECIMAL
import org.javarosa.core.model.Constants.DATATYPE_INTEGER
import org.javarosa.core.model.RangeQuestion
import org.javarosa.core.model.SelectChoice
import org.javarosa.core.model.data.IntegerData
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.widgets.support.FormEntryPromptSelectChoiceLoader

class RangeSliderStateTest {
    private val question = mock<RangeQuestion>()
    private val promptBuilder = MockFormEntryPromptBuilder().withQuestion(question)
    private val selectChoiceLoader = FormEntryPromptSelectChoiceLoader()

    @Test
    fun `sets sliderValue to null when answer is out of range`() {
        mockIntQuestion(1, 10, 1)

        val prompt = promptBuilder.withAnswer(IntegerData(0)).build()
        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)

        assertThat(state.sliderValue, equalTo(null))
    }

    @Test
    fun `sets sliderValue to null when range is invalid`() {
        mockIntQuestion(0, 10, 15)

        val prompt = promptBuilder.withAnswer(IntegerData(5)).build()
        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)

        assertThat(state.sliderValue, equalTo(null))
    }

    @Test
    fun `maps real value correctly for ascending range`() {
        mockIntQuestion(0, 10, 1)

        val prompt = promptBuilder
            .withAnswer(IntegerData(7))
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)

        assertThat(state.sliderValue!!.stripTrailingZeros(), equalTo(0.7.toBigDecimal()))
        assertThat(state.realValue, equalTo(7.toBigDecimal()))
    }

    @Test
    fun `maps real value correctly for descending range`() {
        mockIntQuestion(10, 0, 1)

        val prompt = promptBuilder
            .withAnswer(IntegerData(7))
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)

        assertThat(state.sliderValue!!.stripTrailingZeros(), equalTo(0.3.toBigDecimal()))
        assertThat(state.realValue, equalTo(7.toBigDecimal()))
    }

    @Test
    fun `returns null for realValue when sliderValue is null`() {
        mockIntQuestion(0, 10, 1)

        val prompt = promptBuilder
            .withAnswer(null)
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)

        assertThat(state.realValue, equalTo(null))
    }

    @Test
    fun `sets ranges correctly for ascending range`() {
        mockIntQuestion(0, 10, 1)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)

        assertThat(state.rangeStart, equalTo(0.toBigDecimal()))
        assertThat(state.startLabel, equalTo("0"))

        assertThat(state.rangeEnd, equalTo(10.toBigDecimal()))
        assertThat(state.endLabel, equalTo("10"))
    }

    @Test
    fun `sets ranges correctly for descending range`() {
        mockIntQuestion(10, 0, 1)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)

        assertThat(state.rangeStart, equalTo(10.toBigDecimal()))
        assertThat(state.startLabel, equalTo("10"))

        assertThat(state.rangeEnd, equalTo(0.toBigDecimal()))
        assertThat(state.endLabel, equalTo("0"))
    }

    @Test
    fun `formats ranges correctly for decimal data`() {
        mockDecimalQuestion(0.5F, 10.5F, 0.5F)

        val prompt = promptBuilder
            .withDataType(DATATYPE_DECIMAL)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)

        assertThat(state.startLabel, equalTo("0.5"))
        assertThat(state.endLabel, equalTo("10.5"))
    }

    @Test
    fun `calculates numOfSteps correctly for integer data`() {
        mockIntQuestion(0, 10, 1)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)
        assertThat(state.numOfSteps, equalTo(9))

        whenever(question.rangeStep).thenReturn(2.toBigDecimal())

        val newState = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)
        assertThat(newState.numOfSteps, equalTo(4))
    }

    @Test
    fun `calculates numOfSteps correctly for decimal data`() {
        mockDecimalQuestion(0F, 1F, 0.2F)

        val prompt = promptBuilder
            .withDataType(DATATYPE_DECIMAL)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)

        assertThat(state.numOfSteps, equalTo(4))
    }

    @Test
    fun `treats negative step as positive`() {
        mockIntQuestion(0, 10, -1)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)

        assertThat(state.isValid, equalTo(true))
        assertThat(state.numOfSteps, equalTo(9))
    }

    @Test
    fun `returns empty valueLabel when realValue is null`() {
        mockIntQuestion(0, 10, 1)

        val prompt = promptBuilder
            .withAnswer(null)
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)

        assertThat(state.valueLabel, equalTo(""))
    }

    @Test
    fun `formats valueLabel as integer for discrete data`() {
        mockIntQuestion(0, 10, 1)

        val prompt = promptBuilder
            .withAnswer(IntegerData(5))
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)

        assertThat(state.valueLabel, equalTo("5"))
    }

    @Test
    fun `formats valueLabel as decimal for non-discrete data`() {
        mockDecimalQuestion(0F, 10F, 1F)

        val prompt = promptBuilder
            .withAnswer(IntegerData(5))
            .withDataType(DATATYPE_DECIMAL)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)

        assertThat(state.valueLabel, equalTo("5.0"))
    }

    @Test
    fun `returns true for isDiscrete when dataType is integer`() {
        mockIntQuestion(0, 10, 1)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)
        assertThat(state.isDiscrete, equalTo(true))
    }

    @Test
    fun `returns false for isDiscrete when dataType is decimal`() {
        mockDecimalQuestion(0F, 10F, 1F)

        val prompt = promptBuilder
            .withDataType(DATATYPE_DECIMAL)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)
        assertThat(state.isDiscrete, equalTo(false))
    }

    @Test
    fun `returns true for isEnabled when prompt is not readOnly`() {
        mockIntQuestion(0, 10, 1)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)

        assertThat(state.isEnabled, equalTo(true))
    }

    @Test
    fun `returns false for isEnabled when prompt is readOnly`() {
        mockIntQuestion(0, 10, 1)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .withReadOnly(true)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)

        assertThat(state.isEnabled, equalTo(false))
    }

    @Test
    fun `returns false for isEnabled when range is invalid`() {
        mockIntQuestion(0, 10, 3)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .withReadOnly(false)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)

        assertThat(state.isEnabled, equalTo(false))
    }

    @Test
    fun `returns true for isValid when range is valid`() {
        mockIntQuestion(0, 10, 1)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)
        assertThat(state.isValid, equalTo(true))
    }

    @Test
    fun `returns false for isValid when rangeStart equals rangeEnd`() {
        mockIntQuestion(0, 0, 1)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)
        assertThat(state.isValid, equalTo(false))
    }

    @Test
    fun `returns false for isValid when step does not evenly divide range`() {
        mockIntQuestion(0, 10, 3)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)
        assertThat(state.isValid, equalTo(false))
    }

    @Test
    fun `returns false for isValid when step is 0`() {
        mockIntQuestion(0, 10, 0)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)
        assertThat(state.isValid, equalTo(false))
    }

    @Test
    fun `returns false for isValid when step is greater than range`() {
        mockIntQuestion(0, 10, 100000000)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)
        assertThat(state.isValid, equalTo(false))
    }

    @Test
    fun `returns true for isHorizontal when vertical appearance is not used`() {
        mockIntQuestion(0, 10, 1)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)
        assertThat(state.isHorizontal, equalTo(true))
    }

    @Test
    fun `returns false for isHorizontal when vertical appearance is used`() {
        mockIntQuestion(0, 10, 1)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .withAppearance(Appearances.VERTICAL)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)
        assertThat(state.isHorizontal, equalTo(false))
    }

    @Test
    fun `returns numOfTicks based on possible steps if tickInterval is not set`() {
        mockIntQuestion(0, 10, 1)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)
        assertThat(state.numOfTicks, equalTo(11))

        whenever(question.rangeStep).thenReturn(2.toBigDecimal())

        val newState = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)
        assertThat(newState.numOfTicks, equalTo(6))
    }

    @Test
    fun `returns numOfTicks based on tickInterval if it is set and valid`() {
        mockIntQuestion(0, 10, 1, tickInterval = 2)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)
        assertThat(state.numOfTicks, equalTo(6))
    }

    @Test
    fun `returns numOfTicks based on possible steps if tickInterval is set but invalid`() {
        mockIntQuestion(0, 10, 1, tickInterval = 3)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)
        assertThat(state.numOfTicks, equalTo(11))
    }

    @Test
    fun `returns 0 for numOfTicks when no-ticks appearance is used`() {
        mockIntQuestion(0, 10, 1)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .withAppearance(Appearances.NO_TICKS)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)
        assertThat(state.numOfTicks, equalTo(0))
    }

    @Test
    fun `returns 0 for numOfTicks when range is invalid`() {
        mockIntQuestion(0, 10, 50)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)
        assertThat(state.numOfTicks, equalTo(0))
    }

    @Test
    fun `returns list of empty labels when no choices are available`() {
        mockIntQuestion(0, 5, 1)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)
        assertThat(state.labels, equalTo(listOf("", "", "", "", "", "")))
    }

    @Test
    fun `returns labels when choices are available`() {
        mockIntQuestion(0, 5, 1)

        val prompt = promptBuilder
            .withSelectChoices(
                listOf(
                    SelectChoice("0", "0"),
                    SelectChoice("1", "1"),
                    SelectChoice("2", "2"),
                    SelectChoice("3", "3"),
                    SelectChoice("4", "4"),
                    SelectChoice("5", "5")
                )
            )
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)
        assertThat(state.labels, equalTo(listOf("0", "1", "2", "3", "4", "5")))
    }

    @Test
    fun `returns correct value for valid placeholder`() {
        mockIntQuestion(0, 10, 1, placeholder = 4)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)
        assertThat(state.placeholder!!.stripTrailingZeros(), equalTo(0.4.toBigDecimal()))
    }

    @Test
    fun `returns null for invalid placeholder`() {
        mockIntQuestion(0, 10, 1, placeholder = 11)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt, selectChoiceLoader)
        assertThat(state.placeholder, equalTo(null))
    }

    @Test
    fun `realValue rounds correctly to nearest step for ascending range with integer step`() {
        val state = RangeSliderState(
            sliderValue = 0.34.toBigDecimal(),
            placeholder = null,
            rangeStart = 0.toBigDecimal(),
            rangeEnd = 10.toBigDecimal(),
            step = 1.toBigDecimal(),
            numOfSteps = 9,
            labels = emptyList(),
            isDiscrete = true,
            isHorizontal = true,
            isValid = true,
            isEnabled = true,
            numOfTicks = 11
        )

        assertThat(state.realValue, equalTo(3.toBigDecimal()))
    }

    @Test
    fun `realValue rounds correctly to nearest step for descending range with integer step`() {
        val state = RangeSliderState(
            sliderValue = 0.34.toBigDecimal(),
            placeholder = null,
            rangeStart = 10.toBigDecimal(),
            rangeEnd = 0.toBigDecimal(),
            step = 1.toBigDecimal(),
            numOfSteps = 9,
            labels = emptyList(),
            isDiscrete = true,
            isHorizontal = true,
            isValid = true,
            isEnabled = true,
            numOfTicks = 11
        )

        assertThat(state.realValue, equalTo(7.toBigDecimal()))
    }

    @Test
    fun `realValue rounds correctly to nearest step for ascending range with decimal step`() {
        val state = RangeSliderState(
            sliderValue = 0.14444445.toBigDecimal(),
            placeholder = null,
            rangeStart = 1.toBigDecimal(),
            rangeEnd = 10.toBigDecimal(),
            step = 0.1.toBigDecimal(),
            numOfSteps = 89,
            labels = emptyList(),
            isDiscrete = true,
            isHorizontal = true,
            isValid = true,
            isEnabled = true,
            numOfTicks = 11
        )

        assertThat(state.realValue, equalTo(2.3.toBigDecimal()))
    }

    @Test
    fun `realValue rounds correctly to nearest step for descending range with decimal step`() {
        val state = RangeSliderState(
            sliderValue = 0.14444445.toBigDecimal(),
            placeholder = null,
            rangeStart = 10.toBigDecimal(),
            rangeEnd = 1.toBigDecimal(),
            step = 0.1.toBigDecimal(),
            numOfSteps = 89,
            labels = emptyList(),
            isDiscrete = true,
            isHorizontal = true,
            isValid = true,
            isEnabled = true,
            numOfTicks = 11
        )

        assertThat(state.realValue, equalTo(8.7.toBigDecimal()))
    }

    private fun mockIntQuestion(rangeStart: Int, rangeEnd: Int, rangeStep: Int, tickInterval: Int? = null, placeholder: Int? = null) {
        whenever(question.rangeStart).thenReturn(rangeStart.toBigDecimal())
        whenever(question.rangeEnd).thenReturn(rangeEnd.toBigDecimal())
        whenever(question.rangeStep).thenReturn(rangeStep.toBigDecimal())
        whenever(question.tickInterval).thenReturn(tickInterval?.toBigDecimal())
        whenever(question.placeholder).thenReturn(placeholder?.toBigDecimal())
    }

    private fun mockDecimalQuestion(rangeStart: Float, rangeEnd: Float, rangeStep: Float, tickInterval: Float? = null, placeholder: Float? = null) {
        whenever(question.rangeStart).thenReturn(rangeStart.toBigDecimal())
        whenever(question.rangeEnd).thenReturn(rangeEnd.toBigDecimal())
        whenever(question.rangeStep).thenReturn(rangeStep.toBigDecimal())
        whenever(question.tickInterval).thenReturn(tickInterval?.toBigDecimal())
        whenever(question.placeholder).thenReturn(placeholder?.toBigDecimal())
    }
}
