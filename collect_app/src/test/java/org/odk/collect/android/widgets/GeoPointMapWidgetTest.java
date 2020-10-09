package org.odk.collect.android.widgets;

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
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.widgets.support.FakeGeoButtonClickListener;
import org.odk.collect.android.widgets.utilities.GeoDataRequester;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;
import org.robolectric.RobolectricTestRunner;

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
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAppearanceAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

@RunWith(RobolectricTestRunner.class)
public class GeoPointMapWidgetTest {
    private final FakePermissionUtils permissionUtils = new FakePermissionUtils();
    private final FakeGeoButtonClickListener fakeGeoButtonClickListener = new FakeGeoButtonClickListener();
    private final GeoPointData answer = new GeoPointData(getRandomDoubleArray());

    private QuestionDef questionDef;
    private WaitingForDataRegistry waitingForDataRegistry;
    private View.OnLongClickListener listener;

    @Before
    public void setup() {
        questionDef = mock(QuestionDef.class);
        waitingForDataRegistry = mock(WaitingForDataRegistry.class);
        listener = mock(View.OnLongClickListener.class);

        when(questionDef.getAdditionalAttribute(anyString(), anyString())).thenReturn(null);
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(null));
        assertNull(widget.getAnswer());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsPromptAnswer() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(answer));
        assertEquals(widget.getAnswer().getDisplayText(),
                new GeoPointData(GeoDataRequester.getLocationParamsFromStringAnswer(answer.getDisplayText())).getDisplayText());
    }

    @Test
    public void whenPromptHasAnswer_answerTextViewShowsCorrectAnswer() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(answer));
        assertEquals(widget.binding.geoAnswerText.getText(), GeoDataRequester.getAnswerToDisplay(widget.getContext(), answer.getDisplayText()));
    }

    @Test
    public void whenPromptIsReadOnlyAndDoesNotHaveAnswer_geoButtonIsNotDisplayed() {
        GeoPointMapWidget widget = createWidget(promptWithReadOnly());
        assertEquals(widget.binding.simpleButton.getVisibility(), View.GONE);
    }

    @Test
    public void whenPromptIsReadOnlyAndHasAnswer_viewGeoPointButtonIsShown() {
        GeoPointMapWidget widget = createWidget(promptWithReadOnlyAndAnswer(answer));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.geopoint_view_read_only));
    }

    @Test
    public void whenPromptIsNotReadOnlyAndDoesNotHaveAnswer_startGeoPointButtonIsShown() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(null));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_point));
    }

    @Test
    public void whenPromptIsNotReadOnlyAndHasAnswer_viewOrChangeLocationButtonIsShown() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(answer));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.view_change_location));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(answer));
        widget.clearAnswer();

        assertEquals(widget.binding.geoAnswerText.getText(), "");
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_point));
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void setData_updatesWidgetDisplayedAnswer() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(null));
        widget.setBinaryData(answer.getDisplayText());
        assertEquals(widget.binding.geoAnswerText.getText(), GeoDataRequester.getAnswerToDisplay(widget.getContext(), answer.getDisplayText()));
    }

    @Test
    public void setData_whenDataIsNull_updatesButtonLabel() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(answer));
        widget.setBinaryData(null);
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_point));
    }

    @Test
    public void setData_whenDataIsNotNull_updatesButtonLabel() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(null));
        widget.setBinaryData(answer.getDisplayText());
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.view_change_location));
    }

    @Test
    public void setData_callsValueChangeListener() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.setBinaryData(answer.getDisplayText());

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingButtonAndAnswerTextViewForLong_callsLongClickListener() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(null));
        widget.setOnLongClickListener(listener);
        widget.binding.simpleButton.performLongClick();
        widget.binding.geoAnswerText.performLongClick();

        verify(listener).onLongClick(widget.binding.simpleButton);
        verify(listener).onLongClick(widget.binding.geoAnswerText);
    }

    @Test
    public void buttonClick_whenPromptDoesNotHaveAnswerAndIsReadOnly_requestsGeoIntentWithCorrectValues() {
        GeoPointMapWidget widget = createWidget(promptWithReadOnlyAndAnswer(null));
        widget.setPermissionUtils(permissionUtils);
        widget.binding.simpleButton.performClick();

        assertEquals(fakeGeoButtonClickListener.activityClass, GeoPointMapActivity.class);
        assertEquals(fakeGeoButtonClickListener.requestCode, LOCATION_CAPTURE);
        assertGeoPointBundleArgumentEquals(fakeGeoButtonClickListener.geoBundle, null, GeoDataRequester.DEFAULT_LOCATION_ACCURACY,
                true, true);
    }

    @Test
    public void buttonClick_whenPromptHasAnswerAndMapsAppearance_requestsGeoIntentWithCorrectValues() {
        GeoPointMapWidget widget = createWidget(promptWithAppearanceAndAnswer(MAPS, answer));
        widget.setPermissionUtils(permissionUtils);
        widget.binding.simpleButton.performClick();

        assertEquals(fakeGeoButtonClickListener.activityClass, GeoPointMapActivity.class);
        assertEquals(fakeGeoButtonClickListener.requestCode, LOCATION_CAPTURE);
        assertGeoPointBundleArgumentEquals(fakeGeoButtonClickListener.geoBundle, GeoDataRequester.getLocationParamsFromStringAnswer(answer.getDisplayText()),
                GeoDataRequester.DEFAULT_LOCATION_ACCURACY, false, false);
    }

    @Test
    public void buttonClick_whenPromptHasAccuracyAndPlacementMapsAppearance_requestsGeoIntentWithCorrectValues() {
        when(questionDef.getAdditionalAttribute(null, GeoDataRequester.ACCURACY_THRESHOLD)).thenReturn("10");

        GeoPointMapWidget widget = createWidget(promptWithAppearanceAndAnswer(PLACEMENT_MAP, answer));
        widget.setPermissionUtils(permissionUtils);
        widget.binding.simpleButton.performClick();

        assertEquals(fakeGeoButtonClickListener.activityClass, GeoPointMapActivity.class);
        assertEquals(fakeGeoButtonClickListener.requestCode, LOCATION_CAPTURE);
        assertGeoPointBundleArgumentEquals(fakeGeoButtonClickListener.geoBundle, GeoDataRequester.getLocationParamsFromStringAnswer(answer.getDisplayText()),
                10.0, false, true);
    }

    private GeoPointMapWidget createWidget(FormEntryPrompt prompt) {
        return new GeoPointMapWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"),
                questionDef, waitingForDataRegistry, fakeGeoButtonClickListener);
    }
}