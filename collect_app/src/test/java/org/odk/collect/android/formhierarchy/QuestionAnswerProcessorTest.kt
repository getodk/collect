package org.odk.collect.android.formhierarchy

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.Constants
import org.javarosa.core.model.QuestionDef
import org.javarosa.form.api.FormEntryPrompt
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.utilities.Appearances

// TODO: Add tests for other question/data types
class QuestionAnswerProcessorTest {

    @Test
    fun noAnswerShouldBeDisplayedForThePrinterWidget() {
        val question = mock<QuestionDef>()
        val prompt = MockFormEntryPromptBuilder()
            .withQuestion(question)
            .withAnswerDisplayText("<html>blah</html>>")
            .withAppearance(Appearances.PRINTER)
            .build()

        val answer = QuestionAnswerProcessor.getQuestionAnswer(prompt, mock(), mock())

        assertThat(answer, equalTo(""))
    }

    @Test
    fun noAnswerShouldBeDisplayedIfItDoesNotExistAndMaskedAppearanceIsUsedForTextAndNumberDataTypes() {
        listOf(
            Constants.DATATYPE_TEXT,
            Constants.DATATYPE_INTEGER,
            Constants.DATATYPE_DECIMAL
        ).forEach {
            val prompt = mock<FormEntryPrompt>()
            val question = mock<QuestionDef>()
            whenever(prompt.dataType).thenReturn(it)
            whenever(prompt.controlType).thenReturn(Constants.CONTROL_INPUT)
            whenever(prompt.question).thenReturn(question)
            whenever(prompt.appearanceHint).thenReturn(Appearances.MASKED)

            val answer = QuestionAnswerProcessor.getQuestionAnswer(prompt, mock(), mock())

            assertThat(answer, equalTo(""))
        }
    }

    @Test
    fun noAnswerShouldBeDisplayedIfItIsEmptyAndMaskedAppearanceIsUsedForTextAndNumberDataTypes() {
        listOf(
            Constants.DATATYPE_TEXT,
            Constants.DATATYPE_INTEGER,
            Constants.DATATYPE_DECIMAL
        ).forEach {
            val question = mock<QuestionDef>()
            val prompt = MockFormEntryPromptBuilder()
                .withQuestion(question)
                .withAnswerDisplayText("")
                .withAppearance(Appearances.MASKED)
                .withControlType(Constants.CONTROL_INPUT)
                .withDataType(it)
                .build()

            val answer = QuestionAnswerProcessor.getQuestionAnswer(prompt, mock(), mock())

            assertThat(answer, equalTo(""))
        }
    }

    @Test
    fun maskedAnswerShouldBeDisplayedIfItExistAndMaskedAppearanceIsUsedForTextAndNumberDataTypes() {
        listOf(
            Constants.DATATYPE_TEXT,
            Constants.DATATYPE_INTEGER,
            Constants.DATATYPE_DECIMAL
        ).forEach {
            val question = mock<QuestionDef>()
            val prompt = MockFormEntryPromptBuilder()
                .withQuestion(question)
                .withAnswerDisplayText("blah")
                .withAppearance(Appearances.MASKED)
                .withControlType(Constants.CONTROL_INPUT)
                .withDataType(it)
                .build()

            val answer = QuestionAnswerProcessor.getQuestionAnswer(prompt, mock(), mock())

            assertThat(answer, equalTo("••••••••••"))
        }
    }

    @Test
    fun originalAnswerShouldBeDisplayedIfItExistAndMaskedAppearanceIsUsedForDataTypesOtherThanTextAndNumber() {
        listOf(
            Constants.DATATYPE_DATE_TIME,
            Constants.DATATYPE_DATE,
            Constants.DATATYPE_TIME,
            Constants.DATATYPE_GEOPOINT,
            Constants.DATATYPE_GEOSHAPE,
            Constants.DATATYPE_GEOSHAPE,
            Constants.DATATYPE_GEOTRACE,
            Constants.DATATYPE_BARCODE,
            Constants.DATATYPE_BARCODE
        ).forEach {
            val question = mock<QuestionDef>()
            val prompt = MockFormEntryPromptBuilder()
                .withQuestion(question)
                .withAnswerDisplayText("blah")
                .withAppearance(Appearances.MASKED)
                .withControlType(Constants.CONTROL_INPUT)
                .withDataType(it)
                .build()

            val answer = QuestionAnswerProcessor.getQuestionAnswer(prompt, mock(), mock())

            assertThat(answer, equalTo("blah"))
        }
    }

    @Test
    fun originalAnswerShouldBeDisplayedIfItExistAndMaskedAppearanceIsUsedButControlTypeIsOtherThanInput() {
        listOf(
            Constants.CONTROL_RANGE,
            Constants.CONTROL_RANK,
            Constants.CONTROL_TRIGGER,
            Constants.CONTROL_SELECT_MULTI,
            Constants.CONTROL_SELECT_ONE,
            Constants.CONTROL_VIDEO_CAPTURE,
            Constants.CONTROL_AUDIO_CAPTURE,
            Constants.CONTROL_OSM_CAPTURE,
            Constants.CONTROL_IMAGE_CHOOSE,
            Constants.CONTROL_FILE_CAPTURE
        ).forEach {
            val question = mock<QuestionDef>()
            val prompt = MockFormEntryPromptBuilder()
                .withQuestion(question)
                .withAnswerDisplayText("blah")
                .withAppearance(Appearances.MASKED)
                .withControlType(it)
                .withDataType(Constants.DATATYPE_TEXT)
                .build()

            val answer = QuestionAnswerProcessor.getQuestionAnswer(prompt, mock(), mock())

            assertThat(answer, equalTo("blah"))
        }
    }
}
