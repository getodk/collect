package org.odk.collect.android.widgets.base;

import android.support.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.widgets.IBinaryWidget;
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
public abstract class BinaryWidgetTest<W extends IBinaryWidget> extends WidgetTest<W, StringData> {

    @Mock
    public File instancePath;

    public BinaryWidgetTest(Class<W> clazz) {
        super(clazz);
    }

    public abstract Object createBinaryData(StringData answerData);

    @NonNull
    @Override
    public StringData getInitialAnswer() {
        return new StringData(RandomString.make());
    }

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

        StringData answer = getNextAnswer();
        Object binaryData = createBinaryData(answer);

        widget.setBinaryData(binaryData);

        StringData answerData = (StringData) widget.getAnswer();
        assertNotNull(answerData);
        assertEquals(answerData.getValue(), answer.getValue());
    }

    @Test
    public void settingANewAnswerShouldRemoveTheOldAnswer() {
        StringData answer = getInitialAnswer();
        when(formEntryPrompt.getAnswerText()).thenReturn(answer.getDisplayText());

        W widget = getWidget();

        StringData newAnswer = getNextAnswer();
        Object binaryData = createBinaryData(newAnswer);

        widget.setBinaryData(binaryData);

        StringData answerData = (StringData) widget.getAnswer();
        assertNotNull(answerData);
        assertEquals(answerData.getValue(), newAnswer.getValue());
    }
}