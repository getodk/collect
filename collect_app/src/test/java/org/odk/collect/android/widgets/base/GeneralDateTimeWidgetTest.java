package org.odk.collect.android.widgets.base;

import org.javarosa.core.model.data.IAnswerData;
import org.joda.time.DateTime;
import org.odk.collect.android.widgets.interfaces.Widget;

/**
 * @author James Knight
 */

public abstract class GeneralDateTimeWidgetTest<W extends Widget, A extends IAnswerData>
        extends QuestionWidgetTest<W, A> {

    private DateTime lastDate;

    protected DateTime getNextDateTime() {
        if (lastDate == null) {
            lastDate = DateTime.now()
                    .withSecondOfMinute(0)
                    .withMillisOfSecond(0);

        } else {
            lastDate = lastDate.plusMonths(1)
                    .plusDays(1)
                    .plusHours(1)
                    .plusMinutes(1)
                    .plusSeconds(1);
        }

        return lastDate;
    }
}
