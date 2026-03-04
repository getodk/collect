package org.odk.collect.android.widgets.range

import org.javarosa.core.model.Constants.DATATYPE_INTEGER
import org.javarosa.core.model.RangeQuestion
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.utilities.Appearances
import java.math.BigDecimal
import java.math.RoundingMode

data class RangeSliderState(
    val sliderValue: BigDecimal?,
    val rangeStart: BigDecimal,
    val rangeEnd: BigDecimal,
    val step: BigDecimal,
    val numOfSteps: Int,
    val isDiscrete: Boolean,
    val isHorizontal: Boolean,
    val isValid: Boolean,
    val isEnabled: Boolean,
    val numOfTicks: Int
) {
    val realValue
        get() = sliderValue?.let {
            val raw = rangeStart + it * (rangeEnd - rangeStart)
            roundToStep(raw, step)
        }

    val valueLabel
        get() = realValue?.let {
            if (isDiscrete) it.toInt().toString() else it.toString()
        }.orEmpty()

    val startLabel
        get() = if (isDiscrete) rangeStart.toInt().toString() else rangeStart.toString()

    val endLabel
        get() = if (isDiscrete) rangeEnd.toInt().toString() else rangeEnd.toString()

    private fun roundToStep(value: BigDecimal, step: BigDecimal): BigDecimal {
        return value
            .divide(step, 0, RoundingMode.HALF_UP)
            .multiply(step)
    }

    companion object {
        fun fromPrompt(prompt: FormEntryPrompt): RangeSliderState {
            val rangeQuestion = prompt.question as RangeQuestion
            val start = rangeQuestion.rangeStart
            val end = rangeQuestion.rangeEnd
            val step = rangeQuestion.rangeStep.abs()
            val sanitizedAppearance = Appearances.getSanitizedAppearanceHint(prompt)
            val isHorizontal = !sanitizedAppearance.contains(Appearances.VERTICAL)
            val isDiscrete = prompt.dataType == DATATYPE_INTEGER
            val isValid = step.compareTo(BigDecimal.ZERO) != 0 &&
                start.compareTo(end) != 0 &&
                (end - start).abs() >= step &&
                (end - start).remainder(step).compareTo(BigDecimal.ZERO) == 0
            val isEnabled = !prompt.isReadOnly && isValid

            var sliderValue: BigDecimal? = null
            var numOfSteps = 0
            var numOfTicks = 0

            if (isValid) {
                val value = prompt.answerValue?.value?.toString()?.toBigDecimalOrNull()
                sliderValue = toSliderValue(value, start, end, step)
                numOfSteps = ((end - start).abs().divide(step, 0, RoundingMode.HALF_UP).toInt()) - 1
                numOfTicks = if (sanitizedAppearance.contains(Appearances.NO_TICKS)) {
                    0
                } else {
                    numOfSteps + 2
                }
            }

            return RangeSliderState(
                sliderValue = sliderValue,
                rangeStart = start,
                rangeEnd = end,
                step = step,
                numOfSteps = numOfSteps,
                isDiscrete = isDiscrete,
                isHorizontal = isHorizontal,
                isValid = isValid,
                isEnabled = isEnabled,
                numOfTicks = numOfTicks
            )
        }

        private fun toSliderValue(
            value: BigDecimal?,
            start: BigDecimal,
            end: BigDecimal,
            step: BigDecimal
        ): BigDecimal? {
            if (value == null || start.compareTo(end) == 0) return null

            var normalized = (value - start).divide(end - start, 10, RoundingMode.HALF_UP)
            val stepFraction = step.divide(end - start, 10, RoundingMode.HALF_UP)
            normalized = normalized
                .divide(stepFraction, 0, RoundingMode.HALF_UP)
                .multiply(stepFraction)
                .stripTrailingZeros()

            return normalized.takeIf { it >= BigDecimal.ZERO && it <= BigDecimal.ONE }
        }
    }
}
