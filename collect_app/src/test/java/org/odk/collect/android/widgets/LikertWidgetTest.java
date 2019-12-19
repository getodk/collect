package org.odk.collect.android.widgets;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.annotation.NonNull;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.support.MockFormEntryPromptBuilder;
import org.odk.collect.android.widgets.base.QuestionWidgetTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;

public class LikertWidgetTest extends QuestionWidgetTest<LikertWidget, SelectOneData> {
    private List<SelectChoice> options = new ArrayList<>();

    @Before
    public void setup() {
        options = asList(
                new SelectChoice("1", "1"),
                new SelectChoice("2", "2"),
                new SelectChoice("3", "3"),
                new SelectChoice("4", "4"),
                new SelectChoice("5", "5"));

        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withIndex("i am index")
                .withSelectChoices(options)
                .withReadOnly(true)
                .build();
    }

    @NonNull
    @Override
    public LikertWidget createWidget() {
        return new LikertWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"));
    }

    @NonNull
    @Override
    public SelectOneData getNextAnswer() {
        return new SelectOneData(new Selection(options.get(0)));
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        for (int i = 0; i < getWidget().view.getChildCount(); i++) {
            LinearLayout optionView = (LinearLayout) getWidget().view.getChildAt(0);
            assertEquals(View.VISIBLE, optionView.getVisibility());
            assertFalse(optionView.isEnabled());
        }

        for (Map.Entry<RadioButton, String> radioButtonStringEntry : getWidget().buttonsToName.entrySet()) {
            assertEquals(View.VISIBLE, ((RadioButton) ((Map.Entry) radioButtonStringEntry).getKey()).getVisibility());
            assertFalse(((RadioButton) ((Map.Entry) radioButtonStringEntry).getKey()).isEnabled());
        }
    }
}
