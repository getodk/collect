package org.odk.collect.android.widgets.items

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.javarosa.core.model.data.SelectOneData
import org.javarosa.core.model.data.helper.Selection
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.support.WidgetTestActivity
import org.odk.collect.android.widgets.items.SelectOneFromMapDialogFragment.Companion.ARG_FORM_INDEX
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer
import org.odk.collect.testshared.RobolectricHelpers.getFragmentByClass
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class SelectOneFromMapWidgetTest {

    private val activity = Robolectric.setupActivity(WidgetTestActivity::class.java)

    @Test
    fun `clicking button opens SelectOneFromMapDialog`() {
        val prompt = promptWithAnswer(null)
        val widget = SelectOneFromMapWidget(activity, QuestionDetails(prompt))

        widget.binding.button.performClick()

        val expectedDialog = getFragmentByClass(
            activity.supportFragmentManager,
            SelectOneFromMapDialogFragment::class.java
        )
        assertThat(expectedDialog, notNullValue())
        assertThat(
            expectedDialog?.arguments?.getSerializable(ARG_FORM_INDEX),
            equalTo(prompt.index)
        )
    }

    @Test
    fun `prompt answer is returned from getAnswer`() {
        val answer = SelectOneData(Selection(101))
        val widget = SelectOneFromMapWidget(activity, QuestionDetails(promptWithAnswer(answer)))
        assertThat(widget.answer, equalTo(answer))
    }

    @Test
    fun `clearAnswer removes answer`() {
        val answer = SelectOneData(Selection(101))
        val widget = SelectOneFromMapWidget(activity, QuestionDetails(promptWithAnswer(answer)))
        widget.clearAnswer()
        assertThat(widget.answer, equalTo(null))
    }

    @Test
    fun `setData sets answer`() {
        val widget = SelectOneFromMapWidget(activity, QuestionDetails(promptWithAnswer(null)))

        val answer = SelectOneData(Selection(101))
        widget.setData(answer)
        assertThat(widget.answer, equalTo(answer))
    }
}
