package org.odk.collect.android.widgets;

import androidx.annotation.NonNull;

import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.base.GeneralSelectOneWidgetTest;

/**
 * @author James Knight
 */
public class SelectOneAutocompleteWidgetTest extends GeneralSelectOneWidgetTest<SelectOneAutocompleteWidget> {

    @NonNull
    @Override
    public SelectOneAutocompleteWidget createWidget() {
        return new SelectOneAutocompleteWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"), false);
    }
}
