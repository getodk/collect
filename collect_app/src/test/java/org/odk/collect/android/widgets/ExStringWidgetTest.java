package org.odk.collect.android.widgets;

import androidx.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.base.GeneralExStringWidgetTest;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.odk.collect.android.widgets.utilities.StringRequester;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.utilities.Appearances.MASKED;

import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;

/**
 * @author James Knight
 */

public class ExStringWidgetTest extends GeneralExStringWidgetTest<ExStringWidget, StringData> {

    @Mock
    StringRequester stringRequester;

    @NonNull
    @Override
    public ExStringWidget createWidget() {
        return new ExStringWidget(activity, new QuestionDetails(formEntryPrompt), new FakeWaitingForDataRegistry(), stringRequester);
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(RandomString.make());
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        when(formEntryPrompt.getAppearanceHint()).thenReturn("");
    }

    @Override
    @Test
    public void verifyInputType() {
        ExStringWidget widget = getWidget();
        assertThat(widget.binding.widgetAnswerText.getBinding().editText.getInputType(), equalTo(InputType.TYPE_CLASS_TEXT));
        assertThat(widget.binding.widgetAnswerText.getBinding().editText.getTransformationMethod().getClass(), equalTo(SingleLineTransformationMethod.class));
        assertThat(widget.binding.widgetAnswerText.getBinding().textView.getTransformationMethod(), equalTo(null));
    }

    @Override
    @Test
    public void verifyInputTypeWithMaskedAppearance() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn(MASKED);
        ExStringWidget widget = getWidget();
        assertThat(widget.binding.widgetAnswerText.getBinding().editText.getInputType(), equalTo(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
        assertThat(widget.binding.widgetAnswerText.getBinding().editText.getTransformationMethod().getClass(), equalTo(PasswordTransformationMethod.class));
        assertThat(widget.binding.widgetAnswerText.getBinding().textView.getTransformationMethod().getClass(), equalTo(PasswordTransformationMethod.class));
    }
}
