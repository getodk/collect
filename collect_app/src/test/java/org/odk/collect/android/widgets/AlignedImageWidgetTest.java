package org.odk.collect.android.widgets;

import android.support.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

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
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class AlignedImageWidgetTest extends BinaryNameWidgetTest<AlignedImageWidget, StringData> {

    @NonNull
    @Override
    StringData createAnswer() {
        return new StringData(RandomString.make());
    }

    @NonNull
    @Override
    public AlignedImageWidget createWidget() {
        return new AlignedImageWidget(RuntimeEnvironment.application, formEntryPrompt);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        when(formEntryPrompt.getAppearanceHint()).thenReturn("0");
        when(formEntryPrompt.isReadOnly()).thenReturn(false);
    }

    @Test
    public void getAnswerShouldReturnCorrectAnswerAfterBeingSet() {
        when(formEntryPrompt.getAnswerText()).thenReturn(null);

        widget = new AlignedImageWidget(RuntimeEnvironment.application, formEntryPrompt);
        assertNull(widget.getAnswer());

        File newImage = mock(File.class);
        when(newImage.exists()).thenReturn(true);

        answer = createAnswer();
        when(newImage.getName()).thenReturn(answer.getDisplayText());

        widget.setBinaryData(newImage);

        StringData answerData = (StringData) widget.getAnswer();
        assertEquals(answerData.getValue(), answer.getValue());
    }

    @Test
    public void settingANewAnswerShouldCallDeleteMediaToRemoveTheOldFile() {
        answer = createAnswer();
        when(formEntryPrompt.getAnswerText()).thenReturn(answer.getDisplayText());

        widget = createSpy();

        File newImage = mock(File.class);
        when(newImage.exists()).thenReturn(true);

        StringData newAnswer = createAnswer();
        when(newImage.getName()).thenReturn(newAnswer.getDisplayText());

        widget.setBinaryData(newImage);
        verify(widget).deleteMedia();

        StringData answerData = (StringData) widget.getAnswer();
        assertEquals(answerData.getValue(), newAnswer.getValue());
    }

    @Test
    public void callingClearAnswerShouldCallDeleteMediaAndRemoveTheExistingAnswer() {
        answer = createAnswer();
        when(formEntryPrompt.getAnswerText()).thenReturn(answer.getDisplayText());

        widget = createSpy();
        widget.clearAnswer();

        verify(widget).deleteMedia();
        assertNull(widget.getAnswer());
    }
}