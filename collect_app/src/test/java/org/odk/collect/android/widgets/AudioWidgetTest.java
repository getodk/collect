package org.odk.collect.android.widgets;

import android.media.MediaPlayer;
import android.view.View;

import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;

import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.audio.AudioControllerView;
import org.odk.collect.android.audio.AudioHelper;
import org.odk.collect.android.audio.Clip;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.support.FakeLifecycleOwner;
import org.odk.collect.android.support.FakeScheduler;
import org.odk.collect.android.support.TestScreenContextActivity;
import org.odk.collect.android.utilities.FileUtil;
import org.odk.collect.android.utilities.MediaUtil;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.odk.collect.async.Scheduler;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

@RunWith(RobolectricTestRunner.class)
public class AudioWidgetTest {
    private final String destinationPath = "blah";

    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private final FakeScheduler fakeScheduler = new FakeScheduler();

    private TestScreenContextActivity widgetActivity;
    private AudioControllerView audioController;
    private FileUtil fileUtil;
    private MediaUtil mediaUtil;
    private AudioHelper audioHelper;

    @Before
    public void setUp() {
        widgetActivity = widgetTestActivity();

        audioController = mock(AudioControllerView.class);
        fileUtil = mock(FileUtil.class);
        mediaUtil = mock(MediaUtil.class);

        audioHelper = new FakeAudioHelper(widgetActivity, new FakeLifecycleOwner(), fakeScheduler, () -> mediaPlayer);
    }

    @Test
    public void usingReadOnlyOption_doesNotShowCaptureAndChooseButtons() {
        AudioWidget widget = createWidget(promptWithReadOnly());

        assertThat(widget.captureButton.getVisibility(), equalTo(View.GONE));
        assertThat(widget.chooseButton.getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        AudioWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.getAnswer(), nullValue());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData(destinationPath)));
        assertThat(widget.getAnswer().getDisplayText(), equalTo(destinationPath));
    }

    @Test
    public void whenWidgetIsNew_chooseSoundButtonIsNotShown() {
        FormEntryPrompt prompt = promptWithReadOnly();
        when(prompt.getAppearanceHint()).thenReturn(WidgetAppearanceUtils.NEW);
        AudioWidget widget = createWidget(prompt);

        assertThat(widget.chooseButton.getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_audioPlayerIsNotShown() {
        AudioWidget widget = createWidget(promptWithAnswer(null));
        verify(audioController).hidePlayer();
    }

    @Test
    public void whenPromptHasAnswer_audioPlayerIsShown() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData(destinationPath)));
        verify(audioController).showPlayer();
    }

    @Test
    public void whenPromptHasAnswer_audioPlayerSetsAudio() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData(destinationPath)));
        Clip clip = new Clip(String.valueOf(ViewCompat.generateViewId()), getAudioFile(widget.getInstanceFolder()).getAbsolutePath());

        assertThat(((FakeAudioHelper) widget.audioHelper).getAudioController(), equalTo(audioController));
        assertThat(((FakeAudioHelper) widget.audioHelper).getClip().getURI(), equalTo(clip.getURI()));
    }

    @Test
    public void deleteFile_removesWidgetAnswer() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData(destinationPath)));
        widget.deleteFile();
        assertThat(widget.getAnswer(), nullValue());
    }

    @Test
    public void clearAnswer_removesAnswerAndHidesPlayer() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData(destinationPath)));
        widget.clearAnswer();

        assertThat(widget.getAnswer(), nullValue());
        verify(audioController).hidePlayer();
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData(destinationPath)));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingButtonsForLong_callsOnLongClickListeners() {
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);
        AudioWidget widget = createWidget(promptWithAnswer(null));
        widget.setOnLongClickListener(listener);

        widget.captureButton.performLongClick();
        widget.chooseButton.performLongClick();

        verify(listener).onLongClick(widget.captureButton);
        verify(listener).onLongClick(widget.chooseButton);
    }

    public AudioWidget createWidget(FormEntryPrompt prompt) {
        return new AudioWidget(widgetActivity, new QuestionDetails(prompt, "formAnalyticsID"),
                fileUtil, mediaUtil, audioController, new FakeWaitingForDataRegistry(), audioHelper);
    }

    private File getAudioFile(String instanceFolderPath) {
        return new File(instanceFolderPath + File.separator + destinationPath);
    }

    public static class FakeAudioHelper extends AudioHelper {

        private AudioControllerView audioController;
        private Clip clip;

        public FakeAudioHelper(FragmentActivity activity, LifecycleOwner lifecycleOwner, Scheduler scheduler, Supplier<MediaPlayer> mediaPlayerFactory) {
            super(activity, lifecycleOwner, scheduler, mediaPlayerFactory);
        }

        @Override
        public void setAudio(AudioControllerView view, Clip clip) {
            this.audioController = view;
            this.clip = clip;
        }

        public AudioControllerView getAudioController() {
            return audioController;
        }

        public Clip getClip() {
            return clip;
        }
    }
}