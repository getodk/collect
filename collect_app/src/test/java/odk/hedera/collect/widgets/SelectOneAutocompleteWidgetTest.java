package odk.hedera.collect.widgets;

import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.javarosa.core.model.SelectChoice;
import org.junit.Test;
import odk.hedera.collect.formentry.questions.AudioVideoImageTextLabel;
import odk.hedera.collect.formentry.questions.QuestionDetails;
import org.odk.hedera.collect.support.MockFormEntryPromptBuilder;
import odk.hedera.collect.utilities.WidgetAppearanceUtils;
import odk.hedera.collect.widgets.base.GeneralSelectOneWidgetTest;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.odk.hedera.collect.support.RobolectricHelpers.populateRecyclerView;

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

        populateRecyclerView(getWidget());

        AudioVideoImageTextLabel avitLabel = (AudioVideoImageTextLabel) ((LinearLayout) ((RecyclerView) getSpyWidget().answerLayout.getChildAt(1)).getLayoutManager().getChildAt(0)).getChildAt(0);
        assertThat(avitLabel.isEnabled(), is(Boolean.FALSE));

        resetWidget();

        // No-buttons appearance
        formEntryPrompt = new MockFormEntryPromptBuilder(formEntryPrompt)
                .withAppearance(WidgetAppearanceUtils.NO_BUTTONS)
                .build();

        populateRecyclerView(getWidget());

        FrameLayout view = (FrameLayout) ((RecyclerView) getSpyWidget().answerLayout.getChildAt(1)).getLayoutManager().getChildAt(0);
        assertThat(view.isEnabled(), is(Boolean.FALSE));
    }
}
