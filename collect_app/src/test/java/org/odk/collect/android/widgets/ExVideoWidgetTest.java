package org.odk.collect.android.widgets;

import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;

import org.javarosa.core.model.data.StringData;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.exception.ExternalParamsException;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.ExternalAppIntentProvider;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.widgets.base.FileWidgetTest;
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.robolectric.shadows.ShadowToast;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_FONT_SIZE;
import static org.odk.collect.android.utilities.QuestionFontSizeUtils.DEFAULT_FONT_SIZE;
import static org.robolectric.Shadows.shadowOf;

public class ExVideoWidgetTest extends FileWidgetTest<ExVideoWidget> {
    @Mock
    MediaUtils mediaUtils;

    @Mock
    ExternalAppIntentProvider externalAppIntentProvider;

    @Before
    public void setup() {
        when(mediaUtils.isVideoFile(any())).thenReturn(true);
    }

    @Override
    public StringData getInitialAnswer() {
        return new StringData("video1.mp4");
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData("video2.mp4");
    }

    @NonNull
    @Override
    public ExVideoWidget createWidget() {
        return new ExVideoWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID", readOnlyOverride),
                new FakeQuestionMediaManager(), new FakeWaitingForDataRegistry(), mediaUtils, externalAppIntentProvider, new ActivityAvailability(activity));
    }

    @Test
    public void whenWidgetCreated_shouldButtonsBeVisible() {
        assertThat(getWidget().binding.captureVideoButton.getVisibility(), is(View.VISIBLE));
        assertThat(getWidget().binding.playVideoButton.getVisibility(), is(View.VISIBLE));
    }

    @Test
    public void whenWidgetCreated_shouldButtonsHaveProperNames() {
        assertThat(getWidget().binding.captureVideoButton.getText(), is("Launch"));
        assertThat(getWidget().binding.playVideoButton.getText(), is("Play Video"));
    }

    @Test
    public void whenFontSizeNotChanged_defaultFontSizeShouldBeUsed() {
        assertThat((int) getWidget().binding.captureVideoButton.getTextSize(), is(DEFAULT_FONT_SIZE - 1));
        assertThat((int) getWidget().binding.playVideoButton.getTextSize(), is(DEFAULT_FONT_SIZE - 1));
    }

    @Test
    public void whenFontSizeChanged_CustomFontSizeShouldBeUsed() {
        settingsProvider.getGeneralSettings().save(KEY_FONT_SIZE, "30");

        assertThat((int) getWidget().binding.captureVideoButton.getTextSize(), is(29));
        assertThat((int) getWidget().binding.playVideoButton.getTextSize(), is(29));
    }

    @Test
    public void whenThereIsNoAnswer_shouldOnlyLaunchButtonBeEnabled() {
        assertThat(getWidget().binding.captureVideoButton.isEnabled(), is(true));
        assertThat(getWidget().binding.playVideoButton.isEnabled(), is(false));
    }

    @Test
    public void whenThereIsAnswer_shouldBothButtonsBeEnabled() {
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        assertThat(getWidget().binding.captureVideoButton.isEnabled(), is(true));
        assertThat(getWidget().binding.playVideoButton.isEnabled(), is(true));
    }

    @Test
    public void whenClearAnswerCall_shouldPlayButtonBecomeDisabled() {
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ExVideoWidget widget = getWidget();
        widget.clearAnswer();
        assertThat(widget.binding.captureVideoButton.isEnabled(), is(true));
        assertThat(widget.binding.playVideoButton.isEnabled(), is(false));
    }

    @Test
    public void whenClickingOnChooseButton_externalAppShouldBeLaunchedByIntent() throws ExternalParamsException, XPathSyntaxException {
        Intent intent = mock(Intent.class);
        when(externalAppIntentProvider.getIntentToRunExternalApp(any(), any(), any(), any())).thenReturn(intent);
        getWidget().binding.captureVideoButton.performClick();
        assertThat(shadowOf(activity).getNextStartedActivity(), is(intent));
    }

    @Test
    public void whenClickingOnPlayButton_shouldFileViewerByCalled() {
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ExVideoWidget widget = getWidget();
        widget.binding.playVideoButton.performClick();
        verify(mediaUtils).openFile(activity, widget.answerFile, "video/*");
    }

    @Test
    public void whenSetDataCalledWithNull_shouldExistedAnswerBeRemoved() {
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ExVideoWidget widget = getWidget();
        widget.setData(null);
        assertThat(widget.getAnswer(), is(nullValue()));
        assertThat(widget.binding.playVideoButton.isEnabled(), is(false));
    }

    @Test
    public void whenUnsupportedFileTypeAttached_shouldNotThatFileBeAdded() throws IOException {
        ExVideoWidget widget = getWidget();
        File answer = File.createTempFile("doc", ".pdf");
        when(mediaUtils.isVideoFile(answer)).thenReturn(false);
        widget.setData(answer);
        assertThat(widget.getAnswer(), is(nullValue()));
        assertThat(widget.binding.playVideoButton.isEnabled(), is(false));
    }

    @Test
    public void whenUnsupportedFileTypeAttached_shouldTheFileBeRemoved() throws IOException {
        ExVideoWidget widget = getWidget();
        File answer = File.createTempFile("doc", ".pdf");
        when(mediaUtils.isVideoFile(answer)).thenReturn(false);
        widget.setData(answer);
        verify(mediaUtils).deleteMediaFile(answer.getAbsolutePath());
    }

    @Test
    public void whenUnsupportedFileTypeAttached_shouldToastBeDisplayed() throws IOException {
        ExVideoWidget widget = getWidget();
        File answer = File.createTempFile("doc", ".pdf");
        when(mediaUtils.isVideoFile(answer)).thenReturn(false);
        widget.setData(answer);
        assertThat(ShadowToast.getTextOfLatestToast(), is("Application returned an invalid file type"));
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ExVideoWidget widget = getWidget();
        assertThat(widget.binding.captureVideoButton.getVisibility(), is(View.GONE));
        assertThat(widget.binding.playVideoButton.getVisibility(), is(View.VISIBLE));
        assertThat(widget.binding.playVideoButton.isEnabled(), is(true));
    }

    @Test
    public void whenReadOnlyOverrideOptionIsUsed_shouldAllClickableElementsBeDisabled() {
        readOnlyOverride = true;
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ExVideoWidget widget = getWidget();
        assertThat(widget.binding.captureVideoButton.getVisibility(), is(View.GONE));
        assertThat(widget.binding.playVideoButton.getVisibility(), is(View.VISIBLE));
        assertThat(widget.binding.playVideoButton.isEnabled(), is(true));
    }
}
