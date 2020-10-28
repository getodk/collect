package org.odk.collect.android.widgets.interfaces;

import org.javarosa.core.model.FormIndex;

public interface RangeWidgetDataRequester {

    void requestRangePickerValue(FormIndex formIndex, String[] displayedValuesForNumberPicker, int progress);
}
