package org.odk.collect.android.widgets;

import android.support.annotation.NonNull;

import org.odk.collect.android.widgets.base.GeneralSelectOneWidgetTest;
import org.robolectric.RuntimeEnvironment;

/**
 * @author James Knight
 */
public class SelectOneAutoAdvanceWidgetTest
        extends GeneralSelectOneWidgetTest<SelectOneAutoAdvanceWidget> {

    @NonNull
    @Override
    public SelectOneAutoAdvanceWidget createWidget() {
        return new SelectOneAutoAdvanceWidget(RuntimeEnvironment.application, formEntryPrompt);
    }
}