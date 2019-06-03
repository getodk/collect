package org.odk.collect.android.widgets;

import androidx.annotation.NonNull;

import org.odk.collect.android.widgets.base.GeneralSelectOneWidgetTest;

/**
 * @author James Knight
 */

public class GridWidgetTest extends GeneralSelectOneWidgetTest<GridWidget> {
    @NonNull
    @Override
    public GridWidget createWidget() {
        return new GridWidget(activity, formEntryPrompt, false);
    }
}