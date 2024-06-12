package org.odk.collect.android.formhierarchy

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.Constants
import org.javarosa.core.model.QuestionDef
import org.junit.Test
import org.mockito.Mockito.mock
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
    fun noAnswerShouldBeDisplayedIfItDoesNotExistAndMaskedAppearanceIsUsedForTextDataTypes() {
        val question = mock<QuestionDef>()
        val prompt = MockFormEntryPromptBuilder()
            .withQuestion(question)
            .withDataType(Constants.DATATYPE_TEXT)
            .withControlType(Constants.CONTROL_INPUT)
            .withAppearance(Appearances.MASKED)
            .build()

        val answer = QuestionAnswerProcessor.getQuestionAnswer(prompt, mock(), mock())

        assertThat(answer, equalTo(""))
    }

    @Test
    fun noAnswerShouldBeDisplayedIfItIsEmptyAndMaskedAppearanceIsUsedForTextDataTypes() {
        val question = mock<QuestionDef>()
        val prompt = MockFormEntryPromptBuilder()
            .withQuestion(question)
            .withAnswerDisplayText("")
            .withAppearance(Appearances.MASKED)
            .withControlType(Constants.CONTROL_INPUT)
            .withDataType(Constants.DATATYPE_TEXT)
            .build()

        val answer = QuestionAnswerProcessor.getQuestionAnswer(prompt, mock(), mock())

        assertThat(answer, equalTo(""))
    }

    @Test
    fun maskedAnswerShouldBeDisplayedIfItExistAndMaskedAppearanceIsUsedForTextDataTypes() {
        val question = mock<QuestionDef>()
        val prompt = MockFormEntryPromptBuilder()
            .withQuestion(question)
            .withAnswerDisplayText("blah")
            .withAppearance(Appearances.MASKED)
            .withControlType(Constants.CONTROL_INPUT)
            .withDataType(Constants.DATATYPE_TEXT)
            .build()

        val answer = QuestionAnswerProcessor.getQuestionAnswer(prompt, mock(), mock())

        assertThat(answer, equalTo("••••••••••"))
    }

    @Test
    fun originalAnswerShouldBeDisplayedIfItExistAndMaskedAppearanceIsUsedForDataTypesOtherThanText() {
        listOf(
            Constants.DATATYPE_INTEGER,
            Constants.DATATYPE_DECIMAL,
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
    fun originalAnswerShouldBeDisplayedIfItExistAndMaskedAppearanceIsUsedForDataTypesAndControlTypeOtherThanInput() {
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
