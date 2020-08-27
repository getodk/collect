package org.odk.collect.android.widgets;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
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
import org.odk.collect.android.widgets.support.GeoWidgetHelpers;
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.stringFromDoubleList;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class GeoShapeWidgetTest {

    private final FakePermissionUtils permissionUtils = new FakePermissionUtils();
    private final String answer = stringFromDoubleList();

    private WaitingForDataRegistry waitingForDataRegistry;

    @Before
    public void setup() {
        waitingForDataRegistry = mock(WaitingForDataRegistry.class);
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
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);
        GeoShapeWidget widget = createWidget(promptWithAnswer(null));
        widget.setOnLongClickListener(listener);
        widget.binding.simpleButton.performLongClick();

        verify(listener).onLongClick(widget.binding.simpleButton);
    }

    @Test
    public void clickingAnswerTextViewForLong_callsLongClickListener() {
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);
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
    public void whenPermissionIsNotGranted_buttonShouldNotLaunchAnyIntent() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(null));
        QuestionWidgetHelpers.stubLocationPermissions(permissionUtils, widget, false);
        widget.binding.simpleButton.performClick();
        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();

        assertNull(startedIntent);
    }

    @Test
    public void whenPermissionIsGranted_buttonClickWaitsForLocationData() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        GeoShapeWidget widget = createWidget(prompt);
        QuestionWidgetHelpers.stubLocationPermissions(permissionUtils, widget, true);
        widget.binding.simpleButton.performClick();

        verify(waitingForDataRegistry).waitForData(prompt.getIndex());
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_buttonShouldLaunchCorrectIntent() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(null));
        QuestionWidgetHelpers.stubLocationPermissions(permissionUtils, widget, true);
        widget.binding.simpleButton.performClick();

        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();
        Bundle bundle = startedIntent.getExtras();

        assertEquals(startedIntent.getComponent(), new ComponentName(widgetTestActivity(), GeoPolyActivity.class));
        GeoWidgetHelpers.assertGeoPolyBundleArgumentEquals(bundle, "", GeoPolyActivity.OutputMode.GEOSHAPE);
    }

    @Test
    public void whenPromptHasAnswer_buttonShouldLaunchCorrectIntent() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        QuestionWidgetHelpers.stubLocationPermissions(permissionUtils, widget, true);
        widget.binding.simpleButton.performClick();

        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();
        Bundle bundle = startedIntent.getExtras();

        assertEquals(startedIntent.getComponent(), new ComponentName(widgetTestActivity(), GeoPolyActivity.class));
        GeoWidgetHelpers.assertGeoPolyBundleArgumentEquals(bundle, answer, GeoPolyActivity.OutputMode.GEOSHAPE);
    }

    private GeoShapeWidget createWidget(FormEntryPrompt prompt) {
        return new GeoShapeWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"), waitingForDataRegistry);
    }
}
