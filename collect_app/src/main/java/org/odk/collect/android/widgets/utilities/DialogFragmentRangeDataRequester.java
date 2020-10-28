package org.odk.collect.android.widgets.utilities;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.javarosa.core.model.FormIndex;
import org.odk.collect.android.fragments.dialogs.NumberPickerDialog;
import org.odk.collect.android.utilities.DialogUtils;
import org.odk.collect.android.widgets.interfaces.RangeWidgetDataRequester;

public class DialogFragmentRangeDataRequester implements RangeWidgetDataRequester {

    private final Context context;
    private final WaitingForDataRegistry waitingForDataRegistry;

    public DialogFragmentRangeDataRequester(Context context, WaitingForDataRegistry waitingForDataRegistry) {
        this.context = context;
        this.waitingForDataRegistry = waitingForDataRegistry;
    }

    @Override
    public void requestRangePickerValue(FormIndex formIndex, String[] displayedValuesForNumberPicker, int progress) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(NumberPickerDialog.DISPLAYED_VALUES, displayedValuesForNumberPicker);
        bundle.putInt(NumberPickerDialog.PROGRESS, progress);

        DialogUtils.showIfNotShowing(NumberPickerDialog.class, bundle, ((AppCompatActivity) context).getSupportFragmentManager());
        waitingForDataRegistry.waitForData(formIndex);
    }
}
