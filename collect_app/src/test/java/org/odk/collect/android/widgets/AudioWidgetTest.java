package org.odk.collect.android.widgets;

import android.content.Intent;
import android.view.View;

import androidx.annotation.Nullable;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.audio.AudioControllerView;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.support.TestScreenContextActivity;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.odk.collect.android.widgets.utilities.AudioDataRequester;
import org.odk.collect.android.widgets.utilities.AudioPlayer;
import org.odk.collect.audioclips.Clip;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowToast;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static android.content.Intent.ACTION_GET_CONTENT;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.support.RobolectricHelpers.setupMediaPlayerDataSource;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class AudioWidgetTest {

    private final FakeQuestionMediaManager questionMediaManager = new FakeQuestionMediaManager();
    private final FakeWaitingForDataRegistry waitingForDataRegistry = new FakeWaitingForDataRegistry();
    private final AudioDataRequester audioDataRequester = mock(AudioDataRequester.class);

    private TestScreenContextActivity widgetActivity;
    private ShadowActivity shadowActivity;
    private ActivityAvailability activityAvailability;
    private FormIndex formIndex;
    private File mockedFile;
    private FakeAudioPlayer audioPlayer;

    @Before
    public void setUp() {
        widgetActivity = RobolectricHelpers.buildThemedActivity(TestScreenContextActivity.class).get();
        shadowActivity = shadowOf(widgetActivity);

        activityAvailability = mock(ActivityAvailability.class);
        formIndex = mock(FormIndex.class);
        mockedFile = mock(File.class);
        audioPlayer = new FakeAudioPlayer();

        when(mockedFile.exists()).thenReturn(true);
        when(mockedFile.getName()).thenReturn("newFile.mp3");
        when(mockedFile.getAbsolutePath()).thenReturn("newFilePath");
        when(formIndex.toString()).thenReturn("questionIndex");
        when(activityAvailability.isActivityAvailable(any())).thenReturn(true);
    }

    @Test
    public void usingReadOnlyOption_doesNotShowCaptureAndChooseButtons() {
        AudioWidget widget = createWidget(promptWithReadOnly());
        assertThat(widget.binding.captureButton.getVisibility(), equalTo(GONE));
        assertThat(widget.binding.chooseButton.getVisibility(), equalTo(GONE));
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNullAndHidesAudioPlayer() {
        AudioWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.getAnswer(), nullValue());
        assertThat(widget.binding.audioController.getVisibility(), is(GONE));
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData("blah.mp3")));
        assertThat(widget.getAnswer().getDisplayText(), equalTo("blah.mp3"));
    }

    @Test
    public void whenWidgetIsNew_chooseSoundButtonIsNotShown() {
        FormEntryPrompt prompt = promptWithReadOnly();
        when(prompt.getAppearanceHint()).thenReturn(WidgetAppearanceUtils.NEW);
        AudioWidget widget = createWidget(prompt);

        assertThat(widget.binding.chooseButton.getVisibility(), equalTo(GONE));
    }

    @Test
    public void deleteFile_removesWidgetAnswerAndStopsPlayingMedia() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData("blah.mp3")));
        widget.deleteFile();

        assertThat(widget.getAnswer(), nullValue());
        assertThat(audioPlayer.getCurrentClip(), nullValue());
    }

    @Test
    public void deleteFile_setsFileAsideForDeleting() {
        FormEntryPrompt prompt = promptWithAnswer(new StringData("blah.mp3"));
        when(prompt.getIndex()).thenReturn(formIndex);

        AudioWidget widget = createWidget(prompt);
        widget.deleteFile();

        assertThat(questionMediaManager.originalFiles.get("questionIndex"),
                equalTo(widget.getInstanceFolder() + File.separator + "blah.mp3"));
    }

    @Test
    public void clearAnswer_removesAnswerAndHidesPlayer() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData("blah.mp3")));
        widget.clearAnswer();

        assertThat(widget.getAnswer(), nullValue());
        assertThat(widget.binding.audioController.getVisibility(), is(GONE));
        assertThat(audioPlayer.getCurrentClip(), nullValue());
    }

    @Test
    public void clearAnswer_setsFileAsideForDeleting() {
        FormEntryPrompt prompt = promptWithAnswer(new StringData("blah.mp3"));
        when(prompt.getIndex()).thenReturn(formIndex);

        AudioWidget widget = createWidget(prompt);
        widget.clearAnswer();

        assertThat(questionMediaManager.originalFiles.get("questionIndex"),
                equalTo(widget.getInstanceFolder() + File.separator + "blah.mp3"));
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData("blah.mp3")));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void setData_whenFileExists_replacesOriginalFileWithNewFile() {
        FormEntryPrompt prompt = promptWithAnswer(new StringData("blah.mp3"));
        when(prompt.getIndex()).thenReturn(formIndex);
        AudioWidget widget = createWidget(prompt);
        widget.setData(mockedFile);

        assertThat(questionMediaManager.recentFiles.get("questionIndex"), equalTo("newFilePath"));
    }

    @Test
    public void setData_whenPromptHasDifferentAudioFile_deletesOriginalAnswer() {
        FormEntryPrompt prompt = promptWithAnswer(new StringData("blah.mp3"));
        when(prompt.getIndex()).thenReturn(formIndex);

        AudioWidget widget = createWidget(prompt);
        widget.setData(mockedFile);

        assertThat(questionMediaManager.originalFiles.get("questionIndex"),
                equalTo(widget.getInstanceFolder() + File.separator + "blah.mp3"));
    }

    @Test
    public void setData_whenPromptDoesNotHaveAnswer_doesNotDeleteOriginalAnswer() {
        AudioWidget widget = createWidget(promptWithAnswer(null));
        widget.setData(mockedFile);
        assertThat(questionMediaManager.originalFiles.isEmpty(), equalTo(true));
    }

    @Test
    public void setData_whenPromptHasSameAnswer_doesNotDeleteOriginalAnswer() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData("newFile.mp3")));
        widget.setData(mockedFile);
        assertThat(questionMediaManager.originalFiles.isEmpty(), equalTo(true));
    }

    @Test
    public void setData_whenFileDoesNotExist_doesNotUpdateWidgetAnswer() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData("blah.mp3")));
        widget.setData(new File("newFile.mp3"));
        assertThat(widget.getAnswer().getDisplayText(), equalTo("blah.mp3"));
    }

    @Test
    public void setData_whenFileExists_updatesWidgetAnswer() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData("blah.mp3")));
        widget.setData(mockedFile);
        assertThat(widget.getAnswer().getDisplayText(), equalTo("newFile.mp3"));
    }

    @Test
    public void setData_whenFileExists_updatesPlayerMedia() {
        FormEntryPrompt prompt = promptWithAnswer(new StringData("blah.mp3"));
        AudioWidget widget = createWidget(prompt);
        widget.setData(mockedFile);

        assertThat(widget.binding.audioController.getVisibility(), is(VISIBLE));
        widget.binding.audioController.binding.play.performClick();

        Clip expectedClip = getExpectedClip(prompt, "newFile.mp3");
        assertThat(audioPlayer.getCurrentClip(), is(expectedClip));
    }

    @Test
    public void setData_whenFileExists_callsValueChangeListener() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData("blah.mp3")));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.setData(mockedFile);

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingButtonsForLong_callsOnLongClickListeners() {
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);
        AudioWidget widget = createWidget(promptWithAnswer(null));
        widget.setOnLongClickListener(listener);

        widget.binding.captureButton.performLongClick();
        widget.binding.chooseButton.performLongClick();

        verify(listener).onLongClick(widget.binding.captureButton);
        verify(listener).onLongClick(widget.binding.chooseButton);
    }

    @Test
    public void clickingChooseButton_whenIntentIsNotAvailable_doesNotStartAnyIntentAndCancelsWaitingForData() {
        when(activityAvailability.isActivityAvailable(any())).thenReturn(false);
        AudioWidget widget = createWidget(promptWithAnswer(null));

        widget.binding.chooseButton.performClick();
        Intent startedActivity = shadowActivity.getNextStartedActivity();
        String toastMessage = ShadowToast.getTextOfLatestToast();

        assertThat(startedActivity, nullValue());
        assertThat(waitingForDataRegistry.waiting.isEmpty(), equalTo(true));
        assertThat(toastMessage, equalTo(widget.getContext().getString(R.string.activity_not_found,
                widget.getContext().getString(R.string.choose_audio))));
    }

    @Test
    public void clickingChooseButton_startsChooseAudioFileActivityAndSetsWidgetWaitingForData() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        when(prompt.getIndex()).thenReturn(formIndex);

        AudioWidget widget = createWidget(prompt);
        widget.binding.chooseButton.performClick();

        Intent startedActivity = shadowActivity.getNextStartedActivity();
        assertThat(startedActivity.getAction(), equalTo(ACTION_GET_CONTENT));
        assertThat(startedActivity.getType(), equalTo("audio/*"));

        ShadowActivity.IntentForResult intentForResult = shadowActivity.getNextStartedActivityForResult();
        assertThat(intentForResult.requestCode, equalTo(ApplicationConstants.RequestCodes.AUDIO_CHOOSER));

        assertThat(waitingForDataRegistry.waiting.contains(formIndex), equalTo(true));
    }

    @Test
    public void clickingCaptureButton_requestsRecording() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        AudioWidget widget = createWidget(prompt);

        widget.binding.captureButton.performClick();
        verify(audioDataRequester).requestRecording(prompt);
    }

    @Test
    public void afterSetBinaryData_clickingPlayAndPause_playsAndPausesAudio() throws Exception {
        FormEntryPrompt prompt = promptWithAnswer(null);
        AudioWidget widget = createWidget(prompt);

        File audioFile = File.createTempFile("blah", ".mp3");
        Clip expectedClip = getExpectedClip(prompt, audioFile.getName());
        widget.setData(audioFile);

        AudioControllerView audioController = widget.binding.audioController;
        assertThat(audioController.getVisibility(), is(VISIBLE));
        audioController.binding.play.performClick();
        assertThat(audioPlayer.getCurrentClip(), is(expectedClip));

        audioController.binding.play.performClick();
        assertThat(audioPlayer.getCurrentClip(), is(expectedClip));
        assertThat(audioPlayer.isPaused(), is(true));

        audioController.binding.play.performClick();
        assertThat(audioPlayer.getCurrentClip(), is(expectedClip));
        assertThat(audioPlayer.isPaused(), is(false));
    }

    @Test
    public void afterSetBinaryData_canSkipClipForward() throws Exception {
        FormEntryPrompt prompt = promptWithAnswer(null);

        File audioFile = File.createTempFile("blah", ".mp3");
        Clip expectedClip = getExpectedClip(prompt, audioFile.getName());
        setupMediaPlayerDataSource(expectedClip.getURI(), 322450);

        AudioWidget widget = createWidget(prompt);
        widget.setData(audioFile);

        AudioControllerView audioController = widget.binding.audioController;
        audioController.binding.fastForwardBtn.performClick();
        assertThat(audioPlayer.getPosition(expectedClip.getClipID()), is(5000));
    }

    @Test
    public void afterSetBinaryData_whenPositionOfClipChanges_updatesPosition() throws Exception {
        FormEntryPrompt prompt = promptWithAnswer(null);

        File audioFile = File.createTempFile("blah", ".mp3");
        Clip expectedClip = getExpectedClip(prompt, audioFile.getName());
        setupMediaPlayerDataSource(expectedClip.getURI(), 322450);

        AudioWidget widget = createWidget(prompt);
        widget.setData(audioFile);

        AudioControllerView audioController = widget.binding.audioController;
        assertThat(audioController.binding.currentDuration.getText().toString(), is("00:00"));

        audioPlayer.setPosition(expectedClip.getClipID(), 42000);
        assertThat(audioController.binding.currentDuration.getText().toString(), is("00:42"));
    }

    @Test
    public void afterSetBinaryData_showsDurationOfAudio() throws Exception {
        FormEntryPrompt prompt = promptWithAnswer(null);

        File audioFile = File.createTempFile("blah", ".mp3");
        Clip expectedClip = getExpectedClip(prompt, audioFile.getName());
        setupMediaPlayerDataSource(expectedClip.getURI(), 322450);

        AudioWidget widget = createWidget(prompt);
        widget.setData(audioFile);

        AudioControllerView audioController = widget.binding.audioController;
        assertThat(audioController.binding.totalDuration.getText().toString(), is("05:22"));
    }

    public AudioWidget createWidget(FormEntryPrompt prompt) {
        return new AudioWidget(
                widgetActivity,
                new QuestionDetails(prompt, "formAnalyticsID"),
                questionMediaManager,
                waitingForDataRegistry,
                activityAvailability,
                audioPlayer,
                audioDataRequester
        );
    }

    @NotNull
    private Clip getExpectedClip(FormEntryPrompt prompt, String fileName) {
        return new Clip(
                "audio:" + prompt.getIndex().toString(),
                new File("null", fileName).getAbsolutePath() // This is instanceFolder/fileName
        );
    }

    private static class FakeAudioPlayer implements AudioPlayer {

        private final Map<String, Consumer<Boolean>> playingChangedListeners = new HashMap<>();
        private final Map<String, Consumer<Integer>> positionChangedListeners = new HashMap<>();
        private final Map<String, Integer> positions = new HashMap<>();

        private boolean paused;
        private Clip clip;

        @Override
        public void play(Clip clip) {
            this.clip = clip;
            paused = false;
            playingChangedListeners.get(clip.getClipID()).accept(true);
        }

        @Override
        public void pause() {
            paused = true;
            playingChangedListeners.get(clip.getClipID()).accept(false);
        }

        @Override
        public void setPosition(String clipId, Integer position) {
            positions.put(clipId, position);
            positionChangedListeners.get(clipId).accept(position);
        }

        @Override
        public void onPlayingChanged(String clipID, Consumer<Boolean> playingConsumer) {
            playingChangedListeners.put(clipID, playingConsumer);
        }

        @Override
        public void onPositionChanged(String clipID, Consumer<Integer> positionConsumer) {
            positionChangedListeners.put(clipID, positionConsumer);
        }

        @Override
        public void stop() {
            clip = null;
        }

        @Nullable
        public Clip getCurrentClip() {
            return clip;
        }

        public boolean isPaused() {
            return paused;
        }

        public Integer getPosition(String clipId) {
            return positions.get(clipId);
        }
    }
}