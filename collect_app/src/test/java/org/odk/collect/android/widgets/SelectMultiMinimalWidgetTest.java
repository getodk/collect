package org.odk.collect.android.widgets;

import android.view.View;

import androidx.annotation.NonNull;

import org.javarosa.core.model.SelectChoice;
import org.junit.Test;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.base.GeneralSelectMultiWidgetTest;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

public class SelectMultiMinimalWidgetTest extends GeneralSelectMultiWidgetTest<SelectMultiMinimalWidget> {

    @NonNull
    @Override
    public SelectMultiMinimalWidget createWidget() {
        return new SelectMultiMinimalWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"));
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        List<SelectChoice> selectChoices = getSelectChoices();
        for (SelectChoice selectChoice : selectChoices) {
            when(formEntryPrompt.getSelectChoiceText(selectChoice))
                    .thenReturn(selectChoice.getValue());
        }
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        assertThat(getSpyWidget().binding.choicesSearchBox.getVisibility(), is(View.VISIBLE));
        assertThat(getSpyWidget().binding.choicesSearchBox.isEnabled(), is(Boolean.FALSE));
    }
}