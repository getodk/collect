package org.odk.collect.android.widgets.range

import org.javarosa.core.model.Constants.DATATYPE_INTEGER
import org.javarosa.core.model.RangeQuestion
import org.javarosa.form.api.FormEntryPrompt
import java.math.BigDecimal

object RangePickerWidgetUtils {

    @JvmStatic
    fun getNumbersFromRangeAsc(formEntryPrompt: FormEntryPrompt): Array<String> {
        val rangeQuestion = formEntryPrompt.question as RangeQuestion
        val rangeStart = rangeQuestion.rangeStart
        val rangeEnd = rangeQuestion.rangeEnd
        val rangeStep = rangeQuestion.rangeStep.abs()

        val displayedValuesForNumberPicker = mutableListOf<String>()
        var index = 0
        var firstElement = if (rangeStart.compareTo(rangeEnd) < 1) rangeStart else rangeEnd
        val lastElement = if (rangeStart.compareTo(rangeEnd) < 1) rangeEnd else rangeStart
        while (firstElement.compareTo(lastElement) < 1) {
            displayedValuesForNumberPicker.add(
                if (formEntryPrompt.dataType == DATATYPE_INTEGER) {
                    firstElement.toInt().toString()
                } else {
                    firstElement.toDouble().toString()
                }
            )
            index++
            firstElement = firstElement.plus(rangeStep.abs())
        }
        return displayedValuesForNumberPicker.toTypedArray<String>()
    }

    @JvmStatic
    fun getProgressFromPrompt(prompt: FormEntryPrompt, listOfValues: Array<String>): Int {
        var actualValue: BigDecimal? = null
        val answerValue = prompt.answerValue
        if (answerValue != null) {
            actualValue = BigDecimal(answerValue.value.toString())
        }
        var progress = 0
        if (actualValue != null) {
            progress = listOfValues.indexOf(actualValue.toString())
        }
        return if (progress == -1) 0 else progress
    }
}
