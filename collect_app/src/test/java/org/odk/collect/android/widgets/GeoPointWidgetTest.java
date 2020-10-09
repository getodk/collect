package org.odk.collect.android.widgets;

import android.view.View;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.GeoPointActivity;
import org.odk.collect.android.fakes.FakePermissionUtils;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.widgets.interfaces.GeoWidgetListener;
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.LOCATION_CAPTURE;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.assertGeoPointBundleArgumentEquals;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.getRandomDoubleArray;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

@RunWith(RobolectricTestRunner.class)
public class GeoPointWidgetTest {
    private final FakePermissionUtils permissionUtils = new FakePermissionUtils();
    private final GeoPointData answer = new GeoPointData(getRandomDoubleArray());

    private GeoWidgetListener mockGeoWidgetListener;
    private QuestionDef questionDef;
    private WaitingForDataRegistry waitingForDataRegistry;
    private View.OnLongClickListener listener;

    @Before
    public void setup() {
        mockGeoWidgetListener = mock(GeoWidgetListener.class);
        questionDef = mock(QuestionDef.class);
        waitingForDataRegistry = mock(WaitingForDataRegistry.class);
        listener = mock(View.OnLongClickListener.class);

        when(questionDef.getAdditionalAttribute(anyString(), anyString())).thenReturn(null);
    }

    @Test
    public void usingReadOnlyOption_doesNotShowTheGeoButton() {
        GeoPointWidget widget = createWidget(promptWithReadOnly());
        assertEquals(widget.binding.simpleButton.getVisibility(), View.GONE);
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        assertNull(widget.getAnswer());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        GeoPointWidget widget = createWidget(promptWithAnswer(answer));
        assertEquals(widget.getAnswer().getDisplayText(),
                new GeoPointData(GeoWidgetUtils.getLocationParamsFromStringAnswer(answer.getDisplayText())).getDisplayText());
    }

    @Test
    public void answerTextViewShouldShowCorrectAnswer() {
        GeoPointWidget widget = createWidget(promptWithAnswer(answer));
        assertEquals(widget.binding.geoAnswerText.getText(), GeoWidgetUtils.getAnswerToDisplay(widgetTestActivity(), answer.getDisplayText()));
    }

    @Test
    public void whenPromptDoesNotHaveHasAnswer_buttonShowsCorrectText() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_point));
    }

    @Test
    public void whenPromptHasAnswer_buttonShowsCorrectText() {
        GeoPointWidget widget = createWidget(promptWithAnswer(answer));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.change_location));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        GeoPointWidget widget = createWidget(promptWithAnswer(answer));
        widget.clearAnswer();

        assertEquals(widget.binding.geoAnswerText.getText(), "");
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_point));
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingButtonAndAnswerTextViewForLong_callsLongClickListeners() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        widget.setOnLongClickListener(listener);
        widget.binding.simpleButton.performLongClick();
        widget.binding.geoAnswerText.performLongClick();

        verify(listener).onLongClick(widget.binding.simpleButton);
        verify(listener).onLongClick(widget.binding.geoAnswerText);
    }

    @Test
    public void setData_setsCorrectAnswerInAnswerTextView() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        widget.setBinaryData(answer.getDisplayText());
        assertEquals(widget.binding.geoAnswerText.getText(), GeoWidgetUtils.getAnswerToDisplay(widgetTestActivity(), answer.getDisplayText()));
    }

    @Test
    public void setData_whenDataIsNotNull_updatesButtonLabel() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        widget.setBinaryData(answer.getDisplayText());
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.change_location));
    }

    @Test
    public void setData_whenDataIsNull_updatesButtonLabel() {
        GeoPointWidget widget = createWidget(promptWithAnswer(answer));
        widget.setBinaryData(null);
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_point));
    }

    @Test
    public void setData_callsValueChangeListener() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.setBinaryData(answer.getDisplayText());

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_bundleStoresCorrectValues() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        widget.binding.simpleButton.performClick();

        assertGeoPointBundleArgumentEquals(widget.bundle, null, GeoWidgetUtils.getAccuracyThreshold(questionDef),
                false, false);
    }

    @Test
    public void whenPromptHasAnswer_bundleStoresCorrectValues() {
        GeoPointWidget widget = createWidget(promptWithAnswer(answer));
        widget.binding.simpleButton.performClick();

        assertGeoPointBundleArgumentEquals(widget.bundle, GeoWidgetUtils.getLocationParamsFromStringAnswer(answer.getDisplayText()),
                GeoWidgetUtils.getAccuracyThreshold(questionDef), false, false);
    }

    @Test
    public void buttonClick_callsOnButtonClicked() {
        FormEntryPrompt prompt = promptWithAnswer(answer);
        GeoPointWidget widget = createWidget(prompt);

        widget.setPermissionUtils(permissionUtils);
        widget.binding.simpleButton.performClick();

        verify(mockGeoWidgetListener).onButtonClicked(widget.getContext(), prompt.getIndex(), permissionUtils, null,
                waitingForDataRegistry, GeoPointActivity.class, widget.bundle, LOCATION_CAPTURE);
    }

    private GeoPointWidget createWidget(FormEntryPrompt prompt) {
        return new GeoPointWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"),
                questionDef, waitingForDataRegistry, mockGeoWidgetListener);
    }
}
