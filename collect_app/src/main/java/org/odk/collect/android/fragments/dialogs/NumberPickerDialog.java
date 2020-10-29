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
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.R;
import org.odk.collect.android.widgets.viewmodels.RangePickerViewModel;

public class NumberPickerDialog extends DialogFragment {
    public static final String DISPLAYED_VALUES = "displayedValues";
    public static final String PROGRESS = "progress";

    private RangePickerViewModel rangePickerViewModel;
    private NumberPickerListener listener;

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        try {
            listener = (NumberPickerListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnHeadlineSelectedListener");
        }

        rangePickerViewModel = new ViewModelProvider(this).get(RangePickerViewModel.class);
        rangePickerViewModel.setDisplayedValuesForNumberPicker((String[]) getArguments().getSerializable(DISPLAYED_VALUES));
        rangePickerViewModel.setProgress(getArguments().getInt(PROGRESS));

        rangePickerViewModel.getNumberPickerValue().observe(this, answer -> {
            if (answer != null) {
                listener.onNumberPickerValueSelected(answer);
            }
        });
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.number_picker_dialog, null);

        final NumberPicker numberPicker = view.findViewById(R.id.number_picker);
        numberPicker.setMaxValue(rangePickerViewModel.getDisplayedValuesForNumberPicker().length - 1);
        numberPicker.setMinValue(0);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setDisplayedValues(rangePickerViewModel.getDisplayedValuesForNumberPicker());
        numberPicker.setValue(rangePickerViewModel.getDisplayedValuesForNumberPicker().length - rangePickerViewModel.getProgress() -1);

        return new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.number_picker_title)
                .setView(view)
                .setPositiveButton(R.string.ok,
                        (dialog, whichButton) -> rangePickerViewModel.setNumberPickerValue(numberPicker.getValue()))
                .setNegativeButton(R.string.cancel, (dialog, whichButton) -> { })
                .create();
    }

    public interface NumberPickerListener {
        void onNumberPickerValueSelected(Integer value);
    }
}