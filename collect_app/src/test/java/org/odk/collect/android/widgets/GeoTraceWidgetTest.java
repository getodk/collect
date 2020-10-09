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
import org.odk.collect.android.geo.MapConfigurator;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class GeoTraceWidgetTest {

    private final FakePermissionUtils permissionUtils = new FakePermissionUtils();
    private final List<double[]> answerDoubles = getRandomDoubleArrayList();
    private final String answer = stringFromDoubleList(answerDoubles);

    private WaitingForDataRegistry waitingForDataRegistry;
    private MapConfigurator mapConfigurator;

    @Before
    public void setup() {
        waitingForDataRegistry = mock(WaitingForDataRegistry.class);
        mapConfigurator = mock(MapConfigurator.class);
    }

    @Test
    public void usingReadOnlyOption_doesNotShowTheGeoButton() {
        GeoTraceWidget widget = createWidget(promptWithReadOnly());
        assertThat(widget.startGeoButton.getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        GeoTraceWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.getAnswer(), equalTo(null));
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        GeoTraceWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        assertThat(widget.getAnswer().getDisplayText(), equalTo(answer));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        GeoTraceWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        widget.clearAnswer();
        assertThat(widget.getAnswer(), nullValue());
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
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);
        GeoTraceWidget widget = createWidget(promptWithAnswer(null));
        widget.setOnLongClickListener(listener);
        widget.startGeoButton.performLongClick();

        verify(listener).onLongClick(widget.startGeoButton);
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_textViewDisplaysEmptyString() {
        GeoTraceWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.answerDisplay.getText().toString(), equalTo(""));
    }

    @Test
    public void whenPromptHasAnswer_textViewDisplaysAnswer() {
        GeoTraceWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        assertThat(widget.answerDisplay.getText().toString(), equalTo(answer));
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_StartGeoShapeButtonIsShown() {
        GeoTraceWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.startGeoButton.getText(), equalTo(widget.getContext().getString(R.string.get_trace)));
    }

    @Test
    public void whenPromptHasAnswer_ViewOrChangeGeoShapeButtonIsShown() {
        GeoTraceWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        assertThat(widget.startGeoButton.getText(), equalTo(widget.getContext().getString(R.string.geotrace_view_change_location)));
    }

    @Test
    public void whenPermissionIsNotGranted_buttonShouldNotLaunchAnyIntent() {
        GeoTraceWidget widget = createWidget(promptWithAnswer(null));
        stubLocationPermissions(widget, false);
        widget.startGeoButton.performClick();
        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();

        assertNull(startedIntent);
    }

    @Test
    public void whenMapConfiguratorIsNotAvailable_buttonShouldNotLaunchAnyIntentAnsDisplayMessage() {
        GeoTraceWidget widget = createWidget(promptWithAnswer(null));
        stubLocationPermissions(widget, true);
        when(mapConfigurator.isAvailable(widget.getContext())).thenReturn(false);

        widget.startGeoButton.performClick();
        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();

        assertNull(startedIntent);
        verify(mapConfigurator).showUnavailableMessage(widget.getContext());
    }

    @Test
    public void whenPermissionIsGranted_buttonClickWaitsForLocationData() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        GeoTraceWidget widget = createWidget(prompt);
        stubLocationPermissions(widget, true);
        widget.startGeoButton.performClick();

        verify(waitingForDataRegistry).waitForData(prompt.getIndex());
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_buttonShouldLaunchCorrectIntent() {
        GeoTraceWidget widget = createWidget(promptWithAnswer(null));
        stubLocationPermissions(widget, true);
        when(mapConfigurator.isAvailable(widget.getContext())).thenReturn(true);

        widget.startGeoButton.performClick();
        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();
        Bundle bundle = startedIntent.getExtras();

        assertThat(startedIntent.getComponent(), equalTo(new ComponentName(widgetTestActivity(), GeoPolyActivity.class)));
        assertBundleArgumentEquals(bundle, "", GeoPolyActivity.OutputMode.GEOTRACE);
    }

    @Test
    public void whenPromptHasAnswer_buttonShouldLaunchCorrectIntent() {
        GeoTraceWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        stubLocationPermissions(widget, true);
        when(mapConfigurator.isAvailable(widget.getContext())).thenReturn(true);

        widget.startGeoButton.performClick();
        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();
        Bundle bundle = startedIntent.getExtras();

        assertThat(startedIntent.getComponent(), equalTo(new ComponentName(widgetTestActivity(), GeoPolyActivity.class)));
        assertBundleArgumentEquals(bundle, answer, GeoPolyActivity.OutputMode.GEOTRACE);
    }

    private GeoTraceWidget createWidget(FormEntryPrompt prompt) {
        return new GeoTraceWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"),
                waitingForDataRegistry, mapConfigurator);
    }

    private void assertBundleArgumentEquals(Bundle bundle, String answer, GeoPolyActivity.OutputMode outputMode) {
        assertThat(bundle.getString(GeoPolyActivity.ANSWER_KEY), equalTo(answer));
        assertThat(bundle.get(GeoPolyActivity.OUTPUT_MODE_KEY), equalTo(outputMode));
    }

    protected void stubLocationPermissions(GeoTraceWidget widget, boolean isGranted) {
        permissionUtils.setPermissionGranted(isGranted);
        widget.setPermissionUtils(permissionUtils);
    }

    private String stringFromDoubleList(List<double[]> doubleList) {
        StringBuilder b = new StringBuilder();
        boolean first = true;
        for (double[] doubles : doubleList) {
            if (!first) {
                b.append("; ");
            }
            first = false;
            b.append(stringFromDoubles(doubles));
        }
        return b.toString();
    }

    private String stringFromDoubles(double[] doubles) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < doubles.length; i++) {
            b.append(doubles[i]);
            if (i != doubles.length - 1) {
                b.append(' ');
            }
        }
        return b.toString();
    }

    private ArrayList<double[]> getRandomDoubleArrayList() {
        Random random = new Random();
        ArrayList<double[]> doubleList = new ArrayList<>();

        int pointCount = Math.max(1, random.nextInt() % 5);
        for (int i = 0; i < pointCount; ++i) {
            doubleList.add(getRandomDoubleArray());
        }

        return doubleList;
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
