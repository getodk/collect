package org.odk.collect.android.widgets;

import androidx.annotation.NonNull;

import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.base.GeneralSelectOneWidgetTest;

/**
 * @author James Knight
 */
public class SelectOneSearchWidgetTest extends GeneralSelectOneWidgetTest<SelectOneSearchWidget> {

    @NonNull
    @Override
    public SelectOneSearchWidget createWidget() {
        return new SelectOneSearchWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"), false);
    }
}
