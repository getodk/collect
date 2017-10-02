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
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormController;

import java.util.Date;

/**
 * @author Grzegorz Orczykowski (gorczykowski@soldevelo.com)
 */
public abstract class AbstractDateWidget extends QuestionWidget implements BinaryWidget {

    public enum CalendarMode {
        CALENDAR, FULL_DATE, MONTH_YEAR, YEAR
    }

    protected Button dateButton;
    protected TextView dateTextView;

    protected boolean nullAnswer;

    protected DateTime dateTime;

    protected CalendarMode calendarMode = CalendarMode.CALENDAR;

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
            setDateToCurrent();
        } else {
            dateTime = new DateTime(((Date) formEntryPrompt.getAnswerValue().getValue()).getTime());
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
        setDateToCurrent();
    }

    @Override
    public IAnswerData getAnswer() {
        clearFocus();
        return nullAnswer ? null : new DateData(dateTime.toDate());
    }

    @Override
    public void setBinaryData(Object answer) {
        dateTime = (DateTime) answer;
        setDateLabel();
        cancelWaitingForBinaryData();
    }

    @Override
    public void cancelWaitingForBinaryData() {
        FormController formController = Collect.getInstance().getFormController();
        if (formController != null) {
            formController.setIndexWaitingForData(null);
        }
    }

    @Override
    public boolean isWaitingForBinaryData() {
        FormController formController = Collect.getInstance().getFormController();
        return formController != null && formEntryPrompt.getIndex().equals(formController.getIndexWaitingForData());
    }

    public boolean isDayHidden() {
        return calendarMode.equals(CalendarMode.MONTH_YEAR) || calendarMode.equals(CalendarMode.YEAR);
    }

    public boolean isMonthHidden() {
        return calendarMode.equals(CalendarMode.YEAR);
    }

    public DateTime getDate() {
        return dateTime;
    }

    public boolean isNullAnswer() {
        return nullAnswer;
    }

    private void readAppearance() {
        String appearance = formEntryPrompt.getQuestion().getAppearanceAttr();
        if (appearance != null) {
            if (appearance.contains("month-year")) {
                calendarMode = CalendarMode.MONTH_YEAR;
            } else if (appearance.contains("year")) {
                calendarMode = CalendarMode.YEAR;
            } else if ("no-calendar".equals(appearance)) {
                calendarMode = CalendarMode.FULL_DATE;
            }
        }

        if (!(this instanceof DateWidget)) {
            // We don't support calendar mode for other calendars
            if (calendarMode.equals(CalendarMode.CALENDAR)) {
                calendarMode = CalendarMode.FULL_DATE;
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

    protected void setDateToCurrent() {
        dateTime = DateTime.now();
    }

    protected abstract void setDateLabel();

    protected abstract void showDatePickerDialog();
}
