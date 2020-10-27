package org.odk.collect.android.widgets;

import android.view.View;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.fragments.dialogs.NumberPickerDialog;
import org.odk.collect.android.fragments.dialogs.NumberPickerDialogTest;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.support.TestScreenContextActivity;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.robolectric.RobolectricTestRunner;
import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithQuestionDefAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndQuestionDef;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

@RunWith(RobolectricTestRunner.class)
public class RangePickerDecimalWidgetTest {
    private final FakeWaitingForDataRegistry waitingForDataRegistry = new FakeWaitingForDataRegistry();
    private final TestScreenContextActivity widgetActivity = widgetTestActivity();
    private final RangeQuestion rangeQuestion = mock(RangeQuestion.class);

    @Before
    public void setup() {
        when(rangeQuestion.getRangeStart()).thenReturn(new BigDecimal("1.5"));
        when(rangeQuestion.getRangeEnd()).thenReturn(new BigDecimal("5.5"));
        when(rangeQuestion.getRangeStep()).thenReturn(BigDecimal.ONE);
    }

    @Test
    public void whenWidgetIsReadOnly_widgetButtonIsNotDisplayed() {
        assertEquals(createWidget(promptWithReadOnlyAndQuestionDef(rangeQuestion)).binding
                .widgetButton.getVisibility(), View.GONE);
    }

    @Test
    public void whenWidgetIsInvalid_widgetButtonIsDisabled() {
        when(rangeQuestion.getRangeStep()).thenReturn(BigDecimal.valueOf(3));
        assertFalse(createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null)).binding.widgetButton.isEnabled());
    }

    @Test
    public void whenRangeStepIsZero_widgetButtonIsDisabled() {
        when(rangeQuestion.getRangeStep()).thenReturn(BigDecimal.ZERO);
        assertFalse(createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null)).binding.widgetButton.isEnabled());
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        assertNull(createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null)).getAnswer());
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_correctValuesAreDisplayed() {
        RangePickerDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        assertEquals(widget.binding.widgetButton.getText(), widget.getContext().getString(R.string.select_value));
        assertEquals(widget.binding.widgetAnswerText.getText(), widget.getContext().getString(R.string.no_value_selected));
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        RangePickerDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        assertEquals(widget.getAnswer().getValue(), 2.5);
    }

    @Test
    public void whenPromptHasAnswer_correctValuesAreDisplayed() {
        RangePickerDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        assertEquals(widget.binding.widgetButton.getText(), widget.getContext().getString(R.string.edit_value));
        assertEquals(widget.binding.widgetAnswerText.getText(), "2.5");
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        RangePickerDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        widget.clearAnswer();
        assertNull(widget.getAnswer());
    }

    @Test
    public void clearAnswer_clearsWidgetDisplayedAnswer() {
        RangePickerDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        widget.clearAnswer();
        assertEquals(widget.binding.widgetAnswerText.getText(), widget.getContext().getString(R.string.no_value_selected));
        assertEquals(widget.binding.widgetButton.getText(), widget.getContext().getString(R.string.select_value));
    }

    @Test
    public void clearAnswer_callsValueChangeListener() {
        RangePickerDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void setData_updatesAnswer() {
        RangePickerDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.setData(2);
        assertEquals(widget.getAnswer().getDisplayText(), "3.5");
    }

    @Test
    public void setData_updatesDisplayedAnswer() {
        RangePickerDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.setData(2);
        assertEquals(widget.binding.widgetButton.getText(), widget.getContext().getString(R.string.edit_value));
        assertEquals(widget.getAnswer().getDisplayText(), "3.5");
    }

    @Test
    public void setData_callsValueChangeListener() {
        RangePickerDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.setData(4);
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingWidgetButton_showsNumberPickerDialog_whenPromptDoesNotHaveAnswer() {
        NumberPickerDialogTest.TestRangePickerWidgetActivity activity =
                RobolectricHelpers.createThemedActivity(NumberPickerDialogTest.TestRangePickerWidgetActivity.class);
        RangePickerDecimalWidget widget = createWidget(activity, promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.binding.widgetButton.performClick();

        NumberPickerDialog dialog = (NumberPickerDialog) activity.getSupportFragmentManager()
                .findFragmentByTag(NumberPickerDialog.class.getName());

        assertNotNull(dialog);
        assertThat((String[]) dialog.getArguments().getSerializable(NumberPickerDialog.DISPLAYED_VALUES),
                arrayContainingInAnyOrder("1.5", "2.5", "3.5", "4.5", "5.5"));
        assertEquals(dialog.getArguments().getInt(NumberPickerDialog.PROGRESS), 0);
    }

    @Test
    public void clickingWidgetButton_showsNumberPickerDialog_whenPromptHasAnswer() {
        NumberPickerDialogTest.TestRangePickerWidgetActivity activity =
                RobolectricHelpers.createThemedActivity(NumberPickerDialogTest.TestRangePickerWidgetActivity.class);
        RangePickerDecimalWidget widget = createWidget(activity, promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        widget.binding.widgetButton.performClick();

        NumberPickerDialog dialog = (NumberPickerDialog) activity.getSupportFragmentManager()
                .findFragmentByTag(NumberPickerDialog.class.getName());

        assertNotNull(dialog);
        assertEquals(dialog.getArguments().getInt(NumberPickerDialog.PROGRESS), 1);
    }

    @Test
    public void clickingWidgetButton_setsWidgetWaitingForData() {
        FormIndex formIndex = mock(FormIndex.class);
        FormEntryPrompt prompt = promptWithQuestionDefAndAnswer(rangeQuestion, null);
        when(prompt.getIndex()).thenReturn(formIndex);

        NumberPickerDialogTest.TestRangePickerWidgetActivity activity =
                RobolectricHelpers.createThemedActivity(NumberPickerDialogTest.TestRangePickerWidgetActivity.class);
        RangePickerDecimalWidget widget = createWidget(activity, prompt);
        widget.binding.widgetButton.performClick();
        assertTrue(waitingForDataRegistry.waiting.contains(formIndex));
    }

    @Test
    public void clickingWidgetForLong_callsLongClickListener() {
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);
        RangePickerDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.setOnLongClickListener(listener);
        widget.binding.widgetButton.performLongClick();
        widget.binding.widgetAnswerText.performLongClick();

        verify(listener).onLongClick(widget.binding.widgetButton);
        verify(listener).onLongClick(widget.binding.widgetAnswerText);
    }

    private RangePickerDecimalWidget createWidget(FormEntryPrompt prompt) {
        return new RangePickerDecimalWidget(widgetActivity, new QuestionDetails(prompt, "formAnalyticsID"), waitingForDataRegistry);
    }


    private RangePickerDecimalWidget createWidget(NumberPickerDialogTest.TestRangePickerWidgetActivity activity, FormEntryPrompt prompt) {
        return new RangePickerDecimalWidget(activity, new QuestionDetails(prompt,
                "formAnalyticsID"), waitingForDataRegistry);
    }
}
