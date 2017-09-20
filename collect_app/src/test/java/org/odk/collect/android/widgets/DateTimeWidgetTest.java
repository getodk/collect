package org.odk.collect.android.widgets;

import android.support.annotation.NonNull;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.IAnswerData;
import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.widgets.base.GeneralDateTimeWidgetTest;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertEquals;
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
    public void updatingTheDateAndTimeWidgetsShouldUpdateTheAnswer() {
        DateTimeWidget widget = getWidget();

        DateWidget dateWidget = widget.getDateWidget();
        TimeWidget timeWidget = widget.getTimeWidget();

        DateTime dateTime = getNextDateTime();
        dateWidget.updateDate(dateTime);
        timeWidget.updateTime(dateTime);

        IAnswerData answer = widget.getAnswer();
        DateTime answerDateTime = new DateTime(answer.getValue());

        assertEquals(dateTime, answerDateTime);
    }
}
