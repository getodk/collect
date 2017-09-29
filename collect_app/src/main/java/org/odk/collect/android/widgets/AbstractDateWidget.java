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

package org.odk.collect.android.widgets;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.odk.collect.android.R;

import java.util.Date;

public abstract class AbstractDateWidget extends QuestionWidget {

    public enum CalendarMode {
        FULL_DATE, MONTH_YEAR, YEAR
    }

    protected Button dateButton;
    protected TextView dateTextView;

    protected boolean nullAnswer;

    protected int year;
    protected int month;
    protected int day;

    protected CalendarMode calendarMode = CalendarMode.FULL_DATE;

    public AbstractDateWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        createWidget();
    }

    protected void createWidget() {
        readAppearance();
        createDateButton();
        dateTextView = getAnswerTextView();
        addViews();
        if (formEntryPrompt.getAnswerValue() == null) {
            clearAnswer();
        } else {
            DateTime dt = new DateTime(((Date) formEntryPrompt.getAnswerValue().getValue()).getTime());
            year = dt.getYear();
            month = dt.getMonthOfYear();
            day = dt.getDayOfMonth();
            setDateLabel();
        }
    }

    @Override
    public void setFocus(Context context) {
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
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
            LocalDateTime ldt = new LocalDateTime()
                    .withYear(year)
                    .withMonthOfYear(month)
                    .withDayOfMonth(day)
                    .withHourOfDay(0)
                    .withMinuteOfHour(0);
            return new DateData(ldt.toDate());
        }
    }

    private void readAppearance() {
        String appearance = formEntryPrompt.getQuestion().getAppearanceAttr();
        if (appearance != null) {
            if (appearance.contains("month-year")) {
                calendarMode = CalendarMode.MONTH_YEAR;
            } else if (appearance.contains("year")) {
                calendarMode = CalendarMode.YEAR;
            }
        }
    }

    private void createDateButton() {
        dateButton = getSimpleButton(getContext().getString(R.string.select_date));
        dateButton.setEnabled(!formEntryPrompt.isReadOnly());
    }

    private void addViews() {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(dateButton);
        linearLayout.addView(dateTextView);
        addAnswerView(linearLayout);
    }

    public void onDateChanged(int day, int month, int year) {
        nullAnswer = false;
        this.day = day;
        this.month = month;
        this.year = year;
        setDateLabel();
    }

    protected abstract void setDateLabel();
}
