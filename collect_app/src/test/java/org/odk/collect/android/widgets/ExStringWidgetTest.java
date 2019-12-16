package org.odk.collect.android.widgets;

import android.view.View;

import androidx.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Test;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.base.GeneralExStringWidgetTest;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */

public class ExStringWidgetTest extends GeneralExStringWidgetTest<ExStringWidget, StringData> {

    @NonNull
    @Override
    public ExStringWidget createWidget() {
        return new ExStringWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"));
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

    @Test
    public void readOnlyTest() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        assertEquals(View.GONE, getWidget().launchIntentButton.getVisibility());
        assertEquals(View.VISIBLE, getWidget().answerText.getVisibility());
        assertFalse(getWidget().answerText.isEnabled());
    }
}
