package org.odk.collect.android.widgets;

import androidx.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.odk.collect.android.widgets.base.QuestionWidgetTest;

/**
 * @author James Knight
 */

public class UrlWidgetTest extends QuestionWidgetTest<UrlWidget, StringData> {
    @NonNull
    @Override
    public UrlWidget createWidget() {
        return new UrlWidget(activity, formEntryPrompt);
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(RandomString.make());
    }

    @Override
    public void callingClearShouldRemoveTheExistingAnswer() {
        // The widget is ReadOnly, clear shouldn't do anything.
    }
}
