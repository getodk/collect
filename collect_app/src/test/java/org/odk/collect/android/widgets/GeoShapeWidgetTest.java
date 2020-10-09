package org.odk.collect.android.widgets;

import android.view.View;

import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.GeoPolyActivity;
import org.odk.collect.android.fakes.FakePermissionUtils;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.widgets.support.FakeGeoButtonClickListener;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.GEOSHAPE_CAPTURE;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.assertGeoPolyBundleArgumentEquals;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.stringFromDoubleList;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

@RunWith(RobolectricTestRunner.class)
public class GeoShapeWidgetTest {
    private final FakePermissionUtils permissionUtils = new FakePermissionUtils();
    private final FakeGeoButtonClickListener fakeGeoButtonClickListener = new FakeGeoButtonClickListener();
    private final String answer = stringFromDoubleList();

    private WaitingForDataRegistry waitingForDataRegistry;
    private View.OnLongClickListener listener;

    @Before
    public void setup() {
        waitingForDataRegistry = mock(WaitingForDataRegistry.class);
        listener = mock(View.OnLongClickListener.class);
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(null));
        assertNull(widget.getAnswer());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        assertEquals(widget.getAnswer().getDisplayText(), answer);
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_textViewDisplaysEmptyString() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(null));
        assertEquals(widget.binding.geoAnswerText.getText().toString(), "");
    }

    @Test
    public void whenPromptHasAnswer_textViewDisplaysAnswer() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        assertEquals(widget.binding.geoAnswerText.getText().toString(), answer);
    }

    @Test
    public void whenPromptIsReadOnlyAndDoesNotHaveAnswer_geoButtonIsNotDisplayed() {
        GeoShapeWidget widget = createWidget(promptWithReadOnly());
        assertEquals(widget.binding.simpleButton.getVisibility(), View.GONE);
    }

    @Test
    public void whenPromptIsReadOnlyAndHasAnswer_viewGeoShapeButtonIsShown() {
        GeoShapeWidget widget = createWidget(promptWithReadOnlyAndAnswer(new StringData(answer)));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.geoshape_view_read_only));
    }

    @Test
    public void whenPromptIsNotReadOnlyAndDoesNotHaveAnswer_startGeoShapeButtonIsShown() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(null));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_shape));
    }

    @Test
    public void whenPromptIsNotReadOnlyAndHasAnswer_viewOrChangeGeoShapeButtonIsShown() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.geoshape_view_change_location));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        widget.clearAnswer();

        assertEquals(widget.binding.geoAnswerText.getText(), "");
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_shape));
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingButtonAndAnswerTextViewForLong_callsLongClickListeners() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(null));
        widget.setOnLongClickListener(listener);
        widget.binding.simpleButton.performLongClick();
        widget.binding.geoAnswerText.performLongClick();

        verify(listener).onLongClick(widget.binding.simpleButton);
        verify(listener).onLongClick(widget.binding.geoAnswerText);
    }

    @Test
    public void setData_updatesWidgetDisplayedAnswer() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(null));
        widget.setBinaryData(answer);
        assertEquals(widget.binding.geoAnswerText.getText().toString(), answer);
    }

    @Test
    public void setData_whenDataIsNull_updatesButtonLabel() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        widget.setBinaryData("");
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_shape));
    }

    @Test
    public void setData_whenDataIsNotNull_updatesButtonLabel() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(null));
        widget.setBinaryData(answer);
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.geoshape_view_change_location));
    }

    @Test
    public void setData_callsValueChangeListener() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.setBinaryData(answer);

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void buttonClick_whenPromptIsReadOnlyAndDoesNotHaveAnswer_requestsGeoIntentWithCorrectValues() {
        GeoShapeWidget widget = createWidget(promptWithReadOnly());
        widget.setPermissionUtils(permissionUtils);
        widget.binding.simpleButton.performClick();

        assertEquals(fakeGeoButtonClickListener.activityClass, GeoPolyActivity.class);
        assertEquals(fakeGeoButtonClickListener.requestCode, GEOSHAPE_CAPTURE);
        assertGeoPolyBundleArgumentEquals(fakeGeoButtonClickListener.geoBundle, "", GeoPolyActivity.OutputMode.GEOSHAPE, true);
    }

    @Test
    public void buttonClick_whenPromptIsNotReadOnlyAndHasAnswer_requestsGeoIntentWithCorrectValues() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        widget.setPermissionUtils(permissionUtils);
        widget.binding.simpleButton.performClick();

        assertEquals(fakeGeoButtonClickListener.activityClass, GeoPolyActivity.class);
        assertEquals(fakeGeoButtonClickListener.requestCode, GEOSHAPE_CAPTURE);
        assertGeoPolyBundleArgumentEquals(fakeGeoButtonClickListener.geoBundle, answer, GeoPolyActivity.OutputMode.GEOSHAPE, false);
    }

    private GeoShapeWidget createWidget(FormEntryPrompt prompt) {
        return new GeoShapeWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"),
                waitingForDataRegistry, fakeGeoButtonClickListener);
    }
}
