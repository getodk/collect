package org.odk.collect.android.widgets.base;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.widgets.StringWidget;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */
public abstract class GeneralStringWidgetTest<W extends StringWidget, A extends IAnswerData>
        extends QuestionWidgetTest<W, A> {

    @Mock
    QuestionDef questionDef;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        when(formEntryPrompt.getQuestion()).thenReturn(questionDef);
    }

    @Override
    public void callingClearShouldRemoveTheExistingAnswer() {
        super.callingClearShouldRemoveTheExistingAnswer();

        W widget = getWidget();
        assertEquals(widget.getAnswerText(), "");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void getAnswerShouldReturnExistingAnswerIfPromptHasExistingAnswer() {
        super.getAnswerShouldReturnExistingAnswerIfPromptHasExistingAnswer();

        W widget = getWidget();
        IAnswerData answer = widget.getAnswer();

        assertEquals(widget.getAnswerText(), answer.getDisplayText());
    }


    @Test
    public void getAnswerShouldReturnNewAnswerWhenTextFieldIsUpdated() {
        // Make sure it starts null:
        super.getAnswerShouldReturnNullIfPromptDoesNotHaveExistingAnswer();

        W widget = getWidget();
        IAnswerData answer = getNextAnswer();

        widget.getAnswerTextField().setText(answer.getDisplayText());

        IAnswerData computedAnswer = widget.getAnswer();

        assertEquals(answer.getDisplayText(), computedAnswer.getDisplayText());
    }
}