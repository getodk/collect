package org.odk.collect.android.widgets.range

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.Constants.DATATYPE_DECIMAL
import org.javarosa.core.model.Constants.DATATYPE_INTEGER
import org.javarosa.core.model.RangeQuestion
import org.javarosa.core.model.data.IntegerData
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.utilities.Appearances

class RangeSliderStateTest {
    private val question = mock<RangeQuestion>()
    private val promptBuilder = MockFormEntryPromptBuilder().withQuestion(question)

    @Test
    fun `sets sliderValue to null when answer is out of range`() {
        mockQuestion(0F, 10F, 1F)

        val prompt = promptBuilder.withAnswer(IntegerData(15)).build()
        val state = RangeSliderState.fromPrompt(prompt)

        assertThat(state.sliderValue, equalTo(null))
    }

    @Test
    fun `sets sliderValue to null when range is invalid`() {
        mockQuestion(0F, 10F, 15F)

        val prompt = promptBuilder.withAnswer(IntegerData(5)).build()
        val state = RangeSliderState.fromPrompt(prompt)

        assertThat(state.sliderValue, equalTo(null))
    }

    @Test
    fun `maps real value correctly for ascending range`() {
        mockQuestion(0F, 10F, 1F)

        val prompt = promptBuilder
            .withAnswer(IntegerData(7))
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)

