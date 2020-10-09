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
import org.odk.collect.android.geo.MapConfigurator;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.widgets.interfaces.GeoWidget;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.GEOTRACE_CAPTURE;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.assertGeoPolyBundleArgumentEquals;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.stringFromDoubleList;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

@RunWith(RobolectricTestRunner.class)
public class GeoTraceWidgetTest {
    private final FakePermissionUtils permissionUtils = new FakePermissionUtils();
    private final String answer = stringFromDoubleList();

    private WaitingForDataRegistry waitingForDataRegistry;
    private MapConfigurator mapConfigurator;
    private GeoWidget mockGeoWidget;
    private View.OnLongClickListener listener;

    @Before
    public void setup() {
        waitingForDataRegistry = mock(WaitingForDataRegistry.class);
        mapConfigurator = mock(MapConfigurator.class);
        mockGeoWidget = mock(GeoWidget.class);
        listener = mock(View.OnLongClickListener.class);
    }

    @Test
    public void usingReadOnlyOption_doesNotShowTheGeoButton() {
        GeoTraceWidget widget = createWidget(promptWithReadOnly());
        assertEquals(widget.binding.simpleButton.getVisibility(), View.GONE);
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        GeoTraceWidget widget = createWidget(promptWithAnswer(null));
        assertNull(widget.getAnswer());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        GeoTraceWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        assertEquals(widget.getAnswer().getDisplayText(), answer);
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        GeoTraceWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        widget.clearAnswer();

        assertNull(widget.getAnswer());
        assertEquals(widget.binding.geoAnswerText.getText(), "");
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_trace));
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        GeoTraceWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingButtonForLong_callsLongClickListener() {
        GeoTraceWidget widget = createWidget(promptWithAnswer(null));
        widget.setOnLongClickListener(listener);
        widget.binding.simpleButton.performLongClick();

        verify(listener).onLongClick(widget.binding.simpleButton);
    }

    @Test
    public void clickingAnswerTextViewForLong_callsLongClickListener() {
        GeoTraceWidget widget = createWidget(promptWithAnswer(null));
        widget.setOnLongClickListener(listener);
        widget.binding.geoAnswerText.performLongClick();

        verify(listener).onLongClick(widget.binding.geoAnswerText);
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_textViewDisplaysEmptyString() {
        GeoTraceWidget widget = createWidget(promptWithAnswer(null));
        assertEquals(widget.binding.geoAnswerText.getText().toString(), "");
    }

    @Test
    public void whenPromptHasAnswer_textViewDisplaysAnswer() {
        GeoTraceWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        assertEquals(widget.binding.geoAnswerText.getText().toString(), answer);
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_StartGeoShapeButtonIsShown() {
        GeoTraceWidget widget = createWidget(promptWithAnswer(null));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_trace));
    }

    @Test
    public void whenPromptHasAnswer_ViewOrChangeGeoShapeButtonIsShown() {
        GeoTraceWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.geotrace_view_change_location));
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_bundleStoresCorrectValues() {
        GeoTraceWidget widget = createWidget(promptWithAnswer(null));
        widget.binding.simpleButton.performClick();

        assertGeoPolyBundleArgumentEquals(widget.bundle, "", GeoPolyActivity.OutputMode.GEOTRACE);
    }

    @Test
    public void whenPromptHasAnswer_bundleStoresCorrectValues() {
        GeoTraceWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        widget.binding.simpleButton.performClick();

        assertGeoPolyBundleArgumentEquals(widget.bundle, answer, GeoPolyActivity.OutputMode.GEOTRACE);
    }

    @Test
    public void buttonClick_callsOnButtonClicked() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        GeoTraceWidget widget = createWidget(prompt);

        widget.setPermissionUtils(permissionUtils);
        widget.binding.simpleButton.performClick();

        verify(mockGeoWidget).onButtonClicked(widget.getContext(), prompt.getIndex(), permissionUtils, mapConfigurator,
                waitingForDataRegistry, GeoPolyActivity.class, widget.bundle, GEOTRACE_CAPTURE);
    }

    private GeoTraceWidget createWidget(FormEntryPrompt prompt) {
        return new GeoTraceWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"),
                waitingForDataRegistry, mapConfigurator, mockGeoWidget);
    }
}
