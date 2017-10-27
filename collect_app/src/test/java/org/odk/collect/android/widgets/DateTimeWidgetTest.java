package org.odk.collect.android.widgets;

import android.support.annotation.NonNull;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DateTimeData;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.widgets.base.GeneralDateTimeWidgetTest;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

public class DateTimeWidgetTest extends GeneralDateTimeWidgetTest<DateTimeWidget, DateTimeData> {

    @Mock
    QuestionDef questionDef;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        when(formEntryPrompt.getQuestion()).thenReturn(questionDef);
        when(questionDef.getAppearanceAttr()).thenReturn("");
    }

    @NonNull
    @Override
    public DateTimeWidget createWidget() {
        return new DateTimeWidget(RuntimeEnvironment.application, formEntryPrompt);
    }

    @NonNull
    @Override
    public DateTimeData getNextAnswer() {
        return new DateTimeData(getNextDateTime().toDate());
    }

    @Override
    public DateTimeData getInitialAnswer() {
        return getNextAnswer();
    }

    @Test
    public void setData() {
        DateTimeWidget widget = getWidget();
        LocalDateTime date = new LocalDateTime().withYear(2010).withMonthOfYear(5).withDayOfMonth(12);
        widget.setBinaryData(date);
        assertFalse(widget.isWaitingForData());
        assertFalse(widget.getDateWidget().isNullAnswer);
        assertEquals(widget.getDateWidget().getAnswer().getDisplayText(), new DateData(date.toDate()).getDisplayText());
    }
}
