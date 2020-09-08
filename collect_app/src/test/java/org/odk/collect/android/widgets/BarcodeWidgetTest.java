package org.odk.collect.android.widgets;

import android.content.ComponentName;
import android.content.Intent;
import android.view.View;

import com.google.zxing.client.android.Intents;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.ScannerWithFlashlightActivity;
import org.odk.collect.android.fakes.FakePermissionUtils;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.support.TestScreenContextActivity;
import org.odk.collect.android.utilities.CameraUtils;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowToast;

import static org.hamcrest.MatcherAssert.assertThat;
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
public class BarcodeWidgetTest {
    private final FakeWaitingForDataRegistry waitingForDataRegistry = new FakeWaitingForDataRegistry();
    private final FakePermissionUtils permissionUtils = new FakePermissionUtils();

    private TestScreenContextActivity widgetTestActivity;
    private ShadowActivity shadowActivity;
    private CameraUtils cameraUtils;
    private View.OnLongClickListener listener;
    private FormIndex formIndex;

    @Before
    public void setUp() {
        widgetTestActivity = widgetTestActivity();
        shadowActivity = shadowOf(widgetTestActivity);

        cameraUtils = mock(CameraUtils.class);
        listener = mock(View.OnLongClickListener.class);
        formIndex = mock(FormIndex.class);
        permissionUtils.setPermissionGranted(true);
    }

    @Test
    public void usingReaDOnly_shouldHideBarcodeButton() {
        assertThat(createWidget(promptWithReadOnly()).getBarcodeButton.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenPromptHasAnswer_replaceBarcodeButtonIsDisplayed() {
        BarcodeWidget widget = createWidget(promptWithAnswer(new StringData("blah")));
        assertThat(widget.getBarcodeButton.getText().toString(), is(widget.getContext().getString(R.string.replace_barcode)));
    }

    @Test
    public void whenPromptHasAnswer_answerTextViewShowsCorrectAnswer() {
        BarcodeWidget widget = createWidget(promptWithAnswer(new StringData("blah")));
        assertThat(widget.stringAnswer.getText().toString(), is("blah"));
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        BarcodeWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.getAnswer(), nullValue());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsCorrectAnswer() {
        BarcodeWidget widget = createWidget(promptWithAnswer(new StringData("blah")));
        assertThat(widget.getAnswer().getDisplayText(), is("blah"));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        BarcodeWidget widget = createWidget(promptWithAnswer(new StringData("blah")));
        widget.clearAnswer();

        assertThat(widget.stringAnswer.getText().toString(), is(""));
        assertThat(widget.getBarcodeButton.getText().toString(), is(widget.getContext().getString(R.string.get_barcode)));
    }

    @Test
    public void clearAnswer_callsValueChangeListener() {
        BarcodeWidget widget = createWidget(promptWithAnswer(new StringData("blah")));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void setData_updatesWidgetAnswer_afterStrippingInvalidCharacters() {
        BarcodeWidget widget = createWidget(promptWithAnswer(null));
        widget.setBinaryData("\ud800blah\b");
        assertThat(widget.stringAnswer.getText().toString(), is("blah"));
    }

    @Test
    public void setData_callsValueChangeListener() {
        BarcodeWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.setBinaryData("blah");

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingButtonAndAnswerTextViewForLong_callsLongClickListener() {
        BarcodeWidget widget = createWidget(promptWithAnswer(null));
        widget.setOnLongClickListener(listener);
        widget.getBarcodeButton.performLongClick();
        widget.stringAnswer.performLongClick();

        verify(listener).onLongClick(widget.getBarcodeButton);
        verify(listener).onLongClick(widget.stringAnswer);
    }

    @Test
    public void clickingBarcodeButton_whenPermissionIsNotGranted_doesNotLaunchAnyIntent() {
        BarcodeWidget widget = createWidget(promptWithAnswer(null));
        permissionUtils.setPermissionGranted(false);
        widget.setPermissionUtils(permissionUtils);
        widget.getBarcodeButton.performClick();

        assertThat(shadowActivity.getNextStartedActivity(), nullValue());
        assertThat(waitingForDataRegistry.waiting.isEmpty(), is(true));
    }

    @Test
    public void clickingBarcodeButton_whenPermissionGranted_setsWidgetWaitingForData() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        when(prompt.getIndex()).thenReturn(formIndex);

        BarcodeWidget widget = createWidget(prompt);
        widget.setPermissionUtils(permissionUtils);
        widget.getBarcodeButton.performClick();

        assertThat(waitingForDataRegistry.waiting.contains(formIndex), is(true));
    }

    @Test
    public void clickingBarcodeButton_whenPermissionGranted_launchesCorrectIntent() {
        BarcodeWidget widget = createWidget(promptWithAnswer(null));
        widget.setPermissionUtils(permissionUtils);
        widget.getBarcodeButton.performClick();
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertThat(startedIntent.getComponent(), is(new ComponentName(widgetTestActivity, ScannerWithFlashlightActivity.class)));
        assertThat(startedIntent.getAction(), is(Intents.Scan.ACTION));
    }

    @Test
    public void clickingBarcodeButton_whenFrontCameraIsUnavailable_showsFrontCameraUnavailableToast() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        when(prompt.getAppearanceHint()).thenReturn(WidgetAppearanceUtils.FRONT);
        when(cameraUtils.isFrontCameraAvailable()).thenReturn(false);

        BarcodeWidget widget = createWidget(prompt);
        widget.setPermissionUtils(permissionUtils);
        widget.getBarcodeButton.performClick();

        assertThat(ShadowToast.getTextOfLatestToast(), is(widget.getContext().getString(R.string.error_front_camera_unavailable)));
    }

    @Test
    public void clickingBarcodeButton_whenFrontCameraIsAvailable_launchesCorrectIntent() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        when(prompt.getAppearanceHint()).thenReturn(WidgetAppearanceUtils.FRONT);
        when(cameraUtils.isFrontCameraAvailable()).thenReturn(true);

        BarcodeWidget widget = createWidget(prompt);
        widget.setPermissionUtils(permissionUtils);
        widget.getBarcodeButton.performClick();
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertThat(startedIntent.getBooleanExtra(WidgetAppearanceUtils.FRONT, false), is(true));
    }

    public BarcodeWidget createWidget(FormEntryPrompt prompt) {
        return new BarcodeWidget(widgetTestActivity, new QuestionDetails(prompt, "formAnalyticsID"), waitingForDataRegistry, cameraUtils);
    }
}
