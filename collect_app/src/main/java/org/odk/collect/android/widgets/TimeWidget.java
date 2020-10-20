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

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.DateTime;
import org.odk.collect.android.R;
import org.odk.collect.android.databinding.WidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.utilities.DateTimeWidgetUtils;

import java.util.Date;


@SuppressLint("ViewConstructor")
public class TimeWidget extends QuestionWidget {
    WidgetAnswerBinding binding;

    private int hourOfDay;
    private int minuteOfHour;
    private boolean nullAnswer;

    public TimeWidget(Context context, final QuestionDetails prompt) {
        this(context, prompt, false);
    }

    public TimeWidget(Context context, QuestionDetails prompt, boolean isPartOfDateTimeWidget) {
        super(context, prompt, !isPartOfDateTimeWidget);
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = WidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        View answerView = binding.getRoot();

        if (prompt.isReadOnly()) {
            binding.widgetButton.setVisibility(GONE);
        } else {
            binding.widgetButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
            binding.widgetButton.setText(getContext().getString(R.string.select_time));
            binding.widgetButton.setOnClickListener(v -> onButtonClick());
        }
        binding.widgetAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        if (getFormEntryPrompt().getAnswerValue() == null) {
            clearAnswer();
        } else {
            Date date = (Date) getFormEntryPrompt().getAnswerValue().getValue();

            DateTime dateTime = new DateTime(date);
            updateTime(dateTime, true);
        }

        return answerView;
    }

    @Override
    public void clearAnswer() {
        nullAnswer = true;
        binding.widgetAnswerText.setText(R.string.no_time_selected);
        widgetValueChanged();
    }

    @Override
    public IAnswerData getAnswer() {
        return DateTimeWidgetUtils.getTimeData(hourOfDay, minuteOfHour, nullAnswer);
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

    public void onTimeSet(int hourOfDay, int minute) {
        this.hourOfDay = hourOfDay;
        this.minuteOfHour = minute;
        DateTimeWidgetUtils.setTimeLabel(binding.widgetAnswerText, hourOfDay, minuteOfHour, nullAnswer);
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
            DateTimeWidgetUtils.setTimeLabel(binding.widgetAnswerText, hourOfDay, minuteOfHour, nullAnswer);
        }
    }

    private void onButtonClick() {
        if (nullAnswer) {
            setTimeToCurrent();
        } else {
            updateTime(hourOfDay, minuteOfHour, true);
        }
        DateTimeWidgetUtils.createTimePickerDialog(getContext(), hourOfDay, minuteOfHour);
    }
}
