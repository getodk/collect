package org.odk.collect.android.widgets;

import android.support.annotation.NonNull;

import org.javarosa.core.model.data.IntegerData;
import org.odk.collect.android.widgets.base.GeneralStringWidgetTest;
import org.robolectric.RuntimeEnvironment;

import java.util.Random;

/**
 * @author James Knight
 */
public class IntegerWidgetTest extends GeneralStringWidgetTest<IntegerWidget, IntegerData> {

    @NonNull
    @Override
    public IntegerWidget createWidget() {
        Random random = new Random();
        boolean useThousandSeparator = random.nextBoolean();
        return new IntegerWidget(RuntimeEnvironment.application, formEntryPrompt, false, useThousandSeparator);
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
