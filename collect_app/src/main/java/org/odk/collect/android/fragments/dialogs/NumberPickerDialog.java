/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.fragments.dialogs;

import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import org.odk.collect.android.R;

public class NumberPickerDialog extends DialogFragment {

    public static final String NUMBER_PICKER_DIALOG_TAG = "numberPickerDialogTag";
    public static final String WIDGET_ID = "widgetId";
    public static final String DISPLAYED_VALUES = "displayedValues";
    public static final String PROGRESS = "progress";

    public interface NumberPickerListener {
        void onNumberPickerValueSelected(int widgetId, int value);
    }

    private NumberPickerListener listener;

    public static NumberPickerDialog newInstance(int widgetId, String[] displayedValues, int progress) {
        Bundle bundle = new Bundle();
        bundle.putInt(WIDGET_ID, widgetId);
        bundle.putSerializable(DISPLAYED_VALUES, displayedValues);
        bundle.putInt(PROGRESS, progress);

        NumberPickerDialog dialogFragment = new NumberPickerDialog();
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof NumberPickerListener) {
            listener = (NumberPickerListener) context;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.number_picker_dialog, null);

        final NumberPicker numberPicker = view.findViewById(R.id.number_picker);
        numberPicker.setMaxValue(((String[]) getArguments().getSerializable(DISPLAYED_VALUES)).length - 1);
        numberPicker.setMinValue(0);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setDisplayedValues((String[]) getArguments().getSerializable(DISPLAYED_VALUES));
        numberPicker.setValue(((String[]) getArguments().getSerializable(DISPLAYED_VALUES)).length - 1 - getArguments().getInt(PROGRESS));

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.number_picker_title)
                .setView(view)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                listener.onNumberPickerValueSelected(getArguments().getInt(WIDGET_ID), numberPicker.getValue());
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                .create();
    }
}