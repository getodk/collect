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
import org.odk.collect.android.widgets.interfaces.GeoWidget;
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
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

@RunWith(RobolectricTestRunner.class)
public class GeoShapeWidgetTest {
    private final FakePermissionUtils permissionUtils = new FakePermissionUtils();
    private final String answer = stringFromDoubleList();

    private WaitingForDataRegistry waitingForDataRegistry;
    private GeoWidget mockGeoWidget;
    private View.OnLongClickListener listener;

    @Before
    public void setup() {
        waitingForDataRegistry = mock(WaitingForDataRegistry.class);
        mockGeoWidget = mock(GeoWidget.class);
        listener = mock(View.OnLongClickListener.class);
    }

    @Test
    public void usingReadOnlyOption_doesNotShowTheGeoButton() {
        GeoShapeWidget widget = createWidget(promptWithReadOnly());
        assertEquals(widget.binding.simpleButton.getVisibility(), View.GONE);
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
    public void clearAnswer_clearsWidgetAnswer() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        widget.clearAnswer();

        assertNull(widget.getAnswer());
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
    public void clickingButtonForLong_callsLongClickListener() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(null));
        widget.setOnLongClickListener(listener);
        widget.binding.simpleButton.performLongClick();

        verify(listener).onLongClick(widget.binding.simpleButton);
    }

    @Test
    public void clickingAnswerTextViewForLong_callsLongClickListener() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(null));
        widget.setOnLongClickListener(listener);
        widget.binding.geoAnswerText.performLongClick();

        verify(listener).onLongClick(widget.binding.geoAnswerText);
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
    public void whenPromptDoesNotHaveAnswer_StartGeoShapeButtonIsShown() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(null));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_shape));
    }

    @Test
    public void whenPromptHasAnswer_ViewOrChangeGeoShapeButtonIsShown() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.geoshape_view_change_location));
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_bundleStoresCorrectValues() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(null));
        widget.binding.simpleButton.performClick();

        assertGeoPolyBundleArgumentEquals(widget.bundle, "", GeoPolyActivity.OutputMode.GEOSHAPE);
    }

    @Test
    public void whenPromptHasAnswer_bundleStoresCorrectValues() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        widget.binding.simpleButton.performClick();

        assertGeoPolyBundleArgumentEquals(widget.bundle, answer, GeoPolyActivity.OutputMode.GEOSHAPE);
    }

    @Test
    public void buttonClick_callsOnButtonClicked() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        GeoShapeWidget widget = createWidget(prompt);

        widget.setPermissionUtils(permissionUtils);
        widget.binding.simpleButton.performClick();

        verify(mockGeoWidget).onButtonClicked(widget.getContext(), prompt.getIndex(), permissionUtils, null,
                waitingForDataRegistry, GeoPolyActivity.class, widget.bundle, GEOSHAPE_CAPTURE);
    }

    private GeoShapeWidget createWidget(FormEntryPrompt prompt) {
        return new GeoShapeWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"),
                waitingForDataRegistry, mockGeoWidget);
    }
}
