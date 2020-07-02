package odk.hedera.collect.widgets;

import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.NonNull;

import org.junit.Test;
import odk.hedera.collect.formentry.questions.QuestionDetails;
import odk.hedera.collect.widgets.base.GeneralSelectMultiWidgetTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */

public class ListMultiWidgetTest extends GeneralSelectMultiWidgetTest<ListMultiWidget> {
    @NonNull
    @Override
    public ListMultiWidget createWidget() {
        return new ListMultiWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"), true);
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        for (CheckBox checkBox : getSpyWidget().checkBoxes) {
            assertThat(checkBox.getVisibility(), is(View.VISIBLE));
            assertThat(checkBox.isEnabled(), is(Boolean.FALSE));
        }
    }
}
