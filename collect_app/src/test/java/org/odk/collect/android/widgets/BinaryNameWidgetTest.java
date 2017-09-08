package org.odk.collect.android.widgets;

import android.support.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.BuildConfig;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public abstract class BinaryNameWidgetTest<W extends IBinaryNameWidget, A extends IAnswerData> extends WidgetTest<W, A> {

    @Mock
    File instancePath;

    @NonNull
    public abstract W createWidget();

    @Before
    public void setUp() throws Exception {
        super.setUp();

        when(formController.getInstancePath()).thenReturn(instancePath);
        when(instancePath.getParent()).thenReturn("");
    }

    @Test
    public void settingANewAnswerShouldCallDeleteMediaToRemoveTheOldFile() {
        String answer = RandomString.make();
        when(formEntryPrompt.getAnswerText()).thenReturn(answer);

        widget = createSpy();

        File newImage = mock(File.class);
        when(newImage.exists()).thenReturn(true);

        String newAnswer = RandomString.make();
        when(newImage.getName()).thenReturn(newAnswer);

        widget.setBinaryData(newImage);
        verify(widget).deleteMedia();

        StringData answerData = (StringData) widget.getAnswer();
        assertEquals(answerData.getValue(), answer);
    }

    @Test
    public void callingClearAnswerShouldCallDeleteMediaAndRemoveTheExistingAnswer() {
        String answer = RandomString.make();
        when(formEntryPrompt.getAnswerText()).thenReturn(answer);

        widget = createSpy();
        widget.clearAnswer();

        verify(widget).deleteMedia();
        assertNull(widget.getAnswer());
    }
}