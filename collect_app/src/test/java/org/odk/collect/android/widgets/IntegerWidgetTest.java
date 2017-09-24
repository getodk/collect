package org.odk.collect.android.widgets;

import android.support.annotation.NonNull;

import org.javarosa.core.model.data.IntegerData;
import org.odk.collect.android.widgets.base.GeneralStringWidgetTest;
import org.robolectric.RuntimeEnvironment;

/**
 * @author James Knight
 */
public class IntegerWidgetTest extends GeneralStringWidgetTest<IntegerWidget, IntegerData> {

    public IntegerWidgetTest() {
        super();
    }

    @NonNull
    @Override
    public IntegerWidget createWidget() {
        return new IntegerWidget(RuntimeEnvironment.application, formEntryPrompt, false);
    }

    @NonNull
    @Override
    public IntegerData getNextAnswer() {
        return new IntegerData(randomInteger());
    }

    private int randomInteger() {
        return Math.abs(random.nextInt()) % 1_000_000_000;
    }
}
