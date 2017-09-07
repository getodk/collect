package org.odk.collect.android.widgets;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormController;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.util.Random;
import java.util.UUID;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class AlignedImageWidgetTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private AlignedImageWidget alignedImageWidget;

    @Mock
    FormEntryPrompt formEntryPrompt;

    @Mock
    FormIndex formIndex;

    @Mock
    IFormElement formElement;

    @Mock
    FormController formController;

    @Mock
    File instancePath;

    @Before
    public void setUp() throws Exception {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("0");
        when(formEntryPrompt.isReadOnly()).thenReturn(false);
        when(formEntryPrompt.getIndex()).thenReturn(formIndex);
        when(formEntryPrompt.getFormElement()).thenReturn(formElement);

        Collect.getInstance().setFormController(formController);
        when(formController.getInstancePath()).thenReturn(instancePath);

        when(instancePath.getParent()).thenReturn("");
    }

    @Test
    public void getAnswerShouldReturnNullIfPromptDoesntHaveExistingAnswer() {
        when(formEntryPrompt.getAnswerText()).thenReturn(null);

        alignedImageWidget = new AlignedImageWidget(RuntimeEnvironment.application, formEntryPrompt);
        assertNull(alignedImageWidget.getAnswer());
    }

    @Test
    public void getAnswerShouldReturnExistingAnswerIfPromptHasExistingAnswer() {
        String answer = RandomString.make();
        when(formEntryPrompt.getAnswerText()).thenReturn(answer);

        alignedImageWidget = new AlignedImageWidget(RuntimeEnvironment.application, formEntryPrompt);

        IAnswerData answerData = alignedImageWidget.getAnswer();
        assertNotNull(answerData);

        assertThat(answerData, instanceOf(StringData.class));
        StringData stringData = (StringData) answerData;

        assertEquals(stringData.getValue(), answer);
    }

    @Test
    public void getAnswerShouldReturnCorrectAnswerAfterBeingSet() {
        when(formEntryPrompt.getAnswerText()).thenReturn(null);

        alignedImageWidget = new AlignedImageWidget(RuntimeEnvironment.application, formEntryPrompt);
        assertNull(alignedImageWidget.getAnswer());

        File newImage = mock(File.class);
        when(newImage.exists()).thenReturn(true);

        String answer = RandomString.make();
        when(newImage.getName()).thenReturn(answer);

        alignedImageWidget.setBinaryData(newImage);

        StringData answerData = (StringData) alignedImageWidget.getAnswer();
        assertEquals(answerData.getValue(), answer);
    }

    @Test
    public void settingANewAnswerShouldCallDeleteMediaToRemoveTheOldFile() {
        String answer = RandomString.make();
        when(formEntryPrompt.getAnswerText()).thenReturn(answer);

        alignedImageWidget = new AlignedImageWidget(RuntimeEnvironment.application, formEntryPrompt);
        AlignedImageWidget spy = spy(alignedImageWidget);

        File newImage = mock(File.class);
        when(newImage.exists()).thenReturn(true);

        String newAnswer = RandomString.make();
        when(newImage.getName()).thenReturn(newAnswer);

        spy.setBinaryData(newImage);
        verify(spy).deleteMedia();

        StringData answerData = (StringData) alignedImageWidget.getAnswer();
        assertEquals(answerData.getValue(), answer);
    }

    @Test
    public void callingClearAnswerShouldCallDeleteMediaAndRemoveTheExistingAnswer() {
        String answer = RandomString.make();
        when(formEntryPrompt.getAnswerText()).thenReturn(answer);

        alignedImageWidget = new AlignedImageWidget(RuntimeEnvironment.application, formEntryPrompt);
        AlignedImageWidget spy = spy(alignedImageWidget);
        spy.clearAnswer();

        verify(spy).deleteMedia();
        assertNull(spy.getAnswer());
    }
}