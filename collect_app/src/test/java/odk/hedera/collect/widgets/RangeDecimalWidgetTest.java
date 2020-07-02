package odk.hedera.collect.widgets;

import androidx.annotation.NonNull;

import org.javarosa.core.model.data.DecimalData;
import odk.hedera.collect.formentry.questions.QuestionDetails;
import odk.hedera.collect.widgets.base.RangeWidgetTest;

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
}
