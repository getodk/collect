package org.odk.collect.android.formhierarchy

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.QuestionDef
import org.javarosa.form.api.FormEntryPrompt
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.utilities.Appearances

// TODO: Add tests for other question/data types
class QuestionAnswerProcessorTest {

    @Test
    fun noAnswerShouldBeDisplayedForThePrinterWidget() {
        val prompt = mock<FormEntryPrompt>()
        val question = mock<QuestionDef>()
        whenever(prompt.question).thenReturn(question)
        whenever(question.appearanceAttr).thenReturn(Appearances.PRINTER)

        val answer = QuestionAnswerProcessor.getQuestionAnswer(prompt, mock(), mock())

        assertThat(answer, equalTo(""))
    }
}
