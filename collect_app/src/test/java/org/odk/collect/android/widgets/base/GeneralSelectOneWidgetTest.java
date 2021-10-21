package org.odk.collect.android.widgets.base;


import androidx.annotation.NonNull;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.junit.Test;
import org.odk.collect.android.widgets.interfaces.MultiChoiceWidget;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author James Knight
 */

public abstract class GeneralSelectOneWidgetTest<W extends MultiChoiceWidget>
        extends SelectWidgetTest<W, SelectOneData> {

    @NonNull
    @Override
    public SelectOneData getNextAnswer() {
        List<SelectChoice> selectChoices = getSelectChoices();

        int selectedIndex = Math.abs(random.nextInt()) % selectChoices.size();
        SelectChoice selectChoice = selectChoices.get(selectedIndex);

        Selection selection = new Selection(selectChoice);
        return new SelectOneData(selection);
    }

    @Test
    public void getAnswerShouldReflectTheCurrentlySelectedChoice() {
        W widget = getSpyWidget();
        assertNull(widget.getAnswer());

        List<SelectChoice> selectChoices = getSelectChoices();

        for (int i = 0; i < widget.getChoiceCount(); i++) {
            widget.setChoiceSelected(i, true);

            SelectChoice selectChoice = selectChoices.get(i);
            IAnswerData answer = widget.getAnswer();

            assertEquals(selectChoice.getValue(), answer.getDisplayText());
        }
    }
}
