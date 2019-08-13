package org.odk.collect.android.widgets;

import androidx.annotation.NonNull;

import org.javarosa.core.model.data.IntegerData;
import org.odk.collect.android.widgets.base.RangeWidgetTest;

/**
 * @author James Knight
 */

public class RangeIntegerWidgetTest extends RangeWidgetTest<RangeIntegerWidget, IntegerData> {

    @NonNull
    @Override
    public RangeIntegerWidget createWidget() {
        return new RangeIntegerWidget(activity, formEntryPrompt);
    }

    @NonNull
    @Override
    public IntegerData getNextAnswer() {
        return new IntegerData(random.nextInt());
    }
}
