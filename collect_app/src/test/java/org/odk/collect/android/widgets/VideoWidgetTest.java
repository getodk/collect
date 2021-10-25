package org.odk.collect.android.widgets;

import android.content.Intent;
import android.provider.MediaStore;
import android.view.View;

import androidx.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.CameraUtils;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.widgets.base.FileWidgetTest;
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */
public class VideoWidgetTest extends FileWidgetTest<VideoWidget> {
    @Mock
    MediaUtils mediaUtils;

    private String destinationName;

    @NonNull
    @Override
    public VideoWidget createWidget() {
        return new VideoWidget(activity, new QuestionDetails(formEntryPrompt, readOnlyOverride), new FakeWaitingForDataRegistry(), new FakeQuestionMediaManager(), new CameraUtils(), mediaUtils);
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(destinationName);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        destinationName = RandomString.make();
    }

    @Test
    public void buttonsShouldLaunchCorrectIntents() {
        stubAllRuntimePermissionsGranted(true);

        Intent intent = getIntentLaunchedByClick(R.id.capture_video);
        assertActionEquals(MediaStore.ACTION_VIDEO_CAPTURE, intent);

        intent = getIntentLaunchedByClick(R.id.choose_video);
        assertActionEquals(Intent.ACTION_GET_CONTENT, intent);
        assertTypeEquals("video/*", intent);

        getIntentLaunchedByClick(R.id.play_video);
        verify(mediaUtils).openFile(any(), any(), any());
    }

    @Test
    public void buttonsShouldNotLaunchIntentsWhenPermissionsDenied() {
        stubAllRuntimePermissionsGranted(false);

        assertIntentNotStarted(activity, getIntentLaunchedByClick(R.id.capture_video));
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        assertThat(getSpyWidget().captureButton.getVisibility(), is(View.GONE));
        assertThat(getSpyWidget().chooseButton.getVisibility(), is(View.GONE));
        assertThat(getSpyWidget().playButton.getVisibility(), is(View.VISIBLE));
        assertThat(getSpyWidget().playButton.isEnabled(), is(Boolean.FALSE));
        assertThat(getSpyWidget().playButton.getText(), is("Play Video"));
    }

    @Test
    public void whenReadOnlyOverrideOptionIsUsed_shouldAllClickableElementsBeDisabled() {
        readOnlyOverride = true;
        when(formEntryPrompt.isReadOnly()).thenReturn(false);

        assertThat(getSpyWidget().captureButton.getVisibility(), is(View.GONE));
        assertThat(getSpyWidget().chooseButton.getVisibility(), is(View.GONE));
        assertThat(getSpyWidget().playButton.getVisibility(), is(View.VISIBLE));
        assertThat(getSpyWidget().playButton.isEnabled(), is(Boolean.FALSE));
        assertThat(getSpyWidget().playButton.getText(), is("Play Video"));
    }
}
