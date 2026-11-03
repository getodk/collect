package org.odk.collect.android.widgets.base

import android.view.View
import android.widget.TextView
import net.bytebuddy.utility.RandomString
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.SelectChoice
import org.javarosa.core.model.data.IAnswerData
import org.junit.Test
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.widgets.QuestionWidget
import org.odk.collect.android.widgets.interfaces.Widget
import kotlin.math.abs

abstract class SelectWidgetTest<W : Widget, A : IAnswerData> : QuestionWidgetTest<W, A>() {

    lateinit var selectChoices: List<SelectChoice>
        private set

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        selectChoices = createSelectChoices()

        whenever(formEntryPrompt.selectChoices).thenReturn(selectChoices)
    }

    @Test
    fun `a warning is displayed when there are choices without a value`() {
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withSelectChoices(
                listOf(
                    SelectChoice("A", "a"),
                    SelectChoice("B", "")
                )
            )
            .build()

        val warning = (widget as QuestionWidget).findViewById<TextView>(R.id.warning_text)
        assertThat(warning.visibility, equalTo(View.VISIBLE))
        assertThat(warning.text.toString(), equalTo("1 choice has no value and is not shown."))
    }

    private fun createSelectChoices(): List<SelectChoice> {
        val choiceCount = (abs(random.nextInt()) % 3) + 2

        return (0 until choiceCount).map { i ->
            SelectChoice(i.toString(), RandomString.make()).also { it.index = i }
        }
    }
}
