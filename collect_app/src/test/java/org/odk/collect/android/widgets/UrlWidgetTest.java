package org.odk.collect.android.widgets;

import android.view.View;

import androidx.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Test;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.base.QuestionWidgetTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */

public class UrlWidgetTest extends QuestionWidgetTest<UrlWidget, StringData> {
    @NonNull
    @Override
    public UrlWidget createWidget() {
        return new UrlWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"));
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

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        assertThat(getWidget().openUrlButton.getVisibility(), is(View.GONE));
    }
}
