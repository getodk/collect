package org.odk.collect.android.widgets;

import androidx.annotation.NonNull;

import org.odk.collect.android.widgets.base.GeneralSelectMultiWidgetTest;

/**
 * @author James Knight
 */

public class SelectMultiWidgetTest extends GeneralSelectMultiWidgetTest<SelectMultiWidget> {
    @NonNull
    @Override
    public SelectMultiWidget createWidget() {
        return new SelectMultiWidget(activity, formEntryPrompt);
    }
}
