package org.odk.collect.android.widgets;

import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.support.TestScreenContextActivity;
import org.odk.collect.android.utilities.CameraUtilsProvider;
import org.odk.collect.android.utilities.FileUtil;
import org.odk.collect.android.utilities.MediaUtil;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.widgets.base.FileWidgetTest;
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.odk.collect.android.widgets.utilities.FileWidgetUtils;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowToast;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;
import static org.robolectric.Shadows.shadowOf;

/**
 * @author James Knight
 */
@RunWith(RobolectricTestRunner.class)
public class VideoWidgetTest {
    private static final String FILE_PATH = "blah.mp4";
    private static final String SOURCE_FILE_PATH = "sourceFile.mp4";

    private TestScreenContextActivity widgetActivity;
    private ShadowActivity shadowActivity;
    private FakeWaitingForDataRegistry waitingForDataRegistry;
    private FileUtil fileUtil;
    private MediaUtil mediaUtil;
    private CameraUtilsProvider cameraUtilsProvider;
    private QuestionMediaManager mediaManagerListener;
    private FormIndex formIndex;
    private File mockedFile;

    @Before
    public void setUp() {
        widgetActivity = widgetTestActivity();
        shadowActivity = shadowOf(widgetActivity);

        fileUtil = mock(FileUtil.class);
        mediaUtil = mock(MediaUtil.class);
        cameraUtilsProvider = mock(CameraUtilsProvider.class);
        mediaManagerListener = mock(QuestionMediaManager.class);
        formIndex = mock(FormIndex.class);
        mockedFile = mock(File.class);

        waitingForDataRegistry = new FakeWaitingForDataRegistry();

        when(mockedFile.exists()).thenReturn(true);
        when(mockedFile.getName()).thenReturn("newFile.mp4");
        when(mockedFile.getAbsolutePath()).thenReturn("newFilePath");
        when(formIndex.toString()).thenReturn("questionIndex");
    }

    @Test
    public void usingReadOnlyOption_showsOnlyPlayButton() {
        VideoWidget widget = createWidget(promptWithReadOnly());

        assertThat(widget.captureButton.getVisibility(), is(View.GONE));
        assertThat(widget.chooseButton.getVisibility(), is(View.GONE));
        assertThat(widget.playButton.getVisibility(), is(View.VISIBLE));
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNullAndHidesAudioPlayer() {
        VideoWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.getAnswer(), nullValue());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        VideoWidget widget = createWidget(promptWithAnswer(new StringData(FILE_PATH)));
        assertThat(widget.getAnswer().getDisplayText(), is(FILE_PATH));
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_playButtonIsDisabled() {
        VideoWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.playButton.isEnabled(), is(false));
    }

    @Test
    public void whenWidgetIsNew_chooseSoundButtonIsNotShown() {
        FormEntryPrompt prompt = promptWithReadOnly();
        when(prompt.getAppearanceHint()).thenReturn(WidgetAppearanceUtils.NEW);
        VideoWidget widget = createWidget(prompt);

        assertThat(widget.chooseButton.getVisibility(), is(View.GONE));
    }

    @Test
    public void usingSelfieWidget_hidesChooseButton() {
        FormEntryPrompt prompt = promptWithReadOnly();
        when(prompt.getAppearanceHint()).thenReturn(WidgetAppearanceUtils.SELFIE);
        VideoWidget widget = createWidget(prompt);

        assertThat(widget.chooseButton.getVisibility(), is(View.GONE));
    }

    @Test
    public void usingMediaAppearanceNewFront_hidesChooseButton() {
        FormEntryPrompt prompt = promptWithReadOnly();
        when(prompt.getAppearanceHint()).thenReturn(WidgetAppearanceUtils.NEW_FRONT);
        VideoWidget widget = createWidget(prompt);

        assertThat(widget.chooseButton.getVisibility(), is(View.GONE));
    }

