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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import org.joda.time.LocalDateTime;
import org.joda.time.chrono.GregorianChronology;
import org.odk.collect.android.R;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.utilities.DateTimeUtils;
import org.odk.collect.android.widgets.AbstractDateWidget;

/**
 * @author Grzegorz Orczykowski (gorczykowski@soldevelo.com)
 */
public abstract class CustomDatePickerDialog extends DialogFragment {
    public static final String DATE_PICKER_DIALOG = "datePickerDialog";

    private static final String DATE_WIDGET = "widget";
    private static final String DATE = "date";
    private static final String DATE_PICKER_DETAILS = "datePickerDetails";

    private NumberPicker dayPicker;
    private NumberPicker monthPicker;
    private NumberPicker yearPicker;

    private LocalDateTime date;

    private TextView gregorianDateText;

    private AbstractDateWidget dateWidget;

    private DatePickerDetails datePickerDetails;

    protected static Bundle getArgs(AbstractDateWidget widget, LocalDateTime date, DatePickerDetails datePickerDetails) {
        Bundle args = new Bundle();
        args.putSerializable(DATE_WIDGET, widget);
        args.putSerializable(DATE, date);
        args.putSerializable(DATE_PICKER_DETAILS, datePickerDetails);

        return args;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle savedInstanceStateToRead = savedInstanceState;
        if (savedInstanceStateToRead == null) {
            savedInstanceStateToRead = getArguments();
        }

        dateWidget = (AbstractDateWidget) savedInstanceStateToRead.getSerializable(DATE_WIDGET);
        date = (LocalDateTime) savedInstanceStateToRead.getSerializable(DATE);
        datePickerDetails = (DatePickerDetails) savedInstanceStateToRead.getSerializable(DATE_PICKER_DETAILS);
    }

    @Override
    public void onResume() {
        super.onResume();
        gregorianDateText = getDialog().findViewById(R.id.date_gregorian);
        setUpPickers();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.select_date)
                .setView(R.layout.custom_date_picker_dialog)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dateWidget.setBinaryData(getDateAsGregorian(getOriginalDate()));
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                })
                .create();
    }

    private void setUpPickers() {
        dayPicker = getDialog().findViewById(R.id.day_picker);
        dayPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateGregorianDateLabel();
            }
        });
        monthPicker = getDialog().findViewById(R.id.month_picker);
        monthPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateDays();
                updateGregorianDateLabel();
            }
        });
        yearPicker = getDialog().findViewById(R.id.year_picker);
        yearPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateDays();
                updateGregorianDateLabel();
            }
        });

        hidePickersIfNeeded();
    }

    private void hidePickersIfNeeded() {
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(DATE_WIDGET, dateWidget);
        outState.putSerializable(DATE, getDateAsGregorian(getOriginalDate()));
        outState.putSerializable(DATE_PICKER_DETAILS, datePickerDetails);

        super.onSaveInstanceState(outState);
    }

    protected void updateGregorianDateLabel() {
        String label = DateTimeUtils.getDateTimeLabel(getDateAsGregorian(getOriginalDate()).toDate(), datePickerDetails, false, getContext());
        gregorianDateText.setText(label);
    }

    protected void setUpDayPicker(LocalDateTime date) {
        dayPicker.setMinValue(1);
        dayPicker.setMaxValue(date.dayOfMonth().getMaximumValue());
        if (datePickerDetails.isSpinnerMode()) {
            dayPicker.setValue(date.getDayOfMonth());
        }
    }

    protected void setUpMonthPicker(LocalDateTime date, String[] monthsArray) {
        monthPicker.setMaxValue(monthsArray.length - 1);
        monthPicker.setDisplayedValues(monthsArray);
        if (!datePickerDetails.isYearMode()) {
            monthPicker.setValue(date.getMonthOfYear() - 1);
        }
    }

    protected void setUpYearPicker(LocalDateTime date, int minSupportedYear, int maxSupportedYear) {
        yearPicker.setMinValue(minSupportedYear);
        yearPicker.setMaxValue(maxSupportedYear);
        yearPicker.setValue(date.getYear());
    }

    public int getDay() {
        return dayPicker.getValue();
    }

    public String getMonth() {
        return monthPicker.getDisplayedValues()[monthPicker.getValue()];
    }

    public int getYear() {
        return yearPicker.getValue();
    }

    public LocalDateTime getDate() {
        return date;
    }

    protected abstract void updateDays();

    protected abstract LocalDateTime getOriginalDate();
}