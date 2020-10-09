package org.odk.collect.android.widgets;

import android.content.Intent;
import android.view.View;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.GeoPointMapActivity;
import org.odk.collect.android.fakes.FakePermissionUtils;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.interfaces.GeoWidget;
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
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.LOCATION_CAPTURE;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.MAPS;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.PLACEMENT_MAP;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.assertGeoPointBundleArgumentEquals;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.getRandomDoubleArray;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAppearanceAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class GeoPointMapWidgetTest {

    private final FakePermissionUtils permissionUtils = new FakePermissionUtils();
    private final GeoPointData answer = new GeoPointData(getRandomDoubleArray());

    private GeoWidget mockGeoWidget;
    private QuestionDef questionDef;
    private WaitingForDataRegistry waitingForDataRegistry;
    private View.OnLongClickListener listener;

    @Before
    public void setup() {
        mockGeoWidget = mock(GeoWidget.class);
        questionDef = mock(QuestionDef.class);
        waitingForDataRegistry = mock(WaitingForDataRegistry.class);
        listener = mock(View.OnLongClickListener.class);
        when(questionDef.getAdditionalAttribute(anyString(), anyString())).thenReturn(null);
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsPromptAnswer() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(null));
        assertNull(widget.getAnswer());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsPromptAnswer() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(answer));
        assertEquals(widget.getAnswer().getDisplayText(),
                new GeoPointData(GeoWidgetUtils.getLocationParamsFromStringAnswer(answer.getDisplayText())).getDisplayText());
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(answer));
        widget.clearAnswer();

        assertNull(widget.getAnswer());
        assertEquals(widget.binding.geoAnswerText.getText(), "");
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_point));
    }

    @Test
    public void clickingButtonForLong_callsLongClickListener() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(null));
        widget.setOnLongClickListener(listener);
        widget.binding.simpleButton.performLongClick();

        verify(listener).onLongClick(widget.binding.simpleButton);
    }

    @Test
    public void clickingAnswerTextViewForLong_callsLongClickListener() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(null));
        widget.setOnLongClickListener(listener);
        widget.binding.geoAnswerText.performLongClick();

        verify(listener).onLongClick(widget.binding.geoAnswerText);
    }

    @Test
    public void whenPromptHasAnswer_answerTextViewShowsCorrectAnswer() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(answer));
        assertEquals(widget.binding.geoAnswerText.getText(), GeoWidgetUtils.getAnswerToDisplay(widgetTestActivity(), answer.getDisplayText()));
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
    public void whenPromptDoesNotHaveAnswerAndIsReadOnly_bundleStoresCorrectValues() {
        GeoPointMapWidget widget = createWidget(promptWithReadOnlyAndAnswer(null));
        widget.binding.simpleButton.performClick();

        assertGeoPointBundleArgumentEquals(widget.bundle, null, GeoWidgetUtils.getAccuracyThreshold(questionDef),
                true, true);
    }

    @Test
    public void whenPromptHasAnswerAndMapsAppearance_bundleStoresCorrectValues() {
        GeoPointMapWidget widget = createWidget(promptWithAppearanceAndAnswer(MAPS, answer));
        widget.binding.simpleButton.performClick();

        assertGeoPointBundleArgumentEquals(widget.bundle, GeoWidgetUtils.getLocationParamsFromStringAnswer(answer.getDisplayText()),
                GeoWidgetUtils.getAccuracyThreshold(questionDef), false, false);
    }

    @Test
    public void whenPromptHasAnswerAndPlacementsMapAppearance_bundleStoresCorrectValues() {
        GeoPointMapWidget widget = createWidget(promptWithAppearanceAndAnswer(PLACEMENT_MAP, answer));
        widget.binding.simpleButton.performClick();

        assertGeoPointBundleArgumentEquals(widget.bundle, GeoWidgetUtils.getLocationParamsFromStringAnswer(answer.getDisplayText()),
                GeoWidgetUtils.getAccuracyThreshold(questionDef), false, true);
    }

    @Test
    public void buttonClick_callsOnButtonClicked() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        GeoPointMapWidget widget = createWidget(prompt);

        widget.setPermissionUtils(permissionUtils);
        widget.binding.simpleButton.performClick();

        verify(mockGeoWidget).onButtonClicked(widget.getContext(), prompt.getIndex(), permissionUtils, null,
                waitingForDataRegistry, GeoPointMapActivity.class, widget.bundle, LOCATION_CAPTURE);
    }

    private GeoPointMapWidget createWidget(FormEntryPrompt prompt) {
        return new GeoPointMapWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"),
                questionDef, waitingForDataRegistry, mockGeoWidget);
    }
}