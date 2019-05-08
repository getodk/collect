package org.odk.collect.android.widgets;

import androidx.annotation.NonNull;

import org.javarosa.core.model.SelectChoice;
import org.odk.collect.android.widgets.base.GeneralSelectMultiWidgetTest;

import java.util.List;

import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */
public class SpinnerMultiWidgetTest extends GeneralSelectMultiWidgetTest<SpinnerMultiWidget> {

    @NonNull
    @Override
    public SpinnerMultiWidget createWidget() {
        return new SpinnerMultiWidget(activity, formEntryPrompt);
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
}
