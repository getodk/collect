package org.odk.collect.android.widgets

import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.Gravity
import android.view.View
import net.bytebuddy.utility.RandomString
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.Constants
import org.javarosa.core.model.data.StringData
import org.junit.Test
import org.mockito.Mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.widgets.base.GeneralExStringWidgetTest
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers
import org.odk.collect.android.widgets.utilities.StringRequester

class ExStringWidgetTest : GeneralExStringWidgetTest<ExStringWidget, StringData>() {
    @Mock
    var stringRequester: StringRequester? = null

    override fun createWidget(): ExStringWidget {
        whenever(formEntryPrompt.dataType).thenReturn(Constants.DATATYPE_TEXT)

        return ExStringWidget(
            activity,
            QuestionDetails(formEntryPrompt),
            FakeWaitingForDataRegistry(),
            stringRequester,
            dependencies
        )
    }

    override fun getNextAnswer(): StringData {
        return StringData(RandomString.make())
    }

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        whenever(formEntryPrompt.appearanceHint).thenReturn("")
    }

    @Test
    override fun verifyInputType() {
        assertThat(
            widget.binding.widgetAnswerText.binding.editText.inputType,
            equalTo(
                InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE
            )
        )
        assertThat(
            widget.binding.widgetAnswerText.binding.editText.transformationMethod,
            equalTo(null)
        )
        assertThat(
            widget.binding.widgetAnswerText.binding.textView.transformationMethod,
            equalTo(null)
        )
    }

    @Test
    fun verifyInputTypeWithMaskedAppearance() {
        whenever(formEntryPrompt.appearanceHint).thenReturn(Appearances.MASKED)
        assertThat(
            widget.binding.widgetAnswerText.binding.editText.inputType,
            equalTo(
                InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                    InputType.TYPE_TEXT_VARIATION_PASSWORD
            )
        )
        assertThat(
            widget.binding.widgetAnswerText.binding.editText.transformationMethod.javaClass,
            equalTo(PasswordTransformationMethod::class.java)
        )
        assertThat(
            widget.binding.widgetAnswerText.binding.textView.transformationMethod.javaClass,
            equalTo(PasswordTransformationMethod::class.java)
        )
    }

    @Test
    fun answersShouldNotBeMaskedIfMaskedAppearanceIsNotUsed() {
        assertThat(
            spyWidget.binding.widgetAnswerText.binding.editText.transformationMethod,
            equalTo(null)
        )
        assertThat(
            spyWidget!!.binding.widgetAnswerText.binding.textView.transformationMethod,
            equalTo(null)
        )
    }

    @Test
    fun answersShouldBeMaskedIfMaskedAppearanceIsUsed() {
        whenever(formEntryPrompt.appearanceHint).thenReturn(Appearances.MASKED)

        assertThat(
            spyWidget.binding.widgetAnswerText.binding.editText.transformationMethod.javaClass,
            equalTo(PasswordTransformationMethod::class.java)
        )
        assertThat(
            spyWidget.binding.widgetAnswerText.binding.textView.transformationMethod.javaClass,
            equalTo(PasswordTransformationMethod::class.java)
        )
    }

    @Test
    fun textFieldShouldUseTopStartGravityNoMatterHowManyRowsItContains() {
        assertThat(
            widget.binding.widgetAnswerText.binding.editText.gravity,
            equalTo(Gravity.TOP or Gravity.START)
        )

        whenever(questionDef.getAdditionalAttribute(null, "rows")).thenReturn("5")
        assertThat(
            widget.binding.widgetAnswerText.binding.editText.gravity,
            equalTo(Gravity.TOP or Gravity.START)
        )

        whenever(questionDef.getAdditionalAttribute(null, "rows")).thenReturn(null)
        whenever(formEntryPrompt.appearanceHint).thenReturn(Appearances.MULTILINE)
        assertThat(
            widget.binding.widgetAnswerText.binding.editText.gravity,
            equalTo(Gravity.TOP or Gravity.START)
        )
    }

    @Test
    fun whenNumberOfRowsNotSpecifiedAndMultilineAppearanceNotUsesEditTextShouldHaveProperNumberOfLines() {
        assertThat(widget.binding.widgetAnswerText.binding.editText.minLines, equalTo(0))
        assertThat(widget.binding.widgetAnswerText.binding.editText.maxLines, equalTo(Int.MAX_VALUE))
    }

    @Test
    fun whenNumberOfRowsSpecifiedEditTextShouldHaveProperNumberOfLines() {
        whenever(questionDef.getAdditionalAttribute(null, "rows")).thenReturn("5")

        assertThat(widget.binding.widgetAnswerText.binding.editText.minLines, equalTo(5))
        assertThat(widget.binding.widgetAnswerText.binding.editText.maxLines, equalTo(Int.MAX_VALUE))
    }

    @Test
    fun whenMultilineAppearanceUsedEditTextShouldHaveProperNumberOfLines() {
        whenever(formEntryPrompt.appearanceHint).thenReturn(Appearances.MULTILINE)

        assertThat(widget.binding.widgetAnswerText.binding.editText.minLines, equalTo(4))
        assertThat(widget.binding.widgetAnswerText.binding.editText.maxLines, equalTo(4))
    }

    @Test
    override fun whenPromptHasHiddenAnswerAppearance_answerIsNotDisplayed() {
        val prompt =
            MockFormEntryPromptBuilder(QuestionWidgetHelpers.promptWithAppearance(Appearances.HIDDEN_ANSWER))
                .withAnswer(StringData("original contents"))
                .build()

        val widgetTestActivity = QuestionWidgetHelpers.widgetTestActivity()
        val widget = ExStringWidget(
            widgetTestActivity, QuestionDetails(prompt),
            FakeWaitingForDataRegistry(), stringRequester, dependencies
        )

        // Check initial value is not shown
        assertThat(
            widget.binding.widgetAnswerText.visibility,
            equalTo(View.GONE)
        )
        assertThat(widget.answer, equalTo(StringData("original contents")))

        // Check updates aren't shown
        widget.setData("updated contents")
        assertThat(
            widget.binding.widgetAnswerText.visibility,
            equalTo(View.GONE)
        )
        assertThat(widget.answer, equalTo(StringData("updated contents")))
    }
}