    @Test
    public void usingSelfieWidget_disablesCaptureButton_whenFrontCameraIsNotAvailable() {
        FormEntryPrompt prompt = promptWithReadOnly();
        when(cameraUtilsProvider.checkFrontCameraAvailability()).thenReturn(false);
        when(prompt.getAppearanceHint()).thenReturn(WidgetAppearanceUtils.SELFIE);
        VideoWidget widget = createWidget(prompt);

        assertThat(widget.captureButton.isEnabled(), is(false));
        assertThat(ShadowToast.getTextOfLatestToast(), is(widget.getContext().getString(R.string.error_front_camera_unavailable)));
    }

    @Test
    public void usingMediaAppearanceNewFront_disablesCaptureButton_whenFrontCameraIsNotAvailable() {
        FormEntryPrompt prompt = promptWithReadOnly();
        when(cameraUtilsProvider.checkFrontCameraAvailability()).thenReturn(false);
        when(prompt.getAppearanceHint()).thenReturn(WidgetAppearanceUtils.NEW_FRONT);
        VideoWidget widget = createWidget(prompt);

        assertThat(widget.captureButton.isEnabled(), is(false));
        assertThat(ShadowToast.getTextOfLatestToast(), is(widget.getContext().getString(R.string.error_front_camera_unavailable)));
    }

    @Test
    public void deleteFile_removesWidgetAnswer() {
        VideoWidget widget = createWidget(promptWithAnswer(new StringData(FILE_PATH)));
        widget.deleteFile();
        assertThat(widget.getAnswer(), nullValue());
    }

    @Test
    public void deleteFile_callsMediaManagerListener() {
        FormEntryPrompt prompt = promptWithAnswer(new StringData(FILE_PATH));
        when(prompt.getIndex()).thenReturn(formIndex);
        VideoWidget widget = createWidget(prompt);
        widget.deleteFile();

        verify(mediaManagerListener).markOriginalFileOrDelete("questionIndex",
                widget.getInstanceFolder() + File.separator + FILE_PATH);
    }

    @Test
    public void clearAnswer_removesAnswerAndHidesPlayer() {
        VideoWidget widget = createWidget(promptWithAnswer(new StringData(FILE_PATH)));
        widget.clearAnswer();
        assertThat(widget.getAnswer(), nullValue());
    }

    @Test
    public void clearAnswer_callsMediaManagerListener() {
        FormEntryPrompt prompt = promptWithAnswer(new StringData(FILE_PATH));
        when(prompt.getIndex()).thenReturn(formIndex);
        VideoWidget widget = createWidget(prompt);
        widget.clearAnswer();

        verify(mediaManagerListener).markOriginalFileOrDelete("questionIndex",
                widget.getInstanceFolder() + File.separator + FILE_PATH);
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        VideoWidget widget = createWidget(promptWithAnswer(new StringData(FILE_PATH)));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void setData_whenFileDoesNotExist_doesNotUpdateWidgetAnswer() {
        VideoWidget widget = createWidget(promptWithAnswer(new StringData(FILE_PATH)));
        widget.setBinaryData(new File("newFile.mp4"));
        assertThat(widget.getAnswer().getDisplayText(), is(FILE_PATH));
    }

    @Test
    public void setData_whenDataIsUri_copiesNewFileToSource() {
        Uri newFileUri = Uri.fromFile(mockedFile);
        File sourceFile = new File(SOURCE_FILE_PATH);

        VideoWidget widget = createWidget(promptWithAnswer(new StringData(FILE_PATH)));
        String destinationPath = FileWidgetUtils.getDestinationPathFromSourcePath(SOURCE_FILE_PATH, widget.getInstanceFolder(), fileUtil);

        when(mediaUtil.getPathFromUri(widget.getContext(), newFileUri, MediaStore.Audio.Media.DATA)).thenReturn(SOURCE_FILE_PATH);
        when(fileUtil.getFileAtPath(SOURCE_FILE_PATH)).thenReturn(sourceFile);
        when(fileUtil.getFileAtPath(destinationPath)).thenReturn(mockedFile);

        widget.setBinaryData(newFileUri);
        verify(fileUtil).copyFile(sourceFile, mockedFile);
    }

    @Test
    public void setData_whenFileExists_callsMediaManagerListener() {
        FormEntryPrompt prompt = promptWithAnswer(new StringData(FILE_PATH));
        when(prompt.getIndex()).thenReturn(formIndex);
        when(mockedFile.getAbsolutePath()).thenReturn("newFilePath/newFile.mp4");

        VideoWidget widget = createWidget(prompt);
        widget.setBinaryData(mockedFile);

        verify(mediaManagerListener).replaceRecentFileForQuestion("questionIndex", "newFilePath/newFile.mp4");
    }

    @Test
    public void setData_whenPromptHasDifferentAnswer_deletesOriginalAnswer() {
        FormEntryPrompt prompt = promptWithAnswer(new StringData(FILE_PATH));
        when(prompt.getIndex()).thenReturn(formIndex);

        VideoWidget widget = createWidget(prompt);
        widget.setBinaryData(mockedFile);

        verify(mediaManagerListener).markOriginalFileOrDelete("questionIndex",
                widget.getInstanceFolder() + File.separator + "blah.mp4");
    }

    @Test
    public void setData_whenPromptDoesNotHaveAnswer_doesNotDeleteOriginalAnswer() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        when(prompt.getIndex()).thenReturn(formIndex);

        VideoWidget widget = createWidget(prompt);
        widget.setBinaryData(mockedFile);

        verify(mediaManagerListener, never()).markOriginalFileOrDelete("questionIndex",
                widget.getInstanceFolder() + File.separator + "blah.mp4");
    }

