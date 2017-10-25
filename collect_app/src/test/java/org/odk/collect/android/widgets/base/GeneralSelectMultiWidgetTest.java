package org.odk.collect.android.widgets.base;

import android.support.annotation.NonNull;

import com.google.common.collect.ImmutableList;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;
import org.junit.Test;
import org.odk.collect.android.widgets.interfaces.MultiChoiceWidget;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author James Knight
 */

public abstract class GeneralSelectMultiWidgetTest<W extends MultiChoiceWidget>
        extends SelectWidgetTest<W, SelectMultiData> {

    @NonNull
    @Override
    public SelectMultiData getNextAnswer() {
        List<SelectChoice> selectChoices = getSelectChoices();

        int selectedIndex = Math.abs(random.nextInt()) % selectChoices.size();
        SelectChoice selectChoice = selectChoices.get(selectedIndex);

        Selection selection = new Selection(selectChoice);
        return new SelectMultiData(ImmutableList.of(selection));
    }

    @Test
    public void getAnswerShouldReflectWhichSelectionsWereMade() {
        W widget = getWidget();
        assertNull(widget.getAnswer());

        List<SelectChoice> selectChoices = getSelectChoices();
        List<String> selectedValues = new ArrayList<>();

        boolean atLeastOneSelected = false;

        for (int i = 0; i < widget.getChoiceCount(); i++) {
            boolean shouldBeSelected = random.nextBoolean();
            widget.setChoiceSelected(i, shouldBeSelected);

            atLeastOneSelected = atLeastOneSelected || shouldBeSelected;

            if (shouldBeSelected) {
                SelectChoice selectChoice = selectChoices.get(i);
                selectedValues.add(selectChoice.getValue());
            }
        }

        // Make sure at least one item is selected, so we're not just retesting the
        // null answer case:
        if (!atLeastOneSelected) {
            int randomIndex = (Math.abs(random.nextInt()) % widget.getChoiceCount());
            widget.setChoiceSelected(randomIndex, true);

            SelectChoice selectChoice = selectChoices.get(randomIndex);
            selectedValues.add(selectChoice.getValue());
        }

        SelectMultiData answer = (SelectMultiData) widget.getAnswer();

        @SuppressWarnings("unchecked")
        List<Selection> answerSelections = (List<Selection>) answer.getValue();
        List<String> answerValues = selectionsToValues(answerSelections);

        for (String selectedValue : selectedValues) {
            assertTrue(answerValues.contains(selectedValue));
        }
    }

    private List<String> selectionsToValues(List<Selection> selections) {
        List<String> values = new ArrayList<>();
        for (Selection selection : selections) {
            values.add(selection.getValue());
        }

        return values;
    }
}
