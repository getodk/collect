package org.odk.collect.android.widgets.range

import org.javarosa.core.model.Constants.DATATYPE_INTEGER
import org.javarosa.core.model.RangeQuestion
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.utilities.Appearances
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.math.roundToInt

data class RangeSliderState(
    val sliderValue: Float?,
    val rangeStart: Float,
    val rangeEnd: Float,
    val step: Float,
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
            roundToStep(raw, rangeStart, rangeEnd, step)
        }

    val valueLabel
        get() = realValue?.let {
            if (isDiscrete) it.toInt().toString() else it.toString()
        }.orEmpty()

    val startLabel
        get() = if (isDiscrete) rangeStart.toInt().toString() else rangeStart.toString()

    val endLabel
        get() = if (isDiscrete) rangeEnd.toInt().toString() else rangeEnd.toString()

    private fun roundToStep(
        value: Float,
        rangeStart: Float,
        rangeEnd: Float,
        step: Float
    ): Float {
        val steps = ((value - rangeStart) / step).roundToInt()
        val snapped = rangeStart + steps * step
        return snapped.coerceIn(
            min(rangeStart, rangeEnd),
            max(rangeStart, rangeEnd)
        )
    }

    companion object {
        fun fromPrompt(prompt: FormEntryPrompt): RangeSliderState {
            val rangeQuestion = prompt.question as RangeQuestion
            val start = rangeQuestion.rangeStart.toFloat()
            val end = rangeQuestion.rangeEnd.toFloat()
            val step = rangeQuestion.rangeStep.toFloat().absoluteValue
            val sanitizedAppearance = Appearances.getSanitizedAppearanceHint(prompt)
            val isHorizontal = !sanitizedAppearance.contains(Appearances.VERTICAL)
            val isDiscrete = prompt.dataType == DATATYPE_INTEGER
            val isValid = step != 0f &&
                start != end &&
                abs(end - start) >= abs(step) &&
                isDivisible(end - start, step)
            val isEnabled = !prompt.isReadOnly && isValid

            var sliderValue: Float? = null
            var numOfSteps = 0
            var numOfTicks = 0

            if (isValid) {
                sliderValue = toSliderValue(prompt.answerValue?.value?.toString()?.toFloatOrNull(), start, end)
                numOfSteps = ((end - start).absoluteValue / step).toInt().coerceAtLeast(1) - 1
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

        private fun toSliderValue(value: Float?, start: Float, end: Float): Float? {
            if (value == null || start == end) return null

            val sliderValue = (value - start) / (end - start)
            return if (sliderValue in 0f..1f) sliderValue else null
        }

        private fun isDivisible(totalRange: Float, step: Float): Boolean {
            val quotient = totalRange / step
            val nearestInteger = round(quotient)
            val epsilon = 1e-6f
            return abs(quotient - nearestInteger) < epsilon
        }
    }
}
