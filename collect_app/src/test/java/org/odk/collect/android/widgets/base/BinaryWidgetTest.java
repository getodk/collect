package org.odk.collect.android.widgets.base;

import org.javarosa.core.model.data.IAnswerData;
import org.junit.Test;
import org.odk.collect.android.widgets.interfaces.BinaryWidget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */
public abstract class BinaryWidgetTest<W extends BinaryWidget, A extends IAnswerData>
        extends QuestionWidgetTest<W, A> {

    public abstract Object createBinaryData(A answerData);

    @Test
    public void getAnswerShouldReturnCorrectAnswerAfterBeingSet() {
        W widget = getWidget();
        assertNull(widget.getAnswer());

        A answer = getNextAnswer();
        Object binaryData = createBinaryData(answer);

        widget.setBinaryData(binaryData);

        IAnswerData answerData = widget.getAnswer();

        assertNotNull(answerData);
        assertEquals(answerData.getDisplayText(), answer.getDisplayText());
    }

    @Test
    public void settingANewAnswerShouldRemoveTheOldAnswer() {
        A answer = getInitialAnswer();
        when(formEntryPrompt.getAnswerText()).thenReturn(answer.getDisplayText());

        W widget = getWidget();

        A newAnswer = getNextAnswer();
        Object binaryData = createBinaryData(newAnswer);

        widget.setBinaryData(binaryData);

        IAnswerData answerData = widget.getAnswer();

        assertNotNull(answerData);
        assertEquals(answerData.getDisplayText(), newAnswer.getDisplayText());
    }
}