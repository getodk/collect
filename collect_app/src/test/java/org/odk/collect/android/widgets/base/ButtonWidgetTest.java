package org.odk.collect.android.widgets.base;

import android.widget.Button;

import org.javarosa.core.model.data.IAnswerData;
import org.junit.Test;
import org.odk.collect.android.widgets.interfaces.ButtonWidget;


/**
 * @author Shobhit Agarwal
 */
public abstract class ButtonWidgetTest<W extends ButtonWidget, A extends IAnswerData>
        extends QuestionWidgetTest<W, A> {

    @Test
    public void performButtonClickShouldLaunchIntent() {
        for (Button button : getWidget().getSimpleButtonList()) {
            button.performClick();
            buttonClicked(button);
        }
    }

    public void buttonClicked(Button button) {

    }

}
