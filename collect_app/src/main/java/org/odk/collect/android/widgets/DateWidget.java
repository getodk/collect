/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.odk.collect.android.R;
import org.odk.collect.android.utilities.DateTimeUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Date;

import timber.log.Timber;

import static android.content.Context.ACCESSIBILITY_SERVICE;

/**
 * Displays a DatePicker widget. DateWidget handles leap years and does not allow dates that do not
 * exist.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class DateWidget extends QuestionWidget implements DatePickerDialog.OnDateSetListener {
    private DatePickerDialog datePickerDialog;

    private Button dateButton;
    private TextView dateTextView;

    private boolean hideDay;
    private boolean hideMonth;
    private boolean showCalendar;

    private int year;
    private int month;
    private int dayOfMonth;

    private boolean nullAnswer;

    public DateWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        setGravity(Gravity.START);

        readAppearance();
        createDateButton();
        dateTextView = getAnswerTextView();
        createDatePickerDialog();
        hideDayFieldIfNotInFormat();
        addViews();
    }

    private void readAppearance() {
        String appearance = formEntryPrompt.getQuestion().getAppearanceAttr();
        if ("month-year".equals(appearance)) {
            hideDay = true;
        } else if ("year".equals(appearance)) {
            hideDay = true;
            hideMonth = true;
        } else if (!"no-calendar".equals(appearance)) {
            showCalendar = true;
        }
    }

    private void hideDayFieldIfNotInFormat() {
        if (hideDay) {
            datePickerDialog.getDatePicker().findViewById(
                    Resources.getSystem().getIdentifier("day", "id", "android"))
                    .setVisibility(View.GONE);
        }
        if (hideMonth) {
            datePickerDialog.getDatePicker().findViewById(
                    Resources.getSystem().getIdentifier("month", "id", "android"))
                    .setVisibility(View.GONE);
        }
    }

    @Override
    public void clearAnswer() {
        nullAnswer = true;
        dateTextView.setText(R.string.no_date_selected);
    }

    @Override
    public IAnswerData getAnswer() {
        clearFocus();

        if (nullAnswer) {
            return null;
        } else {
            // This is LDT but TimeWidget is just DT. Seems like we should make these consistent.
            LocalDateTime ldt = new LocalDateTime()
                    .withYear(year)
                    .withMonthOfYear(hideMonth ? 1 : month)
                    .withDayOfMonth((hideMonth || hideDay) ? 1 : dayOfMonth)
                    .withHourOfDay(0)
                    .withMinuteOfHour(0);
            return new DateData(ldt.toDate());
        }
    }

    @Override
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        dateButton.setOnLongClickListener(l);
        dateTextView.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        dateButton.cancelLongPress();
        dateTextView.cancelLongPress();
    }

    private void createDateButton() {
        dateButton = getSimpleButton(getContext().getString(R.string.select_date));
        dateButton.setEnabled(!formEntryPrompt.isReadOnly());
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nullAnswer) {
                    setDateToCurrent();
                } else {
                    updateDate(year, month, dayOfMonth, true);
                }

                datePickerDialog.show();
            }
        });
    }

    private void addViews() {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(dateButton);
        linearLayout.addView(dateTextView);
        addAnswerView(linearLayout);
    }

    public void setDateLabel() {
        nullAnswer = false;
        dateTextView.setText(DateTimeUtils.getDateTimeBasedOnUserLocale(
                (Date) getAnswer().getValue(), formEntryPrompt.getQuestion().getAppearanceAttr(), false));
    }

    private int getTheme() {
        int theme = 0;
        // https://github.com/opendatakit/collect/issues/1424
        // https://github.com/opendatakit/collect/issues/1367
        if (!isBrokenSamsungDevice() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            theme = android.R.style.Theme_Material_Light_Dialog;
        }
        if (!showCalendar || (isBrokenSamsungDevice() && isTalkBackActive())) {
            theme = android.R.style.Theme_Holo_Light_Dialog;
        }

        return theme;
    }

    private void createDatePickerDialog() {
        datePickerDialog = new CustomDatePickerDialog(getContext(), getTheme(), this, 1971, 1, 1); // placeholder date that is valid
        datePickerDialog.setCanceledOnTouchOutside(false);

        if (formEntryPrompt.getAnswerValue() == null) {
            clearAnswer();

        } else {
            Date date = (Date) formEntryPrompt.getAnswerValue().getValue();

            // This is LDT but TimeWidget is just DT, why?:
            LocalDateTime localDateTime = new LocalDateTime(date);
            updateDate(localDateTime.getYear(), localDateTime.getMonthOfYear(), localDateTime.getDayOfMonth(), true);
        }
    }

    // https://stackoverflow.com/questions/28618405/datepicker-crashes-on-my-device-when-clicked-with-personal-app
    private boolean isBrokenSamsungDevice() {
        return (Build.MANUFACTURER.equalsIgnoreCase("samsung")
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    // https://stackoverflow.com/a/34853067/5479029
    private boolean isTalkBackActive() {
        return ((AccessibilityManager) getContext().getSystemService(ACCESSIBILITY_SERVICE)).isTouchExplorationEnabled();
    }

    public boolean isDayHidden() {
        return hideDay;
    }

    public boolean isMonthHidden() {
        return hideMonth;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return dayOfMonth;
    }

    public boolean isNullAnswer() {
        return nullAnswer;
    }

    public void setDateToCurrent() {
        updateDate(DateTime.now(), false);
    }

    public void updateDate(DateTime dateTime) {
        updateDate(dateTime, true);
    }

    public void updateDate(DateTime dateTime, boolean shouldUpdateLabel) {
        updateDate(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), shouldUpdateLabel);
    }

    public void updateDate(int year, int month, int dayOfMonth, boolean shouldUpdateLabel) {
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;

        datePickerDialog.updateDate(year, month - 1, dayOfMonth);
        if (shouldUpdateLabel) {
            setDateLabel();
        }
    }

    // Exposed for testing purposes to avoid reflection.
    public void setDatePickerDialog(DatePickerDialog datePickerDialog) {
        this.datePickerDialog = datePickerDialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        this.year = year;
        this.month = month + 1;
        this.dayOfMonth = dayOfMonth;

        setDateLabel();
    }

    private class CustomDatePickerDialog extends DatePickerDialog {
        private String dialogTitle = getContext().getString(R.string.select_date);
        private int theme;

        CustomDatePickerDialog(Context context, int theme, OnDateSetListener listener, int year, int month, int dayOfMonth) {
            super(context, theme, listener, year, month, dayOfMonth);
            this.theme = theme;
            if (theme == android.R.style.Theme_Holo_Light_Dialog) {
                setTitle(dialogTitle);
                fixSpinner(context, year, month, dayOfMonth);

                Window window = getWindow();
                if (window != null) {
                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }

                //noinspection deprecation
                getDatePicker().setCalendarViewShown(false);
            }
        }

        public void setTitle(CharSequence title) {
            if (theme == android.R.style.Theme_Holo_Light_Dialog) {
                super.setTitle(dialogTitle);
            }
        }

        /**
         * Workaround for this bug: https://code.google.com/p/android/issues/detail?id=222208
         * In Android 7.0 Nougat, spinner mode for the DatePicker in DatePickerDialog is
         * incorrectly displayed as calendar, even when the theme specifies otherwise.
         * <p>
         * Source: https://gist.github.com/jeffdgr8/6bc5f990bf0c13a7334ce385d482af9f
         */
        private void fixSpinner(Context context, int year, int month, int dayOfMonth) {
            // The spinner vs not distinction probably started in lollipop but applying this
            // for versions < nougat leads to a crash trying to get DatePickerSpinnerDelegate
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
                try {
                    // Get the theme's android:datePickerMode
                    final int MODE_SPINNER = 2;
                    Class<?> styleableClass = Class.forName("com.android.internal.R$styleable");
                    Field datePickerStyleableField = styleableClass.getField("DatePicker");
                    int[] datePickerStyleable = (int[]) datePickerStyleableField.get(null);
                    final TypedArray a = context.obtainStyledAttributes(null, datePickerStyleable,
                            android.R.attr.datePickerStyle, 0);
                    Field datePickerModeStyleableField = styleableClass.getField("DatePicker_datePickerMode");
                    int datePickerModeStyleable = datePickerModeStyleableField.getInt(null);
                    final int mode = a.getInt(datePickerModeStyleable, MODE_SPINNER);
                    a.recycle();

                    if (mode == MODE_SPINNER) {

                        Field datePickerField = findField(DatePickerDialog.class,
                                DatePicker.class, "mDatePicker");
                        if (datePickerField == null) {
                            Timber.w("Reflection failed: couldn't find 'mDatePicker' field on DatePickerDialog.");
                            return;
                        }

                        DatePicker datePicker = (DatePicker) datePickerField.get(this);
                        Class<?> delegateClass = Class.forName("android.widget.DatePicker$DatePickerDelegate");

                        Field delegateField = findField(DatePicker.class, delegateClass, "mDelegate");
                        if (delegateField == null) {
                            Timber.w("Reflection failed: couldn't find 'mDelegate' field on DatePickerDialog.");
                            return;
                        }

                        Object delegate = delegateField.get(datePicker);

                        Class<?> spinnerDelegateClass = Class.forName("android.widget.DatePickerSpinnerDelegate");

                        // In 7.0 Nougat for some reason the datePickerMode is ignored and the
                        // delegate is DatePickerCalendarDelegate
                        if (delegate.getClass() != spinnerDelegateClass) {
                            delegateField.set(datePicker, null); // throw out the DatePickerCalendarDelegate!
                            datePicker.removeAllViews(); // remove the DatePickerCalendarDelegate views

                            Constructor spinnerDelegateConstructor = spinnerDelegateClass
                                    .getDeclaredConstructor(DatePicker.class, Context.class,
                                            AttributeSet.class, int.class, int.class);
                            spinnerDelegateConstructor.setAccessible(true);

                            // Instantiate a DatePickerSpinnerDelegate
                            delegate = spinnerDelegateConstructor.newInstance(datePicker, context,
                                    null, android.R.attr.datePickerStyle, 0);

                            // set the DatePicker.mDelegate to the spinner delegate
                            delegateField.set(datePicker, delegate);

                            // Set up the DatePicker again, with the DatePickerSpinnerDelegate
                            datePicker.updateDate(year, month, dayOfMonth);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        private Field findField(Class objectClass, Class fieldClass, String expectedName) {
            try {
                Field field = objectClass.getDeclaredField(expectedName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                Timber.i(e); // ignore
            }

            // search for it if it wasn't found under the expected ivar name
            for (Field searchField : objectClass.getDeclaredFields()) {
                if (searchField.getType() == fieldClass) {
                    searchField.setAccessible(true);
                    return searchField;
                }
            }
            return null;
        }
    }
}
