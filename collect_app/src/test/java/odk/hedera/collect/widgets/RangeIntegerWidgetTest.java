package odk.hedera.collect.widgets;

import androidx.annotation.NonNull;

import org.javarosa.core.model.data.IntegerData;
import odk.hedera.collect.formentry.questions.QuestionDetails;
import odk.hedera.collect.widgets.base.RangeWidgetTest;

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
}
