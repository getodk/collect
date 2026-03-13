package org.odk.collect.android.widgets.range

import org.javarosa.core.model.Constants.DATATYPE_INTEGER
import org.javarosa.core.model.RangeQuestion
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.utilities.Appearances
import java.math.BigDecimal
import java.math.RoundingMode

data class RangeSliderState(
    val sliderValue: BigDecimal?,
    val placeholder: BigDecimal?,
    val rangeStart: BigDecimal,
    val rangeEnd: BigDecimal,
    val step: BigDecimal,
    val numOfSteps: Int,
    val labels: List<String>,
    val isDiscrete: Boolean,
    val isHorizontal: Boolean,
    val isValid: Boolean,
    val isEnabled: Boolean,
    val numOfTicks: Int
) {
    val realValue
        get() = sliderValue?.let {
            val raw = rangeStart + it * (rangeEnd - rangeStart)
            roundToStep(raw, rangeStart, step)
        }

    val valueLabel
        get() = realValue?.let {
            if (isDiscrete) it.toInt().toString() else it.toString()
        }.orEmpty()

    val startLabel
        get() = if (isDiscrete) rangeStart.toInt().toString() else rangeStart.toString()

    val endLabel
        get() = if (isDiscrete) rangeEnd.toInt().toString() else rangeEnd.toString()

    companion object {
        fun fromPrompt(prompt: FormEntryPrompt): RangeSliderState {
            val rangeQuestion = prompt.question as RangeQuestion
            val start = rangeQuestion.rangeStart
            val end = rangeQuestion.rangeEnd
            val range = (end - start).abs()
            val step = rangeQuestion.rangeStep.abs()
            val tickInterval = rangeQuestion.tickInterval
            val placeholder = rangeQuestion.placeholder?.takeIf { it in start.min(end)..start.max(end) }
            val labels = getLabels(prompt)
            val sanitizedAppearance = Appearances.getSanitizedAppearanceHint(prompt)
            val isHorizontal = !sanitizedAppearance.contains(Appearances.VERTICAL)
            val isDiscrete = prompt.dataType == DATATYPE_INTEGER
            val isValid = step.compareTo(BigDecimal.ZERO) != 0 &&
                start.compareTo(end) != 0 &&
                range >= step &&
                (end - start).remainder(step).compareTo(BigDecimal.ZERO) == 0
            val isEnabled = !prompt.isReadOnly && isValid

            var sliderValue: BigDecimal? = null
            var numOfSteps = 0
            var numOfTicks = 0

            if (isValid) {
                val value = prompt.answerValue?.value?.toString()?.toBigDecimalOrNull()
                sliderValue = toSliderValue(value, start, end, step, range)
                numOfSteps = (range.divide(step, 0, RoundingMode.HALF_UP).toInt()) - 1
                numOfTicks = calculateNumOfTicks(sanitizedAppearance, range, tickInterval, step, numOfSteps)
            }

            return RangeSliderState(
                sliderValue = sliderValue,
                placeholder = placeholder,
                rangeStart = start,
                rangeEnd = end,
                step = step,
                numOfSteps = numOfSteps,
                labels = labels,
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
            step: BigDecimal,
            range: BigDecimal
        ): BigDecimal? {
            if (value == null ||
                start.compareTo(end) == 0 ||
                value !in start.min(end)..start.max(end)
            ) {
                return null
            }

            val nearestStepValue = roundToStep(value, start, step)
            val stepWithinRange = (nearestStepValue - start).abs()
            val fractionOfRange = stepWithinRange.divide(range, 10, RoundingMode.HALF_UP)
            return fractionOfRange.takeIf { it >= BigDecimal.ZERO && it <= BigDecimal.ONE }
        }

        private fun roundToStep(value: BigDecimal, start: BigDecimal, step: BigDecimal): BigDecimal {
            val steps = (value - start).divide(step, 0, RoundingMode.HALF_UP)
            return start + steps.multiply(step)
        }

        private fun getLabels(prompt: FormEntryPrompt): List<String> {
            return prompt.selectChoices
                ?.map { prompt.getSelectChoiceText(it) }
                ?: emptyList()
        }

        private fun calculateNumOfTicks(
            appearance: String,
            range: BigDecimal,
            tickInterval: BigDecimal?,
            step: BigDecimal,
            numOfSteps: Int
        ): Int {
            if (appearance.contains(Appearances.NO_TICKS)) {
                return 0
            }

            val isTickIntervalValid = tickInterval != null &&
                tickInterval > BigDecimal.ZERO &&
                tickInterval.remainder(step).compareTo(BigDecimal.ZERO) == 0 &&
                tickInterval <= range &&
                range.remainder(tickInterval).compareTo(BigDecimal.ZERO) == 0

            return if (isTickIntervalValid) {
                range.divide(tickInterval, 0, RoundingMode.UNNECESSARY).toInt() + 1
            } else {
                numOfSteps + 2
            }
        }
    }
}
