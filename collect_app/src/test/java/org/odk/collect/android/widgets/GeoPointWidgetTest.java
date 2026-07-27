package org.odk.collect.android.widgets;

import android.view.View;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.support.MockFormEntryPromptBuilder;
import org.odk.collect.android.utilities.Appearances;
import org.odk.collect.android.widgets.interfaces.GeoDataRequester;
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.getRandomDoubleArray;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAppearance;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetDependencies;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

@RunWith(AndroidJUnit4.class)
public class GeoPointWidgetTest {
    private final GeoPointData answer = new GeoPointData(getRandomDoubleArray());

    private GeoDataRequester geoDataRequester;
    private WaitingForDataRegistry waitingForDataRegistry;

    @Before
    public void setup() {
        geoDataRequester = mock(GeoDataRequester.class);
        waitingForDataRegistry = mock(WaitingForDataRegistry.class);
    }

    @Test
    public void whenPromptIsReadOnlyAndHasNoAnswer_geoPointButtonIsHidden() {
        GeoPointWidget widget = createWidget(promptWithReadOnly());
        assertEquals(widget.binding.simpleButton.getVisibility(), View.GONE);
    }

    @Test
    public void whenPromptIsNotReadOnlyAndHasNoAnswer_geoPointButtonIsShown() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        assertEquals(widget.binding.simpleButton.getVisibility(), View.VISIBLE);
    }

    @Test
    public void whenPromptIsReadOnlyAndHasAnswer_geoPointButtonIsShown() {
        GeoPointWidget widget = createWidget(promptWithReadOnlyAndAnswer(answer));
        assertEquals(widget.binding.simpleButton.getVisibility(), View.VISIBLE);
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(org.odk.collect.strings.R.string.view_point));
    }

    @Test
    public void withMapsAppearance_whenPromptIsReadOnlyAndHasNoAnswer_geoButtonIsNotDisplayed() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withAppearance(Appearances.MAPS)
                .withReadOnly(true)
                .withAnswer(null)
                .build();
        GeoPointWidget widget = createWidget(prompt);
        assertEquals(widget.binding.simpleButton.getVisibility(), View.GONE);
    }

    @Test
    public void withMapsAppearance_whenPromptIsReadOnlyAndHasAnswer_viewGeoPointButtonIsShown() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withAppearance(Appearances.MAPS)
                .withReadOnly(true)
                .withAnswer(answer)
                .build();
        GeoPointWidget widget = createWidget(prompt);
        assertEquals(widget.binding.simpleButton.getVisibility(), View.VISIBLE);
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(org.odk.collect.strings.R.string.view_point));
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        assertNull(widget.getAnswer());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        GeoPointWidget widget = createWidget(promptWithAnswer(answer));
        assertEquals(widget.getAnswer().getDisplayText(), answer.getDisplayText());
    }

    @Test
    public void getAnswer_whenPromptHasInvalidAnswer_returnsNull() {
        GeoPointWidget widget = createWidget(promptWithAnswer(new StringData("blah")));
        assertNull(widget.getAnswer());
    }

    @Test
    public void creatingWidgetWithInvalidValue_doesNotUpdateWidgetDisplayedAnswer() {
        GeoPointWidget widget = createWidget(promptWithAnswer(new StringData("blah")));
        assertEquals(widget.binding.geoAnswerText.getText(), "");
        assertEquals(widget.binding.geoAnswerText.getVisibility(), View.GONE);
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(org.odk.collect.strings.R.string.get_point));
    }

    @Test
    public void answerTextViewShouldShowCorrectAnswer() {
        GeoPointWidget widget = createWidget(promptWithAnswer(answer));
        assertEquals(widget.binding.geoAnswerText.getText(), GeoWidgetUtils.getGeoPointAnswerToDisplay(widget.getContext(), answer.getDisplayText()));
        assertEquals(widget.binding.geoAnswerText.getVisibility(), View.VISIBLE);
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_buttonShowsCorrectText() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(org.odk.collect.strings.R.string.get_point));
    }

    @Test
    public void whenPromptHasAnswer_buttonShowsCorrectText() {
        GeoPointWidget widget = createWidget(promptWithAnswer(answer));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(org.odk.collect.strings.R.string.change_point));
    }

    @Test
    public void withMapsAppearance_whenPromptDoesNotHaveAnswer_buttonShowsCorrectText() {
        GeoPointWidget widget = createWidget(promptWithAppearance(Appearances.MAPS));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(org.odk.collect.strings.R.string.get_point));
    }

    @Test
    public void withMapsAppearance_whenPromptHasAnswer_buttonShowsCorrectText() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withAppearance(Appearances.MAPS)
                .withAnswer(answer)
                .build();
        GeoPointWidget widget = createWidget(prompt);
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(org.odk.collect.strings.R.string.view_or_change_point));
    }

    @Test
    public void withPlacementMapAppearance_whenPromptHasAnswer_buttonShowsCorrectText() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withAppearance(Appearances.PLACEMENT_MAP)
                .withAnswer(answer)
                .build();
        GeoPointWidget widget = createWidget(prompt);
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(org.odk.collect.strings.R.string.view_or_change_point));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        GeoPointWidget widget = createWidget(promptWithAnswer(answer));
        widget.clearAnswer();

        assertEquals(widget.binding.geoAnswerText.getText(), "");
        assertEquals(widget.binding.geoAnswerText.getVisibility(), View.GONE);
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(org.odk.collect.strings.R.string.get_point));
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
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);
        GeoPointWidget widget = createWidget(promptWithAnswer(null));

        widget.setOnLongClickListener(listener);
        widget.binding.simpleButton.performLongClick();
        widget.binding.geoAnswerText.performLongClick();

        verify(listener).onLongClick(widget.binding.simpleButton);
        verify(listener).onLongClick(widget.binding.geoAnswerText);
    }

    @Test
    public void setData_updatesWidgetAnswer() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        widget.setData(answer.getDisplayText());
        assertEquals(widget.getAnswer().getDisplayText(), answer.getDisplayText());
    }

    @Test
    public void setDataWithInvalidValue_doesNotUpdateWidgetAnswer() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        widget.setData("blah");
        assertEquals(widget.getAnswer(), null);
    }

    @Test
    public void setData_updatesWidgetDisplayedAnswer() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        widget.setData(answer.getDisplayText());
        assertEquals(widget.binding.geoAnswerText.getText(), GeoWidgetUtils.getGeoPointAnswerToDisplay(widget.getContext(), answer.getDisplayText()));
        assertEquals(widget.binding.geoAnswerText.getVisibility(), View.VISIBLE);
    }

    @Test
    public void setDataWithInvalidValue_doesNotUpdateWidgetDisplayedAnswer() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        widget.setData("blah");
        assertEquals(widget.binding.geoAnswerText.getText(), "");
        assertEquals(widget.binding.geoAnswerText.getVisibility(), View.GONE);
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(org.odk.collect.strings.R.string.get_point));
    }

    @Test
    public void setData_whenDataIsNull_updatesButtonLabel() {
        GeoPointWidget widget = createWidget(promptWithAnswer(answer));
        widget.setData("");
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(org.odk.collect.strings.R.string.get_point));
    }

    @Test
    public void setData_whenDataIsNotNull_updatesButtonLabel() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        widget.setData(answer.getDisplayText());
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(org.odk.collect.strings.R.string.change_point));
    }

    @Test
    public void withMapsAppearance_setData_whenDataIsNull_updatesButtonLabel() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withAppearance(Appearances.MAPS)
                .withAnswer(answer)
                .build();
        GeoPointWidget widget = createWidget(prompt);
        widget.setData("");
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(org.odk.collect.strings.R.string.get_point));
    }

    @Test
    public void withMapsAppearance_setData_whenDataIsNotNull_updatesButtonLabel() {
        GeoPointWidget widget = createWidget(promptWithAppearance(Appearances.MAPS));
        widget.setData(answer.getDisplayText());
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(org.odk.collect.strings.R.string.view_or_change_point));
    }

    @Test
    public void setData_callsValueChangeListener() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.setData(answer.getDisplayText());
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void buttonClick_requestsGeoPoint() {
        FormEntryPrompt prompt = promptWithAnswer(answer);
        GeoPointWidget widget = createWidget(prompt);
        widget.binding.simpleButton.performClick();

        verify(geoDataRequester).requestGeoPoint(prompt, waitingForDataRegistry);
    }

    @Test
    public void whenPromptIsReadOnlyAndHasAnswer_buttonClick_requestsGeoPoint() {
        FormEntryPrompt prompt = promptWithReadOnlyAndAnswer(answer);
        GeoPointWidget widget = createWidget(prompt);
        widget.binding.simpleButton.performClick();

        verify(geoDataRequester).requestGeoPoint(prompt, waitingForDataRegistry);
    }

    @Test
    public void buttonClick_requestsGeoPoint_whenAnswerIsCleared() {
        FormEntryPrompt prompt = promptWithAnswer(answer);
        GeoPointWidget widget = createWidget(prompt);
        widget.clearAnswer();
        widget.binding.simpleButton.performClick();

        verify(geoDataRequester).requestGeoPoint(prompt, waitingForDataRegistry);
    }

    @Test
    public void buttonClick_requestsGeoPoint_whenAnswerIsUpdated() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        GeoPointWidget widget = createWidget(prompt);
        widget.setData(answer);
        widget.binding.simpleButton.performClick();

        verify(geoDataRequester).requestGeoPoint(prompt, waitingForDataRegistry);
    }

    private GeoPointWidget createWidget(FormEntryPrompt prompt) {
        return new GeoPointWidget(widgetTestActivity(), new QuestionDetails(prompt),
                waitingForDataRegistry, geoDataRequester, widgetDependencies());
    }
}
