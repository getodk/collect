package org.odk.collect.android.widgets;

import androidx.annotation.NonNull;

import org.javarosa.core.model.data.IntegerData;
import org.mockito.Mock;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.junit.Test;
import org.odk.collect.android.widgets.base.GeneralExStringWidgetTest;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.odk.collect.android.widgets.utilities.StringRequester;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.utilities.Appearances.MASKED;
import static org.odk.collect.android.utilities.Appearances.THOUSANDS_SEP;

import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;

/**
 * @author James Knight
 */

public class ExIntegerWidgetTest extends GeneralExStringWidgetTest<ExIntegerWidget, IntegerData> {

    @Mock
    StringRequester stringRequester;

    @NonNull
    @Override
    public ExIntegerWidget createWidget() {
        return new ExIntegerWidget(activity, new QuestionDetails(formEntryPrompt), new FakeWaitingForDataRegistry(), stringRequester);
    }

    @NonNull
    @Override
    public IntegerData getNextAnswer() {
        return new IntegerData(randomInteger());
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        when(formEntryPrompt.getAppearanceHint()).thenReturn("");
    }

    private int randomInteger() {
        return Math.abs(random.nextInt()) % 1_000_000_000;
    }

    @Test
    public void digitsAboveLimitOfNineShouldBeTruncatedFromRight() {
        getWidget().binding.widgetAnswerText.setAnswer("123456789123");
        assertEquals("123456789", getWidget().binding.widgetAnswerText.getAnswer());
    }

    @Test
    public void separatorsShouldBeAddedWhenEnabled() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn(THOUSANDS_SEP);
        getWidget().binding.widgetAnswerText.setAnswer("123456789");

        assertEquals("123,456,789", getWidget().binding.widgetAnswerText.getAnswer());
        assertEquals("123,456,789", getWidget().binding.widgetAnswerText.getBinding().editText.getText().toString());
        assertEquals("123,456,789", getWidget().binding.widgetAnswerText.getBinding().textView.getText().toString());
    }

    @Override
    @Test
    public void verifyInputType() {
        ExIntegerWidget widget = getWidget();
        assertThat(widget.binding.widgetAnswerText.getBinding().editText.getInputType(), equalTo(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED));
        assertThat(widget.binding.widgetAnswerText.getBinding().editText.getTransformationMethod().getClass(), equalTo(SingleLineTransformationMethod.class));
        assertThat(widget.binding.widgetAnswerText.getBinding().textView.getTransformationMethod(), equalTo(null));
    }

    @Override
    @Test
    public void verifyInputTypeWithMaskedAppearance() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn(MASKED);
        ExIntegerWidget widget = getWidget();
        assertThat(widget.binding.widgetAnswerText.getBinding().editText.getInputType(), equalTo(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED));
        assertThat(widget.binding.widgetAnswerText.getBinding().editText.getTransformationMethod().getClass(), equalTo(PasswordTransformationMethod.class));
        assertThat(widget.binding.widgetAnswerText.getBinding().textView.getTransformationMethod().getClass(), equalTo(PasswordTransformationMethod.class));
    }
}
