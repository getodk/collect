package org.odk.collect.android.widgets;

import android.support.annotation.NonNull;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.TimeData;
import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.widgets.base.GeneralDateTimeWidgetTest;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertEquals;

/**
 * @author James Knight
 */
public class TimeWidgetTest extends GeneralDateTimeWidgetTest<TimeWidget, TimeData> {

    @Mock
    QuestionDef questionDef;

    @NonNull
    @Override
    public TimeWidget createWidget() {
        return new TimeWidget(RuntimeEnvironment.application, formEntryPrompt);
    }

    @NonNull
    @Override
    public TimeData getNextAnswer() {
        return new TimeData(getNextDateTime().toDate());
    }

    @Override
    public TimeData getInitialAnswer() {
        return getNextAnswer();
    }

    @Test
    public void updatingTheDateAndTimeWidgetsShouldUpdateTheAnswer() {
        TimeWidget widget = getWidget();

        DateTime dateTime = getNextDateTime();
        widget.updateTime(dateTime);

        IAnswerData answer = widget.getAnswer();
        DateTime answerDateTime = new DateTime(answer.getValue());

        assertEquals(dateTime, answerDateTime);
    }
}
