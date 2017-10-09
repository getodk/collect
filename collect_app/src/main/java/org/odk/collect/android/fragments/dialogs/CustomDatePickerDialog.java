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

import org.javarosa.core.model.FormIndex;
import org.joda.time.LocalDateTime;
import org.joda.time.chrono.GregorianChronology;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.utilities.DateTimeUtils;
import org.odk.collect.android.widgets.AbstractDateWidget;

/**
 * @author Grzegorz Orczykowski (gorczykowski@soldevelo.com)
 */
public abstract class CustomDatePickerDialog extends DialogFragment {
    public static final String DATE_PICKER_DIALOG = "datePickerDialog";

    private static final String FORM_INDEX = "formIndex";
    private static final String DATE = "date";
    private static final String CALENDAR_MODE = "calendarMode";

    protected NumberPicker dayPicker;
    protected NumberPicker monthPicker;
    protected NumberPicker yearPicker;

    protected LocalDateTime date;

    private TextView gregorianDateText;

    private FormIndex formIndex;

    protected AbstractDateWidget.CalendarMode calendarMode;

    public interface CustomDatePickerDialogListener {
        void onDateChanged(LocalDateTime date);
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

        formIndex = (FormIndex) savedInstanceStateToRead.getSerializable(FORM_INDEX);
        date = (LocalDateTime) savedInstanceStateToRead.getSerializable(DATE);
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
                        FormController formController = Collect.getInstance().getFormController();
                        if (formController != null) {
                            formController.setIndexWaitingForData(formIndex);
                        }
                        listener.onDateChanged(getDateAsGregorian(getOriginalDate()));
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
        outState.putSerializable(FORM_INDEX, formIndex);
        outState.putSerializable(DATE, getDateAsGregorian(getOriginalDate()));
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
            dayPicker.setValue(1);
        } else if (calendarMode.equals(AbstractDateWidget.CalendarMode.YEAR)) {
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

    protected static Bundle getArgs(FormIndex formIndex, LocalDateTime date, AbstractDateWidget.CalendarMode calendarMode) {
        Bundle args = new Bundle();
        args.putSerializable(FORM_INDEX, formIndex);
        args.putSerializable(DATE, date);
        args.putSerializable(CALENDAR_MODE, calendarMode);

        return args;
    }

    protected void updateGregorianDateLabel() {
        String label = DateTimeUtils.getDateTime(getDateAsGregorian(
                getOriginalDate()).toDate(), DateTimeUtils.getAppearanceBasedOnCalendarMode(calendarMode), false, getContext());
        gregorianDateText.setText(label);
    }

    protected abstract void updateDays();

    protected abstract LocalDateTime getOriginalDate();
}