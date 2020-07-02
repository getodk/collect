package odk.hedera.collect.widgets;

import androidx.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import odk.hedera.collect.formentry.questions.QuestionDetails;
import odk.hedera.collect.widgets.base.GeneralStringWidgetTest;

/**
 * @author James Knight
 */
public class StringWidgetTest extends GeneralStringWidgetTest<StringWidget, StringData> {

    @NonNull
    @Override
    public StringWidget createWidget() {
        return new StringWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"), false);
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(RandomString.make());
    }
}
