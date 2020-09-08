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

import androidx.lifecycle.ViewModelProvider;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.form.api.FormEntryPrompt;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.databinding.WidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.ScreenContext;
import org.odk.collect.android.widgets.utilities.DateTimeWidgetUtils;
import org.odk.collect.android.widgets.viewmodels.DateTimeViewModel;

@SuppressLint("ViewConstructor")
public class TimeWidget extends QuestionWidget {
    WidgetAnswerBinding binding;

    private final DateTimeViewModel dateTimeViewModel;

    private LocalDateTime selectedTime;

    public TimeWidget(Context context, final QuestionDetails prompt) {
        super(context, prompt);
        dateTimeViewModel = new ViewModelProvider(((ScreenContext) context).getActivity()).get(DateTimeViewModel.class);

        dateTimeViewModel.getSelectedTime().observe(((ScreenContext) context).getViewLifecycle(), localDateTime -> {
            if (localDateTime != null && dateTimeViewModel.isWidgetWaitingForData(getFormEntryPrompt().getIndex())) {
                selectedTime = localDateTime;
                binding.widgetAnswerText.setText(new TimeData(selectedTime.toDate()).getDisplayText());
                widgetValueChanged();
            }
        });
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = WidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());

        if (prompt.isReadOnly()) {
            binding.widgetButton.setVisibility(GONE);
        } else {
            binding.widgetButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
            binding.widgetButton.setText(getContext().getString(R.string.select_time));

            binding.widgetButton.setOnClickListener(v -> {
                dateTimeViewModel.setWidgetWaitingForData(prompt.getIndex());
                DateTimeWidgetUtils.showTimePickerDialog((FormEntryActivity) getContext(), selectedTime);
            });
        }
        binding.widgetAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        if (prompt.getAnswerValue() == null) {
            selectedTime = LocalDateTime.now();
            binding.widgetAnswerText.setText(R.string.no_time_selected);
        } else {
            DateTime dateTime = new DateTime(getFormEntryPrompt().getAnswerValue().getValue());
            selectedTime = dateTime.toLocalDateTime();
            binding.widgetAnswerText.setText(DateTimeWidgetUtils.getTimeData(dateTime).getDisplayText());
        }

        return binding.getRoot();
    }

    @Override
    public void clearAnswer() {
        selectedTime = LocalDateTime.now();
        binding.widgetAnswerText.setText(R.string.no_time_selected);
        widgetValueChanged();
    }

    @Override
    public IAnswerData getAnswer() {
        return binding.widgetAnswerText.getText().equals(getContext().getString(R.string.no_time_selected))
                ? null
                : new TimeData(selectedTime.toDateTime().toDate());
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        binding.widgetButton.setOnLongClickListener(l);
        binding.widgetAnswerText.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        binding.widgetButton.cancelLongPress();
        binding.widgetAnswerText.cancelLongPress();
    }
}
