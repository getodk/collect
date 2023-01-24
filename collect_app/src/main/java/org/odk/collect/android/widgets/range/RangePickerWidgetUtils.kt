package org.odk.collect.android.widgets.range

import org.javarosa.form.api.FormEntryPrompt
import java.math.BigDecimal

object RangePickerWidgetUtils {

    @JvmStatic
    fun getNumbersFromRangeAsc(
        rangeStart: BigDecimal,
        rangeStep: BigDecimal,
        rangeEnd: BigDecimal,
        isIntegerDataType: Boolean
    ): Array<String> {
        val displayedValuesForNumberPicker = mutableListOf<String>()

        var index = 0

        var firstElement = if (rangeStart.compareTo(rangeEnd) < 1) rangeStart else rangeEnd
        val lastElement = if (rangeStart.compareTo(rangeEnd) < 1) rangeEnd else rangeStart

        while (firstElement.compareTo(lastElement) < 1) {
            displayedValuesForNumberPicker.add(
                if (isIntegerDataType) {
                    firstElement.toInt().toString()
                } else {
                    firstElement.toDouble().toString()
                }
            )
            index++
            firstElement = firstElement.plus(rangeStep.abs())
        }

        return displayedValuesForNumberPicker.toTypedArray()
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
