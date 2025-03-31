package org.odk.collect.android.widgets

import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.Gravity
import net.bytebuddy.utility.RandomString
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.Constants
import org.javarosa.core.model.data.StringData
import org.junit.Test
import org.mockito.kotlin.whenever
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.widgets.base.GeneralStringWidgetTest

class StringWidgetTest : GeneralStringWidgetTest<StringWidget, StringData>() {
    override fun createWidget(): StringWidget {
        whenever(formEntryPrompt.dataType).thenReturn(Constants.DATATYPE_TEXT)

        return StringWidget(
            activity,
            QuestionDetails(formEntryPrompt, readOnlyOverride),
            dependencies
        )
    }

    override fun getNextAnswer(): StringData {
        return StringData(RandomString.make())
    }

    @Test
    override fun verifyInputType() {
        assertThat(
            widget.widgetAnswerText.binding.editText.inputType,
            equalTo(
                InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE
            )
        )
        assertThat(
            widget.widgetAnswerText.binding.editText.transformationMethod,
            equalTo(null)
        )
    }

    @Test
    fun verifyInputTypeWithMaskedAppearance() {
        whenever(formEntryPrompt.appearanceHint).thenReturn(Appearances.MASKED)

        assertThat(
            widget.widgetAnswerText.binding.editText.inputType,
            equalTo(
                InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                    InputType.TYPE_TEXT_VARIATION_PASSWORD
            )
        )
        assertThat(
            widget.widgetAnswerText.binding.editText.transformationMethod.javaClass,
            equalTo(PasswordTransformationMethod::class.java)
        )
    }

    @Test
    fun answersShouldNotBeMaskedIfMaskedAppearanceIsNotUsed() {
        assertThat(
            spyWidget.widgetAnswerText.binding.editText.transformationMethod,
            equalTo(null)
        )
        assertThat(
            spyWidget.widgetAnswerText.binding.textView.transformationMethod,
            equalTo(null)
        )
    }

    @Test
    fun answersShouldBeMaskedIfMaskedAppearanceIsUsed() {
        whenever(formEntryPrompt.appearanceHint).thenReturn(Appearances.MASKED)

        assertThat(
            spyWidget.widgetAnswerText.binding.editText.transformationMethod.javaClass,
            equalTo(PasswordTransformationMethod::class.java)
        )
        assertThat(
            spyWidget.widgetAnswerText.binding.textView.transformationMethod.javaClass,
            equalTo(PasswordTransformationMethod::class.java)
        )
    }

    @Test
    fun textFieldShouldUseTopStartGravityNoMatterHowManyRowsItContains() {
        assertThat(widget.widgetAnswerText.binding.editText.gravity, equalTo(Gravity.TOP or Gravity.START))

        whenever(questionDef.getAdditionalAttribute(null, "rows")).thenReturn("5")
        assertThat(widget.widgetAnswerText.binding.editText.gravity, equalTo(Gravity.TOP or Gravity.START))

        whenever(questionDef.getAdditionalAttribute(null, "rows")).thenReturn(null)
        whenever(formEntryPrompt.appearanceHint).thenReturn(Appearances.MULTILINE)
        assertThat(widget.widgetAnswerText.binding.editText.gravity, equalTo(Gravity.TOP or Gravity.START))
    }

    @Test
    fun whenNumberOfRowsNotSpecifiedAndMultilineAppearanceNotUsesEditTextShouldHaveProperNumberOfLines() {
        assertThat(widget.widgetAnswerText.binding.editText.minLines, equalTo(0))
        assertThat(widget.widgetAnswerText.binding.editText.maxLines, equalTo(Int.MAX_VALUE))
    }

    @Test
    fun whenNumberOfRowsSpecifiedEditTextShouldHaveProperNumberOfLines() {
        whenever(questionDef.getAdditionalAttribute(null, "rows")).thenReturn("5")

        assertThat(widget.widgetAnswerText.binding.editText.minLines, equalTo(5))
        assertThat(widget.widgetAnswerText.binding.editText.maxLines, equalTo(Int.MAX_VALUE))
    }

    @Test
    fun whenMultilineAppearanceUsedEditTextShouldHaveProperNumberOfLines() {
        whenever(formEntryPrompt.appearanceHint).thenReturn(Appearances.MULTILINE)

        assertThat(widget.widgetAnswerText.binding.editText.minLines, equalTo(4))
        assertThat(widget.widgetAnswerText.binding.editText.maxLines, equalTo(4))
    }
}
