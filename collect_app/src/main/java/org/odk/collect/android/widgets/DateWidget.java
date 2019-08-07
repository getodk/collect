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
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.LocalDateTime;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.fragments.dialogs.BikramSambatDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.CopticDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.CustomDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.EthiopianDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.IslamicDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.MyanmarDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.PersianDatePickerDialog;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.utilities.DateTimeUtils;
import org.odk.collect.android.widgets.interfaces.BinaryWidget;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Date;

import timber.log.Timber;

import static org.odk.collect.android.fragments.dialogs.CustomDatePickerDialog.DATE_PICKER_DIALOG;

/**
 * Displays a DatePicker widget. DateWidget handles leap years and does not allow dates that do not
 * exist.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class DateWidget extends QuestionWidget implements DatePickerDialog.OnDateSetListener, BinaryWidget {
    private Button dateButton;
    private TextView dateTextView;

    boolean isNullAnswer;

    private LocalDateTime date;

    private DatePickerDetails datePickerDetails;

    public DateWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        createWidget();
    }

    protected void createWidget() {
        datePickerDetails = DateTimeUtils.getDatePickerDetails(getFormEntryPrompt().getQuestion().getAppearanceAttr());
        dateButton = getSimpleButton(getContext().getString(R.string.select_date));
        dateTextView = getAnswerTextView();
        addViews();
        if (getFormEntryPrompt().getAnswerValue() == null) {
            clearAnswer();
            setDateToCurrent();
        } else {
            date = new LocalDateTime(getFormEntryPrompt().getAnswerValue().getValue());
            setDateLabel();
        }
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

    @Override
    public void clearAnswer() {
        clearAnswerWithoutValueChangeEvent();
        widgetValueChanged();
    }

    void clearAnswerWithoutValueChangeEvent() {
        isNullAnswer = true;
        dateTextView.setText(R.string.no_date_selected);
        setDateToCurrent();
    }

    @Override
    public IAnswerData getAnswer() {
        return isNullAnswer ? null : new DateData(date.toDate());
    }

    @Override
    public void setBinaryData(Object answer) {
        if (answer instanceof LocalDateTime) {
            date = (LocalDateTime) answer;
            setDateLabel();
        }
    }

    @Override
    public void onButtonClick(int buttonId) {
        showDatePickerDialog();
    }

    public boolean isDayHidden() {
        return datePickerDetails.isMonthYearMode() || datePickerDetails.isYearMode();
    }

    public LocalDateTime getDate() {
        return date;
    }

    public boolean isNullAnswer() {
        return isNullAnswer;
    }

    private void addViews() {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(dateButton);
        linearLayout.addView(dateTextView);
        addAnswerView(linearLayout);
    }

    protected void setDateToCurrent() {
        date = LocalDateTime
                .now()
                .withHourOfDay(0)
                .withMinuteOfHour(0)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);
    }

    protected void setDateLabel() {
        isNullAnswer = false;
        dateTextView.setText(DateTimeUtils.getDateTimeLabel((Date) getAnswer().getValue(), datePickerDetails, false, getContext()));
    }

    protected void showDatePickerDialog() {
        switch (datePickerDetails.getDatePickerType()) {
            case ETHIOPIAN:
                CustomDatePickerDialog dialog = EthiopianDatePickerDialog.newInstance(getFormEntryPrompt().getIndex(), date, datePickerDetails);
                dialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), DATE_PICKER_DIALOG);
                break;
            case COPTIC:
                dialog = CopticDatePickerDialog.newInstance(getFormEntryPrompt().getIndex(), date, datePickerDetails);
                dialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), DATE_PICKER_DIALOG);
                break;
            case ISLAMIC:
                dialog = IslamicDatePickerDialog.newInstance(getFormEntryPrompt().getIndex(), date, datePickerDetails);
                dialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), DATE_PICKER_DIALOG);
                break;
            case BIKRAM_SAMBAT:
                dialog = BikramSambatDatePickerDialog.newInstance(getFormEntryPrompt().getIndex(), date, datePickerDetails);
                dialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), DATE_PICKER_DIALOG);
                break;
            case MYANMAR:
                dialog = MyanmarDatePickerDialog.newInstance(getFormEntryPrompt().getIndex(), date, datePickerDetails);
                dialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), DATE_PICKER_DIALOG);
                break;
            case PERSIAN:
                dialog = PersianDatePickerDialog.newInstance(getFormEntryPrompt().getIndex(), date, datePickerDetails);
                dialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), DATE_PICKER_DIALOG);
                break;
            default:
                DatePickerDialog datePickerDialog = new FixedDatePickerDialog(getContext(), getTheme(), this);
                datePickerDialog.show();
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        date = new LocalDateTime()
                .withYear(year)
                .withMonthOfYear(month + 1)
                .withDayOfMonth(dayOfMonth)
                .withHourOfDay(0)
                .withMinuteOfHour(0)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);
        setDateLabel();

        widgetValueChanged();
    }

    private int getTheme() {
        int theme = 0;
        // https://github.com/opendatakit/collect/issues/1424
        // https://github.com/opendatakit/collect/issues/1367
        if (!isBrokenSamsungDevice() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            theme = themeUtils.getMaterialDialogTheme();
        }
        if (!datePickerDetails.isCalendarMode() || isBrokenSamsungDevice()) {
            theme = themeUtils.getHoloDialogTheme();
        }

        return theme;
    }

    // https://stackoverflow.com/questions/28618405/datepicker-crashes-on-my-device-when-clicked-with-personal-app
    private boolean isBrokenSamsungDevice() {
        return Build.MANUFACTURER.equalsIgnoreCase("samsung")
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    private class FixedDatePickerDialog extends DatePickerDialog {
        private final String dialogTitle = getContext().getString(R.string.select_date);

        FixedDatePickerDialog(Context context, int theme, OnDateSetListener listener) {
            super(context, theme, listener, date.getYear(), date.getMonthOfYear() - 1, date.getDayOfMonth());
            if (themeUtils.isHoloDialogTheme(theme)) {
                setTitle(dialogTitle);
                fixSpinner(context, date.getYear(), date.getMonthOfYear() - 1, date.getDayOfMonth());
                hidePickersIfNeeded();

                Window window = getWindow();
                if (window != null) {
                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }

                //noinspection deprecation
                getDatePicker().setCalendarViewShown(false);
            }
        }

        private void hidePickersIfNeeded() {
            if (datePickerDetails.isYearMode()) {
                getDatePicker().findViewById(Resources.getSystem().getIdentifier("day", "id", "android"))
                        .setVisibility(View.GONE);

                getDatePicker().findViewById(Resources.getSystem().getIdentifier("month", "id", "android"))
                        .setVisibility(View.GONE);
                getDatePicker().updateDate(date.getYear(), 0, 1);
            } else if (datePickerDetails.isMonthYearMode()) {
                getDatePicker().findViewById(Resources.getSystem().getIdentifier("day", "id", "android"))
                        .setVisibility(View.GONE);
                getDatePicker().updateDate(date.getYear(), date.getMonthOfYear() - 1, 1);
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
