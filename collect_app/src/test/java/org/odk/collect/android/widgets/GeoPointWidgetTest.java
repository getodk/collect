package org.odk.collect.android.widgets;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.GeoPointActivity;
import org.odk.collect.android.fakes.FakePermissionUtils;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;
import org.robolectric.RobolectricTestRunner;

import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.GeoPointMapWidget.ACCURACY_THRESHOLD;
import static org.odk.collect.android.widgets.GeoPointMapWidget.DEFAULT_LOCATION_ACCURACY;
import static org.odk.collect.android.widgets.GeoPointMapWidget.LOCATION;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;
import static org.robolectric.Shadows.shadowOf;

/**
 * @author James Knight
 */

@RunWith(RobolectricTestRunner.class)
public class GeoPointWidgetTest {

    private final FakePermissionUtils permissionUtils = new FakePermissionUtils();
    private final GeoPointData answer = new GeoPointData(getRandomDoubleArray());

    private QuestionDef questionDef;
    private WaitingForDataRegistry waitingForDataRegistry;

    @Before
    public void setup() {
        questionDef = mock(QuestionDef.class);
        waitingForDataRegistry = mock(WaitingForDataRegistry.class);
        when(questionDef.getAdditionalAttribute(anyString(), anyString())).thenReturn(null);
    }

    @Test
    public void usingReadOnlyOption_doesNotShowTheGeoButton() {
        GeoPointWidget widget = createWidget(promptWithReadOnly());
        assertThat(widget.binding.simpleButton.getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void getAnswer_whenPromptAnswerDoesNotHaveAnswer_returnsNull() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.getAnswer(), equalTo(null));
        assertThat(widget.binding.geoAnswerText.getText(), equalTo(""));
    }

    @Test
    public void getAnswer_whenPromptAnswerDoesNotHaveConvertibleString_returnsNull() {
        GeoPointWidget widget = createWidget(promptWithAnswer(new StringData("blah")));
        assertThat(widget.getAnswer(), equalTo(null));
        assertThat(widget.binding.geoAnswerText.getText(), equalTo(""));
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        GeoPointWidget widget = createWidget(promptWithAnswer(answer));
        assertThat(widget.getAnswer().getDisplayText(), equalTo(answer.getDisplayText()));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        GeoPointWidget widget = createWidget(promptWithAnswer(answer));
        widget.clearAnswer();
        assertThat(widget.getAnswer(), nullValue());
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingButtonForLong_callsLongClickListener() {
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        widget.setOnLongClickListener(listener);
        widget.binding.simpleButton.performLongClick();

        verify(listener).onLongClick(widget.binding.simpleButton);
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_textViewDisplaysEmptyString() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.binding.geoAnswerText.getText().toString(), equalTo(""));
    }

    @Test
    public void whenPromptAnswerDoesNotHaveConvertibleString_textViewDisplaysEmptyString() {
        GeoPointWidget widget = createWidget(promptWithAnswer(new StringData("blah")));
        assertThat(widget.binding.geoAnswerText.getText().toString(), equalTo(""));
    }

    @Test
    public void whenPromptHasAnswer_textViewDisplaysAnswer() {
        GeoPointWidget widget = createWidget(promptWithAnswer(answer));
        String[] parts = answer.getDisplayText().split(" ");
        assertThat(widget.binding.geoAnswerText.getText().toString(), equalTo(widget.getContext().getString(
                R.string.gps_result,
                GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(widget.getContext(), Double.parseDouble(parts[0]), "lat"),
                GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(widget.getContext(), Double.parseDouble(parts[1]), "lon"),
                GeoWidgetUtils.truncateDouble(parts[2]),
                GeoWidgetUtils.truncateDouble(parts[3])
        )));
    }

    @Test
    public void whenPromptDoesNotHaveHasAnswer_buttonShowsCorrectText() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.binding.simpleButton.getText(), equalTo(widget.getContext().getString(R.string.get_point)));
    }

    @Test
    public void whenPromptHasAnswer_buttonShowsCorrectText() {
        GeoPointWidget widget = createWidget(promptWithAnswer(answer));
        assertThat(widget.binding.simpleButton.getText(), equalTo(widget.getContext().getString(R.string.change_location)));
    }

    @Test
    public void whenPermissionIsNotGranted_buttonClickShouldNotLaunchAnyIntent() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        stubLocationPermissions(widget, false);
        widget.binding.simpleButton.performClick();
        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();

        assertNull(startedIntent);
    }

    @Test
    public void whenPermissionIsGranted_buttonClickLaunchesIntentAndWaitsForLocationData() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        GeoPointWidget widget = createWidget(prompt);
        stubLocationPermissions(widget, true);
        widget.binding.simpleButton.performClick();

        verify(waitingForDataRegistry).waitForData(prompt.getIndex());
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_buttonShouldLaunchCorrectIntent() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        stubLocationPermissions(widget, true);
        widget.binding.simpleButton.performClick();

        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();
        Bundle bundle = startedIntent.getExtras();

        assertThat(startedIntent.getComponent(), equalTo(new ComponentName(widgetTestActivity(), GeoPointActivity.class)));
        assertBundleArgumentEquals(bundle, null, DEFAULT_LOCATION_ACCURACY);
    }

    @Test
    public void whenPromptHasAnswerAndAccuracyThresholdValue_buttonShouldLaunchCorrectIntent() {
        when(questionDef.getAdditionalAttribute(null, ACCURACY_THRESHOLD)).thenReturn("2.0");

        GeoPointWidget widget = createWidget(promptWithAnswer(answer));
        stubLocationPermissions(widget, true);
        widget.binding.simpleButton.performClick();

        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();
        Bundle bundle = startedIntent.getExtras();

        assertThat(startedIntent.getComponent(), equalTo(new ComponentName(widgetTestActivity(), GeoPointActivity.class)));
        assertBundleArgumentEquals(bundle, GeoWidgetUtils.getLocationParamsFromStringAnswer(answer.getDisplayText()), 2.0);
    }

    private GeoPointWidget createWidget(FormEntryPrompt prompt) {
        return new GeoPointWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"), questionDef,  waitingForDataRegistry);
    }

    private void assertBundleArgumentEquals(Bundle bundle, double[] location, double accuracyThreshold) {
        assertThat(bundle.getDoubleArray(LOCATION), equalTo(location));
        assertThat(bundle.getDouble(ACCURACY_THRESHOLD), equalTo(accuracyThreshold));
    }

    protected void stubLocationPermissions(GeoPointWidget widget, boolean isGranted) {
        permissionUtils.setPermissionGranted(isGranted);
        widget.setPermissionUtils(permissionUtils);
    }

    private double[] getRandomDoubleArray() {
        Random random = new Random();
        return new double[]{
                random.nextDouble(),
                random.nextDouble(),
                random.nextDouble(),
                random.nextDouble()
        };
    }
}