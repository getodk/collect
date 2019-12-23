package org.odk.collect.android.widgets;

import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.javarosa.core.model.SelectChoice;
import org.junit.Test;
import org.odk.collect.android.formentry.questions.AudioVideoImageTextLabel;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.support.MockFormEntryPromptBuilder;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.widgets.base.GeneralSelectOneWidgetTest;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.support.RobolectricHelpers.populateRecyclerView;

/**
 * @author James Knight
 */
public class SelectOneAutocompleteWidgetTest extends GeneralSelectOneWidgetTest<SelectOneAutocompleteWidget> {

    @NonNull
    @Override
    public SelectOneAutocompleteWidget createWidget() {
        return new SelectOneAutocompleteWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"), false);
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        // No appearance
        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withIndex("i am index")
                .withSelectChoices(asList(
                        new SelectChoice("1", "1"),
                        new SelectChoice("2", "2")
                ))
                .withReadOnly(true)
                .build();

        populateRecyclerView(getActualWidget());

        AudioVideoImageTextLabel avitLabel = (AudioVideoImageTextLabel) ((LinearLayout) ((RecyclerView) getWidget().answerLayout.getChildAt(1)).getLayoutManager().getChildAt(0)).getChildAt(0);
        assertThat(avitLabel.isEnabled(), is(Boolean.FALSE));

        resetWidget();

        // No-buttons appearance
        formEntryPrompt = new MockFormEntryPromptBuilder(formEntryPrompt)
                .withAppearance(WidgetAppearanceUtils.NO_BUTTONS)
                .build();

        populateRecyclerView(getActualWidget());

        FrameLayout view = (FrameLayout) ((RecyclerView) getWidget().answerLayout.getChildAt(1)).getLayoutManager().getChildAt(0);
        assertThat(view.isEnabled(), is(Boolean.FALSE));
    }
}
