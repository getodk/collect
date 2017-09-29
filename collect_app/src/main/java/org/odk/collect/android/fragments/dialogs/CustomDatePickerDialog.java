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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.chrono.GregorianChronology;
import org.odk.collect.android.R;
import org.odk.collect.android.utilities.DateTimeUtils;
import org.odk.collect.android.widgets.AbstractDateWidget;

/**
 * @author Grzegorz Orczykowski (gorczykowski@soldevelo.com)
 */
public abstract class CustomDatePickerDialog extends DialogFragment {
    public static final String DATE_PICKER_DIALOG = "datePickerDialog";

    protected static final String WIDGET_ID = "widgetId";
    protected static final String DAY = "day";
    protected static final String MONTH = "month";
    protected static final String YEAR = "year";
    protected static final String CALENDAR_MODE = "calendarMode";

    protected NumberPicker dayPicker;
    protected NumberPicker monthPicker;
    protected NumberPicker yearPicker;

    private TextView gregorianDateText;

    private int widgetId;
    protected int day;
    protected int month;
    protected int year;

    private AbstractDateWidget.CalendarMode calendarMode;

    public interface CustomDatePickerDialogListener {
        void onDateChanged(int widgetId, int day, int month, int year);
    }

    private CustomDatePickerDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (CustomDatePickerDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle savedInstanceStateToRead = savedInstanceState;
        if (savedInstanceStateToRead == null) {
            savedInstanceStateToRead = getArguments();
        }

        widgetId = savedInstanceStateToRead.getInt(WIDGET_ID);
        day = savedInstanceStateToRead.getInt(DAY);
        month = savedInstanceStateToRead.getInt(MONTH);
        year = savedInstanceStateToRead.getInt(YEAR);
        calendarMode = (AbstractDateWidget.CalendarMode) savedInstanceStateToRead.getSerializable(CALENDAR_MODE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.select_date)
                .setView(R.layout.custom_date_picker_dialog)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        DateTime dateTime = getDateAsGregorian(getOriginalDate());
                        listener.onDateChanged(widgetId, dateTime.getDayOfMonth(), dateTime.getMonthOfYear(), dateTime.getYear());
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

    @Override
    public void onResume() {
        super.onResume();
        gregorianDateText = (TextView) getDialog().findViewById(R.id.date_gregorian);
        setUpPickers();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        DateTime dateTime = getDateAsGregorian(getOriginalDate());
        outState.putInt(WIDGET_ID, widgetId);
        outState.putInt(DAY, dateTime.getDayOfMonth());
        outState.putInt(MONTH, dateTime.getMonthOfYear());
        outState.putInt(YEAR, dateTime.getYear());
        outState.putSerializable(CALENDAR_MODE, calendarMode);

        super.onSaveInstanceState(outState);
    }

    private void setUpPickers() {
        dayPicker = (NumberPicker) getDialog().findViewById(R.id.day_picker);
        dayPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateGregorianDateLabel();
            }
        });
        monthPicker = (NumberPicker) getDialog().findViewById(R.id.month_picker);
        monthPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateDays();
                updateGregorianDateLabel();
            }
        });
        yearPicker = (NumberPicker) getDialog().findViewById(R.id.year_picker);
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
        if (calendarMode.equals(AbstractDateWidget.CalendarMode.MONTH_YEAR)) {
            dayPicker.setVisibility(View.GONE);
            dayPicker.setValue(0);
        } else if (calendarMode.equals(AbstractDateWidget.CalendarMode.YEAR)) {
            dayPicker.setVisibility(View.GONE);
            monthPicker.setVisibility(View.GONE);
            dayPicker.setValue(0);
            yearPicker.setValue(0);
        }
    }

    protected static Bundle getArgs(int widgetId, int day, int month, int year, AbstractDateWidget.CalendarMode calendarMode) {
        Bundle args = new Bundle();
        args.putInt(WIDGET_ID, widgetId);
        args.putInt(DAY, day);
        args.putInt(MONTH, month);
        args.putInt(YEAR, year);
        args.putSerializable(CALENDAR_MODE, calendarMode);

        return args;
    }

    protected void updateGregorianDateLabel() {
        gregorianDateText.setText(DateTimeUtils.getDateTimeBasedOnUserLocale(getDateAsGregorian(getOriginalDate()).toLocalDate().toDate(), calendarMode, false));
    }

    protected DateTime getDateAsGregorian(DateTime dateTime) {
        return dateTime.withChronology(GregorianChronology.getInstance());
    }

    protected abstract void updateDays();

    protected abstract DateTime getOriginalDate();
}