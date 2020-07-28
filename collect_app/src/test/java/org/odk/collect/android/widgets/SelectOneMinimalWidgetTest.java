package org.odk.collect.android.widgets;

import android.view.View;

import androidx.annotation.NonNull;

import org.junit.Test;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.base.GeneralSelectOneWidgetTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

public class SelectOneMinimalWidgetTest extends GeneralSelectOneWidgetTest<SelectOneMinimalWidget> {
    @NonNull
    @Override
    public SelectOneMinimalWidget createWidget() {
        return new SelectOneMinimalWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"), false);
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        assertThat(getSpyWidget().binding.choicesSearchBox.getVisibility(), is(View.VISIBLE));
        assertThat(getSpyWidget().binding.choicesSearchBox.isEnabled(), is(Boolean.FALSE));
    }
}