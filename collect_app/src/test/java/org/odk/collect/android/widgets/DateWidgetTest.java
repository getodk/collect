package org.odk.collect.android.widgets;

import android.support.annotation.NonNull;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.widgets.base.GeneralDateTimeWidgetTest;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class DateWidgetTest extends GeneralDateTimeWidgetTest<DateWidget, DateData> {

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
    public DateWidget createWidget() {
        return new DateWidget(RuntimeEnvironment.application, formEntryPrompt);
    }

    @NonNull
    @Override
    public DateData getNextAnswer() {
        return new DateData(getNextDateTime().toDate());
    }

    @Override
    public DateData getInitialAnswer() {
        return getNextAnswer();
    }

    @Test
    public void updatingTheDateAndTimeWidgetsShouldUpdateTheAnswer() {
        DateWidget widget = getWidget();

        DateTime dateTime = getNextDateTime()
                .withHourOfDay(0)
                .withMinuteOfHour(0);

        widget.updateDate(dateTime);

        IAnswerData answer = widget.getAnswer();
        DateTime answerDateTime = new DateTime(answer.getValue());

        assertEquals(dateTime, answerDateTime);
    }
}