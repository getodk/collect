package org.odk.collect.android.widgets.range

import org.javarosa.core.model.Constants.DATATYPE_INTEGER
import org.javarosa.core.model.RangeQuestion
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.widgets.interfaces.SelectChoiceLoader
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
        private const val FRACTION_SCALE = 10

        fun fromPrompt(prompt: FormEntryPrompt, selectChoiceLoader: SelectChoiceLoader): RangeSliderState {
            val rangeQuestion = prompt.question as RangeQuestion
            val start = rangeQuestion.rangeStart
            val end = rangeQuestion.rangeEnd
            val range = (end - start).abs()
            val step = rangeQuestion.rangeStep.abs()
            val tickInterval = rangeQuestion.tickInterval
            val sanitizedAppearance = Appearances.getSanitizedAppearanceHint(prompt)
            val isHorizontal = !sanitizedAppearance.contains(Appearances.VERTICAL)
            val isDiscrete = prompt.dataType == DATATYPE_INTEGER

            val isValid = step.compareTo(BigDecimal.ZERO) != 0 &&
                start.compareTo(end) != 0 &&
                range >= step &&
                range.remainder(step).compareTo(BigDecimal.ZERO) == 0

            val labels = if (isValid) {
                getLabels(prompt, selectChoiceLoader, start, end, step)
            } else {
                emptyList()
            }

            val isEnabled = !prompt.isReadOnly && isValid

            val sliderValue = if (isValid) {
                val value = prompt.answerValue?.value?.toString()?.toBigDecimalOrNull()
                toSliderValue(value, start, end, step, range)
            } else {
                null
            }

            val placeholder = if (isValid) {
                val value = rangeQuestion.placeholder?.takeIf { it in start.min(end)..start.max(end) }
                toSliderValue(value, start, end, step, range)
            } else {
                null
            }

            val numOfSteps = if (isValid) {
                range.divide(step, 0, RoundingMode.HALF_UP).toInt() - 1
            } else {
                0
            }

            val numOfTicks = if (isValid) {
                calculateNumOfTicks(sanitizedAppearance, range, tickInterval, step, numOfSteps)
            } else {
                0
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
            val fractionOfRange = stepWithinRange.divide(range, FRACTION_SCALE, RoundingMode.HALF_UP)
            return fractionOfRange.takeIf { it >= BigDecimal.ZERO && it <= BigDecimal.ONE }
        }

        private fun roundToStep(value: BigDecimal, start: BigDecimal, step: BigDecimal): BigDecimal {
            val steps = (value - start).divide(step, 0, RoundingMode.HALF_UP)
            return start + steps.multiply(step)
        }

        private fun getLabels(prompt: FormEntryPrompt, selectChoiceLoader: SelectChoiceLoader, start: BigDecimal, end: BigDecimal, step: BigDecimal): List<String> {
            val choices = selectChoiceLoader.loadSelectChoices(prompt)

            val labelMap = choices.associate { choice ->
                choice.value.toBigDecimalOrNull() to prompt.getSelectChoiceText(choice)
            }

            val labels = mutableListOf<String>()
            var current = start
            val direction = if (end >= start) BigDecimal.ONE else -BigDecimal.ONE

            while (if (direction > BigDecimal.ZERO) current <= end else current >= end) {
                labels.add(labelMap[current] ?: "")
                current += step * direction
            }

            return labels
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
