package org.odk.collect.android.widgets.base;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.widgets.RangeWidget;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */

public abstract class RangeWidgetTest<W extends RangeWidget, A extends IAnswerData> extends QuestionWidgetTest<W, A> {

    private final BigDecimal rangeStart = BigDecimal.ONE;
    private final BigDecimal rangeEnd = BigDecimal.TEN;
    private final BigDecimal rangeStep = BigDecimal.ONE;

    @Mock
    private RangeQuestion rangeQuestion;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        when(formEntryPrompt.getQuestion()).thenReturn(rangeQuestion);
        when(rangeQuestion.getAppearanceAttr()).thenReturn("picker");


        when(rangeQuestion.getRangeStart()).thenReturn(rangeStart);
        when(rangeQuestion.getRangeEnd()).thenReturn(rangeEnd);
        when(rangeQuestion.getRangeStep()).thenReturn(rangeStep);
    }

    @Test
    public void getAnswerShouldReflectActualValueSetViaSeekBar() {
        W widget = getWidget();
        assertNull(widget.getAnswer());

        int progress = Math.abs(random.nextInt()) % widget.getElementCount();
        widget.onProgressChanged(widget.getSeekBar(), progress, true);

        BigDecimal actualValue;
        if (rangeStart.compareTo(rangeEnd) == -1) {
            actualValue = rangeStart.add(new BigDecimal(progress).multiply(rangeStep));
        } else {
            actualValue = rangeStart.subtract(new BigDecimal(progress).multiply(rangeStep));
        }

        IAnswerData answer = widget.getAnswer();
        IAnswerData compareTo;
        if (answer instanceof DecimalData) {
            compareTo = new DecimalData(actualValue.doubleValue());
        } else {
            compareTo = new IntegerData(actualValue.intValue());
        }

        assertEquals(answer.getDisplayText(), compareTo.getDisplayText());
    }
}
