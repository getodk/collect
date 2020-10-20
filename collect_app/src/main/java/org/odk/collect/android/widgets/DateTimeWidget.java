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
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.TimePicker;

import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.databinding.DateTimeWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.fragments.dialogs.BikramSambatDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.CopticDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.CustomDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.CustomTimePickerDialog;
import org.odk.collect.android.fragments.dialogs.EthiopianDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.FixedDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.IslamicDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.MyanmarDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.PersianDatePickerDialog;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver;
import org.odk.collect.android.widgets.interfaces.ButtonClickListener;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.utilities.DateTimeUtils;
import org.odk.collect.android.utilities.DialogUtils;

import java.util.Date;

import static org.odk.collect.android.fragments.dialogs.CustomDatePickerDialog.DATE_PICKER_DIALOG;
import static org.odk.collect.android.fragments.dialogs.CustomTimePickerDialog.CURRENT_TIME;
import static org.odk.collect.android.fragments.dialogs.CustomTimePickerDialog.TIME_PICKER_THEME;
import static org.odk.collect.android.fragments.dialogs.FixedDatePickerDialog.CURRENT_DATE;
import static org.odk.collect.android.fragments.dialogs.FixedDatePickerDialog.DATE_PICKER_DETAILS;
import static org.odk.collect.android.fragments.dialogs.FixedDatePickerDialog.DATE_PICKER_THEME;

