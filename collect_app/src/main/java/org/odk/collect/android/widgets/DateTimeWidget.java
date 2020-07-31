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

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.odk.collect.android.R;
import org.odk.collect.android.databinding.DateTimeWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.ScreenContext;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.utilities.DateTimeUtils;
import org.odk.collect.android.widgets.interfaces.DateTimeWidgetListener;
import org.odk.collect.android.widgets.utilities.DateTimeWidgetUtils;
import org.odk.collect.android.widgets.viewmodels.DateTimeViewModel;

/**
 * Displays a DatePicker widget. DateWidget handles leap years and does not allow dates that do not
 * exist.
 */
@SuppressLint("ViewConstructor")
public class DateTimeWidget extends QuestionWidget {
    DateTimeWidgetAnswerBinding binding;

    private final DateTimeWidgetListener listener;

    private LocalDateTime selectedDateTime;
    private DatePickerDetails datePickerDetails;

    public DateTimeWidget(Context context, QuestionDetails prompt, LifecycleOwner lifecycleOwner, DateTimeWidgetListener listener) {
        super(context, prompt);
        this.listener = listener;
        DateTimeViewModel dateTimeViewModel = new ViewModelProvider(((ScreenContext) context).getActivity()).get(DateTimeViewModel.class);

        dateTimeViewModel.getSelectedDate().observe(lifecycleOwner, localDateTime -> {
            if (localDateTime != null && listener.isWidgetWaitingForData(getFormEntryPrompt().getIndex())) {
                selectedDateTime = DateTimeWidgetUtils.getSelectedDate(localDateTime, selectedDateTime);
                binding.dateWidget.widgetAnswerText.setText(DateTimeUtils.getDateTimeLabel(
                        localDateTime.toDate(), datePickerDetails, false, getContext()));
                widgetValueChanged();
            }
        });

        dateTimeViewModel.getSelectedTime().observe(lifecycleOwner, localDateTime -> {
            if (localDateTime != null && listener.isWidgetWaitingForData(getFormEntryPrompt().getIndex())) {
                DateTime selectedTime = localDateTime.toDateTime();
                selectedDateTime = DateTimeWidgetUtils.getSelectedTime(selectedTime.toLocalDateTime(), selectedDateTime);
                binding.timeWidget.widgetAnswerText.setText(DateTimeWidgetUtils.getTimeData(selectedTime).getDisplayText());
                widgetValueChanged();
            }
        });
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = DateTimeWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        datePickerDetails = DateTimeUtils.getDatePickerDetails(prompt.getQuestion().getAppearanceAttr());

        if (prompt.isReadOnly()) {
            binding.dateWidget.widgetButton.setVisibility(GONE);
            binding.timeWidget.widgetButton.setVisibility(GONE);
        } else {
            binding.dateWidget.widgetButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
            binding.timeWidget.widgetButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

            binding.dateWidget.widgetButton.setText(getContext().getString(R.string.select_date));
            binding.timeWidget.widgetButton.setText(getContext().getString(R.string.select_time));

            binding.dateWidget.widgetButton.setOnClickListener(v -> {
                listener.setWidgetWaitingForData(prompt.getIndex());
                listener.displayDatePickerDialog(context, prompt.getIndex(), datePickerDetails, selectedDateTime);
            });

            binding.timeWidget.widgetButton.setOnClickListener(v -> {
                listener.setWidgetWaitingForData(prompt.getIndex());
                listener.displayTimePickerDialog(context, selectedDateTime);
            });
        }
        binding.dateWidget.widgetAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
        binding.timeWidget.widgetAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        if (getFormEntryPrompt().getAnswerValue() == null) {
            resetAnswerFields();
        } else {
            selectedDateTime = DateTimeWidgetUtils.getCurrentDateTime();

            LocalDateTime selectedDate = new LocalDateTime(getFormEntryPrompt().getAnswerValue().getValue());
            selectedDateTime = DateTimeWidgetUtils.getSelectedDate(selectedDate, selectedDateTime);
            binding.dateWidget.widgetAnswerText.setText(DateTimeUtils.getDateTimeLabel(
                    selectedDate.toDate(), datePickerDetails, false, context));

            DateTime selectedTime = new DateTime(getFormEntryPrompt().getAnswerValue().getValue());
            selectedDateTime = DateTimeWidgetUtils.getSelectedTime(selectedTime.toLocalDateTime(), selectedDateTime);
            binding.timeWidget.widgetAnswerText.setText(DateTimeWidgetUtils.getTimeData(selectedTime).getDisplayText());
        }

        return binding.getRoot();
    }

    @Override
    public IAnswerData getAnswer() {
        if (isNullValue()) {
            return null;
        } else {
            if (isTimeNull()) {
                selectedDateTime = DateTimeWidgetUtils.getSelectedDate(selectedDateTime, LocalDateTime.now());
            } else if (isDateNull()) {
                selectedDateTime = DateTimeWidgetUtils.getSelectedTime(selectedDateTime, LocalDateTime.now());
            }
            return new DateTimeData(selectedDateTime.toDate());
        }
    }

    @Override
    public void clearAnswer() {
        resetAnswerFields();
        widgetValueChanged();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        binding.dateWidget.widgetButton.setOnLongClickListener(l);
        binding.dateWidget.widgetAnswerText.setOnLongClickListener(l);

        binding.timeWidget.widgetButton.setOnLongClickListener(l);
        binding.timeWidget.widgetAnswerText.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        binding.dateWidget.widgetButton.cancelLongPress();
        binding.dateWidget.widgetAnswerText.cancelLongPress();

        binding.timeWidget.widgetButton.cancelLongPress();
        binding.timeWidget.widgetAnswerText.cancelLongPress();
    }

    private void resetAnswerFields() {
        selectedDateTime = DateTimeWidgetUtils.getCurrentDateTime();
        binding.dateWidget.widgetAnswerText.setText(R.string.no_date_selected);
        binding.timeWidget.widgetAnswerText.setText(R.string.no_time_selected);
    }

    private boolean isNullValue() {
        return getFormEntryPrompt().isRequired()
                ? isDateNull() || isTimeNull()
                : isDateNull() && isTimeNull();
    }

    private boolean isDateNull() {
        return binding.dateWidget.widgetAnswerText.getText().equals(getContext().getString(R.string.no_date_selected));
    }

    private boolean isTimeNull() {
        return binding.timeWidget.widgetAnswerText.getText().equals(getContext().getString(R.string.no_time_selected));
    }
}
