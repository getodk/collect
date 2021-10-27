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
import static org.mockito.Mockito.when;
import static org.odk.collect.android.utilities.Appearances.THOUSANDS_SEP;

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
        getWidget().answerText.setText("123456789123");
        assertEquals("123456789", getWidget().getAnswerText());
    }

    @Test
    public void separatorsShouldBeAddedWhenEnabled() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn(THOUSANDS_SEP);
        getWidget().answerText.setText("123456789");
        assertEquals("123,456,789", getWidget().answerText.getText().toString());
    }
}
