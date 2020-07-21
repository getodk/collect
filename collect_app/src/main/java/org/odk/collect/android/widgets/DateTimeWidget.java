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
import android.util.TypedValue;
import android.view.View;

import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.databinding.DateTimeWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver;
import org.odk.collect.android.widgets.interfaces.ButtonClickListener;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.utilities.DateTimeUtils;
import org.odk.collect.android.widgets.utilities.DateTimeWidgetUtils;

import java.util.Date;

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

            binding.dateButton.setOnClickListener(v -> DateTimeWidgetUtils.showDatePickerDialog(
                    (FormEntryActivity) context, prompt, datePickerDetails, date));
            binding.timeButton.setOnClickListener(v -> onButtonClick(binding.timeButton.getId()));
        }
        binding.dateAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
        binding.timeAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        if (getFormEntryPrompt().getAnswerValue() == null) {
            clearAnswer();
        } else {
            date = new LocalDateTime(getFormEntryPrompt().getAnswerValue().getValue());
            isDateNull = false;
            DateTimeWidgetUtils.setDateLabel(getContext(), binding.dateAnswerText, (Date) getAnswer().getValue(), datePickerDetails);

            Date date = (Date) getFormEntryPrompt().getAnswerValue().getValue();
            DateTime dateTime = new DateTime(date);
            isTimeNull = false;
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
            } else if (isDateNull) {
                setDateToCurrent();
            }
            LocalDateTime ldt = new LocalDateTime()
                    .withYear(date.getYear())
                    .withMonthOfYear(date.getMonthOfYear())
                    .withDayOfMonth(date.getDayOfMonth())
                    .withHourOfDay(hourOfDay)
                    .withMinuteOfHour(minuteOfHour)
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
        setTimeToCurrent();
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
            isDateNull = false;
            DateTimeWidgetUtils.setDateLabel(getContext(), binding.dateAnswerText, (Date) getAnswer().getValue(), datePickerDetails);
        }
    }

    @Override
    public void onButtonClick(int buttonId) {
        DateTimeWidgetUtils.createTimePickerDialog((FormEntryActivity) getContext(), hourOfDay, minuteOfHour);
    }

    @Override
    public void widgetValueChanged(QuestionWidget changedWidget) {
        widgetValueChanged();
    }

    public void onTimeSet(int hourOfDay, int minute) {
        this.hourOfDay = hourOfDay;
        this.minuteOfHour = minute;
        isTimeNull = false;
        DateTimeWidgetUtils.setTimeLabel(binding.timeAnswerText, hourOfDay, minuteOfHour, false);
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
            DateTimeWidgetUtils.setTimeLabel(binding.timeAnswerText, hourOfDay, minuteOfHour, isTimeNull);
        }
    }

    private boolean isNullValue() {
        return getFormEntryPrompt().isRequired()
                ? isDateNull || isTimeNull
                : isDateNull && isTimeNull;
    }
}
