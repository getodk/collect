package org.odk.collect.android.widgets.base;

import org.javarosa.core.model.data.IAnswerData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.widgets.BinaryWidget;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */
@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public abstract class BinaryWidgetTest<W extends BinaryWidget, A extends IAnswerData> extends WidgetTest<W, A> {

    @Mock
    public File instancePath;

    public BinaryWidgetTest(Class<W> clazz) {
        super(clazz);
    }

    public abstract Object createBinaryData(A answerData);

    @Before
    public void setUp() throws Exception {
        super.setUp();

        when(formController.getInstancePath()).thenReturn(instancePath);
        when(instancePath.getParent()).thenReturn("");
    }

    @Test
    public void getAnswerShouldReturnCorrectAnswerAfterBeingSet() {
        when(formEntryPrompt.getAnswerText()).thenReturn(null);

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