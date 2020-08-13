package org.odk.collect.android.widgets;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;

import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.audio.AudioControllerView;
import org.odk.collect.android.audio.AudioHelper;
import org.odk.collect.android.audio.Clip;
import org.odk.collect.android.fakes.FakePermissionUtils;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.support.FakeLifecycleOwner;
import org.odk.collect.android.support.FakeScheduler;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.support.TestScreenContextActivity;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.FileUtil;
import org.odk.collect.android.utilities.MediaUtil;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.odk.collect.async.Scheduler;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowToast;

import java.io.File;
import java.util.function.Supplier;

import static android.content.Intent.ACTION_GET_CONTENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class AudioWidgetTest {
    private static final String FILE_PATH = "blah.mp3";
    private static final String SOURCE_FILE_PATH = "sourceFile.mp3";

    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private final FakeScheduler fakeScheduler = new FakeScheduler();
    private final FakePermissionUtils fakePermissionUtils = new FakePermissionUtils();
    private final FakeQuestionMediaManager questionMediaManager = new FakeQuestionMediaManager();
    private final FakeWaitingForDataRegistry waitingForDataRegistry = new FakeWaitingForDataRegistry();

    private TestScreenContextActivity widgetActivity;
    private ShadowActivity shadowActivity;
    private AudioControllerView audioController;
    private FileUtil fileUtil;
    private MediaUtil mediaUtil;
    private FakeAudioHelper audioHelper;
    private ActivityAvailability activityAvailability;
    private FormIndex formIndex;
    private File mockedFile;

    @Before
    public void setUp() {
        widgetActivity = RobolectricHelpers.buildThemedActivity(TestScreenContextActivity.class).get();
        shadowActivity = shadowOf(widgetActivity);

        audioController = mock(AudioControllerView.class);
        fileUtil = mock(FileUtil.class);
        mediaUtil = mock(MediaUtil.class);
        activityAvailability = mock(ActivityAvailability.class);
        formIndex = mock(FormIndex.class);
        mockedFile = mock(File.class);
        audioHelper = new FakeAudioHelper(widgetActivity, new FakeLifecycleOwner(), fakeScheduler, () -> mediaPlayer);

        when(mockedFile.exists()).thenReturn(true);
        when(mockedFile.getName()).thenReturn("newFile.mp3");
        when(mockedFile.getAbsolutePath()).thenReturn("newFilePath");
        when(formIndex.toString()).thenReturn("questionIndex");
        when(activityAvailability.isActivityAvailable(any())).thenReturn(true);
    }

    @Test
    public void usingReadOnlyOption_doesNotShowCaptureAndChooseButtons() {
        AudioWidget widget = createWidget(promptWithReadOnly());
        assertThat(widget.binding.captureButton.getVisibility(), equalTo(View.GONE));
        assertThat(widget.binding.chooseButton.getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNullAndHidesAudioPlayer() {
        AudioWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.getAnswer(), nullValue());
        verify(audioController).hidePlayer();
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData(FILE_PATH)));
        assertThat(widget.getAnswer().getDisplayText(), equalTo(FILE_PATH));
    }

    @Test
    public void whenWidgetIsNew_chooseSoundButtonIsNotShown() {
        FormEntryPrompt prompt = promptWithReadOnly();
        when(prompt.getAppearanceHint()).thenReturn(WidgetAppearanceUtils.NEW);
        AudioWidget widget = createWidget(prompt);

        assertThat(widget.binding.chooseButton.getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void whenPromptHasAnswer_updatesPlayerMedia() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData(FILE_PATH)));
        Clip clip = getAnswerAudioClip(widget.getInstanceFolder(), widget.getAnswer());

        assertThat(audioHelper.audioController, equalTo(audioController));
        assertThat(audioHelper.clip.getURI(), equalTo(clip.getURI()));
        verify(audioController).showPlayer();
    }

    @Test
    public void deleteFile_removesWidgetAnswerAndStopsPlayingMedia() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData(FILE_PATH)));
        widget.deleteFile();

        assertThat(widget.getAnswer(), nullValue());
        assertThat(audioHelper.isMediaStopped, equalTo(true));
    }

    @Test
    public void deleteFile_setsFileAsideForDeleting() {
        FormEntryPrompt prompt = promptWithAnswer(new StringData(FILE_PATH));
        when(prompt.getIndex()).thenReturn(formIndex);

        AudioWidget widget = createWidget(prompt);
        widget.deleteFile();

        assertThat(questionMediaManager.originalFiles.get("questionIndex"),
                equalTo(widget.getInstanceFolder() + File.separator + "blah.mp3"));
    }

    @Test
    public void clearAnswer_removesAnswerAndHidesPlayer() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData(FILE_PATH)));
        widget.clearAnswer();

        assertThat(widget.getAnswer(), nullValue());
        assertThat(audioHelper.isMediaStopped, equalTo(true));
        verify(audioController).hidePlayer();
    }

    @Test
    public void clearAnswer_setsFileAsideForDeleting() {
        FormEntryPrompt prompt = promptWithAnswer(new StringData(FILE_PATH));
        when(prompt.getIndex()).thenReturn(formIndex);

        AudioWidget widget = createWidget(prompt);
        widget.clearAnswer();

        assertThat(questionMediaManager.originalFiles.get("questionIndex"),
                equalTo(widget.getInstanceFolder() + File.separator + "blah.mp3"));
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData(FILE_PATH)));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void setData_whenFileExists_replacesOriginalFileWithNewFile() {
        FormEntryPrompt prompt = promptWithAnswer(new StringData(FILE_PATH));
        when(prompt.getIndex()).thenReturn(formIndex);
        AudioWidget widget = createWidget(prompt);
        widget.setBinaryData(mockedFile);

        assertThat(questionMediaManager.recentFiles.get("questionIndex"), equalTo("newFilePath"));
    }

    @Test
    public void setData_whenPromptHasDifferentAudioFile_deletesOriginalAnswer() {
        FormEntryPrompt prompt = promptWithAnswer(new StringData(FILE_PATH));
        when(prompt.getIndex()).thenReturn(formIndex);

        AudioWidget widget = createWidget(prompt);
        widget.setBinaryData(mockedFile);

        assertThat(questionMediaManager.originalFiles.get("questionIndex"),
                equalTo(widget.getInstanceFolder() + File.separator + "blah.mp3"));
    }

    @Test
    public void setData_whenPromptDoesNotHaveAnswer_doesNotDeleteOriginalAnswer() {
        AudioWidget widget = createWidget(promptWithAnswer(null));
        widget.setBinaryData(mockedFile);
        assertThat(questionMediaManager.originalFiles.isEmpty(), equalTo(true));
    }

    @Test
    public void setData_whenPromptHasSameAnswer_doesNotDeleteOriginalAnswer() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData("newFile.mp3")));
        widget.setBinaryData(mockedFile);
        assertThat(questionMediaManager.originalFiles.isEmpty(), equalTo(true));
    }

    @Test
    public void setData_whenFileDoesNotExist_doesNotUpdateWidgetAnswer() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData(FILE_PATH)));
        widget.setBinaryData(new File("newFile.mp3"));
        assertThat(widget.getAnswer().getDisplayText(), equalTo(FILE_PATH));
    }

    @Test
    public void setData_whenDataIsUri_copiesNewFileToSource() {
        Uri newFileUri = Uri.fromFile(mockedFile);
        File sourceFile = new File(SOURCE_FILE_PATH);

        AudioWidget widget = createWidget(promptWithAnswer(new StringData(FILE_PATH)));

        when(mediaUtil.getPathFromUri(widget.getContext(), newFileUri, MediaStore.Audio.Media.DATA)).thenReturn(SOURCE_FILE_PATH);
        when(fileUtil.getFileAtPath(SOURCE_FILE_PATH)).thenReturn(sourceFile);
        when(fileUtil.getFileAtPath("null/null.mp3")).thenReturn(mockedFile);

        widget.setBinaryData(newFileUri);
        verify(fileUtil).copyFile(sourceFile, mockedFile);
    }

    @Test
    public void setData_whenFileExists_updatesWidgetAnswer() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData(FILE_PATH)));
        widget.setBinaryData(mockedFile);
        assertThat(widget.getAnswer().getDisplayText(), equalTo("newFile.mp3"));
    }

    @Test
    public void setData_whenFileExists_updatesPlayerMedia() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData(FILE_PATH)));
        widget.setBinaryData(mockedFile);
        Clip clip = getAnswerAudioClip(widget.getInstanceFolder(), widget.getAnswer());

        assertThat(audioHelper.audioController, equalTo(audioController));
        assertThat(audioHelper.clip.getURI(), equalTo(clip.getURI()));
        verify(audioController, times(2)).showPlayer();
    }

    @Test
    public void setData_whenFileExists_callsValueChangeListener() {
        AudioWidget widget = createWidget(promptWithAnswer(new StringData(FILE_PATH)));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.setBinaryData(mockedFile);

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
    public void clickingCaptureButton_whenIntentIsNotAvailable_doesNotStartAnyIntentAndCancelsWaitingForData() {
        when(activityAvailability.isActivityAvailable(any())).thenReturn(false);

        AudioWidget widget = createWidget(promptWithAnswer(null));
        widget.setPermissionUtils(fakePermissionUtils);
        fakePermissionUtils.setPermissionGranted(true);

        widget.binding.captureButton.performClick();
        Intent startedActivity = shadowActivity.getNextStartedActivity();
        String toastMessage = ShadowToast.getTextOfLatestToast();

        assertThat(startedActivity, nullValue());
        assertThat(waitingForDataRegistry.waiting.isEmpty(), equalTo(true));
        assertThat(toastMessage, equalTo(widget.getContext().getString(R.string.activity_not_found,
                widget.getContext().getString(R.string.capture_audio))));
    }

    @Test
    public void clickingCaptureButton_whenPermissionIsNotGranted_doesNotStartAnyIntentAndCancelsWaitingForData() {
        AudioWidget widget = createWidget(promptWithAnswer(null));
        widget.setPermissionUtils(fakePermissionUtils);
        fakePermissionUtils.setPermissionGranted(false);

        widget.binding.captureButton.performClick();
        Intent startedActivity = shadowActivity.getNextStartedActivity();

        assertThat(startedActivity, nullValue());
        assertThat(waitingForDataRegistry.waiting.isEmpty(), equalTo(true));
    }

    @Test
    public void clickingCaptureButton_whenPermissionIsGranted_startsRecordSoundIntentAndSetsWidgetWaitingForData() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        when(prompt.getIndex()).thenReturn(formIndex);

        AudioWidget widget = createWidget(prompt);
        widget.setPermissionUtils(fakePermissionUtils);
        fakePermissionUtils.setPermissionGranted(true);

        widget.binding.captureButton.performClick();
        Intent startedActivity = shadowActivity.getNextStartedActivity();

        assertThat(startedActivity.getAction(), equalTo(MediaStore.Audio.Media.RECORD_SOUND_ACTION));
        assertThat(startedActivity.getStringExtra(MediaStore.EXTRA_OUTPUT), equalTo(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                .toString()));

        ShadowActivity.IntentForResult intentForResult = shadowActivity.getNextStartedActivityForResult();
        assertThat(intentForResult.requestCode, equalTo(ApplicationConstants.RequestCodes.AUDIO_CAPTURE));

        assertThat(waitingForDataRegistry.waiting.contains(formIndex), equalTo(true));
    }

    public AudioWidget createWidget(FormEntryPrompt prompt) {
        return new AudioWidget(widgetActivity, new QuestionDetails(prompt, "formAnalyticsID"), fileUtil,
                mediaUtil, audioController, questionMediaManager, waitingForDataRegistry, audioHelper, activityAvailability);
    }

    private Clip getAnswerAudioClip(String instanceFolderPath, IAnswerData answer) {
        return new Clip(String.valueOf(ViewCompat.generateViewId()), getAudioFilePath(instanceFolderPath, answer));
    }

    private String getAudioFilePath(String instanceFolderPath, IAnswerData answer) {
        return (new File(instanceFolderPath + File.separator + answer.getDisplayText())).getAbsolutePath();
    }

    private static class FakeAudioHelper extends AudioHelper {
        public AudioControllerView audioController;
        public Clip clip;

        public boolean isMediaStopped;

        FakeAudioHelper(FragmentActivity activity, LifecycleOwner lifecycleOwner, Scheduler scheduler, Supplier<MediaPlayer> mediaPlayerFactory) {
            super(activity, lifecycleOwner, scheduler, mediaPlayerFactory);
            isMediaStopped = false;
        }

        @Override
        public void setAudio(AudioControllerView view, Clip clip) {
            this.audioController = view;
            this.clip = clip;
        }

        @Override
        public void stop() {
            isMediaStopped = true;
        }
    }
}