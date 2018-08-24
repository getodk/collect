package org.odk.collect.android.widgets;

import android.support.annotation.NonNull;

import org.odk.collect.android.widgets.base.GeneralSelectOneWidgetTest;
import org.robolectric.RuntimeEnvironment;

/**
 * @author James Knight
 */

public class SelectOneWidgetTest extends GeneralSelectOneWidgetTest<AbstractSelectOneWidget> {

    @NonNull
    @Override
    public SelectOneWidget createWidget() {
        return new SelectOneWidget(RuntimeEnvironment.application, formEntryPrompt, false);
    }
}
