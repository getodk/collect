package odk.hedera.collect.widgets;

import android.view.View;

import androidx.annotation.NonNull;

import org.junit.Test;
import odk.hedera.collect.formentry.questions.QuestionDetails;
import odk.hedera.collect.widgets.base.GeneralSelectOneWidgetTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */

public class SpinnerWidgetTest extends GeneralSelectOneWidgetTest<SpinnerWidget> {
    @NonNull
    @Override
    public SpinnerWidget createWidget() {
        return new SpinnerWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"), false);
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        assertThat(getSpyWidget().spinner.getVisibility(), is(View.VISIBLE));
        assertThat(getSpyWidget().spinner.isEnabled(), is(Boolean.FALSE));
    }
}
