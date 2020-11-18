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
import org.odk.collect.android.utilities.CameraUtils;
import org.odk.collect.android.utilities.ContentUriProvider;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowToast;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
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

    private final FakeQuestionMediaManager fakeQuestionMediaManager = new FakeQuestionMediaManager();
    private final FakeWaitingForDataRegistry waitingForDataRegistry = new FakeWaitingForDataRegistry();
    private final FakePermissionUtils permissionUtils = new FakePermissionUtils();

    private TestScreenContextActivity widgetActivity;
    private ShadowActivity shadowActivity;
    private CameraUtils cameraUtils;
    private FormIndex formIndex;
    private ActivityAvailability activityAvailability;
    private ContentUriProvider contentUriProvider;

    @Before
    public void setUp() {
        widgetActivity = widgetTestActivity();
        shadowActivity = shadowOf(widgetActivity);

        cameraUtils = mock(CameraUtils.class);
        activityAvailability = mock(ActivityAvailability.class);
        contentUriProvider = mock(ContentUriProvider.class);
        formIndex = mock(FormIndex.class);

        permissionUtils.setPermissionGranted(true);

        when(activityAvailability.isActivityAvailable(ArgumentMatchers.any())).thenReturn(true);
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
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        VideoWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.getAnswer(), nullValue());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        VideoWidget widget = createWidget(promptWithAnswer(new StringData("blah.mp4")));
        assertThat(widget.getAnswer().getDisplayText(), is("blah.mp4"));
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_playButtonIsDisabled() {
        VideoWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.binding.playVideo.isEnabled(), is(false));
    }

    @Test
    public void whenWidgetIsNew_chooseVideoButtonIsNotShown() {
        FormEntryPrompt prompt = promptWithReadOnly();
        when(prompt.getAppearanceHint()).thenReturn(WidgetAppearanceUtils.NEW);
        VideoWidget widget = createWidget(prompt);

        assertThat(widget.binding.chooseVideo.getVisibility(), is(View.GONE));
    }

    @Test
    public void usingSelfieAppearance_hidesChooseButton() {
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
        when(cameraUtils.isFrontCameraAvailable()).thenReturn(false);
        when(prompt.getAppearanceHint()).thenReturn(WidgetAppearanceUtils.SELFIE);
        VideoWidget widget = createWidget(prompt);

        assertThat(widget.binding.captureVideo.isEnabled(), is(false));
        assertThat(ShadowToast.getTextOfLatestToast(), is(widget.getContext().getString(R.string.error_front_camera_unavailable)));
    }

    @Test
    public void usingMediaAppearanceNewFront_disablesCaptureButton_whenFrontCameraIsNotAvailable() {
        FormEntryPrompt prompt = promptWithReadOnly();
        when(cameraUtils.isFrontCameraAvailable()).thenReturn(false);
        when(prompt.getAppearanceHint()).thenReturn(WidgetAppearanceUtils.NEW_FRONT);
        VideoWidget widget = createWidget(prompt);

        assertThat(widget.binding.captureVideo.isEnabled(), is(false));
        assertThat(ShadowToast.getTextOfLatestToast(), is(widget.getContext().getString(R.string.error_front_camera_unavailable)));
    }

    @Test
    public void clearAnswer_removesAnswerAndDisablesPlayButton() {
        VideoWidget widget = createWidget(promptWithAnswer(new StringData("blah.mp4")));
        widget.clearAnswer();
        assertThat(widget.getAnswer(), nullValue());
        assertThat(widget.binding.playVideo.isEnabled(), is(false));
    }

    @Test
    public void clearAnswer_setsFileAsideForDeleting() {
        FormEntryPrompt prompt = promptWithAnswer(new StringData("blah.mp4"));
        when(prompt.getIndex()).thenReturn(formIndex);

        VideoWidget widget = createWidget(prompt);
        widget.clearAnswer();
        assertThat(fakeQuestionMediaManager.originalFiles.get("questionIndex"),
                is(fakeQuestionMediaManager.getAnswerFile("blah.mp4").toString()));
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        VideoWidget widget = createWidget(promptWithAnswer(new StringData("blah.mp4")));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void setData_whenFileExists_replacesOriginalFileWithNewFile() throws Exception {
        FormEntryPrompt prompt = promptWithAnswer(new StringData("blah.mp4"));
        when(prompt.getIndex()).thenReturn(formIndex);
        VideoWidget widget = createWidget(prompt);

        File newFile = File.createTempFile("newFIle", ".mp4", fakeQuestionMediaManager.getDir());
        widget.setData(newFile.getName());

        assertThat(fakeQuestionMediaManager.recentFiles.get("questionIndex"), equalTo(newFile.getAbsolutePath()));
    }

    @Test
    public void setData_whenPromptHasDifferentAudioFile_deletesOriginalAnswer() throws Exception {
        FormEntryPrompt prompt = promptWithAnswer(new StringData("blah.mp4"));
        when(prompt.getIndex()).thenReturn(formIndex);

        VideoWidget widget = createWidget(prompt);
        File newFile = File.createTempFile("newFIle", ".mp4", fakeQuestionMediaManager.getDir());
        widget.setData(newFile.getName());

        assertThat(fakeQuestionMediaManager.originalFiles.get("questionIndex"), equalTo(fakeQuestionMediaManager.getAnswerFile("blah.mp4").toString()));
    }

    @Test
    public void setData_whenPromptDoesNotHaveAnswer_doesNotDeleteOriginalAnswer() throws Exception {
        VideoWidget widget = createWidget(promptWithAnswer(null));
        File newFile = File.createTempFile("newFIle", ".mp4", fakeQuestionMediaManager.getDir());
        widget.setData(newFile.getName());

        assertThat(fakeQuestionMediaManager.originalFiles.isEmpty(), equalTo(true));
    }

    @Test
    public void setData_whenPromptHasSameAnswer_doesNotDeleteOriginalAnswer() throws Exception {
        File newFile = File.createTempFile("newFIle", ".mp4", fakeQuestionMediaManager.getDir());
        VideoWidget widget = createWidget(promptWithAnswer(new StringData(newFile.getName())));
        widget.setData(newFile.getName());
        assertThat(fakeQuestionMediaManager.originalFiles.isEmpty(), equalTo(true));
    }

    @Test
    public void setData_whenFileDoesNotExist_doesNotUpdateWidgetAnswer() {
        VideoWidget widget = createWidget(promptWithAnswer(new StringData("blah.mp4")));
        widget.setData("newFile.mp4");
        assertThat(widget.getAnswer().getDisplayText(), equalTo("blah.mp4"));
    }

    @Test
    public void setData_whenFileExists_updatesWidgetAnswer() throws Exception {
        VideoWidget widget = createWidget(promptWithAnswer(new StringData("blah.mp4")));
        File newFile = File.createTempFile("newFIle", ".mp4", fakeQuestionMediaManager.getDir());
        widget.setData(newFile.getName());
        assertThat(widget.getAnswer().getDisplayText(), equalTo(newFile.getName()));
    }

    @Test
    public void setData_supportsFilesAsWellAsStrings() throws Exception {
        VideoWidget widget = createWidget(promptWithAnswer(null));

        File newFile = File.createTempFile("newFIle", ".mp4", fakeQuestionMediaManager.getDir());
        widget.setData(newFile);
        assertThat(widget.getAnswer().getDisplayText(), equalTo(newFile.getName()));
    }

    @Test
    public void setData_whenFileExists_callsValueChangeListener() throws Exception {
        VideoWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);

        File newFile = File.createTempFile("newFIle", ".mp4", fakeQuestionMediaManager.getDir());
        widget.setData(newFile.getName());
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void setData_whenFileExists_enablesPlayButton() throws Exception {
        VideoWidget widget = createWidget(promptWithAnswer(null));
        File newFile = File.createTempFile("newFIle", ".mp4", fakeQuestionMediaManager.getDir());
        widget.setData(newFile.getName());
        assertThat(widget.binding.playVideo.isEnabled(), is(true));
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
        assertThat(intentForResult.requestCode, is(ApplicationConstants.RequestCodes.VIDEO_CAPTURE));

        assertThat(waitingForDataRegistry.waiting.contains(formIndex), is(true));
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
        assertThat(intentForResult.requestCode, is(ApplicationConstants.RequestCodes.VIDEO_CAPTURE));

        assertThat(waitingForDataRegistry.waiting.contains(formIndex), is(true));
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
        assertThat(intentForResult.requestCode, is(ApplicationConstants.RequestCodes.VIDEO_CAPTURE));

        assertThat(waitingForDataRegistry.waiting.contains(formIndex), is(true));
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
        assertThat(intentForResult.requestCode, is(ApplicationConstants.RequestCodes.VIDEO_CHOOSER));

        assertThat(waitingForDataRegistry.waiting.contains(formIndex), is(true));
    }

    @Test
    public void clickingPlayVideoButton_launchesCorrectIntent() throws IOException {
        File file = File.createTempFile("blah", ".mp4", fakeQuestionMediaManager.getDir());

        VideoWidget widget = createWidget(promptWithAnswer(new StringData(file.getName())));
        when(contentUriProvider.getUriForFile(widgetActivity,
                BuildConfig.APPLICATION_ID + ".provider",
                fakeQuestionMediaManager.getAnswerFile(file.getName()))).thenReturn(Uri.parse("content://blah"));
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
        VideoWidget widget = createWidget(promptWithAnswer(new StringData("blah.mp4")));

        widget.setPermissionUtils(permissionUtils);
        widget.binding.playVideo.performClick();

        assertThat(shadowActivity.getNextStartedActivity(), nullValue());
        assertThat(ShadowToast.getTextOfLatestToast(), is(widget.getContext().getString(R.string.activity_not_found,
                widget.getContext().getString(R.string.view_video))));
    }

    public VideoWidget createWidget(FormEntryPrompt prompt) {
        return new VideoWidget(widgetActivity, new QuestionDetails(prompt, "formAnalyticsID"),
                waitingForDataRegistry, cameraUtils, fakeQuestionMediaManager, activityAvailability, contentUriProvider);
    }
}