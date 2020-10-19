package org.odk.collect.android.widgets;

import android.view.View;

import androidx.annotation.Nullable;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.audio.AudioControllerView;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.support.TestScreenContextActivity;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager;
import org.odk.collect.android.widgets.utilities.AudioDataRequester;
import org.odk.collect.android.widgets.utilities.AudioPlayer;
import org.odk.collect.audioclips.Clip;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.support.RobolectricHelpers.setupMediaPlayerDataSource;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;

@RunWith(RobolectricTestRunner.class)
public class AudioWidgetTest {

    private final FakeQuestionMediaManager questionMediaManager = new FakeQuestionMediaManager();
    private final AudioDataRequester audioDataRequester = mock(AudioDataRequester.class);

    private TestScreenContextActivity widgetActivity;
    private FormIndex formIndex;
    private FakeAudioPlayer audioPlayer;

    @Before
    public void setUp() throws Exception {
        widgetActivity = RobolectricHelpers.buildThemedActivity(TestScreenContextActivity.class).get();

        formIndex = mock(FormIndex.class);
        when(formIndex.toString()).thenReturn("questionIndex");

        audioPlayer = new FakeAudioPlayer();
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_showsButtonsAndHidesAudioController() {
        AudioWidget widget = createWidget(promptWithAnswer(null));

        assertThat(widget.binding.captureButton.getVisibility(), is(VISIBLE));
        assertThat(widget.binding.chooseButton.getVisibility(), is(VISIBLE));
        assertThat(widget.binding.audioController.getVisibility(), is(GONE));
    }

    @Test
    public void whenPromptHasAnswer_hidesButtonsAndShowsAudioController() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData("blah.mp3")));

        assertThat(widget.binding.captureButton.getVisibility(), is(GONE));
        assertThat(widget.binding.chooseButton.getVisibility(), is(GONE));
        assertThat(widget.binding.audioController.getVisibility(), is(VISIBLE));
    }

    @Test
    public void usingReadOnlyOption_doesNotShowCaptureAndChooseButtons() {
        AudioWidget widget = createWidget(promptWithReadOnly());
        assertThat(widget.binding.captureButton.getVisibility(), equalTo(GONE));
        assertThat(widget.binding.chooseButton.getVisibility(), equalTo(GONE));
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        AudioWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.getAnswer(), nullValue());
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

        assertThat(questionMediaManager.originalFiles.get("questionIndex"), equalTo(questionMediaManager.getAnswerFile("blah.mp3").toString()));
    }

    @Test
    public void clearAnswer_removesAnswerAndHidesPlayer() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData("blah.mp3")));
        widget.clearAnswer();

        assertThat(widget.getAnswer(), nullValue());
        assertThat(widget.binding.audioController.getVisibility(), is(GONE));
    }

    @Test
    public void clearAnswer_setsFileAsideForDeleting() {
        FormEntryPrompt prompt = promptWithAnswer(new StringData("blah.mp3"));
        when(prompt.getIndex()).thenReturn(formIndex);

        AudioWidget widget = createWidget(prompt);
        widget.clearAnswer();

        assertThat(questionMediaManager.originalFiles.get("questionIndex"), equalTo(questionMediaManager.getAnswerFile("blah.mp3").toString()));
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData("blah.mp3")));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void setData_whenFileExists_replacesOriginalFileWithNewFile() throws Exception {
        FormEntryPrompt prompt = promptWithAnswer(new StringData("blah.mp3"));
        when(prompt.getIndex()).thenReturn(formIndex);
        AudioWidget widget = createWidget(prompt);

        File newFile = File.createTempFile("newFIle", ".mp3", questionMediaManager.getDir());
        widget.setData(newFile.getName());

        assertThat(questionMediaManager.recentFiles.get("questionIndex"), equalTo(newFile.getAbsolutePath()));
    }

    @Test
    public void setData_whenPromptHasDifferentAudioFile_deletesOriginalAnswer() throws Exception {
        FormEntryPrompt prompt = promptWithAnswer(new StringData("blah.mp3"));
        when(prompt.getIndex()).thenReturn(formIndex);

        AudioWidget widget = createWidget(prompt);

        File newFile = File.createTempFile("newFIle", ".mp3", questionMediaManager.getDir());
        widget.setData(newFile.getName());

        assertThat(questionMediaManager.originalFiles.get("questionIndex"), equalTo(questionMediaManager.getAnswerFile("blah.mp3").toString()));
    }

    @Test
    public void setData_whenPromptDoesNotHaveAnswer_doesNotDeleteOriginalAnswer() throws Exception {
        AudioWidget widget = createWidget(promptWithAnswer(null));

        File newFile = File.createTempFile("newFIle", ".mp3", questionMediaManager.getDir());
        widget.setData(newFile.getName());
        assertThat(questionMediaManager.originalFiles.isEmpty(), equalTo(true));
    }

    @Test
    public void setData_whenPromptHasSameAnswer_doesNotDeleteOriginalAnswer() throws Exception {
        File newFile = File.createTempFile("newFIle", ".mp3", questionMediaManager.getDir());
        AudioWidget widget = createWidget(promptWithAnswer(new StringData(newFile.getName())));
        widget.setData(newFile.getName());
        assertThat(questionMediaManager.originalFiles.isEmpty(), equalTo(true));
    }

    @Test
    public void setData_whenFileDoesNotExist_doesNotUpdateWidgetAnswer() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData("blah.mp3")));
        widget.setData("newFile.mp3");
        assertThat(widget.getAnswer().getDisplayText(), equalTo("blah.mp3"));
    }

    @Test
    public void setData_whenFileExists_updatesWidgetAnswer() throws Exception {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData("blah.mp3")));

        File newFile = File.createTempFile("newFIle", ".mp3", questionMediaManager.getDir());
        widget.setData(newFile.getName());
        assertThat(widget.getAnswer().getDisplayText(), equalTo(newFile.getName()));
    }

    /**
     * Currently choosing audio is locked into the {@link org.odk.collect.android.tasks.MediaLoadingTask}
     * flow and so we'd need to rip this apart to let us drop support for accepting File as data. In
     * this case it will just grab the name off the file and use {@link org.odk.collect.android.utilities.QuestionMediaManager}
     * to handle grabbing the actual file
     */
    @Test
    public void setData_supportsFilesAsWellAsStrings() throws Exception {
        AudioWidget widget = createWidget(promptWithAnswer(null));

        File newFile = File.createTempFile("newFIle", ".mp3", questionMediaManager.getDir());
        widget.setData(newFile);
        assertThat(widget.getAnswer().getDisplayText(), equalTo(newFile.getName()));
    }

    @Test
    public void setData_whenFileExists_callsValueChangeListener() throws Exception {
        AudioWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);

        File newFile = File.createTempFile("newFIle", ".mp3", questionMediaManager.getDir());
        widget.setData(newFile.getName());

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void setData_whenFileExists_hidesButtonsAndShowsAudioController() throws Exception {
        AudioWidget widget = createWidget(promptWithAnswer(null));

        File newFile = File.createTempFile("newFIle", ".mp3", questionMediaManager.getDir());
        widget.setData(newFile.getName());

        assertThat(widget.binding.captureButton.getVisibility(), is(GONE));
        assertThat(widget.binding.captureButton.getVisibility(), is(GONE));
        assertThat(widget.binding.audioController.getVisibility(), is(VISIBLE));
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
    public void clickingChooseButton_requestsAudioFile() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        AudioWidget widget = createWidget(prompt);

        widget.binding.chooseButton.performClick();
        verify(audioDataRequester).requestFile(prompt);
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

        File audioFile = File.createTempFile("blah", ".mp3", questionMediaManager.getDir());
        Clip expectedClip = getExpectedClip(prompt, audioFile.getName());
        widget.setData(audioFile.getName());

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

        File audioFile = File.createTempFile("blah", ".mp3", questionMediaManager.getDir());
        Clip expectedClip = getExpectedClip(prompt, audioFile.getName());
        setupMediaPlayerDataSource(expectedClip.getURI(), 322450);

        AudioWidget widget = createWidget(prompt);
        widget.setData(audioFile.getName());

        AudioControllerView audioController = widget.binding.audioController;
        audioController.binding.fastForwardBtn.performClick();
        assertThat(audioPlayer.getPosition(expectedClip.getClipID()), is(5000));
    }

    @Test
    public void afterSetBinaryData_whenPositionOfClipChanges_updatesPosition() throws Exception {
        FormEntryPrompt prompt = promptWithAnswer(null);

        File audioFile = File.createTempFile("blah", ".mp3", questionMediaManager.getDir());
        Clip expectedClip = getExpectedClip(prompt, audioFile.getName());
        setupMediaPlayerDataSource(expectedClip.getURI(), 322450);

        AudioWidget widget = createWidget(prompt);
        widget.setData(audioFile.getName());

        AudioControllerView audioController = widget.binding.audioController;
        assertThat(audioController.binding.currentDuration.getText().toString(), is("00:00"));

        audioPlayer.setPosition(expectedClip.getClipID(), 42000);
        assertThat(audioController.binding.currentDuration.getText().toString(), is("00:42"));
    }

    @Test
    public void afterSetBinaryData_showsDurationOfAudio() throws Exception {
        FormEntryPrompt prompt = promptWithAnswer(null);

        File audioFile = File.createTempFile("blah", ".mp3", questionMediaManager.getDir());
        Clip expectedClip = getExpectedClip(prompt, audioFile.getName());
        setupMediaPlayerDataSource(expectedClip.getURI(), 322450);

        AudioWidget widget = createWidget(prompt);
        widget.setData(audioFile.getName());

        AudioControllerView audioController = widget.binding.audioController;
        assertThat(audioController.binding.totalDuration.getText().toString(), is("05:22"));
    }

    @Test
    public void clickingRemove_clearsAnswer() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData("blah.mp3")));
        widget.binding.audioController.binding.remove.performClick();

        assertThat(widget.getAnswer(), nullValue());
    }

    @Test
    public void clickingRemove_hidesAudioControllerAndShowsButtons() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData("blah.mp3")));
        widget.binding.audioController.binding.remove.performClick();

        assertThat(widget.binding.audioController.getVisibility(), is(GONE));
        assertThat(widget.binding.captureButton.getVisibility(), is(VISIBLE));
        assertThat(widget.binding.chooseButton.getVisibility(), is(VISIBLE));
    }

    public AudioWidget createWidget(FormEntryPrompt prompt) {
        return new AudioWidget(
                widgetActivity,
                new QuestionDetails(prompt, "formAnalyticsID"),
                questionMediaManager,
                audioPlayer,
                audioDataRequester
        );
    }

    @NotNull
    private Clip getExpectedClip(FormEntryPrompt prompt, String fileName) {
        return new Clip(
                "audio:" + prompt.getIndex().toString(),
                questionMediaManager.getAnswerFile(fileName).getAbsolutePath()
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