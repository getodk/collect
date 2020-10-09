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
import org.odk.collect.android.activities.GeoPointMapActivity;
import org.odk.collect.android.fakes.FakePermissionUtils;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.support.GeoWidgetHelpers;
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers;
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;
import org.robolectric.RobolectricTestRunner;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.MAPS;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.PLACEMENT_MAP;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.getRandomDoubleArray;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAppearanceAndReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;
import static org.odk.collect.android.widgets.utilities.GeoWidgetUtils.ACCURACY_THRESHOLD;
import static org.odk.collect.android.widgets.utilities.GeoWidgetUtils.DEFAULT_LOCATION_ACCURACY;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class GeoPointMapWidgetTest {

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
    public void getAnswer_returnsPromptAnswer() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(answer));
        assertEquals(widget.getAnswer().getDisplayText(),
                GeoWidgetUtils.getAnswer(promptWithAnswer(answer).getAnswerText()).getDisplayText());
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(answer));
        widget.clearAnswer();
        assertNull(widget.getAnswer());
    }

    @Test
    public void clickingButtonForLong_callsLongClickListener() {
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);
        GeoPointMapWidget widget = createWidget(promptWithAnswer(null));
        widget.setOnLongClickListener(listener);
        widget.binding.simpleButton.performLongClick();

        verify(listener).onLongClick(widget.binding.simpleButton);
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_textViewDisplaysEmptyString() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(null));
        assertEquals(widget.binding.geoAnswerText.getText().toString(), "");
    }

    @Test
    public void whenPromptAnswerDoesNotHaveConvertibleString_textViewDisplaysEmptyString() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(new StringData("blah")));
        assertEquals(widget.binding.geoAnswerText.getText().toString(), "");
    }

    @Test
    public void whenPromptHasAnswer_textViewDisplaysAnswer() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(answer));
        String[] parts = answer.getDisplayText().split(" ");
        assertEquals(widget.binding.geoAnswerText.getText().toString(), widget.getContext().getString(
                R.string.gps_result,
                GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(widget.getContext(), Double.parseDouble(parts[0]), "lat"),
                GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(widget.getContext(), Double.parseDouble(parts[1]), "lon"),
                GeoWidgetUtils.truncateDouble(parts[2]),
                GeoWidgetUtils.truncateDouble(parts[3])
        ));
    }

    @Test
    public void whenPromptIsReadOnlyAndDoesNotHaveAnswer_buttonIsNotDisplayed() {
        GeoPointMapWidget widget = createWidget(promptWithReadOnly());
        assertEquals(widget.binding.simpleButton.getVisibility(), GONE);
    }

    @Test
    public void whenPromptIsReadOnlyAndHasAnswer_buttonShowsCorrectText() {
        GeoPointMapWidget widget = createWidget(promptWithReadOnlyAndAnswer(answer));

        assertEquals(widget.binding.simpleButton.getVisibility(), VISIBLE);
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.geopoint_view_read_only));
    }

    @Test
    public void whenPromptDoesNotHaveHasAnswer_buttonShowsCorrectText() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(null));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_point));
    }

    @Test
    public void whenPromptHasAnswer_buttonShowsCorrectText() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(answer));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.view_change_location));
    }

    @Test
    public void whenPermissionIsNotGranted_buttonClickShouldNotLaunchAnyIntent() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(null));
        QuestionWidgetHelpers.stubLocationPermissions(permissionUtils, widget, false);
        widget.binding.simpleButton.performClick();
        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();

        assertNull(startedIntent);
    }

    @Test
    public void whenPermissionIsGranted_buttonClickLaunchesIntentAndWaitsForLocationData() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        GeoPointMapWidget widget = createWidget(prompt);
        QuestionWidgetHelpers.stubLocationPermissions(permissionUtils, widget, true);
        widget.binding.simpleButton.performClick();

        verify(waitingForDataRegistry).waitForData(prompt.getIndex());
    }

    @Test
    public void whenPromptIsReadOnly_buttonShouldLaunchCorrectIntent() {
        GeoPointMapWidget widget = createWidget(promptWithReadOnly());
        QuestionWidgetHelpers.stubLocationPermissions(permissionUtils, widget, true);
        widget.binding.simpleButton.performClick();

        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();
        Bundle bundle = startedIntent.getExtras();

        assertEquals(startedIntent.getComponent(), new ComponentName(widgetTestActivity(), GeoPointMapActivity.class));
        GeoWidgetHelpers.assertGroPointBundleArgumentEquals(bundle, null, DEFAULT_LOCATION_ACCURACY, true, true);
    }

    @Test
    public void whenPromptHasAnswerAndAccuracyThresholdValue_buttonShouldLaunchCorrectIntent() {
        when(questionDef.getAdditionalAttribute(null, ACCURACY_THRESHOLD)).thenReturn("2.0");

        GeoPointMapWidget widget = createWidget(promptWithAnswer(answer));
        QuestionWidgetHelpers.stubLocationPermissions(permissionUtils, widget, true);
        widget.binding.simpleButton.performClick();

        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();
        Bundle bundle = startedIntent.getExtras();

        assertEquals(startedIntent.getComponent(), new ComponentName(widgetTestActivity(), GeoPointMapActivity.class));
        GeoWidgetHelpers.assertGroPointBundleArgumentEquals(bundle, GeoWidgetUtils.getLocationParamsFromStringAnswer(
                answer.getDisplayText()), 2.0, false, true);
    }

    @Test
    public void ifWidgetHasPlacementMapsAppearance_buttonShouldLaunchCorrectIntent() {
        GeoPointMapWidget widget = createWidget(promptWithAppearanceAndReadOnly(PLACEMENT_MAP, false));
        QuestionWidgetHelpers.stubLocationPermissions(permissionUtils, widget, true);
        widget.binding.simpleButton.performClick();

        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();
        Bundle bundle = startedIntent.getExtras();

        assertEquals(startedIntent.getComponent(), new ComponentName(widgetTestActivity(), GeoPointMapActivity.class));
        GeoWidgetHelpers.assertGroPointBundleArgumentEquals(bundle, null, DEFAULT_LOCATION_ACCURACY, false, true);
    }

    @Test
    public void ifWidgetHasMapsAppearance_buttonShouldLaunchCorrectIntent() {
        GeoPointMapWidget widget = createWidget(promptWithAppearanceAndReadOnly(MAPS, true));
        QuestionWidgetHelpers.stubLocationPermissions(permissionUtils, widget, true);
        widget.binding.simpleButton.performClick();

        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();
        Bundle bundle = startedIntent.getExtras();

        assertEquals(startedIntent.getComponent(), new ComponentName(widgetTestActivity(), GeoPointMapActivity.class));
        GeoWidgetHelpers.assertGroPointBundleArgumentEquals(bundle, null, DEFAULT_LOCATION_ACCURACY, true, false);
    }

    private GeoPointMapWidget createWidget(FormEntryPrompt prompt) {
        return new GeoPointMapWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"), questionDef, waitingForDataRegistry);
    }
}