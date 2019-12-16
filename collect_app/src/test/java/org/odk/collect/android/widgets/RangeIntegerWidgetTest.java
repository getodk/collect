package org.odk.collect.android.widgets;

import android.view.View;

import androidx.annotation.NonNull;

import org.javarosa.core.model.data.IntegerData;
import org.junit.Test;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.base.RangeWidgetTest;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */
public class RangeIntegerWidgetTest extends RangeWidgetTest<RangeIntegerWidget, IntegerData> {

    @NonNull
    @Override
    public RangeIntegerWidget createWidget() {
        return new RangeIntegerWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"));
    }

    @NonNull
    @Override
    public IntegerData getNextAnswer() {
        return new IntegerData(random.nextInt());
    }

    @Test
    public void readOnlyPickerAppearanceTest() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        assertEquals(View.GONE, getWidget().pickerButton.getVisibility());
        assertEquals(View.VISIBLE, getWidget().answerTextView.getVisibility());
    }

    @Test
    public void readOnlyNoAppearanceTest() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);
        when(rangeQuestion.getAppearanceAttr()).thenReturn(null);

        assertEquals(View.VISIBLE, getWidget().seekBar.getVisibility());
        assertFalse(getWidget().seekBar.isEnabled());
        assertEquals(View.VISIBLE, getWidget().currentValue.getVisibility());
    }
}
