package org.odk.collect.android.widgets;

import androidx.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.mockito.Mock;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.base.GeneralExStringWidgetTest;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.odk.collect.android.widgets.utilities.ExWidgetIntentLauncher;

import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */

public class ExStringWidgetTest extends GeneralExStringWidgetTest<ExStringWidget, StringData> {

    @Mock
    ExWidgetIntentLauncher exWidgetIntentLauncher;

    @NonNull
    @Override
    public ExStringWidget createWidget() {
        return new ExStringWidget(activity, new QuestionDetails(formEntryPrompt), new FakeWaitingForDataRegistry(), exWidgetIntentLauncher);
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(RandomString.make());
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        when(formEntryPrompt.getAppearanceHint()).thenReturn("");
    }
}
