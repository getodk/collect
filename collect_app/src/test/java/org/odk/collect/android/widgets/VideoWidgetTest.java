package org.odk.collect.android.widgets;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;

import org.odk.collect.android.activities.CaptureSelfieVideoActivity;
import org.odk.collect.android.fakes.FakePermissionUtils;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.support.TestScreenContextActivity;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.CameraUtilsProvider;
import org.odk.collect.android.utilities.ContentUriFetcher;
import org.odk.collect.android.utilities.FileUtil;
import org.odk.collect.android.utilities.MediaUtil;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
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
    private QuestionMediaManager questionMediaManager;
    private FormIndex formIndex;
    private File mockedFile;
    private FakePermissionUtils permissionUtils;
    private ActivityAvailability activityAvailability;
    private ContentUriFetcher contentUriFetcher;

    @Before
    public void setUp() {
        widgetActivity = widgetTestActivity();
        shadowActivity = shadowOf(widgetActivity);

        fileUtil = mock(FileUtil.class);
        mediaUtil = mock(MediaUtil.class);
        cameraUtilsProvider = mock(CameraUtilsProvider.class);
        questionMediaManager = mock(QuestionMediaManager.class);
        activityAvailability = mock(ActivityAvailability.class);
        contentUriFetcher = mock(ContentUriFetcher.class);
        formIndex = mock(FormIndex.class);
        mockedFile = mock(File.class);

        waitingForDataRegistry = new FakeWaitingForDataRegistry();
        permissionUtils = new FakePermissionUtils();
        permissionUtils.setPermissionGranted(true);

        when(activityAvailability.isActivityAvailable(ArgumentMatchers.any())).thenReturn(true);
        when(mockedFile.exists()).thenReturn(true);
        when(mockedFile.getName()).thenReturn("newFile.mp4");
        when(mockedFile.getAbsolutePath()).thenReturn("newFilePath");
        when(formIndex.toString()).thenReturn("questionIndex");
    }

    @Test
    public void usingReadOnlyOption_showsOnlyPlayButton() {
        VideoWidget widget = createWidget(promptWithReadOnly());

        assertThat(widget.binding.captureVideo.getVisibility(), is(View.GONE));
        assertThat(widget.binding.chooseVideo.getVisibility(), is(View.GONE));
        assertThat(widget.binding.playVideo.getVisibility(), is(View.VISIBLE));
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
        assertThat(widget.binding.playVideo.isEnabled(), is(false));
    }

    @Test
    public void whenWidgetIsNew_chooseSoundButtonIsNotShown() {
        FormEntryPrompt prompt = promptWithReadOnly();
        when(prompt.getAppearanceHint()).thenReturn(WidgetAppearanceUtils.NEW);
        VideoWidget widget = createWidget(prompt);

        assertThat(widget.binding.chooseVideo.getVisibility(), is(View.GONE));
    }

    @Test
    public void usingSelfieWidget_hidesChooseButton() {
        FormEntryPrompt prompt = promptWithReadOnly();
        when(prompt.getAppearanceHint()).thenReturn(WidgetAppearanceUtils.SELFIE);
        VideoWidget widget = createWidget(prompt);

        assertThat(widget.binding.chooseVideo.getVisibility(), is(View.GONE));
    }

    @Test
    public void usingMediaAppearanceNewFront_hidesChooseButton() {
        FormEntryPrompt prompt = promptWithReadOnly();
        when(prompt.getAppearanceHint()).thenReturn(WidgetAppearanceUtils.NEW_FRONT);
        VideoWidget widget = createWidget(prompt);

        assertThat(widget.binding.chooseVideo.getVisibility(), is(View.GONE));
    }

    @Test
    public void usingSelfieWidget_disablesCaptureButton_whenFrontCameraIsNotAvailable() {
        FormEntryPrompt prompt = promptWithReadOnly();
        when(cameraUtilsProvider.checkFrontCameraAvailability()).thenReturn(false);
        when(prompt.getAppearanceHint()).thenReturn(WidgetAppearanceUtils.SELFIE);
        VideoWidget widget = createWidget(prompt);

        assertThat(widget.binding.captureVideo.isEnabled(), is(false));
        assertThat(ShadowToast.getTextOfLatestToast(), is(widget.getContext().getString(R.string.error_front_camera_unavailable)));
    }

    @Test
    public void usingMediaAppearanceNewFront_disablesCaptureButton_whenFrontCameraIsNotAvailable() {
        FormEntryPrompt prompt = promptWithReadOnly();
        when(cameraUtilsProvider.checkFrontCameraAvailability()).thenReturn(false);
        when(prompt.getAppearanceHint()).thenReturn(WidgetAppearanceUtils.NEW_FRONT);
        VideoWidget widget = createWidget(prompt);

        assertThat(widget.binding.captureVideo.isEnabled(), is(false));
        assertThat(ShadowToast.getTextOfLatestToast(), is(widget.getContext().getString(R.string.error_front_camera_unavailable)));
    }

    @Test
    public void deleteFile_removesWidgetAnswer() {
        VideoWidget widget = createWidget(promptWithAnswer(new StringData(FILE_PATH)));
        widget.deleteFile();
        assertThat(widget.getAnswer(), nullValue());
    }

    @Test
    public void deleteFile_callsMarkOriginalFileOrDelete() {
        FormEntryPrompt prompt = promptWithAnswer(new StringData(FILE_PATH));
        when(prompt.getIndex()).thenReturn(formIndex);
        VideoWidget widget = createWidget(prompt);
        widget.deleteFile();

        verify(questionMediaManager).markOriginalFileOrDelete("questionIndex",
                widget.getInstanceFolder() + File.separator + FILE_PATH);
    }

    @Test
    public void clearAnswer_removesAnswerAndHidesPlayer() {
        VideoWidget widget = createWidget(promptWithAnswer(new StringData(FILE_PATH)));
        widget.clearAnswer();
        assertThat(widget.getAnswer(), nullValue());
    }

    @Test
    public void clearAnswer_callsMarkOriginalFileOrDelete() {
        FormEntryPrompt prompt = promptWithAnswer(new StringData(FILE_PATH));
        when(prompt.getIndex()).thenReturn(formIndex);
        VideoWidget widget = createWidget(prompt);
        widget.clearAnswer();

        verify(questionMediaManager).markOriginalFileOrDelete("questionIndex",
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

        when(mediaUtil.getPathFromUri(widget.getContext(), newFileUri, MediaStore.Audio.Media.DATA)).thenReturn(SOURCE_FILE_PATH);
        when(fileUtil.getFileAtPath(SOURCE_FILE_PATH)).thenReturn(sourceFile);
        when(fileUtil.getFileAtPath("null/null.mp4")).thenReturn(mockedFile);

        widget.setBinaryData(newFileUri);
        verify(fileUtil).copyFile(sourceFile, mockedFile);
    }

    @Test
    public void setData_whenFileExists_callsReplaceRecentFileForQuestion() {
        FormEntryPrompt prompt = promptWithAnswer(new StringData(FILE_PATH));
        when(prompt.getIndex()).thenReturn(formIndex);
        when(mockedFile.getAbsolutePath()).thenReturn("newFilePath/newFile.mp4");

        VideoWidget widget = createWidget(prompt);
        widget.setBinaryData(mockedFile);

        verify(questionMediaManager).replaceRecentFileForQuestion("questionIndex", "newFilePath/newFile.mp4");
    }

    @Test
    public void setData_whenPromptHasDifferentAnswer_deletesOriginalAnswer() {
        FormEntryPrompt prompt = promptWithAnswer(new StringData(FILE_PATH));
        when(prompt.getIndex()).thenReturn(formIndex);

        VideoWidget widget = createWidget(prompt);
        widget.setBinaryData(mockedFile);

        verify(questionMediaManager).markOriginalFileOrDelete("questionIndex",
                widget.getInstanceFolder() + File.separator + "blah.mp4");
    }

    @Test
    public void setData_whenPromptDoesNotHaveAnswer_doesNotDeleteOriginalAnswer() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        when(prompt.getIndex()).thenReturn(formIndex);

        VideoWidget widget = createWidget(prompt);
        widget.setBinaryData(mockedFile);

        verify(questionMediaManager, never()).markOriginalFileOrDelete("questionIndex",
                widget.getInstanceFolder() + File.separator + "blah.mp4");
    }

    @Test
    public void setData_whenPromptHasSameAnswer_doesNotDeleteOriginalAnswer() {
        FormEntryPrompt prompt = promptWithAnswer(new StringData("newFile.mp4"));
        when(prompt.getIndex()).thenReturn(formIndex);

        VideoWidget widget = createWidget(prompt);
        widget.setBinaryData(mockedFile);

        verify(questionMediaManager, never()).markOriginalFileOrDelete("questionIndex",
                widget.getInstanceFolder() + File.separator + "newFile.mp4");
    }

    @Test
    public void setData_whenFileDoesNotExists_doesNotChangeWidgetAnswer() {
        VideoWidget widget = createWidget(promptWithAnswer(new StringData(FILE_PATH)));
        widget.setBinaryData(new File("newFile.mp4"));
        assertThat(widget.getAnswer().getDisplayText(), is(FILE_PATH));
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
        assertThat(widget.binding.playVideo.isEnabled(), is(true));
    }

    @Test
    public void setData_whenFileExists_callsValueChangeListener() {
        VideoWidget widget = createWidget(promptWithAnswer(new StringData(FILE_PATH)));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.setBinaryData(mockedFile);

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingButtonsForLong_callsOnLongClickListeners() {
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);
        VideoWidget widget = createWidget(promptWithAnswer(null));
        widget.setOnLongClickListener(listener);

        widget.binding.captureVideo.performLongClick();
        widget.binding.chooseVideo.performLongClick();
        widget.binding.playVideo.performLongClick();

        verify(listener).onLongClick(widget.binding.captureVideo);
        verify(listener).onLongClick(widget.binding.chooseVideo);
        verify(listener).onLongClick(widget.binding.playVideo);
    }

    @Test
    public void clickingCaptureVideoButton_doesNotLaunchAnyIntent_whenPermissionIsNotGranted() {
        VideoWidget widget = createWidget(promptWithAnswer(null));
        widget.setPermissionUtils(permissionUtils);
        permissionUtils.setPermissionGranted(false);
        widget.binding.captureVideo.performClick();

        assertThat(shadowActivity.getNextStartedActivity(), nullValue());
    }

    @Test
    public void clickingCaptureVideoButton_doesNotLaunchAnyIntentAndCancelsWaitingForData_whenIntentIsNotAvailable() {
        when(activityAvailability.isActivityAvailable(ArgumentMatchers.any())).thenReturn(false);
        VideoWidget widget = createWidget(promptWithAnswer(null));
        widget.setPermissionUtils(permissionUtils);
        widget.binding.captureVideo.performClick();

        assertThat(shadowActivity.getNextStartedActivity(), nullValue());
        assertThat(ShadowToast.getTextOfLatestToast(), is(widget.getContext().getString(R.string.activity_not_found,
                widget.getContext().getString(R.string.capture_video))));
        assertThat(waitingForDataRegistry.waiting.isEmpty(), is(true));
    }

    @Test
    public void clickingCaptureVideoButton_launchesCaptureSelfieVideoActivityAndWaitsForData_inCaseOfSelfieWidget() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        when(prompt.getIndex()).thenReturn(formIndex);
        when(prompt.getAppearanceHint()).thenReturn(WidgetAppearanceUtils.SELFIE);

        VideoWidget widget = createWidget(prompt);
        widget.setPermissionUtils(permissionUtils);
        widget.binding.captureVideo.performClick();

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertThat(startedIntent.getComponent().getClassName(), is(CaptureSelfieVideoActivity.class.getName()));

        ShadowActivity.IntentForResult intentForResult = shadowActivity.getNextStartedActivityForResult();
        assertThat(intentForResult.requestCode, equalTo(ApplicationConstants.RequestCodes.VIDEO_CAPTURE));

        assertThat(waitingForDataRegistry.waiting.contains(formIndex), equalTo(true));
    }

    @Test
    public void clickingCaptureVideoButton_launchesCaptureSelfieVideoActivityAndWaitsForData_whenUsingMediaAppearanceNewFront() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        when(prompt.getIndex()).thenReturn(formIndex);
        when(prompt.getAppearanceHint()).thenReturn(WidgetAppearanceUtils.NEW_FRONT);

        VideoWidget widget = createWidget(prompt);
        widget.setPermissionUtils(permissionUtils);
        widget.binding.captureVideo.performClick();

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertThat(startedIntent.getComponent().getClassName(), is(CaptureSelfieVideoActivity.class.getName()));

        ShadowActivity.IntentForResult intentForResult = shadowActivity.getNextStartedActivityForResult();
        assertThat(intentForResult.requestCode, equalTo(ApplicationConstants.RequestCodes.VIDEO_CAPTURE));

        assertThat(waitingForDataRegistry.waiting.contains(formIndex), equalTo(true));
    }

    @Test
    public void clickingCaptureVideoButton_launchesVideoCaptureIntentAndWaitsForData() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        when(prompt.getIndex()).thenReturn(formIndex);

        VideoWidget widget = createWidget(prompt);
        widget.setPermissionUtils(permissionUtils);
        widget.binding.captureVideo.performClick();

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertThat(startedIntent.getAction(), is(MediaStore.ACTION_VIDEO_CAPTURE));
        assertThat(startedIntent.getStringExtra(MediaStore.EXTRA_OUTPUT), is(MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString()));

        ShadowActivity.IntentForResult intentForResult = shadowActivity.getNextStartedActivityForResult();
        assertThat(intentForResult.requestCode, equalTo(ApplicationConstants.RequestCodes.VIDEO_CAPTURE));

        assertThat(waitingForDataRegistry.waiting.contains(formIndex), equalTo(true));
    }

    @Test
    public void clickingChooseVideoButton_doesNotLaunchAnyIntentAndCancelsWaitingForData_whenIntentIsNotAvailable() {
        when(activityAvailability.isActivityAvailable(ArgumentMatchers.any())).thenReturn(false);
        VideoWidget widget = createWidget(promptWithAnswer(null));
        widget.setPermissionUtils(permissionUtils);
        widget.binding.chooseVideo.performClick();

        assertThat(shadowActivity.getNextStartedActivity(), nullValue());
        assertThat(ShadowToast.getTextOfLatestToast(), is(widget.getContext().getString(R.string.activity_not_found,
                widget.getContext().getString(R.string.choose_video))));
        assertThat(waitingForDataRegistry.waiting.isEmpty(), is(true));
    }

    @Test
    public void clickingChooseVideoButton_launchesGetContentIntentAndWaitsForData() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        when(prompt.getIndex()).thenReturn(formIndex);

        VideoWidget widget = createWidget(prompt);
        widget.setPermissionUtils(permissionUtils);
        widget.binding.chooseVideo.performClick();

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertThat(startedIntent.getAction(), is(Intent.ACTION_GET_CONTENT));
        assertThat(startedIntent.getType(), is("video/*"));

        ShadowActivity.IntentForResult intentForResult = shadowActivity.getNextStartedActivityForResult();
        assertThat(intentForResult.requestCode, equalTo(ApplicationConstants.RequestCodes.VIDEO_CHOOSER));

        assertThat(waitingForDataRegistry.waiting.contains(formIndex), equalTo(true));
    }

    @Test
    public void clickingPlayVideoButton_launchesCorrectIntent() {
        VideoWidget widget = createWidget(promptWithAnswer(new StringData(FILE_PATH)));
        when(contentUriFetcher.getUri(widgetActivity,
                BuildConfig.APPLICATION_ID + ".provider",
                new File(widget.getInstanceFolder() + File.separator + FILE_PATH))).thenReturn(Uri.parse("content://blah"));
        widget.setPermissionUtils(permissionUtils);

        widget.binding.playVideo.performClick();
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertThat(startedIntent.getAction(), is(Intent.ACTION_VIEW));
        assertThat(startedIntent.getFlags(), is(Intent.FLAG_GRANT_READ_URI_PERMISSION));
        assertThat(startedIntent.getData(), is(Uri.parse("content://blah")));
        assertThat(startedIntent.getType(), is("video/*"));
    }

    @Test
    public void clickingPlayVideoButton_doesNotLaunchAnyIntentAndShowsActivityNotFoundToast_whenIntentIsNotAvailable() {
        when(activityAvailability.isActivityAvailable(ArgumentMatchers.any())).thenReturn(false);
        VideoWidget widget = createWidget(promptWithAnswer(null));

        widget.setPermissionUtils(permissionUtils);
        widget.binding.playVideo.performClick();

        assertThat(shadowActivity.getNextStartedActivity(), nullValue());
        assertThat(ShadowToast.getTextOfLatestToast(), is(widget.getContext().getString(R.string.activity_not_found,
                widget.getContext().getString(R.string.view_video))));
    }

    public VideoWidget createWidget(FormEntryPrompt prompt) {
        return new VideoWidget(widgetActivity, new QuestionDetails(prompt, "formAnalyticsID"),
                fileUtil, mediaUtil, waitingForDataRegistry, cameraUtilsProvider, questionMediaManager, activityAvailability, contentUriFetcher);
    }
}