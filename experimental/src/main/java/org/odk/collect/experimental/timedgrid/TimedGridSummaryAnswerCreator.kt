package org.odk.collect.experimental.timedgrid

import org.javarosa.core.model.FormIndex
import org.javarosa.core.model.GroupDef
import org.javarosa.core.model.IFormElement
import org.javarosa.core.model.QuestionDef
import org.javarosa.core.model.data.BooleanData
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.core.model.data.IntegerData
import org.javarosa.core.model.data.StringData
import org.javarosa.core.model.instance.TreeReference
import org.javarosa.form.api.FormEntryPrompt

class TimedGridSummaryAnswerCreator(
    private val formEntryPrompt: FormEntryPrompt,
    private val formControllerFacade: FormControllerFacade,
    private val formAnswerRefresher: FormAnswerRefresher
) {
    companion object {
        val SUMMARY_QUESTION_APPEARANCE_REGEX = Regex("""timed-grid-answer\((.+),(.+)\)""")
    }

    fun answerSummaryQuestions(summary: TimedGridSummary) {
        val timedGridQuestionId = formEntryPrompt.index.reference.toString(false)

        forEachFormQuestionDef(formControllerFacade.getFormElements()) { questionIndex, questionDef ->
            val summaryQuestionMatch =
                SUMMARY_QUESTION_APPEARANCE_REGEX.find(questionDef.appearanceAttr ?: "")

            if (summaryQuestionMatch != null) {
                val referencedQuestion = summaryQuestionMatch.groupValues[1].trim()
                val metadataName = summaryQuestionMatch.groupValues[2].trim()

                if (referencedQuestion == timedGridQuestionId) {
                    val answer = getSummaryAnswer(metadataName, summary)
                    formControllerFacade.saveAnswer(questionIndex, answer)
                    formAnswerRefresher.refreshAnswer(questionIndex)
                }
            }
        }
    }

    private fun forEachFormQuestionDef(
        iFormElements: Iterable<IFormElement>?,
        currentFormIndex: FormIndex? = null,
        action: (formIndex: FormIndex, IFormElement) -> Unit
    ) {
        iFormElements?.forEachIndexed { index, formElement ->
            val nextLevelIndex = FormIndex(index, formElement.bind.reference as TreeReference)
            if (formElement is GroupDef) {
                forEachFormQuestionDef(formElement.children, nextLevelIndex, action)
            } else if (formElement is QuestionDef) {
                action(FormIndex(nextLevelIndex, currentFormIndex), formElement)
            }
        }
    }

    private fun getSummaryAnswer(metadataName: String, summary: TimedGridSummary): IAnswerData {
        return when (metadataName) {
            "time-remaining" -> IntegerData(summary.secondsRemaining)
            "attempted-count" -> IntegerData(summary.attemptedCount)
            "incorrect-count" -> IntegerData(summary.incorrectCount)
            "correct-count" -> IntegerData(summary.correctCount)
            "first-line-all-incorrect" -> BooleanData(summary.firstLineAllIncorrect)
            "sentences-passed" -> IntegerData(summary.sentencesPassed)
            "correct-items" -> StringData(summary.correctItems)
            "unanswered-items" -> StringData(summary.unansweredItems)
            "punctuation-count" -> IntegerData(summary.punctuationCount)
            else -> throw IllegalArgumentException("Unknown metadata name: $metadataName")
        }
    }
}

interface FormControllerFacade {
    fun getFormElements(): List<IFormElement>?
    fun saveAnswer(index: FormIndex, answer: IAnswerData)
}

interface FormAnswerRefresher {
    fun refreshAnswer(index: FormIndex)
}