/**
 * Displays a DatePicker widget. DateWidget handles leap years and does not allow dates that do not
 * exist.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

@SuppressLint("ViewConstructor")
public class DateTimeWidget extends QuestionWidget implements WidgetDataReceiver, WidgetValueChangedListener, ButtonClickListener {
    DateTimeWidgetAnswerBinding binding;

    private LocalDateTime date;
    private DatePickerDetails datePickerDetails;
    private int hourOfDay;
    private int minuteOfHour;

    private boolean isDateNull;
    private boolean isTimeNull;

    public DateTimeWidget(Context context, QuestionDetails prompt) {
        super(context, prompt);
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = DateTimeWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        View answerView = binding.getRoot();

        datePickerDetails = DateTimeUtils.getDatePickerDetails(prompt.getQuestion().getAppearanceAttr());

        if (prompt.isReadOnly()) {
            binding.dateButton.setVisibility(GONE);
            binding.timeButton.setVisibility(GONE);
        } else {
            binding.dateButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
            binding.timeButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

            binding.dateButton.setOnClickListener(v -> showDatePickerDialog());
            binding.timeButton.setOnClickListener(v -> onButtonClick(binding.timeButton.getId()));
        }
        binding.dateAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
        binding.timeAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        if (getFormEntryPrompt().getAnswerValue() == null) {
            clearAnswer();
            setDateToCurrent();
        } else {
            date = new LocalDateTime(getFormEntryPrompt().getAnswerValue().getValue());
            setDateLabel();

            Date date = (Date) getFormEntryPrompt().getAnswerValue().getValue();
            DateTime dateTime = new DateTime(date);
            updateTime(dateTime, true);
        }

        return answerView;
    }

    @Override
    public IAnswerData getAnswer() {
        if (isNullValue()) {
            return null;
        } else {
            if (isTimeNull) {
                setTimeToCurrent();
                setTimeLabel();
            } else if (isDateNull) {
                setDateToCurrent();
                setDateLabel();
            }

            int year = date.getYear();
            int month = date.getMonthOfYear();
            int day = date.getDayOfMonth();
            int hour = hourOfDay;
            int minute = minuteOfHour;

            LocalDateTime ldt = new LocalDateTime()
                    .withYear(year)
                    .withMonthOfYear(month)
                    .withDayOfMonth(day)
                    .withHourOfDay(hour)
                    .withMinuteOfHour(minute)
                    .withSecondOfMinute(0)
                    .withMillisOfSecond(0);

            return new DateTimeData(ldt.toDate());
        }
    }

    @Override
    public void clearAnswer() {
        isDateNull = true;
        binding.dateAnswerText.setText(R.string.no_date_selected);
        setDateToCurrent();

        isTimeNull = true;
        binding.timeAnswerText.setText(R.string.no_time_selected);
        widgetValueChanged();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        binding.dateButton.setOnLongClickListener(l);
        binding.dateAnswerText.setOnLongClickListener(l);

        binding.timeButton.setOnLongClickListener(l);
        binding.timeAnswerText.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        binding.dateButton.cancelLongPress();
        binding.dateAnswerText.cancelLongPress();

        binding.timeButton.cancelLongPress();
        binding.timeAnswerText.cancelLongPress();
    }

    @Override
    public void setData(Object answer) {
        if (answer instanceof LocalDateTime) {
            date = (LocalDateTime) answer;
            setDateLabel();
        }
    }

    @Override
    public void onButtonClick(int buttonId) {
        if (isTimeNull) {
            setTimeToCurrent();
        } else {
            updateTime(hourOfDay, minuteOfHour, true);
        }
        createTimePickerDialog();
    }

    @Override
    public void widgetValueChanged(QuestionWidget changedWidget) {
        widgetValueChanged();
    }

    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
        timePicker.clearFocus();
        this.hourOfDay = hourOfDay;
        this.minuteOfHour = minute;
        setTimeLabel();
    }

    private void setDateToCurrent() {
        date = LocalDateTime
                .now()
                .withHourOfDay(0)
                .withMinuteOfHour(0)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);
    }

    private void setTimeToCurrent() {
        updateTime(DateTime.now(), false);
    }

    private void updateTime(DateTime dateTime, boolean shouldUpdateLabel) {
        updateTime(dateTime.getHourOfDay(), dateTime.getMinuteOfHour(), shouldUpdateLabel);
    }

    private void updateTime(int hourOfDay, int minuteOfHour, boolean shouldUpdateLabel) {
        this.hourOfDay = hourOfDay;
        this.minuteOfHour = minuteOfHour;

        if (shouldUpdateLabel) {
            setTimeLabel();
        }
    }

    private void setDateLabel() {
        isDateNull = false;
        binding.dateAnswerText.setText(DateTimeUtils.getDateTimeLabel((Date) getAnswer().getValue(), datePickerDetails, false, getContext()));
    }

    private void setTimeLabel() {
        isTimeNull = false;
        binding.timeAnswerText.setText(getTimeData().getDisplayText());
    }

    private TimeData getTimeData() {
        // use picker time, convert to today's date, store as utc
        DateTime localDateTime = new DateTime()
                .withTime(hourOfDay, minuteOfHour, 0, 0);
        return !isTimeNull
                ? new TimeData(localDateTime.toDate())
                : null;
    }

    private boolean isNullValue() {
        return getFormEntryPrompt().isRequired()
                ? isDateNull || isTimeNull
                : isDateNull && isTimeNull;
    }

    private void showDatePickerDialog() {
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
                Bundle bundle = new Bundle();
                bundle.putInt(DATE_PICKER_THEME, getTheme());
                bundle.putSerializable(CURRENT_DATE, date);
                bundle.putSerializable(DATE_PICKER_DETAILS, datePickerDetails);

                DialogUtils.showIfNotShowing(FixedDatePickerDialog.class, bundle, ((FormEntryActivity) getContext()).getSupportFragmentManager());
        }
    }

    private void createTimePickerDialog() {
        Bundle bundle = new Bundle();
        bundle.putInt(TIME_PICKER_THEME, themeUtils.getHoloDialogTheme());
        bundle.putSerializable(CURRENT_TIME, new DateTime().withTime(hourOfDay, minuteOfHour, 0, 0));

        DialogUtils.showIfNotShowing(CustomTimePickerDialog.class, bundle, ((FormEntryActivity) getContext()).getSupportFragmentManager());
    }

    private int getTheme() {
        int theme = 0;
        if (!isBrokenSamsungDevice()) {
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
                && Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1;
    }
}
