package org.odk.collect.android.widgets;

import android.support.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.odk.collect.android.BuildConfig;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public abstract class BinaryNameWidgetTest<W extends IBinaryNameWidget> extends WidgetTest<W, StringData> {

    @Mock
    File instancePath;

    BinaryNameWidgetTest(Class<W> clazz) {
        super(clazz);
    }

    abstract Object createBinaryData(StringData answerData);

    @NonNull
    @Override
    StringData getInitialAnswer() {
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
    public void settingANewAnswerShouldCallDeleteMediaToRemoveTheOldFile() {
        StringData answer = getInitialAnswer();
        when(formEntryPrompt.getAnswerText()).thenReturn(answer.getDisplayText());

        W widget = getWidget();

        StringData newAnswer = getNextAnswer();
        Object binaryData = createBinaryData(newAnswer);

        widget.setBinaryData(binaryData);
        verify(widget).deleteMedia();

        StringData answerData = (StringData) widget.getAnswer();
        assertEquals(answerData.getValue(), newAnswer.getValue());
    }

    @Test
    public void callingClearAnswerShouldCallDeleteMediaAndRemoveTheExistingAnswer() {
        StringData answer = getNextAnswer();
        when(formEntryPrompt.getAnswerText()).thenReturn(answer.getDisplayText());

        W widget = getWidget();
        widget.clearAnswer();

        verify(widget).deleteMedia();
        assertNull(widget.getAnswer());
    }
}