package org.odk.collect.android.widgets;

import android.view.View;

import androidx.annotation.NonNull;

import org.javarosa.core.model.data.DecimalData;
import org.junit.Test;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.base.RangeWidgetTest;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */

public class RangeDecimalWidgetTest extends RangeWidgetTest<RangeDecimalWidget, DecimalData> {

    @NonNull
    @Override
    public RangeDecimalWidget createWidget() {
        return new RangeDecimalWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"));
    }

    @NonNull
    @Override
    public DecimalData getNextAnswer() {
        return new DecimalData(random.nextDouble());
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
