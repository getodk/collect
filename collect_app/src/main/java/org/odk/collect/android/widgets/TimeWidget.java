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
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.TimeData;
import org.joda.time.DateTime;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.formentry.questions.WidgetViewUtils;
import org.odk.collect.android.widgets.interfaces.ButtonClickListener;
import org.odk.collect.android.fragments.dialogs.CustomTimePickerDialog;
import org.odk.collect.android.utilities.DialogUtils;
import java.util.Date;

import static org.odk.collect.android.formentry.questions.WidgetViewUtils.createAnswerTextView;
import static org.odk.collect.android.formentry.questions.WidgetViewUtils.createSimpleButton;
import static org.odk.collect.android.fragments.dialogs.CustomTimePickerDialog.CURRENT_TIME;
import static org.odk.collect.android.fragments.dialogs.CustomTimePickerDialog.TIME_PICKER_THEME;

@SuppressLint("ViewConstructor")
public class TimeWidget extends QuestionWidget implements ButtonClickListener {
    Button timeButton;
    final TextView timeTextView;

    private int hourOfDay;
    private int minuteOfHour;

    private boolean nullAnswer;

    public TimeWidget(Context context, final QuestionDetails prompt) {
        this(context, prompt, false);
    }

    public TimeWidget(Context context, QuestionDetails prompt, boolean isPartOfDateTimeWidget) {
        super(context, prompt, !isPartOfDateTimeWidget);
        createTimeButton();
        timeTextView = createAnswerTextView(getContext(), getAnswerFontSize());

        if (getFormEntryPrompt().getAnswerValue() == null) {
            clearAnswer();
        } else {
            Date date = (Date) getFormEntryPrompt().getAnswerValue().getValue();

            DateTime dateTime = new DateTime(date);
            updateTime(dateTime, true);
        }
        addViews();
    }

    @Override
    public void clearAnswer() {
        clearAnswerWithoutValueChangeEvent();
        widgetValueChanged();
    }

    void clearAnswerWithoutValueChangeEvent() {
        nullAnswer = true;
        timeTextView.setText(R.string.no_time_selected);
    }

    @Override
    public IAnswerData getAnswer() {
        // use picker time, convert to today's date, store as utc
        DateTime localDateTime = new DateTime()
                .withTime(hourOfDay, minuteOfHour, 0, 0);

        return !nullAnswer
                ? new TimeData(localDateTime.toDate())
                : null;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        timeButton.setOnLongClickListener(l);
        timeTextView.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        timeButton.cancelLongPress();
        timeTextView.cancelLongPress();
    }

    private void createTimeButton() {
        timeButton = createSimpleButton(getContext(), getFormEntryPrompt().isReadOnly(), getContext().getString(R.string.select_time), getAnswerFontSize(), this);
    }

    private void addViews() {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(timeButton);
        linearLayout.addView(timeTextView);
        addAnswerView(linearLayout, WidgetViewUtils.getStandardMargin(getContext()));
    }

    public void setTimeLabel() {
        nullAnswer = false;
        timeTextView.setText(getAnswer().getDisplayText());
    }

    private void createTimePickerDialog() {
        Bundle bundle = new Bundle();
        bundle.putInt(TIME_PICKER_THEME, themeUtils.getHoloDialogTheme());
        bundle.putSerializable(CURRENT_TIME, new DateTime().withTime(hourOfDay, minuteOfHour, 0, 0));

        DialogUtils.showIfNotShowing(CustomTimePickerDialog.class, bundle, ((FormEntryActivity) getContext()).getSupportFragmentManager());
    }

    public int getHour() {
        return hourOfDay;
    }

    public int getMinute() {
        return minuteOfHour;
    }

    public boolean isNullAnswer() {
        return nullAnswer;
    }

    public void setTimeToCurrent() {
        updateTime(DateTime.now(), false);
    }

    public void updateTime(DateTime dateTime, boolean shouldUpdateLabel) {
        updateTime(dateTime.getHourOfDay(), dateTime.getMinuteOfHour(), shouldUpdateLabel);
    }

    public void updateTime(int hourOfDay, int minuteOfHour, boolean shouldUpdateLabel) {
        this.hourOfDay = hourOfDay;
        this.minuteOfHour = minuteOfHour;

        if (shouldUpdateLabel) {
            setTimeLabel();
        }
    }

    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
        timePicker.clearFocus();
        this.hourOfDay = hourOfDay;
        this.minuteOfHour = minute;
        setTimeLabel();
    }

    @Override
    public void onButtonClick(int buttonId) {
        if (nullAnswer) {
            setTimeToCurrent();
        } else {
            updateTime(hourOfDay, minuteOfHour, true);
        }
        createTimePickerDialog();
    }
}
