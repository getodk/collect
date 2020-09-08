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
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import org.joda.time.LocalDateTime;
import org.joda.time.chrono.GregorianChronology;
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

    private DateTimeViewModel dateTimeViewModel;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dateTimeViewModel = new ViewModelProvider(requireActivity()).get(DateTimeViewModel.class);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.select_date)
                .setView(R.layout.custom_date_picker_dialog)
                .setPositiveButton(R.string.ok, (dialog, id) -> {
                    LocalDateTime date = getDateAsGregorian(getOriginalDate());
                    dateTimeViewModel.setSelectedDateTime(date.getYear(), date.getMonthOfYear() - 1, date.getDayOfMonth());
                    dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dismiss())
                .create();
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
        dayPicker.setValue(1);
        monthPicker = getDialog().findViewById(R.id.month_picker);
        monthPicker.setOnValueChangedListener((picker, oldVal, newVal) -> monthUpdated());
        yearPicker = getDialog().findViewById(R.id.year_picker);
        yearPicker.setOnValueChangedListener((picker, oldVal, newVal) -> yearUpdated());

        hidePickersIfNeeded();
    }

    private void hidePickersIfNeeded() {
        DatePickerDetails datePickerDetails = (DatePickerDetails) getArguments().getSerializable(DateTimeWidgetUtils.DATE_PICKER_DETAILS);
        if (datePickerDetails.isMonthYearMode()) {
            dayPicker.setVisibility(View.GONE);
            dayPicker.setValue(1);
        } else if (datePickerDetails.isYearMode()) {
            dayPicker.setVisibility(View.GONE);
            monthPicker.setVisibility(View.GONE);
            dayPicker.setValue(1);
            yearPicker.setValue(1);
        }
    }

    private LocalDateTime getDateAsGregorian(LocalDateTime date) {
        return DateTimeUtils.skipDaylightSavingGapIfExists(date)
                .toDateTime()
                .withChronology(GregorianChronology.getInstance())
                .toLocalDateTime();
    }

    protected void updateGregorianDateLabel() {
        String label = DateTimeUtils.getDateTimeLabel(getDateAsGregorian(getOriginalDate()).toDate(),
                (DatePickerDetails) getArguments().getSerializable(DateTimeWidgetUtils.DATE_PICKER_DETAILS), false, getContext());
        gregorianDateText.setText(label);
    }

    protected void setUpDayPicker(int dayOfMonth, int daysInMonth) {
        setUpDayPicker(1, dayOfMonth, daysInMonth);
    }

    protected void setUpDayPicker(int minDay, int dayOfMonth, int daysInMonth) {
        dayPicker.setMinValue(minDay);
        dayPicker.setMaxValue(daysInMonth);
        if (((DatePickerDetails) getArguments().getSerializable(DateTimeWidgetUtils.DATE_PICKER_DETAILS)).isSpinnerMode()) {
            dayPicker.setValue(dayOfMonth);
        }
    }

    protected void setUpMonthPicker(int monthOfYear, String[] monthsArray) {
        // In Myanmar calendar we don't have specified amount of months, it's dynamic so clear
        // values first to avoid ArrayIndexOutOfBoundsException
        monthPicker.setDisplayedValues(null);
        monthPicker.setMaxValue(monthsArray.length - 1);
        monthPicker.setDisplayedValues(monthsArray);
        if (!((DatePickerDetails) getArguments().getSerializable(DateTimeWidgetUtils.DATE_PICKER_DETAILS)).isYearMode()) {
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
        return (LocalDateTime) getArguments().getSerializable(DateTimeWidgetUtils.DATE);
    }

    public LocalDateTime getDateWithSkippedDaylightSavingGapIfExists() {
        return DateTimeUtils
                .skipDaylightSavingGapIfExists(getDate())
                .toDateTime()
                .toLocalDateTime();
    }

    protected abstract void updateDays();

    protected abstract LocalDateTime getOriginalDate();
}