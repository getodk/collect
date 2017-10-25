package org.odk.collect.android.widgets.base;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.odk.collect.android.widgets.interfaces.Widget;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

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
