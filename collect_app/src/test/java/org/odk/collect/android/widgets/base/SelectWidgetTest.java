package org.odk.collect.android.widgets.base;

import android.view.View;
import android.widget.TextView;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.junit.Test;
import org.odk.collect.android.R;
import org.odk.collect.android.support.MockFormEntryPromptBuilder;
import org.odk.collect.android.widgets.QuestionWidget;
import org.odk.collect.android.widgets.interfaces.Widget;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static java.util.Arrays.asList;

/**
 * @author James Knight
 */
public abstract class SelectWidgetTest<W extends Widget, A extends IAnswerData>
        extends QuestionWidgetTest<W, A> {

    private List<SelectChoice> selectChoices;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        selectChoices = createSelectChoices();

        when(formEntryPrompt.getSelectChoices()).thenReturn(selectChoices);
    }

    @Test
    public void whenChoicesWithoutValueExist_warningIsDisplayed() {
        formEntryPrompt = new MockFormEntryPromptBuilder(formEntryPrompt)
                .withSelectChoices(asList(
                        new SelectChoice("A", "a"),
                        new SelectChoice("B", "")
                ))
                .build();

        TextView warningTv = ((QuestionWidget) getWidget()).findViewById(R.id.warning_text);
        assertThat(warningTv.getVisibility(), is(View.VISIBLE));
        assertThat(warningTv.getText(), is("1 choice has no value and is not shown."));
    }

    public List<SelectChoice> getSelectChoices() {
        return selectChoices;
    }

    private List<SelectChoice> createSelectChoices() {
        int choiceCount = (Math.abs(random.nextInt()) % 3) + 2;

        List<SelectChoice> selectChoices = new ArrayList<>();
        for (int i = 0; i < choiceCount; i++) {
            SelectChoice selectChoice = new SelectChoice(Integer.toString(i), RandomString.make());

            selectChoice.setIndex(i);
            selectChoices.add(selectChoice);
        }

        return selectChoices;
    }
}
