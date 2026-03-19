package org.odk.collect.android.widgets.range

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.Constants
import org.javarosa.core.model.DataType
import org.javarosa.core.model.RangeQuestion
import org.javarosa.core.model.data.IntegerData
import org.javarosa.form.api.FormEntryPrompt
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import java.math.BigDecimal

class RangePickerWidgetUtilsTest {

    @Test
    fun `list of numbers should contain only rangeStart when range has equal start and end`() {
        val rangeQuestion = RangeQuestion()
        rangeQuestion.rangeStart = BigDecimal(5.0)
        rangeQuestion.rangeEnd = BigDecimal(5.0)
        rangeQuestion.rangeStep = BigDecimal(1.0)
        val prompt = MockFormEntryPromptBuilder()
            .withQuestion(rangeQuestion)
            .withDataType(Constants.DATATYPE_DECIMAL)
            .build()

        assertThat(
            RangePickerWidgetUtils.getNumbersFromRangeAsc(prompt),
            equalTo(arrayOf("5.0"))
        )
    }

    @Test
    fun `list of numbers should contain only rangeStart when step is bigger than range`() {
        val rangeQuestion = RangeQuestion()
        rangeQuestion.rangeStart = BigDecimal(-5)
        rangeQuestion.rangeEnd = BigDecimal(5)
        rangeQuestion.rangeStep = BigDecimal(100)
        val prompt = MockFormEntryPromptBuilder()
            .withQuestion(rangeQuestion)
            .withDataType(Constants.DATATYPE_INTEGER)
            .build()

        assertThat(
            RangePickerWidgetUtils.getNumbersFromRangeAsc(prompt),
            equalTo(arrayOf("-5"))
        )
    }

    @Test
    fun `list of numbers should contain numbers in ascending order when range is increasing`() {
        val rangeQuestion = RangeQuestion()
        rangeQuestion.rangeStart = BigDecimal(-5.0)
        rangeQuestion.rangeEnd = BigDecimal(5.0)
        rangeQuestion.rangeStep = BigDecimal(1.5)
        val prompt = MockFormEntryPromptBuilder()
            .withQuestion(rangeQuestion)
            .withDataType(Constants.DATATYPE_DECIMAL)
            .build()

        assertThat(
            RangePickerWidgetUtils.getNumbersFromRangeAsc(prompt),
            equalTo(arrayOf("-5.0", "-3.5", "-2.0", "-0.5", "1.0", "2.5", "4.0"))
        )
    }

    @Test
    fun `list of numbers should contain numbers in ascending order when range is decreasing`() {
        val rangeQuestion = RangeQuestion()
        rangeQuestion.rangeStart = BigDecimal(5)
        rangeQuestion.rangeEnd = BigDecimal(-5)
        rangeQuestion.rangeStep = BigDecimal(1)
        val prompt = MockFormEntryPromptBuilder()
            .withQuestion(rangeQuestion)
            .withDataType(Constants.DATATYPE_INTEGER)
            .build()

        assertThat(
            RangePickerWidgetUtils.getNumbersFromRangeAsc(prompt),
            equalTo(arrayOf("-5", "-4", "-3", "-2", "-1", "0", "1", "2", "3", "4", "5"))
        )
    }

    @Test
    fun `list of numbers should contain numbers in ascending order when range is decreasing and step is a negative number`() {
        val rangeQuestion = RangeQuestion()
        rangeQuestion.rangeStart = BigDecimal(5.0)
        rangeQuestion.rangeEnd = BigDecimal(-5.0)
        rangeQuestion.rangeStep = BigDecimal(-1.5)
        val prompt = MockFormEntryPromptBuilder()
            .withQuestion(rangeQuestion)
            .withDataType(Constants.DATATYPE_DECIMAL)
            .build()

        assertThat(
            RangePickerWidgetUtils.getNumbersFromRangeAsc(prompt),
            equalTo(arrayOf("-5.0", "-3.5", "-2.0", "-0.5", "1.0", "2.5", "4.0"))
        )
    }

    @Test
    fun `list of numbers should contain numbers in ascending order when step is bigger than 1`() {
        val rangeQuestion = RangeQuestion()
        rangeQuestion.rangeStart = BigDecimal(-5)
        rangeQuestion.rangeEnd = BigDecimal(5)
        rangeQuestion.rangeStep = BigDecimal(2)
        val prompt = MockFormEntryPromptBuilder()
            .withQuestion(rangeQuestion)
            .withDataType(Constants.DATATYPE_INTEGER)
            .build()

        assertThat(
            RangePickerWidgetUtils.getNumbersFromRangeAsc(prompt),
            equalTo(arrayOf("-5", "-3", "-1", "1", "3", "5"))
        )
    }

    @Test
    fun `getProgressFromPrompt() returns the position of the value in the list`() {
        val prompt = mock<FormEntryPrompt>().apply {
            whenever(this.answerValue).thenReturn(IntegerData(2))
        }

        val listOfValuesAsc = arrayOf("1", "2", "3", "4", "5")

        assertThat(
            RangePickerWidgetUtils.getProgressFromPrompt(prompt, listOfValuesAsc),
            equalTo(1)
        )
    }

    @Test
    fun `getProgressFromPrompt() returns 0 if the current value does not exist in the list of values`() {
        val prompt = mock<FormEntryPrompt>().apply {
            whenever(this.answerValue).thenReturn(IntegerData(10))
        }

        val listOfValuesAsc = arrayOf("1", "2", "3", "4", "5")

        assertThat(
            RangePickerWidgetUtils.getProgressFromPrompt(prompt, listOfValuesAsc),
            equalTo(0)
        )
    }

    @Test
    fun `getProgressFromPrompt() returns 0 if the current value is null`() {
        val prompt = mock<FormEntryPrompt>().apply {
            whenever(this.answerValue).thenReturn(null)
        }

        val listOfValuesAsc = arrayOf("1", "2", "3", "4", "5")

        assertThat(
            RangePickerWidgetUtils.getProgressFromPrompt(prompt, listOfValuesAsc),
            equalTo(0)
        )
    }
}