        assertThat(state.sliderValue, equalTo(0.7f))
        assertThat(state.realValue, equalTo(7f))
    }

    @Test
    fun `maps real value correctly for descending range`() {
        mockQuestion(10F, 0F, 1F)

        val prompt = promptBuilder
            .withAnswer(IntegerData(7))
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)

        assertThat(state.sliderValue, equalTo(0.3f))
        assertThat(state.realValue, equalTo(7f))
    }

    @Test
    fun `returns null for realValue when sliderValue is null`() {
        mockQuestion(0F, 10F, 1F)

        val prompt = promptBuilder
            .withAnswer(null)
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)

        assertThat(state.realValue, equalTo(null))
    }

    @Test
    fun `sets ranges correctly for ascending range`() {
        mockQuestion(0F, 10F, 1F)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)

        assertThat(state.rangeStart, equalTo(0f))
        assertThat(state.startLabel, equalTo("0"))

        assertThat(state.rangeEnd, equalTo(10f))
        assertThat(state.endLabel, equalTo("10"))
    }

    @Test
    fun `sets ranges correctly for descending range`() {
        mockQuestion(10F, 0F, 1F)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)

        assertThat(state.rangeStart, equalTo(10f))
        assertThat(state.startLabel, equalTo("10"))

        assertThat(state.rangeEnd, equalTo(0f))
        assertThat(state.endLabel, equalTo("0"))
    }

    @Test
    fun `formats ranges correctly for decimal data`() {
        mockQuestion(0.5F, 10.5F, 0.5F)

        val prompt = promptBuilder
            .withDataType(DATATYPE_DECIMAL)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)

        assertThat(state.startLabel, equalTo("0.5"))
        assertThat(state.endLabel, equalTo("10.5"))
    }

    @Test
    fun `calculates numOfSteps correctly for integer data`() {
        mockQuestion(0F, 10F, 1F)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)
        assertThat(state.numOfSteps, equalTo(9))

        whenever(question.rangeStep).thenReturn(2.toBigDecimal())

        val newState = RangeSliderState.fromPrompt(prompt)
        assertThat(newState.numOfSteps, equalTo(4))
    }

    @Test
    fun `calculates numOfSteps correctly for decimal data`() {
        mockQuestion(0F, 1F, 0.2F)

        val prompt = promptBuilder
            .withDataType(DATATYPE_DECIMAL)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)

        assertThat(state.numOfSteps, equalTo(4))
    }

    @Test
    fun `treats negative step as positive`() {
        mockQuestion(0F, 10F, -1F)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)

        assertThat(state.isValid, equalTo(true))
        assertThat(state.numOfSteps, equalTo(9))
    }

    @Test
    fun `returns empty valueLabel when realValue is null`() {
        mockQuestion(0F, 10F, 1F)

        val prompt = promptBuilder
            .withAnswer(null)
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)

        assertThat(state.valueLabel, equalTo(""))
    }

    @Test
    fun `formats valueLabel as integer for discrete data`() {
        mockQuestion(0F, 10F, 1F)

        val prompt = promptBuilder
            .withAnswer(IntegerData(5))
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)

        assertThat(state.valueLabel, equalTo("5"))
    }

    @Test
    fun `formats valueLabel as decimal for non-discrete data`() {
        mockQuestion(0F, 10F, 1F)

        val prompt = promptBuilder
            .withAnswer(IntegerData(5))
            .withDataType(DATATYPE_DECIMAL)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)

        assertThat(state.valueLabel, equalTo("5.0"))
    }

    @Test
    fun `returns true for isDiscrete when dataType is integer`() {
        mockQuestion(0F, 10F, 1F)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)
        assertThat(state.isDiscrete, equalTo(true))
    }

    @Test
    fun `returns false for isDiscrete when dataType is decimal`() {
        mockQuestion(0F, 10F, 1F)

        val prompt = promptBuilder
            .withDataType(DATATYPE_DECIMAL)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)
        assertThat(state.isDiscrete, equalTo(false))
    }

    @Test
    fun `returns true for isEnabled when prompt is not readOnly`() {
        mockQuestion(0F, 10F, 1F)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)

        assertThat(state.isEnabled, equalTo(true))
    }

    @Test
    fun `returns false for isEnabled when prompt is readOnly`() {
        mockQuestion(0F, 10F, 1F)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .withReadOnly(true)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)

        assertThat(state.isEnabled, equalTo(false))
    }

    @Test
    fun `returns false for isEnabled when range is invalid`() {
        mockQuestion(0F, 10F, 3F)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .withReadOnly(false)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)

        assertThat(state.isEnabled, equalTo(false))
    }

    @Test
    fun `returns true for isValid when range is valid`() {
        mockQuestion(0F, 10F, 1F)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)
        assertThat(state.isValid, equalTo(true))
    }

    @Test
    fun `returns false for isValid when rangeStart equals rangeEnd`() {
        mockQuestion(0F, 0F, 1F)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)
        assertThat(state.isValid, equalTo(false))
    }

    @Test
    fun `returns false for isValid when step does not evenly divide range`() {
        mockQuestion(0F, 10F, 3F)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)
        assertThat(state.isValid, equalTo(false))
    }

    @Test
    fun `returns false for isValid when step is 0`() {
        mockQuestion(0F, 10F, 0F)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)
        assertThat(state.isValid, equalTo(false))
    }

    @Test
    fun `returns false for isValid when step is greater than range`() {
        mockQuestion(0F, 10F, 100000000F)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)
        assertThat(state.isValid, equalTo(false))
    }

    @Test
    fun `returns true for isHorizontal when vertical appearance is not used`() {
        mockQuestion(0F, 10F, 1F)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)
        assertThat(state.isHorizontal, equalTo(true))
    }

    @Test
    fun `returns false for isHorizontal when vertical appearance is used`() {
        mockQuestion(0F, 10F, 1F)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .withAppearance(Appearances.VERTICAL)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)
        assertThat(state.isHorizontal, equalTo(false))
    }

    @Test
    fun `calculates numOfTicks correctly`() {
        mockQuestion(0F, 10F, 1F)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)
        assertThat(state.numOfTicks, equalTo(11))

        whenever(question.rangeStep).thenReturn(2.toBigDecimal())

        val newState = RangeSliderState.fromPrompt(prompt)
        assertThat(newState.numOfTicks, equalTo(6))
    }

    @Test
    fun `returns 0 for numOfTicks when no-ticks appearance is used`() {
        mockQuestion(0F, 10F, 1F)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .withAppearance(Appearances.NO_TICKS)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)
        assertThat(state.numOfTicks, equalTo(0))
    }

    @Test
    fun `returns 0 for numOfTicks when range is invalid`() {
        mockQuestion(0F, 10F, 50F)

        val prompt = promptBuilder
            .withDataType(DATATYPE_INTEGER)
            .build()

        val state = RangeSliderState.fromPrompt(prompt)
        assertThat(state.numOfTicks, equalTo(0))
    }

    private fun mockQuestion(rangeStart: Float, rangeEnd: Float, rangeStep: Float) {
        whenever(question.rangeStart).thenReturn(rangeStart.toBigDecimal())
        whenever(question.rangeEnd).thenReturn(rangeEnd.toBigDecimal())
        whenever(question.rangeStep).thenReturn(rangeStep.toBigDecimal())
    }
}
