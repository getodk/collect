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

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.joda.time.LocalDateTime;
import org.odk.collect.android.R;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.utilities.DateTimeUtils;
import org.odk.collect.android.widgets.utilities.DateTimeWidgetUtils;
import org.odk.collect.android.widgets.viewmodels.DateTimeViewModel;

/**
 * @author Grzegorz Orczykowski (gorczykowski@soldevelo.com)
 */
public abstract class CustomDatePickerDialog extends DialogFragment {
    private NumberPicker dayPicker;
    private NumberPicker monthPicker;
    private NumberPicker yearPicker;

    private TextView gregorianDateText;

    private DateTimeViewModel viewModel;
    private DateChangeListener dateChangeListener;

    public interface DateChangeListener {
        void onDateChanged(LocalDateTime selectedDate);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof DateChangeListener) {
            dateChangeListener = (DateChangeListener) context;
        }

        viewModel = new ViewModelProvider(this).get(DateTimeViewModel.class);
        if (viewModel.getLocalDateTime() == null) {
            viewModel.setLocalDateTime((LocalDateTime) getArguments().getSerializable(DateTimeWidgetUtils.DATE));
        }
        viewModel.setDatePickerDetails((DatePickerDetails) getArguments().getSerializable(DateTimeWidgetUtils.DATE_PICKER_DETAILS));

        viewModel.getSelectedDate().observe(this, localDateTime -> {
            if (localDateTime != null) {
                dateChangeListener.onDateChanged(localDateTime);
            }
        });
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialAlertDialogBuilder(getActivity())
                .setTitle(R.string.select_date)
                .setView(R.layout.custom_date_picker_dialog)
                .setPositiveButton(R.string.ok, (dialog, id) -> {
                    LocalDateTime date = DateTimeUtils.getDateAsGregorian(getOriginalDate());
                    viewModel.setSelectedDate(date.getYear(), date.getMonthOfYear() - 1, date.getDayOfMonth());
                    dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dismiss())
                .create();
    }

    @Override
    public void onDestroyView() {
        viewModel.setLocalDateTime(DateTimeUtils.getDateAsGregorian(getOriginalDate()));
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        gregorianDateText = getDialog().findViewById(R.id.date_gregorian);
        setUpPickers();
    }

    private void setUpPickers() {
        dayPicker = getDialog().findViewById(R.id.day_picker);
        dayPicker.setOnValueChangedListener((picker, oldVal, newVal) -> updateGregorianDateLabel());
        monthPicker = getDialog().findViewById(R.id.month_picker);
        monthPicker.setOnValueChangedListener((picker, oldVal, newVal) -> monthUpdated());
        yearPicker = getDialog().findViewById(R.id.year_picker);
        yearPicker.setOnValueChangedListener((picker, oldVal, newVal) -> yearUpdated());

        hidePickersIfNeeded();
    }

    private void hidePickersIfNeeded() {
        if (viewModel.getDatePickerDetails().isMonthYearMode()) {
            dayPicker.setVisibility(View.GONE);
        } else if (viewModel.getDatePickerDetails().isYearMode()) {
            dayPicker.setVisibility(View.GONE);
            monthPicker.setVisibility(View.GONE);
        }
    }

    protected void updateGregorianDateLabel() {
        String label = DateTimeWidgetUtils.getDateTimeLabel(DateTimeUtils.getDateAsGregorian(getOriginalDate()).toDate(),
                viewModel.getDatePickerDetails(), false, getContext());
        gregorianDateText.setText(label);
    }

    protected void setUpDayPicker(int dayOfMonth, int daysInMonth) {
        setUpDayPicker(1, dayOfMonth, daysInMonth);
    }

    protected void setUpDayPicker(int minDay, int dayOfMonth, int daysInMonth) {
        dayPicker.setMinValue(minDay);
        dayPicker.setMaxValue(daysInMonth);
        if (viewModel.getDatePickerDetails().isSpinnerMode()) {
            dayPicker.setValue(dayOfMonth);
        }
    }

    protected void setUpMonthPicker(int monthOfYear, String[] monthsArray) {
        // In Myanmar calendar we don't have specified amount of months, it's dynamic so clear
        // values first to avoid ArrayIndexOutOfBoundsException
        monthPicker.setDisplayedValues(null);
        monthPicker.setMaxValue(monthsArray.length - 1);
        monthPicker.setDisplayedValues(monthsArray);
        if (!viewModel.getDatePickerDetails().isYearMode()) {
            monthPicker.setValue(monthOfYear - 1);
        }
    }

    protected void setUpYearPicker(int year, int minSupportedYear, int maxSupportedYear) {
        yearPicker.setMinValue(minSupportedYear);
        yearPicker.setMaxValue(maxSupportedYear);
        yearPicker.setValue(year);
    }

    protected void monthUpdated() {
        updateDays();
        updateGregorianDateLabel();
    }

    protected void yearUpdated() {
        updateDays();
        updateGregorianDateLabel();
    }

    public int getDay() {
        return dayPicker.getValue();
    }

    public String getMonth() {
        return monthPicker.getDisplayedValues()[monthPicker.getValue()];
    }

    public int getMonthId() {
        return monthPicker.getValue();
    }

    public int getYear() {
        return yearPicker.getValue();
    }

    public LocalDateTime getDate() {
        return getDay() == 0 ? viewModel.getLocalDateTime() : getOriginalDate();
    }

    protected abstract void updateDays();

    protected abstract LocalDateTime getOriginalDate();
}
