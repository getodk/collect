package org.odk.collect.android.widgets;

import android.view.View;
import android.widget.RadioButton;

import androidx.annotation.NonNull;

import org.junit.Test;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.base.GeneralSelectOneWidgetTest;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */

public class ListWidgetTest extends GeneralSelectOneWidgetTest<ListWidget> {
    @NonNull
    @Override
    public ListWidget createWidget() {
        return new ListWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"), false, false);
    }

    @Test
    public void readOnlyTest() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        for(RadioButton radioButton : getWidget().buttons) {
            assertEquals(View.VISIBLE, radioButton.getVisibility());
            assertFalse(radioButton.isEnabled());
        }
    }
}
