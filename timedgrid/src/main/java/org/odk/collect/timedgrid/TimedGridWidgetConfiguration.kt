package org.odk.collect.timedgrid

import androidx.core.text.isDigitsOnly
import org.javarosa.form.api.FormEntryPrompt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class TimedGridWidgetConfiguration(
    /** The type of assessment being conducted (e.g., LETTERS, WORDS). */
    val type: AssessmentType,
    /** The number of columns to display in the grid. */
    val columns: Int,
    /** The number of rows to display per page in the grid. */
    val rowsPerPage: Int,
    /** The total duration of the timed assessment. */
    val duration: Duration,
    /** The number of consecutive incorrect answers after which the assessment should end early. */
    val endAfterConsecutive: Int,
    /** Enable strict mode that disables selection after time expires and enforces early-end behavior. */
    val strict: Boolean,
    /** Allows to pause timer during assessment. */
    val allowPause: Boolean,
    /** A special value stored when all answers are marked correctly. */
    val allAnsweredCorrectly: String,
    /** Determines how the task finishes */
    val finish: FinishType
) {
    class Builder {
        private var type: AssessmentType = AssessmentType.LETTERS
        private var columns: Int = 10
        private var rowsPerPage: Int = 5
        private var duration: Duration = 120.seconds
        private var endAfterConsecutive: Int = 5
        private var strict: Boolean = false
        private var allowPause: Boolean = true
        private var allAnsweredCorrectly: String = "999"
        private var finish: FinishType = FinishType.CONFIRM_AND_PICK

        fun type(type: AssessmentType) = apply { this.type = type }
        fun columns(columns: Int) = apply { this.columns = columns }
        fun rowsPerPage(rowsPerPage: Int) = apply { this.rowsPerPage = rowsPerPage }
        fun duration(duration: Duration) = apply { this.duration = duration }
        fun endAfterConsecutive(endAfterConsecutive: Int) = apply { this.endAfterConsecutive = endAfterConsecutive }
        fun strict(strict: Boolean) = apply { this.strict = strict }
        fun allowPause(allowPause: Boolean) = apply { this.allowPause = allowPause }
        fun allAnsweredCorrectly(allAnsweredCorrectly: String) = apply { this.allAnsweredCorrectly = allAnsweredCorrectly }
        fun finish(finish: FinishType) = apply { this.finish = finish }

        fun build(): TimedGridWidgetConfiguration {
            return TimedGridWidgetConfiguration(
                type,
                columns,
                rowsPerPage,
                duration,
                endAfterConsecutive,
                strict,
                allowPause,
                allAnsweredCorrectly,
                finish
            )
        }
    }

    companion object {
        private object Keys {
            const val TYPE = "type"
            const val COLUMNS = "columns"
            const val PAGE_ROWS = "page-rows"
            const val DURATION = "duration"
            const val END_AFTER = "end-after"
            const val STRICT = "strict"
            const val PAUSE = "pause"
            const val ALL_ANSWERED = "all-answered"
            const val FINISH = "finish"
        }

        fun fromPrompt(prompt: FormEntryPrompt): TimedGridWidgetConfiguration {
            val params = extractParams(prompt.appearanceHint)

            val detectedType: AssessmentType
            var columnsOverride: Int? = null

            val typeParam = params[Keys.TYPE]
            if (typeParam != null) {
                if (typeParam.isDigitsOnly()) {
                    detectedType = AssessmentType.LETTERS
                    columnsOverride = typeParam.toInt()
                } else {
                    detectedType = try {
                        AssessmentType.valueOf(typeParam.uppercase())
                    } catch (e: IllegalArgumentException) {
                        AssessmentType.LETTERS
                    }
                }
            } else {
                detectedType = AssessmentType.LETTERS
            }

            val configBuilder = Builder().apply {
                type(detectedType)
                when (detectedType) {
                    AssessmentType.LETTERS -> {
                        columns(10)
                        rowsPerPage(5)
                        duration(120.seconds)
                        endAfterConsecutive(5)
                        strict(false)
                        allowPause(false)
                        allAnsweredCorrectly("999")
                        finish(FinishType.CONFIRM_AND_PICK)
                    }
                    AssessmentType.WORDS -> {
                        columns(5)
                        rowsPerPage(5)
                        duration(120.seconds)
                        endAfterConsecutive(5)
                        strict(false)
                        allowPause(false)
                        allAnsweredCorrectly("999")
                        finish(FinishType.CONFIRM_AND_PICK)
                    }
                    AssessmentType.READING -> {
                        columns(1)
                        rowsPerPage(1)
                        duration(120.seconds)
                        endAfterConsecutive(5)
                        strict(false)
                        allowPause(false)
                        allAnsweredCorrectly("999")
                        finish(FinishType.CONFIRM_AND_PICK)
                    }
                    AssessmentType.NUMBERS -> {
                        columns(5)
                        rowsPerPage(5)
                        duration(120.seconds)
                        endAfterConsecutive(5)
                        strict(false)
                        allowPause(false)
                        allAnsweredCorrectly("999")
                        finish(FinishType.CONFIRM_AND_PICK)
                    }
                    AssessmentType.ARITHMETIC -> {
                        columns(2)
                        rowsPerPage(5)
                        duration(120.seconds)
                        endAfterConsecutive(5)
                        strict(false)
                        allowPause(false)
                        allAnsweredCorrectly("999")
                        finish(FinishType.CONFIRM_AND_PICK)
                    }
                }
            }

            columnsOverride?.let { configBuilder.columns(it) }

            if (columnsOverride == null) {
                params[Keys.COLUMNS]?.let { configBuilder.columns(it.toInt()) }
            }
            params[Keys.FINISH]?.let {
                val finishValue = it.toIntOrNull()
                if (finishValue != null) {
                    configBuilder.finish(FinishType.fromInt(finishValue))
                }
            }
            params[Keys.PAGE_ROWS]?.let { configBuilder.rowsPerPage(it.toInt()) }
            params[Keys.DURATION]?.let { configBuilder.duration(it.toInt().seconds) }
            params[Keys.END_AFTER]?.let { configBuilder.endAfterConsecutive(it.toInt()) }
            params[Keys.STRICT]?.let { configBuilder.strict(it.toBoolean()) }
            params[Keys.PAUSE]?.let {
                val normalized = it.trim().lowercase()
                val allowPause = normalized == "true" || normalized == "1"
                configBuilder.allowPause(allowPause)
            }
            params[Keys.ALL_ANSWERED]?.let { configBuilder.allAnsweredCorrectly(it) }

            return configBuilder.build()
        }
    }
}

private fun extractParams(appearance: String): Map<String, String> {
    val params = mutableMapOf<String, String>()
    // Allow for optional whitespace around '=' and support hyphenated keys
    val regex = Regex("([\\w-]+)\\s*=\\s*([^,)]+)")
    val matches = regex.findAll(appearance)

    for (matchResult in matches) {
        val (key, value) = matchResult.destructured
        params[key.trim()] = value.trim()
    }
    return params
}