    @Test
    public void setData_whenPromptHasSameAnswer_doesNotDeleteOriginalAnswer() {
        FormEntryPrompt prompt = promptWithAnswer(new StringData("newFile.mp4"));
        when(prompt.getIndex()).thenReturn(formIndex);

        VideoWidget widget = createWidget(prompt);
        widget.setBinaryData(mockedFile);

        verify(mediaManagerListener, never()).markOriginalFileOrDelete("questionIndex",
                widget.getInstanceFolder() + File.separator + "newFile.mp4");
    }

    @Test
    public void setData_whenFileDoesNotExists_setsWidgetAnswerToNull() {
        VideoWidget widget = createWidget(promptWithAnswer(new StringData(FILE_PATH)));
        widget.setBinaryData(new File("newFile.mp4"));
        assertThat(widget.getAnswer(), nullValue());
    }

    @Test
    public void setData_whenFileDoesNotExists_disablesPlayButton() {
        VideoWidget widget = createWidget(promptWithAnswer(new StringData(FILE_PATH)));
        widget.setBinaryData(new File("newFile.mp4"));
        assertThat(widget.playButton.isEnabled(), is(false));
    }

    @Test
    public void setData_whenFileExists_updatesWidgetAnswer() {
        VideoWidget widget = createWidget(promptWithAnswer(new StringData(FILE_PATH)));
        widget.setBinaryData(mockedFile);
        assertThat(widget.getAnswer().getDisplayText(), equalTo("newFile.mp4"));
    }

    @Test
    public void setData_whenFileExists_enablesPlayButton() {
        VideoWidget widget = createWidget(promptWithAnswer(null));
        widget.setBinaryData(mockedFile);
        assertThat(widget.playButton.isEnabled(), is(true));
    }

    @Test
    public void setData_whenFileExists_callsValueChangeListener() {
        VideoWidget widget = createWidget(promptWithAnswer(new StringData(FILE_PATH)));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.setBinaryData(mockedFile);

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    public VideoWidget createWidget(FormEntryPrompt prompt) {
        return new VideoWidget(widgetActivity, new QuestionDetails(prompt, "formAnalyticsID"),
                fileUtil, mediaUtil, mediaManagerListener, waitingForDataRegistry, cameraUtilsProvider);
    }
}