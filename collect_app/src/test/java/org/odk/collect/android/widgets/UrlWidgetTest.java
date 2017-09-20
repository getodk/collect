package org.odk.collect.android.widgets;

import android.support.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.odk.collect.android.widgets.base.QuestionWidgetTest;
import org.robolectric.RuntimeEnvironment;

/**
 * @author James Knight
 */

public class UrlWidgetTest extends QuestionWidgetTest<UrlWidget, StringData> {
    @NonNull
    @Override
    public UrlWidget createWidget() {
        return new UrlWidget(RuntimeEnvironment.application, formEntryPrompt);
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
